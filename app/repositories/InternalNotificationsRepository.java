package repositories;

import domain.Notification;
import domain.User;
import play.db.jpa.JPA;

import javax.persistence.Query;
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
        Query getAllNotificationsForUserQuery = JPA.em().createQuery("SELECT n " +
                                                                     "FROM Notification n " +
                                                                     "WHERE n.user = :user");

        getAllNotificationsForUserQuery.setParameter("user", user);
        return getAllNotificationsForUserQuery.getResultList();
    }

    public void update(Notification notification) {

    }
}
