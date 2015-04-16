package services;

import domain.Notification;
import domain.User;
import org.junit.Test;
import repositories.InternalNotificationsRepository;

import java.util.*;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class SendingInternalNotificationsTest {

    private final static String SOME_NOTIFICATION_MSG = "notificationMsg";

    private final InternalNotificationsRepository internalNotificationsRepository = new InternalNotificationsRepositoryStub();
    private final NotificationService notificationService =
            new InternalNotificationService(internalNotificationsRepository);

    private boolean hasUserAnyNotification(User user) {
        return notificationService.getNotificationsFor(user, 0).size() > 0;
    }

    @Test
    public void shouldUserHaveNoNotificationsIfNoneWasSentToHim() throws Exception {
        //given
        User user = new User("username");

        //then
        assertFalse(hasUserAnyNotification(user));
    }

    @Test
    public void shouldUserHaveNotificationsIfOneWasSentToHim() throws Exception {
        //given
        User userToNotify = new User("username");

        //when
        notifyUser(userToNotify);

        //then
        assertTrue(hasUserAnyNotification(userToNotify));
    }

    private void notifyUser(User userToNotify) {
        notificationService.notifyUser(userToNotify, SOME_NOTIFICATION_MSG, null, null, null);
    }

    @Test
    public void shouldStoreNewUnreadNotificationInRepositoryWhenOneIsSent() throws Exception {
        //given
        User userToNotify = new User("username");

        //when
        notifyUser(userToNotify);

        //then
        assertTrue(notificationService.hasUserUnreadNotification(userToNotify));
    }

    @Test
    public void shouldStoreNoUnreadNotificationsInRepositoryIfOneIsAlreadyRead() throws Exception {
        //given
        User user = new User("username");
        notifyUser(user);

        //when
        List<Notification> notifications = notificationService.getNotificationsFor(user, 0);
        Notification notification = notifications.get(0);
        notificationService.readNotification(notification);

        //then
        assertFalse(notificationService.hasUserUnreadNotification(user));
        assertTrue(hasUserAnyNotification(user));
    }


    private static class InternalNotificationsRepositoryStub extends InternalNotificationsRepository {

        private Map<User, List<Notification>> usersNotifications = new HashMap<User, List<Notification>>();

        @Override
        public boolean hasUserUnreadNotification(User user) {
            List<Notification> userNotifications = usersNotifications.get(user);
            List<Notification> unreadNotifications = new ArrayList<Notification>();
            for (Notification userNotification : userNotifications) {
                if (!userNotification.isRead()) {
                    unreadNotifications.add(userNotification);
                }
            }

            return !unreadNotifications.isEmpty();
        }

        @Override
        public List<Notification> getNotificationsFor(User user, int offset) {
            List<Notification> userNotifications = usersNotifications.get(user);
            if(userNotifications == null) {
                return Collections.emptyList();
            }
            return userNotifications;
        }

        @Override
        public Notification update(Notification notification) {
            Collection<List<Notification>> allNotifications = usersNotifications.values();
            for (List<Notification> userNotifications : allNotifications) {
                for (Notification userNotification : userNotifications) {
                    if (userNotification.equals(notification)) {
                        userNotification.read();
                        return userNotification;
                    }
                }
            }
            return notification;
        }

        @Override
        public Notification addNotification(Notification notification) {
            User user = notification.getUser();
            List<Notification> userNotifications = usersNotifications.get(user);
            if (userNotifications == null) {
                usersNotifications.put(user, Arrays.asList(notification));
            } else {
                usersNotifications.get(user).add(notification);
            }
            return notification;
        }

        @Override
        public Long getNumberOfUnreadNotifications(User user) {
            throw new RuntimeException("getNumberOfUnreadNotifications is not implemented");
        }

        @Override
        public List<Notification> addNotifications(List<Notification> notifications) {
            throw new RuntimeException("addNotifications is not implemented");
        }

        @Override
        public long getNotificationsNrFor(User user) {
            throw new RuntimeException("getNotificationsNrFor is not implemented");
        }

        @Override
        public List<Notification> getNewestNotificationsForUser(User user) {
            throw new RuntimeException("getNewestNotificationsForUser");
        }

        @Override
        public List<Notification> getNewestUnreadNotificationsForUser(User user) {
            throw new RuntimeException("getNewestUnreadNotificationsForUser");
        }

        @Override
        public Notification getNotification(long id) {
            throw new RuntimeException("getNotification");
        }
    }
}
