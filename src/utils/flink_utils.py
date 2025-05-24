from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment, EnvironmentSettings
from pyflink.datastream.checkpointing_mode import CheckpointingMode
import yaml
import logging
import os

def setup_logging(config):
    """Configure logging based on config settings."""
    logging.basicConfig(
        level=config['logging']['level'],
        format=config['logging']['format']
    )
    return logging.getLogger(__name__)

def load_config(config_path='config/config.yaml'):
    """Load configuration from YAML file."""
    with open(config_path, 'r') as f:
        return yaml.safe_load(f)

def create_flink_env(config):
    """Create and configure Flink execution environment."""
    # Create execution environment
    env = StreamExecutionEnvironment.get_execution_environment()
    
    # Configure job
    env.set_parallelism(config['flink']['job']['parallelism'])
    
    # Configure checkpointing
    env.enable_checkpointing(
        interval=config['flink']['job']['checkpoint_interval'],
        mode=CheckpointingMode.EXACTLY_ONCE
    )
    
    # Create table environment with Flink 2.0 settings
    settings = EnvironmentSettings.new_instance() \
        .in_streaming_mode() \
        .with_built_in_catalog_name("default_catalog") \
        .with_built_in_database_name("default_database") \
        .build()
    
    table_env = StreamTableEnvironment.create(env, settings)
    
    return table_env

def get_kafka_source_config(config):
    """Get Kafka source configuration."""
    return {
        'bootstrap.servers': config['kafka']['bootstrap_servers'],
        'group.id': config['kafka']['group_id'],
        'auto.offset.reset': config['kafka']['auto_offset_reset']
    } 