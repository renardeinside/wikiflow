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

## Known issues

- Sometimes you need to increase docker memory limit for your machine (for Mac it's 2.0GB by default).