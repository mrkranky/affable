# Affable ![CI status](https://img.shields.io/badge/build-passing-brightgreen.svg)

Java consumer which consumes influencer's information for storing, ranking, aggregation and time-series analytics.

## Installation
To install and package -

`$ mvn package`

To run the consumer for Cassandra writing

`$ java -cp affable-1.0-SNAPSHOT.jar com.affable.consumer.DBConsumer`

To run the consumer for time-series analytics, ranking etc.

`$ java -cp affable-1.0-SNAPSHOT.jar com.affable.consumer.AnalyticsConsumer`

## Prerequisites/Setup
Create a kafka topic 'influencers-analytics', in which the influencer's update after cassandra writing would be pushed.

`$ bin/kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 10 --topic influencers-analytics`

Create cassandra keyspace `affable` and table `users`

`CREATE KEYSPACE affable WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy', 'datacenter1' : 3 } AND DURABLE_WRITES = false;`

`CREATE TABLE users(userid int PRIMARY KEY, username varchar, followerCount varint, followingCount varint, isSuspicious boolean, time varint);`

Install/configure InfluxDB and create database, retention policy

`$ influx`

`> CREATE DATABASE affable_influencers`

`> CREATE RETENTION POLICY defaultPolicy ON affable_influencers DURATION 30d REPLICATION 1`

## Architecture

Please read the WIKI for this part of details

## Benchmarks
Benchmarking on -
```
Macbook Air
8 GB 1600 MHz DDR3
1.6 GHz Intel Core i5
```

1. With single instance of each consumer -

   `Cassandra Writing: 800 writes/sec`
   
   `Ranking and Influx: 700 writes/sec`

2. With two instances of each consumer -
   
   `Cassandra Writing: 1500 writes/sec`
   
   `Ranking and Influx: 1300 writes/sec`
   
3. Making cassandra writes as asynchronous, writing performance was increased by upto 33 percent.

## Scaling

Increasing consumer instances, increases computing capabilities of both the consumers.

Ideally the number of topic partitions should be equal to number of consumer instances and both can scale pretty easily.

##### Redis
To scale out redis horizontally, we can have keys sharding by using hash of hash.

To further enhance the speed of ranking and averaging influencer's follower through Redis, we can follow batching approach. With this way, we would only be doing ranking/averaging only after 'n' of updates.  
