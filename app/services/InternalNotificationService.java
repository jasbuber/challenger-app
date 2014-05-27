package services;

import domain.Notification;
import domain.User;
import play.libs.F;
import repositories.InternalNotificationsRepository;

import java.util.List;

public class InternalNotificationService extends TransactionalBase implements NotificationService {

    private final InternalNotificationsRepository internalNotificationsRepository;

    public InternalNotificationService(InternalNotificationsRepository internalNotificationsRepository) {
        this.internalNotificationsRepository = internalNotificationsRepository;
    }

    @Override
    public void notifyUser(User user) {
        final Notification notification = new Notification(user);
        withTransaction(new F.Function0<Notification>() {
            @Override
            public Notification apply() throws Throwable {
                return internalNotificationsRepository.addNotification(notification);
            }
        });
    }

    @Override
    public void notifyUsers(List<User> users) {

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
}
