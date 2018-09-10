package com.affable.consumer;

import com.affable.connector.CassandraConnector;
import com.affable.connector.InfluxConnector;
import com.affable.connector.RedisConnector;
import com.affable.pojo.User;
import com.affable.producer.RecordProducer;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.json.simple.JSONArray;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DBConsumer {
    private static final String TOPIC_NAME = "influencers";

    private CassandraConnector cassandraClient;
    private RedisConnector redisClient;
    private InfluxConnector influxClient;
    private RecordProducer recordProducer;

    Gson gson = new Gson();

    private Consumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "consumer-1");
        props.put("enable.auto.commit", "false");
        //props.put("auto.commit.interval.ms", "1000");
        //props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        //consumer.poll(0);
        //consumer.seekToBeginning(Arrays.asList(new TopicPartition(TOPIC_NAME, 0)));

        return consumer;
    }

    public void readTopic() {
        Consumer<String, String> consumer = createConsumer();
        Session session = cassandraClient.getSession();

        System.out.println("DBConsumer started..");

        //String insertQuery = "insert into users (userid, username, followercount, followingcount, time) values (?, ?, ?, ?, ?)";
        String updateQuery = "update affable.users set username=?, followercount=?, followingcount=?, time=? where userid=?";

        Instant start = Instant.now();
        int num=0;

        while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(100);

            if (consumerRecords.count()==0) {
                continue;
            }

            num = num + consumerRecords.count();
            List<String> batch = new ArrayList<>();

            List<Object[]> bindedUsers = new ArrayList<>();

            consumerRecords.forEach(record -> {
                batch.add(record.value());

                // insert the record into cassandra
                User user = gson.fromJson(record.value(), User.class);

                Object[] bind = new Object[]{user.getUsername(),
                        user.getFollowerCount(), user.getFollowingCount(), user.getTimestamp(), user.getPk()};

                syncWriter(updateQuery, bind, cassandraClient);

                //bindedUsers.add(bind);
            });

            // send the batch to kafka producer
            recordProducer.sendMessages(batch);
            consumer.commitAsync();

            System.out.println("Messaged written and produced " + num);

            //new CassandraWriterAsync(2).ingest(bindedUsers.iterator(), insertQuery, cassandraClient);
        }
    }

    public void syncWriter(String query, Object[] bind, CassandraConnector client) {
        Session session = client.getSession();

        PreparedStatement prepared = session.prepare(query);
        session.execute(prepared.bind(bind));
    }

    public static void main(String args[]) {
        CassandraConnector client = new CassandraConnector();
        client.connect("127.0.0.1", 9042);

        RedisConnector redisConnector = new RedisConnector();
        redisConnector.connect("localhost");

        InfluxConnector influxConnector = new InfluxConnector();
        influxConnector.connect("http://127.0.0.1:8086");

        DBConsumer dbConsumer = new DBConsumer();
        dbConsumer.recordProducer = new RecordProducer();
        dbConsumer.cassandraClient = client;
        dbConsumer.redisClient = redisConnector;
        dbConsumer.influxClient = influxConnector;

        /*// test
        redisConnector.putRank("ankur", "20");
        redisConnector.putRank("ankur", "20");
        redisConnector.putRank("ankur", "30");
        redisConnector.putRank("prachi", "10");
        redisConnector.putRank("ankur", "35");
        redisConnector.putRank("prachi", "27");
        redisConnector.putRank("arjun", "27");
        redisConnector.putRank("sankalp", "20");


        System.out.println("Rank of ankur: " + redisConnector.getRank("ankur"));
        System.out.println("Rank of prachi: " + redisConnector.getRank("prachi"));
        System.out.println("Rank of arjun: " + redisConnector.getRank("arjun"));
        System.out.println("Rank of sankalp: " + redisConnector.getRank("sankalp"));*/

        //redisConnector.setUp();

        /*redisConnector.putRank("ankur", "20");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));
        redisConnector.putRank("ankur", "20");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));
        redisConnector.putRank("ankur", "30");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));
        redisConnector.putRank("prachi", "10");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));
        redisConnector.putRank("ankur", "35");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));
        redisConnector.putRank("prachi", "20");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));
        redisConnector.putRank("arvind", "30");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));*/

        /*redisConnector.setFollowerAverage("50");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));

        redisConnector.setFollowerAverage("60");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));

        redisConnector.setFollowerAverage("100");
        System.out.println(redisConnector.getClient().hget(RedisConnector.FOLLOWER_AVERAGE_KEY, "avg"));*/

        dbConsumer.readTopic();

        //influxConnector.setUp();
    }
}
