package services;

import domain.Notification;
import domain.User;

import java.util.List;

public interface NotificationService {

    void notifyUser(User user, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId);

    void notifyUsers(List<User> users, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId);

    boolean hasUserAnyNotification(User user);

    boolean hasUserUnreadNotification(User user);

    List<Notification> getAllNotifications(User user);

    Notification readNotification(Notification notification);

    Long getNumberOfUnreadNotifications(User user);

    List<Notification> getNewestNotifications(final User user);
}
