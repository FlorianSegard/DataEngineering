### Data Engineering Project: Drone Data Processor

### Prerequisites

Before starting, ensure you have downloaded and extracted the following files at the project root:

1. [Apache Kafka 2.13-3.7.0](https://www.apache.org/dyn/closer.cgi?path=/kafka/3.7.0/kafka_2.13-3.7.0.tgz)
2. [Apache Spark 3.5.1 with Hadoop 3](https://spark.apache.org/downloads.html)

### Installation and Configuration

1. **Set up Spark Environment Variables:**

   ```sh
   export SPARK_HOME=~/spark-3.5.1-bin-hadoop3
   export PATH=$SPARK_HOME/bin:$PATH
   ```

2. **Install MinIO and MinIO Client (mc):**

   ```sh
   brew install minio/stable/minio
   brew install minio/stable/mc
   ```

3. **Create a directory for MinIO:**

   ```sh
   mkdir -p ~/minio/data
   ```

4. **Set up MinIO Server with access credentials:**

   ```sh
   export MINIO_ROOT_USER=StrongPass!2024
   export MINIO_ROOT_PASSWORD=hadoopUser123
   minio server ~/minio/data
   ```

5. **Configure MinIO Client to access your server:**

   ```sh
   mc alias set myminio http://127.0.0.1:9000 hadoopUser123 'StrongPass!2024'
   ```

6. **Create a bucket to store your data:**

   ```sh
   mc mb myminio/drone-data-lake
   ```

7. **Create a second bucket for storing analysis results:**

   ```sh
   mc mb myminio/storageanalyse
   ```

8. **Configure Spark to use MinIO:**

   Modify the `spark-defaults.conf` file located at `$SPARK_HOME/conf/spark-defaults.conf` (or `$SPARK_HOME/conf/spark-defaults.conf.template`):

   ```properties
   spark.hadoop.fs.s3a.endpoint           http://127.0.0.1:9000
   spark.hadoop.fs.s3a.access.key         'StrongPass!2024'
   spark.hadoop.fs.s3a.secret.key         hadoopUser123
   spark.hadoop.fs.s3a.path.style.access  true
   spark.hadoop.fs.s3a.impl               org.apache.hadoop.fs.s3a.S3AFileSystem
   ```

### Step-by-Step Execution

#### 1. Kafka and Data Generation

1. **Start Zookeeper (in the first terminal):**

   ```sh
   bin/zookeeper-server-start.sh config/zookeeper.properties
   ```

2. **Start the first Kafka broker (in the second terminal):**

   ```sh
   bin/kafka-server-start.sh config/server.properties
   ```

3. **Start the second Kafka broker (in the third terminal):**

   ```sh
   bin/kafka-server-start.sh config/server-1.properties
   ```

   Example configuration for `server-1.properties`:

   ```properties
   broker.id=1
   log.dirs=/tmp/kafka-logs1
   listeners=PLAINTEXT://:9093
   ```

4. **Create Kafka topics:**

   ```sh
   bin/kafka-topics.sh --create --topic drone-data --bootstrap-server localhost:9092
   bin/kafka-topics.sh --create --topic high-danger-alerts --bootstrap-server localhost:9092
   ```

5. **Configure topics with 3 partitions:**

   ```sh
   bin/kafka-topics.sh --alter --topic drone-data --partitions 3 --bootstrap-server localhost:9092
   bin/kafka-topics.sh --alter --topic high-danger-alerts --partitions 3 --bootstrap-server localhost:9092
   ```

6. **Assign partitions to brokers with replicas to prevent data loss in case of a crash:**

   Create a file named `topics.json` with the following content:

   ```json
   {
     "version": 1,
     "partitions": [
       {"topic": "drone-data", "partition": 0, "replicas": [0, 1]},
       {"topic": "drone-data", "partition": 1, "replicas": [0, 1]},
       {"topic": "drone-data", "partition": 2, "replicas": [1, 0]},
       {"topic": "high-danger-alerts", "partition": 0, "replicas": [0, 1]},
       {"topic": "high-danger-alerts", "partition": 1, "replicas": [0, 1]},
       {"topic": "high-danger-alerts", "partition": 2, "replicas": [1, 0]}
     ]
   }
   ```

   Execute the reassignment command:

   ```sh
   bin/kafka-reassign-partitions.sh --bootstrap-server localhost:9092 --reassignment-json-file topics.json --execute
   ```

7. **Generate data:**

   ```sh
   sbt clean
   sbt compile
   sbt run
   ```

#### 2. MinIO

1. **Start MinIO server with access credentials:**

   ```sh
   export MINIO_ROOT_USER=StrongPass!2024
   export MINIO_ROOT_PASSWORD=hadoopUser123
   minio server ~/minio/data
   ```

2. **Create a bucket for data storage:**

   ```sh
   mc mb myminio/drone-data-lake
   ```

3. **Create a second bucket for storing analysis results:**

   ```sh
   mc mb myminio/storageanalyse
   ```

#### 3. Spark

1. **Generate JAR files for the consumers:**

   ```sh
   cd consumerSparkAlert
   sbt package
   cd ../consumerSparkDatalake
   sbt package
   cd ../analysisDatalake
   sbt package
   ```

2. **Run the consumers using Spark:**

   - **Consumer for the datalake:**

     ```sh
     spark-submit --class ConsumerDatalake --master "local[*]" --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0,org.apache.hadoop:hadoop-aws:3.3.4,com.amazonaws:aws-java-sdk:1.12.452 consumerdatalake_2.12-0.1.jar
     ```

   - **Consumer for the alert:**

     ```sh
     spark-submit --class ConsumerAlert --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0 consumeralert_2.12-0.1.jar
     ```

   - **Consumer for the alert process:**

     ```sh
     spark-submit --class ConsumerAlertProcess --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0 consumeralert_2.12-0.1.jar
     ```

#### 4. Data Analysis

1. **Run the data analysis consumer:**

   ```sh
   spark-submit --class ConsumerDatalake --master "local[*]" --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0,org.apache.hadoop:hadoop-aws:3.3.4,com.amazonaws:aws-java-sdk:1.12.452 consumerdatalake_2.12-0.1.jar
   ```

### Summary

To launch the project, ensure all environment variables for Spark are set, Kafka brokers are running, and MinIO is configured correctly. The steps above guide you through setting up Kafka topics, generating data, running MinIO, and executing the Spark consumers and data analysis. Adjust paths and configurations based on your system setup.