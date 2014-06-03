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
            new InternalNotificationServiceWithoutTransactionMgmt(internalNotificationsRepository);

    @Test
    public void shouldUserHaveNoNotificationsIfNoneWasSentToHim() throws Exception {
        //given
        User user = new User("username");

        //then
        assertFalse(notificationService.hasUserAnyNotification(user));
    }

    @Test
    public void shouldUserHaveNotificationsIfOneWasSentToHim() throws Exception {
        //given
        User userToNotify = new User("username");

        //when
        notificationService.notifyUser(userToNotify, SOME_NOTIFICATION_MSG);

        //then
        assertTrue(notificationService.hasUserAnyNotification(userToNotify));
    }

    @Test
    public void shouldStoreNewUnreadNotificationInRepositoryWhenOneIsSent() throws Exception {
        //given
        User userToNotify = new User("username");

        //when
        notificationService.notifyUser(userToNotify, SOME_NOTIFICATION_MSG);

        //then
        assertTrue(notificationService.hasUserUnreadNotification(userToNotify));
    }

    @Test
    public void shouldStoreNoUnreadNotificationsInRepositoryIfOneIsAlreadyRead() throws Exception {
        //given
        User user = new User("username");
        notificationService.notifyUser(user, SOME_NOTIFICATION_MSG);

        //when
        List<Notification> notifications = notificationService.getAllNotifications(user);
        Notification notification = notifications.get(0);
        notificationService.readNotification(notification);

        //then
        assertFalse(notificationService.hasUserUnreadNotification(user));
        assertTrue(notificationService.hasUserAnyNotification(user));
    }


    private static class InternalNotificationsRepositoryStub extends InternalNotificationsRepository {

        private Map<User, List<Notification>> usersNotifications = new HashMap<User, List<Notification>>();

        @Override
        public boolean hasUserAnyNotification(User user) {
            List<Notification> userNotifications = usersNotifications.get(user);
            return userNotifications != null && !userNotifications.isEmpty();
        }

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
        public List<Notification> getAllNotificationsFor(User user) {
            return usersNotifications.get(user);
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
    }
}
