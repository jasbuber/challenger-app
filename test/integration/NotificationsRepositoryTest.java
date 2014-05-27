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
        openTransaction();
        User user = usersRepository.createUser("username");
        closeTransaction();

        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getAllNotificationsFor(user);
        closeTransaction();

        assertTrue(notifications.isEmpty());
    }

    @Test
    public void shouldUserHaveNotificationAfterNotifyingHim() throws Exception {
        openTransaction();
        User user = usersRepository.createUser("username");
        closeTransaction();

        openTransaction();
        internalNotificationsRepository.addNotification(new Notification(user));
        closeTransaction();


        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getAllNotificationsFor(user);
        closeTransaction();

        assertEquals(1, notifications.size());
    }


}
