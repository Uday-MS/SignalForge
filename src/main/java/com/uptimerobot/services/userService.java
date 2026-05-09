package com.uptimerobot.services;

import com.uptimerobot.entity.MonitoredUrl;
import com.uptimerobot.entity.User;
import com.uptimerobot.repository.JPARepo;
import com.uptimerobot.repository.userRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class userService {

    @Autowired private userRepo userRepo;
    @Autowired private MonitoredUrlService monitoredUrlService;
    @Autowired private JPARepo jpaRepo;

    public void addUrl(Integer userId, MonitoredUrl url) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        url.setUser(user);
        url.setResponseTime(0L);
        monitoredUrlService.save(url);
    }

    public List<MonitoredUrl> getUrl(Integer userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUrls();
    }

    public void deleteUrl(Integer urlId, UserDetails userDetails) {
        User user = getUser(userDetails);
        MonitoredUrl url = jpaRepo.findById(urlId)
                .orElseThrow(() -> new RuntimeException("URL not found"));
        if (!url.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't own this URL");
        }
        jpaRepo.deleteById(urlId);
    }

    public User getUser(UserDetails userDetails) {
        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
