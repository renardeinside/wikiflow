FROM python:3.5.6-stretch

# MAINTAINER  Ludwig Prager <ludwig.prager@celp.de>
# MAINTAINER Ivan Trusov

RUN apt update && apt -y install \
	wget \
	openjdk-8-jdk \
	vim less

RUN rm -rf /var/lib/apt/lists/*
ENV SBT 1.2.8
RUN wget https://dl.bintray.com/sbt/debian/sbt-${SBT}.deb
RUN dpkg -i sbt-${SBT}.deb
RUN rm sbt-${SBT}.deb
RUN sbt sbtVersion

COPY . /usr/src/app
WORKDIR /usr/src/app
RUN sbt assembly

ENTRYPOINT java -jar target/scala-2.12/producer-assembly-1.0.0-SNAPSHOT.jar
