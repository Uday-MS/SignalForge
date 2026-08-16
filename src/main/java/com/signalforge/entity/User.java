package com.signalforge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<MonitoredUrl> urls;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<MonitoredUrl> getUrls() { return urls; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Integer id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setUrls(List<MonitoredUrl> urls) { this.urls = urls; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
