package services;

import domain.Notification;
import domain.User;

import java.util.List;

public interface NotificationService {

    void notifyUser(User user, String notificationMsg);

    void notifyUsers(List<User> users, String notificationMsg);

    boolean hasUserAnyNotification(User user);

    boolean hasUserUnreadNotification(User user);

    List<Notification> getAllNotifications(User user);

    Notification readNotification(Notification notification);
}
