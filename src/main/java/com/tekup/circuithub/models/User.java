package com.tekup.circuithub.models;

import java.time.LocalDate;

public class User {
    private String id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String joinDate;

    public User() {}

    public User(String id, String fullName, String email, String passwordHash) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.joinDate = LocalDate.now().toString();
    }

    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getJoinDate() { return joinDate; }

    public void setId(String id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
}
