package repositories;

import domain.Notification;
import domain.User;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class InternalNotificationsRepository {

    private final int pagingRowNumber = 10;

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

    public List<Notification> getNotificationsFor(User user, int offsetIndex) {
        Query getNotificationsForUserQuery = JPA.em().createQuery("SELECT n " +
                "FROM Notification n " +
                "WHERE n.user = :user " +
                "ORDER BY n.creationTimestamp DESC");

        getNotificationsForUserQuery.setParameter("user", user);
        getNotificationsForUserQuery.setMaxResults(pagingRowNumber);
        getNotificationsForUserQuery.setFirstResult(calculateOffsetNumber(offsetIndex));
        return getNotificationsForUserQuery.getResultList();
    }

    public long getNotificationsNrFor(User user) {
        Query getNotificationsForUserQuery = JPA.em().createQuery("SELECT count(n) " +
                "FROM Notification n " +
                "WHERE n.user = :user ");

        getNotificationsForUserQuery.setParameter("user", user);
        return (long)getNotificationsForUserQuery.getSingleResult();
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

    public List<Notification> getNewestUnreadNotificationsForUser(User user) {
        Query getNewestNotificationsForUserQuery = JPA.em().createQuery("SELECT n " +
                        "FROM Notification n " +
                        "WHERE n.user = :user AND n.isRead='N' " +
                        "ORDER BY n.creationTimestamp DESC"
        );

        getNewestNotificationsForUserQuery.setParameter("user", user);
        getNewestNotificationsForUserQuery.setMaxResults(5);
        return getNewestNotificationsForUserQuery.getResultList();
    }

    public Notification getNotification(long id){ return JPA.em().find(Notification.class, id); }

    private int calculateOffsetNumber(int index){
        return index * pagingRowNumber;
    }

}
