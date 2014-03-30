package domain;

public class ChallengeParticipation {

    private final Challenge challenge;
    private final User user;

    public ChallengeParticipation(Challenge challenge, User user) {
        this.challenge = challenge;
        this.user = user;
    }
}
