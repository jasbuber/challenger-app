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
    private User participator;

    protected ChallengeParticipation() {
        //for jpa purposes...
    }


    public ChallengeParticipation(Challenge challenge, User participator) {
        this.challenge = challenge;
        this.participator = participator;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public User getParticipator() {
        return participator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChallengeParticipation that = (ChallengeParticipation) o;

        if (!challenge.equals(that.challenge)) return false;
        if (!participator.equals(that.participator)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = challenge.hashCode();
        result = 31 * result + participator.hashCode();
        return result;
    }

    public User getCreator() {
        return challenge.getCreator();
    }

    public Long getId() {
        return id;
    }
}
