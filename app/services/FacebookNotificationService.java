package services;

import domain.User;

import java.util.List;

public class FacebookNotificationService implements NotificationService {


    @Override
    public void notifyUser(User user) {

    }

    @Override
    public void notifyUsers(List<User> users) {

    }

    @Override
    public boolean hasUserAnyNotification(User user) {
        return false;
    }

    @Override
    public boolean hasUserUnreadNotification(User user) {
        return false;
    }
}
