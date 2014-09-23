package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.Notification;
import domain.User;

import java.util.List;

public class ChallengeNotificationsService {

    private final NotificationService notificationService;

    public ChallengeNotificationsService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void notifyChallengeCreator(Challenge challenge, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId) {
        notificationService.notifyUser(challenge.getCreator(), notificationMsg, shortNotificationMsg, notificationType, relevantObjectId);
    }

    private void notifyAllParticipators(List<User> participators, final Challenge challenge, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId) {
        notificationService.notifyUsers(participators, notificationMsg, shortNotificationMsg, notificationType, relevantObjectId);
    }

    public void notifyAboutNewChallengeParticipation(Challenge challenge, String participatorUsername, List<User> participators) {
        String challengeCreatorMsg = "New participation was added to your challenge " + challenge.getChallengeName() + "." +
                " Participator username is " + participatorUsername;
        String shortChallengeCreatorMsg = "User joined a challenge";

        String challengeParticipatorsMsg = "New participation was added to the challenge " + challenge.getChallengeName() + ". " +
                "Participator username is " + participatorUsername + "." +
                " Challenge creator is " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User is also participating.";

        notifyChallengeCreator(challenge, challengeCreatorMsg, shortChallengeCreatorMsg, Notification.NotificationType.new_participant, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.new_participant, String.valueOf(challenge.getId()));
    }

    public void notifyAboutChallengeLeaving(Challenge challenge, String participatorUsername, List<User> participators) {
        String challengeCreatorMsg = "Participator " + participatorUsername + " has left your challenge " + challenge.getChallengeName();
        String shortChallengeCreatorMsg = "User left a challenge";

        String challengeParticipatorsMsg = "Participator " + participatorUsername + " has left challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User is no longer participating.";

        notifyChallengeCreator(challenge, challengeCreatorMsg, shortChallengeCreatorMsg, Notification.NotificationType.participant_left, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.participant_left, String.valueOf(challenge.getId()));
    }

    public void notifyAboutSubmittingChallengeResponse(ChallengeParticipation challengeParticipation, List<User> participators) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeCreatorMsg = "User " + challengeParticipation.getParticipator() + " has just submitted response to your challenge " + challenge.getChallengeName();
        String shortChallengeCreatorMsg = "New response available";

        String challengeParticipatorsMsg = "User " + challengeParticipation.getParticipator() + " has just submitted response to the challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User added a response";

        notifyChallengeCreator(challenge, challengeCreatorMsg, shortChallengeCreatorMsg, Notification.NotificationType.new_response, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.new_response, String.valueOf(challenge.getId()));
    }

    public void notifyAboutChallengeResponseAcceptance(ChallengeParticipation challengeParticipation, List<User> participators) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorMsg = "Response accepted!";

        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User response accepted...";

        participators.remove(challengeParticipation.getParticipator());
        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg, shortChallengeParticipatorMsg, Notification.NotificationType.response_accepted, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, participatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.response_accepted, String.valueOf(challenge.getId()));
    }

    public void notifyAboutChallengeResponseRefusal(ChallengeParticipation challengeParticipation, List<User> participators) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorMsg = "Response refused.";

        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User response refused...";

        participators.remove(challengeParticipation.getParticipator());
        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg, shortChallengeParticipatorMsg, Notification.NotificationType.response_refused, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, participatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.response_refused, String.valueOf(challenge.getId()));
    }
}
