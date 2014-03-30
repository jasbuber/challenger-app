package repositories;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;

public class ChallengesRepository {

    public Challenge createChallenge(User user) {
        return new Challenge(user);
    }

    public ChallengeParticipation createChallengeParticipation(Challenge challenge, User user) {
        return new ChallengeParticipation(challenge, user);
    }
}
