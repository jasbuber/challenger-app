package domain;

/*
    It seems that is not needed. First candidate to be removed -> it is still domain term however it seems that is not
    needed in code.
 */

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "CHALLENGE_PARTICIPATIONS")
public class ChallengeParticipation {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CHALLENGE")
    @NotNull
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "PARTICIPATOR")
    @NotNull
    private User user;

    protected ChallengeParticipation() {
        //for jpa purposes...
    }


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

    public User getCreator() {
        return challenge.getCreator();
    }
}
