# Spark flow on top of the Wikipedia SSE Stream

## How-to run

- Create the docker network:
```bash
make create-network
```
- Run the streaming appliance
```bash
make run-appliance
```
- To run streaming consumption of data via legacy API (DStreams), please run:
```bash
make run-legacy-consumer
```

- To run streaming consumption of data via structured API, please run:
```bash
make run-structured-consumer
```

- To run streaming consumption of data via structured API with write to delta, please run:
```bash
make run-analytics-consumer
```

You could also access the SparkUI for this Job at http://localhost:4040/jobs



## Known issues

- Sometimes you need to increase docker memory limit for your machine (for Mac it's 2.0GB by default).
- To debug memory usage and status of the containers, please use this command:
```bash
docker stats
```
- Sometimes docker couldn't gracefully stop the consuming applications, please use this command in case if container hangs:
```bash
docker-compose -f <name of compose file with the job>.yaml down
```
