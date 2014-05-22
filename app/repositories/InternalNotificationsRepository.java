package repositories;

import domain.Notification;
import domain.User;

import java.util.Collections;
import java.util.List;

public class InternalNotificationsRepository {
    public boolean hasUserUnreadNotification(User user) {
        return false;
    }

    public boolean hasUserAnyNotification(User user) {
        return false;
    }

    public void notifyUser(User user, Notification notification) {

    }

    public List<Notification> getAllNotificationsFor(User user) {
        return Collections.emptyList();
    }
}
