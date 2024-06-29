# Data Engineering Project : Drone Data Processor

## Prérequis

Avant de commencer, assurez-vous d'avoir téléchargé et extrait les fichiers suivants à la racine du projet :

1. [Apache Kafka 2.13-3.7.0](https://www.apache.org/dyn/closer.cgi?path=/kafka/3.7.0/kafka_2.13-3.7.0.tgz)
2. [Apache Spark 3.5.1 avec Hadoop 3](https://spark.apache.org/downloads.html)

La structure du projet doit ressembler à ceci :

```plaintext
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

## Installation et Configuration

1. Téléchargez et extrayez Apache Kafka et Apache Spark comme mentionné ci-dessus.
2. Configurez les variables d'environnement pour Spark :

   ```sh
   export SPARK_HOME=/path/to/spark-3.5.1-bin-hadoop3
   export PATH=$SPARK_HOME/bin:$PATH
   ```

## Démarrage des Services

1. Démarrez Zookeeper :

   ```sh
   kafka_2.13-3.7.0/bin/zookeeper-server-start.sh kafka_2.13-3.7.0/config/zookeeper.properties
   ```

2. Démarrez le serveur Kafka :

   ```sh
   kafka_2.13-3.7.0/bin/kafka-server-start.sh kafka_2.13-3.7.0/config/server.properties
   ```

3. Créez un topic Kafka nommé `drone-data` :

   ```sh
   kafka_2.13-3.7.0/bin/kafka-topics.sh --create --topic drone-data --bootstrap-server localhost:9092
   ```

## Génération des Données

1. Allez dans le répertoire `streamDatageneration` :

   ```sh
   cd streamDatageneration
   ```

2. Nettoyez, compilez et exécutez le générateur de données :

   ```sh
   sbt clean
   sbt compile
   sbt run
   ```

## Traitement des Données

1. Allez dans le répertoire `dataProcessSpark` :

   ```sh
   cd ../dataProcessSpark
   ```

2. Nettoyez, compilez et packagez le projet :

   ```sh
   sbt clean
   sbt package
   ```

3. Soumettez l'application Spark pour traiter les données :

   ```sh
   ../spark-3.5.1-bin-hadoop3/bin/spark-submit \
     --class DroneDataProcessor \
     --master local[*] \
     --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0 \
     target/scala-2.12/dronedataprocessor_2.12-0.1.jar
   ```

4. Compilez et exécutez le traitement des données :

   ```sh
   sbt compile
   sbt run
   ```

## Schéma des Données

- **id**: Integer
- **timestamp**: Long
- **latitude**: Double
- **longitude**: Double
- **altitude**: Double
- **dangerousity**: Double

## Structure des Répertoires

```plaintext
drone-data-processor/
├── src/
│   ├── main/
│   │   ├── resources/
│   │   └── scala/
│   │       └── processdata.scala
├── target/
├── project/
├── build.sbt
└── README.md
```

## Configuration des Checkpoints

Le répertoire de checkpoint est configuré dans le code comme suit :

```scala
.option("checkpointLocation", "checkpoint")
```

Le répertoire de sauvegarde des données est configuré comme suit :

```scala
.option("path", "SaveDatalake")
```

Assurez-vous que ces répertoires existent et sont accessibles par Spark.