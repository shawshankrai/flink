from pyflink.table import StreamTableEnvironment, EnvironmentSettings
from pyflink.datastream import StreamExecutionEnvironment
from src.utils.flink_utils import load_config, setup_logging, create_flink_env
import logging

def create_word_count_job():
    """Create a word count streaming job."""
    # Load configuration
    config = load_config()
    logger = setup_logging(config)
    
    logger.info("Starting Word Count Job...")
    
    # Create Flink environment
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(config['flink']['job']['parallelism'])
    
    # Create table environment
    settings = EnvironmentSettings.new_instance() \
        .in_streaming_mode() \
        .with_built_in_catalog_name("default_catalog") \
        .with_built_in_database_name("default_database") \
        .build()
    
    table_env = StreamTableEnvironment.create(env, settings)
    logger.info("Flink environment created successfully")
    
    # Create source table with Flink 2.0 syntax
    source_ddl = f"""
        CREATE TABLE source_table (
            word STRING
        ) WITH (
            'connector' = '{config['source']['type']}',
            'rows-per-second' = '{config['source']['rows_per_second']}',
            'fields.word.length' = '{config['source']['word_length']}',
            'fields.word.kind' = 'random'
        )
    """
    
    # Create sink table with Flink 2.0 syntax
    sink_ddl = f"""
        CREATE TABLE sink_table (
            `word` STRING,
            `count` BIGINT,
            PRIMARY KEY (`word`) NOT ENFORCED
        ) WITH (
            'connector' = '{config['sink']['type']}',
            'print-identifier' = 'Word Count Results'
        )
    """
    
    # Create processing query with Flink 2.0 syntax
    query = """
        INSERT INTO sink_table
        SELECT word, COUNT(*) as `count`
        FROM source_table
        GROUP BY word
    """
    
    # Execute the job
    logger.info("Creating source table...")
    table_env.execute_sql(source_ddl)
    logger.info("Source table created successfully")
    
    logger.info("Creating sink table...")
    table_env.execute_sql(sink_ddl)
    logger.info("Sink table created successfully")
    
    # Execute the query
    logger.info("Executing word count query...")
    statement_set = table_env.create_statement_set()
    statement_set.add_insert_sql(query)
    statement_set.execute()

if __name__ == '__main__':
    create_word_count_job() 