FROM debian:stretch-slim


# Spark
ARG APACHE_SPARK_VERSION=2.4.0
ARG HADOOP_VERSION=2.7
ENV SPARK_NAME=spark-${APACHE_SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}

ENV SPARK_DIR /opt/${SPARK_NAME}
ENV SPARK_HOME /usr/local/spark
ENV PYTHONPATH $SPARK_HOME/python:$SPARK_HOME/python/lib/py4j-0.10.4-src.zip:$SPARK_HOME/python/lib/pyspark.zip
ENV SPARK_OPTS --driver-java-options=-Xms1024M --driver-java-options=-Xmx4096M --driver-java-options=-Dlog4j.logLevel=info

RUN apt-get update && \
    apt-get -y install python3-pip wget && \
    apt-get remove -y gcc-6 libgcc-6-dev perl perl-modules-5.24 && \
    apt-get autoremove -y 

RUN mkdir -p /usr/share/man/man1 && \
    apt-get install -y --no-install-recommends \
      openjdk-8-jre-headless \
      ca-certificates-java \
      curl && \
    rm -rf /var/lib/apt/* && \
    curl http://apache.rediris.es/spark/spark-${APACHE_SPARK_VERSION}/${SPARK_NAME}.tgz | \
    tar xzf - -C /opt && \
    apt-get remove -y curl && \
    apt-get clean

# Standardize system
RUN ln -s $SPARK_DIR $SPARK_HOME && \
    ln -s /usr/bin/pip3 /usr/bin/pip && \
    ln -s /usr/bin/python3 /usr/bin/python

# Env variables
ENV SCALA_VERSION 2.11.8
ENV SBT_VERSION 1.2.8

RUN apt-get update && apt-get install -y \
curl wget
# Install Scala
## Piping curl directly in tar
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion && \
  mkdir project && \
  echo "scalaVersion := \"${SCALA_VERSION}\"" > build.sbt && \
  echo "sbt.version=${SBT_VERSION}" > project/build.properties && \
  echo "case object Temp" > Temp.scala && \
  sbt compile && \
  rm -r project && rm build.sbt && rm Temp.scala && rm -r target


RUN cd /usr/local/spark/jars && wget http://repo1.maven.org/maven2/org/apache/spark/spark-streaming-kafka-0-10-assembly_2.11/2.4.0/spark-streaming-kafka-0-10-assembly_2.11-2.4.0.jar
RUN cd /usr/local/spark/jars && wget http://central.maven.org/maven2/org/apache/kafka/kafka-clients/2.0.0/kafka-clients-2.0.0.jar
RUN cd /usr/local/spark/jars && wget http://central.maven.org/maven2/org/apache/spark/spark-tags_2.11/2.4.0/spark-tags_2.11-2.4.0.jar
RUN cd /usr/local/spark/jars && wget http://central.maven.org/maven2/org/apache/spark/spark-sql-kafka-0-10_2.11/2.4.0/spark-sql-kafka-0-10_2.11-2.4.0.jar
RUN cd /usr/local/spark/jars && wget http://central.maven.org/maven2/postgresql/postgresql/9.1-901-1.jdbc4/postgresql-9.1-901-1.jdbc4.jar

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN sbt -ivy /root/.ivy2 assembly

ENTRYPOINT /usr/local/spark/bin/spark-submit \
		--class "consumer.FlowConsumer" \
		--master "local[2]" \
		target/scala-2.11/consumer-assembly-1.0.0-SNAPSHOT.jar
