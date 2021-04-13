# TL;DR

Log requests from a given Kafka topic to Avro files
periodically backed up to S3.
TODO - should look into kafka S3 connector.

## kafka docker-compose

The `testStuff/` folder has a docker-compose setup for running kafka.

```
(
    cd testStuff/
    docker-compose -f ./kafka-docker-compose.yml up -d
)
```

```
kafka-topics.sh --bootstrap-server --if-not-exists localhost:9092 --create --topic audittest --partitions 1 --config retention.ms=300000
```

## References

* kafka with docker - https://medium.com/big-data-engineering/hello-kafka-world-the-complete-guide-to-kafka-with-docker-and-python-f788e2588cfc
