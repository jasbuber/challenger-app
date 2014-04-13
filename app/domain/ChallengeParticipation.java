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

    public Challenge getChallenge() {
        return challenge;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChallengeParticipation that = (ChallengeParticipation) o;

        if (!challenge.equals(that.challenge)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = challenge.hashCode();
        result = 31 * result + user.hashCode();
        return result;
    }
}
