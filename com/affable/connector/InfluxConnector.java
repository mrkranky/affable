package com.affable.connector;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;

public class InfluxConnector {
    public static final String DB_NAME = "affable_influencers";

    private InfluxDB client;

    public void connect(String host) {
        client = InfluxDBFactory.connect(host);

        Pong response = client.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error connecting to influxDB");
            return;
        } else {
            System.out.println("InfluxDB client connected");
        }

    }

    public InfluxDB getClient() {
        return client;
    }

    public void setUp() {
        client.createDatabase("affable_influencers");
        client.createRetentionPolicy(
                "defaultPolicy", "affable_influencers", "30d", 1, true);
    }
}