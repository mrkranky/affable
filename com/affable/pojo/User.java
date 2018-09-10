package com.affable.pojo;

import java.math.BigInteger;

public class User {

    private Integer pk;
    private String username;
    private BigInteger followerCount;
    private BigInteger followingCount;
    private BigInteger timestamp;

    public User(Integer userid, String username, BigInteger followerCount,
                BigInteger followingCount, BigInteger timestamp) {
        this.pk = userid;
        this.username = username;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.timestamp = timestamp;
    }

    public Integer getPk() {
        return this.pk;
    }

    public void setPk(Integer pk) {
        this.pk = pk;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigInteger getFollowerCount() {
        return this.followerCount;
    }

    public void setFollowerCount(BigInteger followerCount) {
        this.followerCount = followerCount;
    }

    public BigInteger getFollowingCount() {
        return this.followingCount;
    }

    public void setFollowingCount(BigInteger followingCount) {
        this.followingCount = followingCount;
    }

    public BigInteger getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }
}
