package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;

import java.util.List;

public class ChallengeNotificationsService {

    private final NotificationService notificationService;

    public ChallengeNotificationsService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyAboutChallengeCreation(Challenge challenge) {
        String notificationMsg = "Challenge " + challenge.getChallengeName() + " was successfully created";
        notifyChallengeCreator(challenge, notificationMsg);
    }

    private void notifyChallengeCreator(Challenge challenge, String notificationMsg) {
        notificationService.notifyUser(challenge.getCreator(), notificationMsg);
    }

    private void notifyAllParticipators(List<User> participators, final Challenge challenge, String notificationMsg) {
        notificationService.notifyUsers(participators, notificationMsg);
    }

    public void notifyAboutNewChallengeParticipation(Challenge challenge, String participatorUsername, List<User> participators) {
        String challengeCreatorMsg = "New participation was added to your challenge " + challenge.getChallengeName() + "." +
                " Participator username is " + participatorUsername;
        String challengeParticipatorsMsg = "New participation was added to the challenge " + challenge.getChallengeName() + ". " +
                "Participator username is " + participatorUsername + "." +
                " Challenge creator is " + challenge.getCreator().getUsername();

        notifyChallengeCreator(challenge, challengeCreatorMsg);
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg);
    }

    public void notifyAboutChallengeLeaving(Challenge challenge, String participatorUsername, List<User> participators) {
        String challengeCreatorMsg = "Participator " + participatorUsername + " has left your challenge " + challenge.getChallengeName();
        String challengeParticipatorsMsg = "Participator " + participatorUsername + " has left challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();

        notifyChallengeCreator(challenge, challengeCreatorMsg);
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg);
    }

    public void notifyAboutSubmittingChallengeResponse(ChallengeParticipation challengeParticipation, List<User> participators) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeCreatorMsg = "User " + challengeParticipation.getParticipator() + " has just submitted response to your challenge " + challenge.getChallengeName();
        String challengeParticipatorsMsg = "User " + challengeParticipation.getParticipator() + " has just submitted response to the challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();

        notifyChallengeCreator(challenge, challengeCreatorMsg);
        notifyAllParticipators(participators, challenge, challengeParticipatorsMsg);
    }

    public void notifyAboutChallengeResponseAcceptance(ChallengeParticipation challengeParticipation, List<User> participators) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getUsername();
        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getUsername();

        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg);
        notifyAllParticipators(participators, challenge, participatorsMsg);
    }

    public void notifyAboutChallengeResponseRefusal(ChallengeParticipation challengeParticipation, List<User> participators) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getUsername();
        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getUsername();

        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg);
        notifyAllParticipators(participators, challenge, participatorsMsg);
    }
}
