# Apache Flink Project

This project contains Apache Flink streaming applications built with Python.

## Project Structure

```
flink/
├── src/                    # Source code
│   ├── jobs/              # Flink job definitions
│   ├── operators/         # Custom operators and transformations
│   └── utils/             # Utility functions and helpers
├── tests/                 # Test files
├── config/                # Configuration files
├── data/                  # Sample data and schemas
├── docs/                  # Documentation
└── scripts/               # Utility scripts
```

## Setup Commands

1. Create project structure:
```bash
mkdir -p src/jobs src/operators src/utils tests config data docs scripts
```

2. Create virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. Install dependencies:
```bash
pip install -r requirements.txt
```

## Directory Purposes

- `src/jobs/`: Contains Flink job definitions
- `src/operators/`: Custom operators and transformations
- `src/utils/`: Common utility functions and helpers
- `tests/`: Unit and integration tests
- `config/`: Configuration files (YAML/JSON)
- `data/`: Sample data and schemas
- `docs/`: Project documentation
- `scripts/`: Utility scripts for deployment/maintenance

## Development Guidelines

1. Follow PEP 8 style guide
2. Write unit tests for new features
3. Update documentation as needed
4. Use meaningful variable names
5. Keep functions small and focused
6. Implement proper error handling
7. Use logging for debugging

## License

[Your License]

## Flink Cluster & Job Management Commands

### Starting the Flink Cluster
```
/opt/homebrew/opt/apache-flink/libexec/bin/start-cluster.sh
```

### Stopping the Flink Cluster
```
/opt/homebrew/opt/apache-flink/libexec/bin/stop-cluster.sh
```

### Submitting a Job
```
./scripts/submit_job.sh
```

### Listing Running Jobs
```
/opt/homebrew/opt/apache-flink/libexec/bin/flink list
```

### Canceling a Job
First, list running jobs to get the Job ID:
```
/opt/homebrew/opt/apache-flink/libexec/bin/flink list
```
Then, cancel the job using its Job ID:
```
/opt/homebrew/opt/apache-flink/libexec/bin/flink cancel <job_id>
```

## Increasing Task Slots in Flink Cluster

To increase the number of task slots in your Flink cluster, follow these steps:

1. **Locate the Configuration File**  
   Open the Flink configuration file at:
   ```
   /opt/homebrew/opt/apache-flink/libexec/conf/config.yaml
   ```

2. **Edit the Configuration**  
   Find the `taskmanager` section and update the `numberOfTaskSlots` value. For example, to set it to 4:
   ```yaml
   taskmanager:
     numberOfTaskSlots: 4
   ```

3. **Restart the Flink Cluster**  
   After saving the changes, restart the cluster to apply the new settings:
   ```bash
   /opt/homebrew/opt/apache-flink/libexec/bin/stop-cluster.sh
   /opt/homebrew/opt/apache-flink/libexec/bin/start-cluster.sh
   ```

4. **Verify the Changes**  
   You can verify the new task slot configuration by checking the Flink Web UI or running:
   ```bash
   /opt/homebrew/opt/apache-flink/libexec/bin/flink list
   ```