package repositories;

import domain.Notification;
import domain.User;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class InternalNotificationsRepository {
    public boolean hasUserUnreadNotification(User user) {
        return getNumberOfUnreadNotifications(user) > 0;
    }

    public Long getNumberOfUnreadNotifications(User user) {
        Query getUnreadNotificationsForUserQuery = JPA.em().createQuery("SELECT COUNT(n) " +
                "FROM Notification n " +
                "WHERE n.user = :user " +
                "AND n.isRead <> 'Y'");
        getUnreadNotificationsForUserQuery.setParameter("user", user);
        return (Long) getUnreadNotificationsForUserQuery.getSingleResult();
    }

    public boolean hasUserAnyNotification(User user) {
        return !getAllNotificationsFor(user).isEmpty();
    }

    public Notification addNotification(Notification notification) {
        JPA.em().persist(notification);
        return notification;
    }

    public List<Notification> addNotifications(List<Notification> notifications) {
        List<Notification> persistedNotifications = new ArrayList<Notification>(notifications.size());
        for (Notification notification : notifications) {
            persistedNotifications.add(addNotification(notification));
        }
        return persistedNotifications;
    }

    public List<Notification> getAllNotificationsFor(User user) {
        Query getAllNotificationsForUserQuery = JPA.em().createQuery("SELECT n " +
                "FROM Notification n " +
                "WHERE n.user = :user " +
                "ORDER BY n.creationTimestamp DESC");

        getAllNotificationsForUserQuery.setParameter("user", user);
        return getAllNotificationsForUserQuery.getResultList();
    }

    public Notification update(Notification notification) {
        return JPA.em().merge(notification);
    }

    public List<Notification> getNewestNotificationsForUser(User user) {
        Query getNewestNotificationsForUserQuery = JPA.em().createQuery("SELECT n " +
                "FROM Notification n " +
                "WHERE n.user = :user " +
                "ORDER BY n.creationTimestamp DESC"
        );

        getNewestNotificationsForUserQuery.setParameter("user", user);
        getNewestNotificationsForUserQuery.setMaxResults(5);
        return getNewestNotificationsForUserQuery.getResultList();
    }

    public Notification getNotification(long id){ return JPA.em().find(Notification.class, id); }

}
