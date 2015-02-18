package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.Notification;
import domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ChallengeNotificationsService {

    private final NotificationService notificationService;
    private static final Logger logger = LoggerFactory.getLogger(ChallengeNotificationsService.class);

    public ChallengeNotificationsService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void notifyChallengeCreator(Challenge challenge, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId) {
        notificationService.notifyUser(challenge.getCreator(), notificationMsg, shortNotificationMsg, notificationType, relevantObjectId);
    }

    private void notifyAllParticipators(List<User> participators, final Challenge challenge, String notificationMsg, String shortNotificationMsg, Notification.NotificationType notificationType, String relevantObjectId) {
        notificationService.notifyUsers(participators, notificationMsg, shortNotificationMsg, notificationType, relevantObjectId);
    }

    public void notifyAboutNewChallengeParticipation(Challenge challenge, String participatorId, String participatorName, List<User> participators) {
        logger.debug("Notifying about new challenge participation in challenge {} by user {}", challenge.getChallengeName(), participatorName);
        
        String challengeCreatorMsg = "New participation was added to your challenge " + challenge.getChallengeName() + "." +
                " Participator username is " + participatorName;
        String shortChallengeCreatorMsg = "User joined a challenge";

        String challengeParticipatorsMsg = "New participation was added to the challenge " + challenge.getChallengeName() + ". " +
                "Participator username is " + participatorName + "." +
                " Challenge creator is " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User is also participating.";

        notifyChallengeCreator(challenge, challengeCreatorMsg, shortChallengeCreatorMsg, Notification.NotificationType.new_participant, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.new_participant, String.valueOf(challenge.getId()));
    }

    public void notifyAboutChallengeLeaving(Challenge challenge, String participatorId, String participatorName, List<User> participators) {
        logger.debug("Notifying about challenge leaving of {}", participatorName);
        
        String challengeCreatorMsg = "Participator " + participatorName + " has left your challenge " + challenge.getChallengeName();
        String shortChallengeCreatorMsg = "User left a challenge";

        String challengeParticipatorsMsg = "Participator " + participatorName + " has left challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();
        String shortChallengeParticipatorsMsg = "User is no longer participating.";

        notifyChallengeCreator(challenge, challengeCreatorMsg, shortChallengeCreatorMsg, Notification.NotificationType.participant_left, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.participant_left, String.valueOf(challenge.getId()));
    }

    public void notifyAboutSubmittingChallengeResponse(ChallengeParticipation challengeParticipation, List<User> participators) {
        logger.debug("Notifying about submitting challenge response for challenge participation {}", challengeParticipation.getId());
        
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeCreatorMsg = "User " + challengeParticipation.getParticipator().getFormattedName() + " has just submitted response to your challenge " + challenge.getChallengeName();
        String shortChallengeCreatorMsg = "New response available";

        String challengeParticipatorsMsg = "User " + challengeParticipation.getParticipator().getFormattedName() + " has just submitted response to the challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getFormattedName();
        String shortChallengeParticipatorsMsg = "User added a response";

        participators.remove(challengeParticipation.getParticipator());
        notifyChallengeCreator(challenge, challengeCreatorMsg, shortChallengeCreatorMsg, Notification.NotificationType.new_response, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.new_response, String.valueOf(challenge.getId()));
    }

    public void notifyAboutChallengeResponseAcceptance(ChallengeParticipation challengeParticipation, List<User> participators) {
        logger.debug("Notifying about challenge response acceptance for challenge participation {}", challengeParticipation.getId());
        
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getFormattedName();
        String shortChallengeParticipatorMsg = "Response accepted!";

        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getFormattedName();
        String shortChallengeParticipatorsMsg = "User response accepted...";

        participators.remove(challengeParticipation.getParticipator());
        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg, shortChallengeParticipatorMsg, Notification.NotificationType.response_accepted, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, participatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.response_accepted, String.valueOf(challenge.getId()));
    }

    public void notifyAboutChallengeResponseRefusal(ChallengeParticipation challengeParticipation, List<User> participators) {
        logger.debug("Notifying about challenge response refusal for challenge participation {}", challengeParticipation.getId());
        
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getFormattedName();
        String shortChallengeParticipatorMsg = "Response refused.";

        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getFormattedName();
        String shortChallengeParticipatorsMsg = "User response refused...";

        participators.remove(challengeParticipation.getParticipator());
        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg, shortChallengeParticipatorMsg, Notification.NotificationType.response_refused, String.valueOf(challenge.getId()));
        notifyAllParticipators(participators, challenge, participatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.response_refused, String.valueOf(challenge.getId()));
    }

    public void notifyAboutNewPrivateChallenge(Challenge challenge, List<User> participators) {
        logger.debug("Notifying about creation of private challenge {}" + challenge.getChallengeName());

        String participatorsMsg = "You have been invited to challenge: " + challenge.getChallengeName() +
                " by " + challenge.getCreator().getFormattedName();
        String shortChallengeParticipatorsMsg = "New challenge invitation...";

        notifyAllParticipators(participators, challenge, participatorsMsg, shortChallengeParticipatorsMsg, Notification.NotificationType.challenge_invitation, String.valueOf(challenge.getId()));
    }
}
