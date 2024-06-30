#!/bin/bash

# Attendre que Kafka soit prêt
echo "Waiting for Kafka to be ready..."
while ! nc -z kafka 9092; do   
  sleep 1 # attendre 1 seconde avant de réessayer
done

# Créer le topic
$KAFKA_HOME/bin/kafka-topics.sh --create --topic drone-data --bootstrap-server kafka:9092 --partitions 1 --replication-factor 1

# Garder le conteneur en cours d'exécution
tail -f /dev/null
