package domain;

public class ChallengeCreationNotification extends NotificationContext {

    public ChallengeCreationNotification(Challenge challenge) {
        super(createNotificationMessage(challenge));
    }

    private static String createNotificationMessage(Challenge challenge) {
        return ;
    }
}
