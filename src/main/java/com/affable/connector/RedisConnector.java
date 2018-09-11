package com.affable.connector;

import redis.clients.jedis.Jedis;

import java.util.Map;

public class RedisConnector {

    public static final String USER_TO_SCORE_KEY = "USER_TO_SCORE";
    public static final String DISTINCT_SCORE_KEY = "DISTINCT_SCORE";
    public static final String SORTED_SCORE_KEY = "SORTED_SCORE";
    public static final String FOLLOWER_AVERAGE_KEY = "FOLLOWER_AVERAGE";
    public static final String SUSPICIOUS_KEY = "SUSPICIOUS";


    private Jedis client;

    /**
     * Should just be called once, for setting up the application.
     * Once run, there would be no need to run this again
     */
    public void setUp() {
        // initially the average is 0
        client.hset(FOLLOWER_AVERAGE_KEY, "avg", "0");
    }

    public void connect(String node) {
        client = new Jedis(node);
    }

    public Jedis getClient() {
        return this.client;
    }

    public void insertRecords(String keyspace, Map<String, Double> scores) {
        client.zadd(keyspace, scores);
    }

    public void putRank(String user, String newScore) {

        // check if the user is suspicious
        // a suspicious user should not affect ranking
        if (client.hexists(SUSPICIOUS_KEY, user)) {
            return;
        }

        // get the old followerCount of the user
        String oldScore = client.hget(USER_TO_SCORE_KEY, user);

        // update the hash with new follower count of the user
        client.hset(USER_TO_SCORE_KEY, user, newScore);

        if (oldScore != null) {
            Long oldCount = client.hincrBy(DISTINCT_SCORE_KEY, oldScore, -1);

            if (oldCount == 0) {
                client.zrem(SORTED_SCORE_KEY, oldScore);
                client.hdel(DISTINCT_SCORE_KEY, oldScore);
            }

            // manipulate the average, as the old score should no longer be considered for average
            Long totalUsers = client.hlen(USER_TO_SCORE_KEY);
            double new_average=0;

            // compute the average by removing this user's score
            if (totalUsers != 1L) {
                double old_average = Double.parseDouble(client.hget(FOLLOWER_AVERAGE_KEY, "avg"));
                new_average = (old_average-(Double.parseDouble(oldScore)/totalUsers.doubleValue())) * (totalUsers.doubleValue()/(totalUsers.doubleValue()-1.0));
            }
            client.hset(FOLLOWER_AVERAGE_KEY, "avg", String.valueOf(new_average));
        }

        client.hincrBy(DISTINCT_SCORE_KEY, newScore, 1);
        client.zadd(SORTED_SCORE_KEY, Double.parseDouble(newScore), newScore);

        setFollowerAverage(newScore);
    }

    public void setFollowerAverage(String newScore) {
        // take care of averaging out followers
        Long totalUsers = client.hlen(USER_TO_SCORE_KEY);

        // get old average
        String oldAverage = client.hget(FOLLOWER_AVERAGE_KEY, "avg");
        double oldAvg = 0;

        if (oldAverage != null) {
            oldAvg = Double.parseDouble(oldAverage);
        }

        // calculate new average
        double newAvg = oldAvg + ((Double.parseDouble(newScore)-oldAvg)/totalUsers.doubleValue());
        client.hset(FOLLOWER_AVERAGE_KEY, "avg", String.valueOf(newAvg));
    }

    public Long getRank(String user) {
        String score = client.hget(USER_TO_SCORE_KEY, user);
        return client.zrevrank(SORTED_SCORE_KEY, score)+1;
    }
}