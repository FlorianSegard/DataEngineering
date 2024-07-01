#!/bin/bash
if ! kafka-topics.sh --bootstrap-server kafka:9092 --list | grep -q "drone-data"; then
  kafka-topics.sh --bootstrap-server kafka:9092 --create --topic drone-data --partitions 1 --replication-factor 1
  # kafka-topics.sh --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic drone-data
else
  echo "Topic 'drone-data' already exists."
fi
