package services;

import domain.Notification;
import domain.User;

import java.util.Collections;
import java.util.List;

public class FacebookNotificationService implements NotificationService {


    @Override
    public void notifyUser(User user, String notificationMsg) {

    }

    @Override
    public void notifyUsers(List<User> users, String notificationMsg) {

    }

    @Override
    public boolean hasUserAnyNotification(User user) {
        return false;
    }

    @Override
    public boolean hasUserUnreadNotification(User user) {
        return false;
    }

    @Override
    public List<Notification> getAllNotifications(User user) {
        return null;
    }

    @Override
    public Notification readNotification(Notification notification) {
        return null;
    }

    @Override
    public Long getNumberOfUnreadNotifications(User user) {
        return null;
    }

    @Override
    public List<Notification> getNewestNotifications(User user) {
        return null;
    }
}
