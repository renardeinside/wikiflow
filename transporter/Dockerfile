FROM openjdk:8
COPY ./target /usr/bin/app
WORKDIR /usr/bin/app
ENTRYPOINT java -jar transporter-1.0-SNAPSHOT-uber.jar com.renarde.wikiflow.transporter.DataTransporter
