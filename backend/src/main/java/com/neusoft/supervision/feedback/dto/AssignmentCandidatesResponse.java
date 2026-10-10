package com.neusoft.supervision.feedback.dto;

import java.util.List;

public class AssignmentCandidatesResponse {
    private Long afId;
    private Long cityId;
    private String cityName;
    private boolean hasLocalMembers;
    private boolean crossProvinceEnabled;
    private String message;
    private List<GridMemberResponse> members;

    public Long getAfId() {
        return afId;
    }

    public void setAfId(Long afId) {
        this.afId = afId;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public boolean isHasLocalMembers() {
        return hasLocalMembers;
    }

    public void setHasLocalMembers(boolean hasLocalMembers) {
        this.hasLocalMembers = hasLocalMembers;
    }

    public boolean isCrossProvinceEnabled() {
        return crossProvinceEnabled;
    }

    public void setCrossProvinceEnabled(boolean crossProvinceEnabled) {
        this.crossProvinceEnabled = crossProvinceEnabled;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GridMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<GridMemberResponse> members) {
        this.members = members;
    }
}
