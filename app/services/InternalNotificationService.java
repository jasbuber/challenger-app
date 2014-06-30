package services;

import domain.Notification;
import domain.User;
import play.libs.F;
import repositories.InternalNotificationsRepository;

import java.util.ArrayList;
import java.util.List;

public class InternalNotificationService extends TransactionalBase implements NotificationService {

    private final InternalNotificationsRepository internalNotificationsRepository;

    public InternalNotificationService(InternalNotificationsRepository internalNotificationsRepository) {
        this.internalNotificationsRepository = internalNotificationsRepository;
    }

    @Override
    public void notifyUser(User user, String notificationMsg) {
        final Notification notification = new Notification(user, notificationMsg);
        withTransaction(new F.Function0<Notification>() {
            @Override
            public Notification apply() throws Throwable {
                return internalNotificationsRepository.addNotification(notification);
            }
        });
    }

    @Override
    public void notifyUsers(List<User> users, String notificationMsg) {
        final List<Notification> notifications = createNotificationsFor(users, notificationMsg);

        withTransaction(new F.Function0<List<Notification>>() {
            @Override
            public List<Notification> apply() throws Throwable {
                return internalNotificationsRepository.addNotifications(notifications);
            }
        });
    }

    private List<Notification> createNotificationsFor(List<User> users, String notificationMsg) {
        final List<Notification> notifications = new ArrayList<Notification>();
        for (User user : users) {
            notifications.add(new Notification(user, notificationMsg));
        }
        return notifications;
    }

    @Override
    public boolean hasUserAnyNotification(final User user) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return internalNotificationsRepository.hasUserAnyNotification(user);
            }
        });
    }

    @Override
    public boolean hasUserUnreadNotification(final User user) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return internalNotificationsRepository.hasUserUnreadNotification(user);
            }
        });
    }

    @Override
    public List<Notification> getAllNotifications(final User user) {
        return withReadOnlyTransaction(new F.Function0<List<Notification>>() {
            @Override
            public List<Notification> apply() throws Throwable {
                return internalNotificationsRepository.getAllNotificationsFor(user);
            }
        });
    }

    @Override
    public Notification readNotification(final Notification notification) {
        notification.read();
        return withTransaction(new F.Function0<Notification>() {
            @Override
            public Notification apply() throws Throwable {
                return internalNotificationsRepository.update(notification);
            }
        });
    }

    @Override
    public Long getNumberOfUnreadNotifications(final User user) {
        return withReadOnlyTransaction(new F.Function0<Long>() {
            @Override
            public Long apply() throws Throwable {
                return internalNotificationsRepository.getNumberOfUnreadNotifications(user);
            }
        });
    }

    @Override
    public List<Notification> getNewestNotifications(final User user) {
        return withReadOnlyTransaction(new F.Function0<List<Notification>>() {
            @Override
            public List<Notification> apply() throws Throwable {
                return internalNotificationsRepository.getNewestNotificationsForUser(user);
            }
        });
    }
}
