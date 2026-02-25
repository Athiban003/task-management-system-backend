package com.athiban.task_management.dto;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType="Bearer";

    public LoginResponse(String accessToken, String refreshToken){
        this.accessToken=accessToken;
        this.refreshToken=refreshToken;
    }

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType() { return tokenType; }
}
