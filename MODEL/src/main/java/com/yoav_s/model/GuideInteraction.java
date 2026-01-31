package com.yoav_s.model;

import com.google.firebase.Timestamp;
import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class GuideInteraction extends BaseEntity implements Serializable {
    private String guideId;
    private String userId;

    private String body;
    private Timestamp createdAt;

    private boolean like;
    private double rating;

    public GuideInteraction() {}

    public GuideInteraction(String guideId, String userId, String body, Timestamp createdAt, boolean like, double rating) {
        this.guideId = guideId;
        this.userId = userId;
        this.body = body;
        this.createdAt = createdAt;
        this.like = like;
        this.rating = rating;
    }

    public String getGuideId() { return guideId; }
    public void setGuideId(String guideId) { this.guideId = guideId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isLike() { return like; }
    public void setLike(boolean like) { this.like = like; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GuideInteraction that = (GuideInteraction) o;
        return like == that.like && Double.compare(rating, that.rating) == 0 && Objects.equals(guideId, that.guideId) && Objects.equals(userId, that.userId) && Objects.equals(body, that.body) && Objects.equals(createdAt, that.createdAt);
    }
}

