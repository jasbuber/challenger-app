package domain;

public abstract class NotificationContext {

    private final String notificationMessage;


    public NotificationContext(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}
