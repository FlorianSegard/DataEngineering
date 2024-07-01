#!/bin/bash

# Create the Kafka topic if it doesn't already exist
kafka-topics.sh --create --topic drone-data --bootstrap-server kafka:9092 --replication-factor 1 --partitions 1 || true

echo "Kafka topic drone-data created (or already exists)."