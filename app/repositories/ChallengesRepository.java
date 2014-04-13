package repositories;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;

public class ChallengesRepository {

    public Challenge createChallenge(User user, String challengeName) {
        return new Challenge(user, challengeName);
    }

    public boolean isChallengeWithGivenNameExistsForUser(String challengeName, String creator) {
        return false;
    }

    public ChallengeParticipation createChallengeParticipation(Challenge challenge, User user) {
        return new ChallengeParticipation(challenge, user);
    }

    public boolean isUserParticipatingInChallenge(Challenge challenge, String participator) {
        return false;
    }
}
