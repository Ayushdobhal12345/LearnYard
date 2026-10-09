package com.bruteforce.ila.auth.dto;

public class AuthResponse {

    private String token;
    private Long studentId;
    private String name;
    private String email;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long studentId, String name, String email) {
        this.token = token;
        this.studentId = studentId;
        this.name = name;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}