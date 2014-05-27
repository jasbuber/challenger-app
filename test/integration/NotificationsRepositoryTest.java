package integration;

import domain.Notification;
import domain.User;
import org.junit.Test;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class NotificationsRepositoryTest extends EmTestsBase {

    private final InternalNotificationsRepository internalNotificationsRepository = new InternalNotificationsRepository();
    private final UsersRepository usersRepository = new UsersRepository();

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

    /*@Test
    public void shouldUserHaveNotificationAfterNotifyingHim() throws Exception {
        openTransaction();
        User user = usersRepository.createUser("username");
        closeTransaction();

        openTransaction();
        internalNotificationsRepository.notifyUser(user, new Notification());
        closeTransaction();


        openTransaction();
        List<Notification> notifications = internalNotificationsRepository.getAllNotificationsFor(user);
        closeTransaction();

        assertEquals(1, notifications.size());
    }*/


}
