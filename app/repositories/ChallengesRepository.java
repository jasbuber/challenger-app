package repositories;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.User;
import play.db.DB;

import java.sql.Connection;

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

    public ChallengeResponse addChallengeResponse(ChallengeParticipation challengeParticipation) {
        return new ChallengeResponse(challengeParticipation);
    }

    public ChallengeParticipation getChallengeParticipation(Challenge challenge, String participatorUsername) {
        return new ChallengeParticipation(challenge, new User(participatorUsername));
    }

    public boolean isNotScoredChallengeResponseExistsFor(ChallengeParticipation challengeParticipation) {
        return false;
    }
}
