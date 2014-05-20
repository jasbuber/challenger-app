package services;

import domain.User;
import org.junit.Test;
import repositories.InternalNotificationsRepository;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class SendingInternalNotificationsTest {

    private final InternalNotificationsRepository internalNotificationsRepository = new InternalNotificationsRepositoryStub();
    private final NotificationService notificationService = new InternalNotificationService(internalNotificationsRepository);

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
        notificationService.notifyUser(userToNotify);

        //then
        assertTrue(notificationService.hasUserAnyNotification(userToNotify));
    }

    @Test
    public void shouldStoreNewUnreadNotificationInRepositoryWhenOneIsSend() throws Exception {
        //given
        User userToNotify = new User("username");

        //when
        notificationService.notifyUser(userToNotify);

        //then
        assertTrue(notificationService.hasUserUnreadNotification(userToNotify));
    }

    private static class InternalNotificationsRepositoryStub extends InternalNotificationsRepository {

        private Map<User, Boolean> isUserNotifiedMap = new HashMap<User, Boolean>();

        @Override
        public boolean hasUserAnyNotification(User user) {
            Boolean isNotified = isUserNotifiedMap.get(user);
            return isNotified != null && isNotified;
        }

        @Override
        public void notifyUser(User user) {
            isUserNotifiedMap.put(user, true);
        }

        @Override
        public boolean hasUserUnreadNotification(User user) {
            Boolean isNotified = isUserNotifiedMap.get(user);
            return isNotified != null && isNotified;
        }
    }
}
