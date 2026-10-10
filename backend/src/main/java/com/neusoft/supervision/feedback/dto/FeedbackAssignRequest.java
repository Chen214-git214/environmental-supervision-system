package com.neusoft.supervision.feedback.dto;

import jakarta.validation.constraints.NotNull;

public class FeedbackAssignRequest {
    @NotNull
    private Long gridMemberId;

    @NotNull
    private Boolean crossProvince;

    public Long getGridMemberId() {
        return gridMemberId;
    }

    public void setGridMemberId(Long gridMemberId) {
        this.gridMemberId = gridMemberId;
    }

    public Boolean getCrossProvince() {
        return crossProvince;
    }

    public void setCrossProvince(Boolean crossProvince) {
        this.crossProvince = crossProvince;
    }
}
