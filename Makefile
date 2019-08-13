create-network:
	docker network create wikiflow-network

run-appliance:
	mvn clean package -pl transporter
	docker-compose up --build

run-structured-consumer:
	mvn clean package -pl consumer
	docker-compose -f docker-compose-structured-consumer.yaml build
	docker-compose -f docker-compose-structured-consumer.yaml up

run-legacy-consumer:
	mvn clean package -pl consumer
	docker-compose -f docker-compose-legacy-consumer.yaml build
	docker-compose -f docker-compose-legacy-consumer.yaml up

run-analytics-consumer:
	mvn clean package -pl consumer
	docker-compose -f docker-compose-analytics-consumer.yaml build
	docker-compose -f docker-compose-analytics-consumer.yaml up
