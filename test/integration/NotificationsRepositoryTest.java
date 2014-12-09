package integration;

import domain.Notification;
import domain.User;
import org.junit.After;
import org.junit.Test;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class NotificationsRepositoryTest extends EmTestsBase {

    private final static String SOME_NOTIFICATION_MSG = "notificationMsg";

    private final InternalNotificationsRepository internalNotificationsRepository = new InternalNotificationsRepository();
    private final UsersRepository usersRepository = new UsersRepository();

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void shouldGetNoNotificationsIfNoneForUserInRepository() throws Exception {
        User user = createUser("username");

        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getNotificationsFor(user, 0);
        closeTransaction();

        assertTrue(notifications.isEmpty());
    }

    @Test
    public void shouldUserHaveUnreadNotificationJustAfterBeingNotified() throws Exception {
        User user = createUser("username");

        openTransaction();
        internalNotificationsRepository.addNotification(new Notification(user, SOME_NOTIFICATION_MSG));
        closeTransaction();


        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getNotificationsFor(user, 0);
        boolean hasUserAnyNotification = notifications.size() > 0;
        boolean hasUserUnreadNotifications = internalNotificationsRepository.hasUserUnreadNotification(user);
        closeTransaction();

        assertEquals(1, notifications.size());
        assertTrue(hasUserAnyNotification);
        assertTrue(hasUserUnreadNotifications);
    }

    @Test
    public void shouldUserHaveNoUnreadNotificationAfterSetOnlyAsRead() throws Exception {
        User user = createUser("username");

        openTransaction();
        Notification notification = new Notification(user, SOME_NOTIFICATION_MSG);
        internalNotificationsRepository.addNotification(notification);
        closeTransaction();

        openTransaction();
        notification.read();
        internalNotificationsRepository.update(notification);
        closeTransaction();

        openTransaction();
        boolean hasUnreadNotification = internalNotificationsRepository.hasUserUnreadNotification(user);
        long nrOfUnreadNotifications = internalNotificationsRepository.getNumberOfUnreadNotifications(user);
        closeTransaction();

        assertFalse(hasUnreadNotification);
        assertEquals(0, nrOfUnreadNotifications);
    }

    @Test
    public void shouldBothUsersHaveNotificationAfterNotifyingThem() throws Exception {
        User userOne = createUser("userOne");
        User userTwo = createUser("userTwo");

        openTransaction();
        internalNotificationsRepository.addNotifications(Arrays.asList(new Notification(userOne, SOME_NOTIFICATION_MSG), new Notification(userTwo, SOME_NOTIFICATION_MSG)));
        closeTransaction();

        openTransaction();
        List<Notification> notificationsUserOne = internalNotificationsRepository.getNotificationsFor(userOne, 0);
        List<Notification> notificationsUserTwo = internalNotificationsRepository.getNotificationsFor(userTwo, 0);
        closeTransaction();

        assertEquals(1, notificationsUserOne.size());
        assertEquals(1, notificationsUserTwo.size());
    }

    private User createUser(String username) {
        openTransaction();
        User user = usersRepository.createUser(username);
        closeTransaction();
        return user;
    }

}
