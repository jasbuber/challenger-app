package services;

import domain.Notification;
import domain.User;
import repositories.InternalNotificationsRepository;

import java.util.ArrayList;
import java.util.List;

public class InternalNotificationService implements NotificationService {

    private final InternalNotificationsRepository internalNotificationsRepository;

    public InternalNotificationService(InternalNotificationsRepository internalNotificationsRepository) {
        this.internalNotificationsRepository = internalNotificationsRepository;
    }

    @Override
    public void notifyUser(User user, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId) {
        final Notification notification = new Notification(user, notificationType, notificationMsg, shortNotificationMsg, relevantObjectId);

        internalNotificationsRepository.addNotification(notification);
    }

    @Override
    public void notifyUsers(List<User> users, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId) {
        final List<Notification> notifications = createNotificationsFor(users, notificationType, notificationMsg, shortNotificationMsg, relevantObjectId);

        internalNotificationsRepository.addNotifications(notifications);
    }

    private List<Notification> createNotificationsFor(List<User> users, Notification.NotificationType notificationType, String notificationMsg, String shortNotificationMsg, String relevantObjectId) {
        final List<Notification> notifications = new ArrayList<Notification>();
        for (User user : users) {
            notifications.add(new Notification(user, notificationType, notificationMsg, shortNotificationMsg, relevantObjectId));
        }
        return notifications;
    }

    @Override
    public boolean hasUserUnreadNotification(final User user) {
        return internalNotificationsRepository.hasUserUnreadNotification(user);
    }

    @Override
    public List<Notification> getNotificationsFor(final User user, final int offsetIndex) {
        return internalNotificationsRepository.getNotificationsFor(user, offsetIndex);
    }

    @Override
    public long getNotificationsNrFor(final User user) {
        return internalNotificationsRepository.getNotificationsNrFor(user);
    }

    @Override
    public Notification readNotification(final Notification notification) {
        notification.read();
        return internalNotificationsRepository.update(notification);
    }

    @Override
    public Long getNumberOfUnreadNotifications(final User user) {
        return internalNotificationsRepository.getNumberOfUnreadNotifications(user);
    }

    @Override
    public List<Notification> getNewestNotifications(final User user) {
        return internalNotificationsRepository.getNewestNotificationsForUser(user);
    }

    public Notification getNotification(final long id) {
        return internalNotificationsRepository.getNotification(id);
    }

    public List<Notification> getNewestUnreadNotifications(final User user) {
        return internalNotificationsRepository.getNewestUnreadNotificationsForUser(user);
    }
}
