# Utiliser une image de base plus récente avec OpenJDK et SBT
FROM hseeberger/scala-sbt:11.0.13_1.5.5_2.13.6

# Installer les dépendances nécessaires, y compris netcat
RUN apt-get update && apt-get install -y wget tar netcat

# Installer Spark
RUN wget https://downloads.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz && \
    tar xvf spark-3.5.1-bin-hadoop3.tgz && \
    mv spark-3.5.1-bin-hadoop3 /opt/spark

# Installer Kafka
RUN wget https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz && \
    tar xvf kafka_2.13-3.7.0.tgz && \
    mv kafka_2.13-3.7.0 /opt/kafka

# Ajouter les variables d'environnement pour Spark et Kafka
ENV SPARK_HOME /opt/spark
ENV KAFKA_HOME /opt/kafka
ENV PATH $SPARK_HOME/bin:$KAFKA_HOME/bin:$PATH

# Copier les fichiers de l'application dans le conteneur
COPY . /app
WORKDIR /app

# Exécuter le package SBT
RUN sbt clean package

# Commande pour exécuter l'application Spark
CMD $SPARK_HOME/bin/spark-submit \
    --class DroneDataProcessor \
    --master local[*] \
    --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.1,org.apache.kafka:kafka-clients:3.7.0 \
    /app/target/scala-2.12/dronedataprocessor_2.12-0.1.jar
