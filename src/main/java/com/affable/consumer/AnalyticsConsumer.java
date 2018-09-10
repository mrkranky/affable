package com.affable.consumer;

import com.affable.connector.InfluxConnector;
import com.affable.connector.RedisConnector;
import com.affable.pojo.User;
import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class AnalyticsConsumer {
    private static final String TOPIC_NAME = "influencers-analytics";

    private RedisConnector redisClient;
    private InfluxConnector influxClient;

    Gson gson = new Gson();

    private Consumer<String, String> createConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "consumer-2");
        props.put("enable.auto.commit", "false");
        //props.put("auto.commit.interval.ms", "1000");
        //props.put("session.timeout.ms", "30000");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(TOPIC_NAME));

        return consumer;
    }

    public void readTopic() {
        Consumer<String, String> consumer = createConsumer();

        System.out.println("AnalyticsConsumer started...");

        int num=0;

        BatchPoints batchPoints = BatchPoints
                .database(InfluxConnector.DB_NAME)
                .retentionPolicy("defaultPolicy")
                .build();

        while (true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);

            if (consumerRecords.count() == 0) {
                continue;
            }

            num += consumerRecords.count();

            List<Object[]> bindedUsers = new ArrayList<>();

            consumerRecords.forEach(record -> {

                User user = gson.fromJson(record.value(), User.class);

                // Ranking and averaging in REDIS
                redisClient.putRank(user.getPk().toString(), user.getFollowerCount().toString());

                // Data point for influxDB
                double ratio = user.getFollowerCount().doubleValue()/user.getFollowingCount().doubleValue();

                Point point = Point.measurement("influencer-stat")
                        .time(user.getTimestamp().longValue(), TimeUnit.MILLISECONDS)
                        .addField("userid", user.getPk().toString())
                        .addField("followers", user.getFollowerCount().longValue())
                        .addField("following", user.getFollowingCount().longValue())
                        .addField("followerratio", ratio)
                        .build();

                batchPoints.point(point);
            });

            consumer.commitAsync();

            influxClient.getClient().write(batchPoints);
            System.out.println("Messages ranked and pointed " + num);
        }
    }

    public static void main(String args[]) {
        RedisConnector redisConnector = new RedisConnector();
        redisConnector.connect("localhost");

        InfluxConnector influxConnector = new InfluxConnector();
        influxConnector.connect("http://127.0.0.1:8086");

        AnalyticsConsumer analyticsConsumer = new AnalyticsConsumer();
        analyticsConsumer.redisClient = redisConnector;
        analyticsConsumer.influxClient = influxConnector;

        analyticsConsumer.readTopic();

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

        //influxConnector.setUp();
    }
}
