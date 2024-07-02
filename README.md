## Data Engineering Project: Drone Data Processor

### Prerequisites

Before starting, ensure you have downloaded and extracted the following files at the project root:

1. [Apache Kafka 2.13-3.7.0](https://www.apache.org/dyn/closer.cgi?path=/kafka/3.7.0/kafka_2.13-3.7.0.tgz)
2. [Apache Spark 3.5.1 with Hadoop 3](https://spark.apache.org/downloads.html)

Your project structure should resemble the following:

```
.
├── ArchitectureDE.jpg
├── kafka_2.13-3.7.0
├── dataProcessSpark
│   ├── build.sbt
│   ├── checkpoint
│   ├── datalake
│   ├── dumps
│   ├── project
│   ├── SaveDatalake
│   └── src
│       └── main
│           └── scala
│               └── processdata.scala
├── projectPresentation.pptx
├── README.md
├── spark-3.5.1-bin-hadoop3
├── streamDatageneration
│   ├── build.sbt
│   ├── dumps
│   ├── project
│   └── src
│       └── main
│           └── scala
│               └── generate.scala
```

### Installation and Configuration

1. **Download and Extract Apache Kafka and Apache Spark** as mentioned above.
2. **Set up Spark Environment Variables:**

   ```sh
   export SPARK_HOME=/path/to/spark-3.5.1-bin-hadoop3
   export PATH=$SPARK_HOME/bin:$PATH
   ```

### Starting Services

1. **Start Zookeeper:**

   ```sh
   kafka_2.13-3.7.0/bin/zookeeper-server-start.sh kafka_2.13-3.7.0/config/zookeeper.properties
   ```

2. **Start Kafka Server:**

   ```sh
   kafka_2.13-3.7.0/bin/kafka-server-start.sh kafka_2.13-3.7.0/config/server.properties
   ```

3. **Create Kafka Topic named drone-data:**

   ```sh
   kafka_2.13-3.7.0/bin/kafka-topics.sh --create --topic drone-data --bootstrap-server localhost:9092
   ```

### Generating Data

1. **Navigate to streamDatageneration directory:**

   ```sh
   cd streamDatageneration
   ```

2. **Clean, compile, and run the data generator:**

   ```sh
   sbt clean
   sbt compile
   sbt run
   ```

### Data Lake Management (datalake)

#### Spark Configuration

Edit `$SPARK_HOME/conf/spark-defaults.conf` (or `$SPARK_HOME/conf/spark-defaults.conf.template` for some configurations) and add:

```properties
spark.hadoop.fs.s3a.endpoint           http://127.0.0.1:9000
spark.hadoop.fs.s3a.access.key         'StrongPass!2024'
spark.hadoop.fs.s3a.secret.key         hadoopUser123
spark.hadoop.fs.s3a.path.style.access  true
spark.hadoop.fs.s3a.impl               org.apache.hadoop.fs.s3a.S3AFileSystem
```

#### MinIO Installation

Install MinIO:

```sh
brew install minio/stable/minio
```

Install MinIO Client (mc):

```sh
brew install minio/stable/mc
```

Create a directory for MinIO:

```sh
mkdir -p ~/minio/data
```

Start MinIO server with access credentials:

```sh
export MINIO_ROOT_USER=StrongPass!2024
export MINIO_ROOT_PASSWORD=hadoopUser123
minio server ~/minio/data
```

Set up MinIO client to access your server:

```sh
mc alias set myminio http://127.0.0.1:9000 hadoopUser123 'StrongPass!2024'
```

Create a bucket to store your data:

```sh
mc mb myminio/drone-data-lake
```

### Managing Alerts

#### Consumer for Alert Processing

Navigate to `consumerSparkAlert` directory and execute:

```sh
spark-submit --class ConsumerAlertProcess --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0,com.softwaremill.sttp.client4:core_2.12:4.0.0-M16 target/scala-2.12/consumeralert_2.12-0.1.jar
```

#### Recap

To summarize, the project now includes:

- A data generator that sends data to the `drone-data` Kafka topic.
- Two Spark consumers: one for managing the data lake (`ConsumerDatalake`) and one for handling alerts (`ConsumerAlert`).
- Configuration steps for Apache Kafka, Apache Spark, and MinIO.

Ensure Spark environment variables are set before starting services. Adjust paths and configurations based on your system setup.