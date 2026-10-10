package com.neusoft.supervision.auth.dto;

public class GridMemberLoginResponse {
    private String token;
    private GridMemberInfo gridMemberInfo;

    public GridMemberLoginResponse() {
    }

    public GridMemberLoginResponse(String token, GridMemberInfo gridMemberInfo) {
        this.token = token;
        this.gridMemberInfo = gridMemberInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public GridMemberInfo getGridMemberInfo() {
        return gridMemberInfo;
    }

    public void setGridMemberInfo(GridMemberInfo gridMemberInfo) {
        this.gridMemberInfo = gridMemberInfo;
    }
}
