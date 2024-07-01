FROM hseeberger/scala-sbt:11.0.13_1.5.5_2.13.6

COPY . /app
WORKDIR /app

RUN sbt clean

CMD ["sbt", "run"]
