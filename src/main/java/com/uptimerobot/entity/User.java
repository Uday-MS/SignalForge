package com.uptimerobot.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<MonitoredUrl> urls;

    public User() {}

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<MonitoredUrl> getUrls() { return urls; }

    public void setId(Integer id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setUrls(List<MonitoredUrl> urls) { this.urls = urls; }
}
