# Wikiflow

Sample project with components:
1. Wikipedia input stream
2. Akka HTTP reader + Kafka Producer
3. Spark-based app for some data transformation
4. Superset for visualisation

# Warning 

The setup is insecure (uses passwords in plaintext) and should be used **ONLY** for development purposes. 

# Components:

- **producer** is the starting point - it takes event stream from Wikipedia SSE and rotates it to Kafka
- **consumer** is the streaming ETL app - just a `filter` -> `groupBy` -> `reduceByKeyByWindow` chain of operations. 