import unittest
from pyflink.table import StreamTableEnvironment, EnvironmentSettings
from pyflink.datastream import StreamExecutionEnvironment
from src.jobs.word_count_job import create_word_count_job
from src.utils.flink_utils import load_config

class TestWordCountJob(unittest.TestCase):
    def setUp(self):
        self.config = load_config()
        self.env = StreamExecutionEnvironment.get_execution_environment()
        self.settings = EnvironmentSettings.new_instance().in_streaming_mode().build()
        self.table_env = StreamTableEnvironment.create(self.env, self.settings)
    
    def test_source_table_creation(self):
        """Test if source table can be created."""
        source_ddl = f"""
            CREATE TABLE source_table (
                word STRING
            ) WITH (
                'connector' = '{self.config['source']['type']}',
                'rows-per-second' = '{self.config['source']['rows_per_second']}',
                'fields.word.length' = '{self.config['source']['word_length']}'
            )
        """
        self.table_env.execute_sql(source_ddl)
        tables = self.table_env.list_tables()
        self.assertIn('source_table', [table[0] for table in tables])
    
    def test_sink_table_creation(self):
        """Test if sink table can be created."""
        sink_ddl = f"""
            CREATE TABLE sink_table (
                word STRING,
                count BIGINT
            ) WITH (
                'connector' = '{self.config['sink']['type']}'
            )
        """
        self.table_env.execute_sql(sink_ddl)
        tables = self.table_env.list_tables()
        self.assertIn('sink_table', [table[0] for table in tables])

if __name__ == '__main__':
    unittest.main() 