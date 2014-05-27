package services;

import domain.Notification;
import domain.User;
import repositories.InternalNotificationsRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InternalNotificationService implements NotificationService {

    private final InternalNotificationsRepository internalNotificationsRepository;

    public InternalNotificationService(InternalNotificationsRepository internalNotificationsRepository) {
        this.internalNotificationsRepository = internalNotificationsRepository;
    }

    @Override
    public void notifyUser(User user) {
        internalNotificationsRepository.notifyUser(user, new Notification());
    }

    @Override
    public void notifyUsers(List<User> users) {

    }

    @Override
    public boolean hasUserAnyNotification(User user) {
        return internalNotificationsRepository.hasUserAnyNotification(user);
    }

    @Override
    public boolean hasUserUnreadNotification(User user) {
        return internalNotificationsRepository.hasUserUnreadNotification(user);
    }

    @Override
    public List<Notification> getAllNotifications(User user) {
        return internalNotificationsRepository.getAllNotificationsFor(user);
    }

    @Override
    public void readNotification(Notification notification) {
        notification.read();
        internalNotificationsRepository.update(notification);
    }
}
