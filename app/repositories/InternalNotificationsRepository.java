package repositories;

import domain.User;

public class InternalNotificationsRepository {
    public boolean hasUserUnreadNotification(User user) {
        return false;
    }

    public boolean hasUserAnyNotification(User user) {
        return false;
    }

    public void notifyUser(User user) {

    }
}
