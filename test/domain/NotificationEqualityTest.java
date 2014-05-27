package domain;

import org.junit.Test;

import java.lang.reflect.Field;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class NotificationEqualityTest {

    private final Notification notificationOne = new Notification();
    private final Notification notificationTwo = new Notification();


    @Test
    public void shouldNotificationsBeEqualIfSameNotNullId() throws Exception {
        setNotificationId(notificationOne, 1L);
        setNotificationId(notificationTwo, 1L);

        assertEquals(notificationOne, notificationTwo);
    }

    @Test
    public void shouldNotificationsBeNotEqualIfNotSameNotNullId() throws Exception {
        setNotificationId(notificationOne, 1L);
        setNotificationId(notificationTwo, 0L);

        assertTrue(!notificationOne.equals(notificationTwo));
    }

    @Test
    public void shouldNotificationBeNotEqualsIfOneWithNullId() throws Exception {
        setNotificationId(notificationOne, 1L);
        setNotificationId(notificationTwo, null);

        assertTrue(!notificationOne.equals(notificationTwo));
    }

    @Test
    public void shouldNotificationBeNotEqualsIfBothWithNullId() throws Exception {
        setNotificationId(notificationOne, null);
        setNotificationId(notificationTwo, null);

        assertTrue(!notificationOne.equals(notificationTwo));
    }

    private Notification setNotificationId(Notification notification, Long id) throws NoSuchFieldException, IllegalAccessException {
        Field notificationIdField = Notification.class.getDeclaredField("id");
        notificationIdField.setAccessible(true);
        notificationIdField.set(notification, id);
        return notification;
    }
}
