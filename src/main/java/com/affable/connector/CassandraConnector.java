package com.affable.connector;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class CassandraConnector {

    private Cluster cluster;

    private Session session;

    public void connect(String node, Integer port) {
        System.out.println("creating connection...");
        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();

        session = cluster.connect();
        session.execute("USE affable");
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public void selectAll(String tablename) {
        StringBuilder sb =
                new StringBuilder("SELECT * FROM ").append(tablename);

        String query = sb.toString();
        ResultSet rs = session.execute(query);

        rs.forEach(r -> {
            System.out.println(r);
        });
    }
}