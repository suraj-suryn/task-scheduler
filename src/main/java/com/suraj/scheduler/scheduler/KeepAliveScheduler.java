package com.suraj.scheduler.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Pings the app's own /login page every 5 minutes to prevent Railway
 * (and similar platforms) from putting the app to sleep due to inactivity.
 * Only active in the production profile.
 */
@Component
@Profile("prod")
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    @Value("${app.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void keepAlive() {
        try {
            String pingUrl = baseUrl + "/login";
            restTemplate.getForStatusCode(pingUrl);
            log.debug("Keep-alive ping sent to {}", pingUrl);
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
