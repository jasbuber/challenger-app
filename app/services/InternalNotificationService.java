package services;

import domain.User;
import repositories.InternalNotificationsRepository;

import java.util.List;

public class InternalNotificationService implements NotificationService {

    private final InternalNotificationsRepository internalNotificationsRepository;

    public InternalNotificationService(InternalNotificationsRepository internalNotificationsRepository) {
        this.internalNotificationsRepository = internalNotificationsRepository;
    }

    @Override
    public void notifyUser(User user) {
        internalNotificationsRepository.notifyUser(user);
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
}
