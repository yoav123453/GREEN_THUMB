package com.yoav_s.model;

import com.yoav_s.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Plant extends BaseEntity implements Serializable {
    private String speciesId;
    private String nickname;
    private String location;

    public Plant() {}

    public Plant(String speciesId, String nickname, String location) {
        this.speciesId = speciesId;
        this.nickname = nickname;
        this.location = location;
    }

    public String getSpeciesId() { return speciesId; }
    public void setSpeciesId(String speciesId) { this.speciesId = speciesId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Plant plant = (Plant) o;
        return Objects.equals(speciesId, plant.speciesId) && Objects.equals(nickname, plant.nickname) && Objects.equals(location, plant.location);
    }
}

