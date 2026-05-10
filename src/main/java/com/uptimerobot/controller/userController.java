package com.uptimerobot.controller;

import com.uptimerobot.entity.MonitoredUrl;
import com.uptimerobot.entity.User;
import com.uptimerobot.repository.userRepo;
import com.uptimerobot.services.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/uptimerobot")
public class userController {

    @Autowired private userService userService;
    @Autowired private userRepo userRepo;

    @PostMapping("/addurl")
    public ResponseEntity<String> addUrl(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody MonitoredUrl url) {
        User user = userService.getUser(userDetails);
        userService.addUrl(user.getId(), url);
        return ResponseEntity.ok("URL added successfully");
    }

    @GetMapping("/geturls")
    public ResponseEntity<List<MonitoredUrl>> getUrls(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUser(userDetails);
        return ResponseEntity.ok(userService.getUrl(user.getId()));
    }

    @DeleteMapping("/deleteurl/{urlId}")
    public ResponseEntity<String> deleteUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer urlId) {

        userService.deleteUrl(urlId, userDetails);

        return ResponseEntity.ok("URL deleted successfully");
    }


}
