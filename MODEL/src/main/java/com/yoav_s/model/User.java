package com.yoav_s.model;

import com.yoav_s.model.BASE.BaseEntity;

import java.util.Objects;
import java.io.Serializable;

public class User extends BaseEntity implements Serializable {

    public enum Role { USER, CONTENT_CREATOR }

    private String displayName;
    private Role role;
    private String email;
    private String password;

    public User() {
        this.role = Role.USER;
    }

    public User(String displayName, Role role, String email, String password) {
        this.displayName = displayName;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(displayName, user.displayName) && role == user.role && Objects.equals(email, user.email) && Objects.equals(password, user.password);
    }
}
