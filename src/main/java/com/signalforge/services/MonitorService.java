package com.signalforge.services;

import com.signalforge.entity.MonitoredUrl;
import com.signalforge.entity.User;
import com.signalforge.repository.MonitorRepository;
import com.signalforge.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MonitoredUrlService monitoredUrlService;
    @Autowired
    private MonitorRepository monitorRepository;

    public void addUrl(Integer userId, MonitoredUrl url) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        url.setUser(user);
        url.setResponseTime(0L);
        monitoredUrlService.save(url);
    }

    public List<MonitoredUrl> getUrls(Integer userId) {
        return monitorRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteUrl(Integer urlId, UserDetails userDetails) {
        User user = getUser(userDetails);
        MonitoredUrl url = monitorRepository.findById(urlId)
                .orElseThrow(() -> new RuntimeException("URL not found"));
        if (!url.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't own this URL");
        }
        monitorRepository.deleteById(urlId);
    }

    public User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
