package com.signalforge.services;

import com.signalforge.entity.AlertHistory;
import com.signalforge.entity.MonitoredUrl;
import com.signalforge.repository.AlertHistoryRepository;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service responsible for creating alert history records
 * and sending email notifications when monitors go DOWN or RECOVER.
 */
@Service
public class AlertService {

    private static final Logger logger = Logger.getLogger(AlertService.class.getName());

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${SENDER_EMAIL:signalforge@gmail.com}")
    private String senderEmail;

    /**
     * Called when a monitor's status changes.
     * Records the event and sends an email notification to the monitor owner.
     */
    public void handleStatusChange(MonitoredUrl monitor, String previousStatus, String newStatus) {
        if (monitor.getUser() == null || monitor.getUser().getEmail() == null) return;

        String eventType;
        if ("UP".equals(previousStatus) && !"UP".equals(newStatus)) {
            eventType = "DOWN";
        } else if (!"UP".equals(previousStatus) && "UP".equals(newStatus)) {
            eventType = "RECOVERED";
        } else {
            return; // No meaningful status change
        }

        // Record alert history
        AlertHistory alert = new AlertHistory(monitor, eventType, newStatus, monitor.getResponseTime());
        alertHistoryRepository.save(alert);

        // Send email notification
        try {
            sendAlertEmail(monitor.getUser().getEmail(), monitor.getUrl(), monitor.getName(), eventType);
            alert.setNotified(true);
            alertHistoryRepository.save(alert);
        } catch (Exception e) {
            logger.severe("Failed to send alert email: " + e.getMessage());
        }
    }

    private void sendAlertEmail(String email, String url, String name, String eventType) throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKeyAuth.setApiKey(brevoApiKey);

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(senderEmail);
        sender.setName("SignalForge");

        SendSmtpEmailTo recipient = new SendSmtpEmailTo();
        recipient.setEmail(email);

        String displayName = (name != null && !name.isBlank()) ? name : url;
        boolean isDown = "DOWN".equals(eventType);

        String statusColor = isDown ? "#EF4444" : "#10B981";
        String statusIcon = isDown ? "🔴" : "🟢";
        String statusText = isDown ? "DOWN" : "RECOVERED";
        String subject = isDown
                ? "🚨 Alert: " + displayName + " is DOWN"
                : "✅ Recovered: " + displayName + " is back UP";

        TransactionalEmailsApi transactionalEmailsApi = new TransactionalEmailsApi();

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.sender(sender);
        sendSmtpEmail.setTo(List.of(recipient));
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setHtmlContent(
                "<div style='font-family: Inter, sans-serif; max-width: 520px; margin: 0 auto; padding: 40px 20px;'>" +
                "<h2 style='color: #635BFF; margin-bottom: 24px;'>SignalForge</h2>" +
                "<div style='background: #f9fafb; border-radius: 12px; padding: 24px; border-left: 4px solid " + statusColor + ";'>" +
                "<p style='font-size: 18px; font-weight: 600; margin-bottom: 8px;'>" + statusIcon + " " + displayName + " is " + statusText + "</p>" +
                "<p style='color: #6b7280; margin-bottom: 4px;'>URL: <code style='background: #e5e7eb; padding: 2px 6px; border-radius: 4px;'>" + url + "</code></p>" +
                "</div>" +
                "<p style='color: #9ca3af; font-size: 13px; margin-top: 24px;'>— SignalForge Monitoring</p>" +
                "</div>"
        );

        transactionalEmailsApi.sendTransacEmail(sendSmtpEmail);
    }
}
