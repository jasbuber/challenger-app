package integration;

import domain.Notification;
import domain.User;
import org.junit.After;
import org.junit.Test;
import play.db.jpa.JPA;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class NotificationsRepositoryTest extends EmTestsBase {

    private final InternalNotificationsRepository internalNotificationsRepository = new InternalNotificationsRepository();
    private final UsersRepository usersRepository = new UsersRepository();

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test
    public void shouldGetNoNotificationsIfNoneForUserInRepository() throws Exception {
        User user = createUser();

        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getAllNotificationsFor(user);
        closeTransaction();

        assertTrue(notifications.isEmpty());
    }

    @Test
    public void shouldUserHaveNotificationAfterNotifyingHim() throws Exception {
        User user = createUser();

        openTransaction();
        internalNotificationsRepository.addNotification(new Notification(user));
        closeTransaction();


        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getAllNotificationsFor(user);
        boolean hasUserAnyNotification = internalNotificationsRepository.hasUserAnyNotification(user);
        closeTransaction();

        assertEquals(1, notifications.size());
        assertTrue(hasUserAnyNotification);
    }

    @Test
    public void shouldUserHaveNoUnreadNotificationAfterSetOnlyAsRead() throws Exception {
        User user = createUser();

        openTransaction();
        Notification notification = new Notification(user);
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

        assertTrue(hasUnreadNotification);
        assertEquals(1, nrOfUnreadNotifications);
    }

    private User createUser() {
        openTransaction();
        User user = usersRepository.createUser("username");
        closeTransaction();
        return user;
    }


}
