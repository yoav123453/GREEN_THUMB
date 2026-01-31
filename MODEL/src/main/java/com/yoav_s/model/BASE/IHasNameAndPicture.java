package com.yoav_s.model.BASE;

/**
 * Defines the contract required for any Model (T) used by BaseListActivity.
 * Ensures the generic activity can safely access both the name (for the UI)
 * and the picture String (for the list item adapter binding).
 *
 * NOTE: Simpler classes (like ToyCategory) must implement this and return
 * null or "" for getPicture().
 */
public interface IHasNameAndPicture {
    String getName();
    void setName(String name);

    // MANDATORY property for this generic list setup
    String getPicture();
    void setPicture(String picture);
}
