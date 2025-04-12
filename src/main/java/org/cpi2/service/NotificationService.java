package org.cpi2.service;

import org.cpi2.entities.Notification;
import org.cpi2.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationService {
    private NotificationRepository notificationRepository;
    private LocalDateTime lastRefresh;

    public NotificationService() {
        this.notificationRepository = new NotificationRepository();
        this.lastRefresh = LocalDateTime.now();
    }

    public List<Notification> getAllNotifications() {
        lastRefresh = LocalDateTime.now();
        return notificationRepository.getAllNotifications();
    }

    public List<Notification> getRecentNotifications() {
        return getAllNotifications().stream()
                .filter(n -> "ACTIVE".equals(n.getStatut()))
                .collect(Collectors.toList());
    }

    public List<Notification> getUpcomingNotifications() {
        return getAllNotifications().stream()
                .filter(n -> "A_VENIR".equals(n.getStatut()))
                .collect(Collectors.toList());
    }

    public List<Notification> getProcessedNotifications() {
        return getAllNotifications().stream()
                .filter(n -> "TRAITEE".equals(n.getStatut()))
                .collect(Collectors.toList());
    }

    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.getNotificationsByType(type);
    }

    public List<Notification> getNotificationsByStatut(String statut) {
        return notificationRepository.getNotificationsByStatut(statut);
    }

    public void markAsHandled(int vehiculeId, String type) {
        notificationRepository.marquerCommeTraite(vehiculeId, type);
    }

    public String getLastRefreshFormatted() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return lastRefresh.format(formatter);
    }

    public String getStatisticsText() {
        int recent = getRecentNotifications().size();
        int upcoming = getUpcomingNotifications().size();
        int processed = getProcessedNotifications().size();
        
        return recent + " récentes | " + upcoming + " à venir | " + processed + " traitées";
    }

    public List<Notification> searchNotifications(String searchTerm, String typeFilter, String statusFilter) {
        List<Notification> result = getAllNotifications();

        if (typeFilter != null && !typeFilter.isEmpty() && !typeFilter.equalsIgnoreCase("all")) {
            result = result.stream()
                .filter(n -> n.getType().equals(typeFilter))
                .collect(Collectors.toList());
        }

        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("all")) {
            result = result.stream()
                .filter(n -> n.getStatut().equals(statusFilter))
                .collect(Collectors.toList());
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String search = searchTerm.toLowerCase();
            result = result.stream()
                .filter(n -> n.getTitre().toLowerCase().contains(search) ||
                       n.getDescription().toLowerCase().contains(search) ||
                       n.getVehiculeInfo().toLowerCase().contains(search))
                .collect(Collectors.toList());
        }
        
        return result;
    }
}
