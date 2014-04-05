package domain;

/*
    It seems that is needed. First candidate to be removed -> it is still domain term however it seems that is not
    needed in code.
 */
public class ChallengeParticipation {

    private final Challenge challenge;
    private final User user;

    public ChallengeParticipation(Challenge challenge, User user) {
        this.challenge = challenge;
        this.user = user;
    }
}
