FROM folioci/alpine-jre-openjdk8:latest

ENV VERTICLE_FILE mod-source-record-storage-server-fat.jar

# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles

# Copy your fat jar to the container
COPY mod-source-record-storage-server/target/${VERTICLE_FILE} ${VERTICLE_HOME}/${VERTICLE_FILE}

# Expose this port locally in the container.
EXPOSE 8081
