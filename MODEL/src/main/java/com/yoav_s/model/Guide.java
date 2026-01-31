package com.yoav_s.model;

import com.google.firebase.Timestamp;
import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class Guide extends BaseEntity implements Serializable {
    private String contentCreatorId;
    private String title;
    private String text;

    private int viewsCount;
    private int commentsCount;
    private double avgRating;

    private Timestamp publishedAt;

    public Guide() {}

    public Guide(String contentCreatorId, String title, String text,
                 int viewsCount, int commentsCount, double avgRating, Timestamp publishedAt) {
        this.contentCreatorId = contentCreatorId;
        this.title = title;
        this.text = text;
        this.viewsCount = viewsCount;
        this.commentsCount = commentsCount;
        this.avgRating = avgRating;
        this.publishedAt = publishedAt;
    }

    public String getContentCreatorId() { return contentCreatorId; }
    public void setContentCreatorId(String contentCreatorId) { this.contentCreatorId = contentCreatorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getViewsCount() { return viewsCount; }
    public void setViewsCount(int viewsCount) { this.viewsCount = viewsCount; }

    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }

    public Timestamp getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Timestamp publishedAt) { this.publishedAt = publishedAt; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Guide guide = (Guide) o;
        return viewsCount == guide.viewsCount && commentsCount == guide.commentsCount && Double.compare(avgRating, guide.avgRating) == 0 && Objects.equals(contentCreatorId, guide.contentCreatorId) && Objects.equals(title, guide.title) && Objects.equals(text, guide.text) && Objects.equals(publishedAt, guide.publishedAt);
    }
}
