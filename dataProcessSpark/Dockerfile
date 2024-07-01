FROM hseeberger/scala-sbt:11.0.13_1.5.5_2.13.6

RUN apt-get update && apt-get install -y wget tar netcat

RUN wget https://downloads.apache.org/spark/spark-3.5.1/spark-3.5.1-bin-hadoop3.tgz && \
    tar xvf spark-3.5.1-bin-hadoop3.tgz && \
    mv spark-3.5.1-bin-hadoop3 /opt/spark

RUN wget https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz && \
    tar xvf kafka_2.13-3.7.0.tgz && \
    mv kafka_2.13-3.7.0 /opt/kafka

ENV SPARK_HOME /opt/spark
ENV KAFKA_HOME /opt/kafka
ENV PATH $SPARK_HOME/bin:$KAFKA_HOME/bin:$PATH

COPY . /app
WORKDIR /app

RUN sbt clean package

CMD ["sbt", "run"]
