# Utiliser une image de base avec OpenJDK et SBT
FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1

# Copier les fichiers de l'application dans le conteneur
COPY . /app
WORKDIR /app

# Exécuter le package SBT
RUN sbt clean compile

# Commande pour exécuter l'application
CMD sbt run
