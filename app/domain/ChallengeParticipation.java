package domain;

/*
    It seems that is not needed. First candidate to be removed -> it is still domain term however it seems that is not
    needed in code.
 */

import org.apache.commons.lang3.time.DateUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "CHALLENGE_PARTICIPATIONS")
public class ChallengeParticipation {

    public static int CREATOR_STATE = 3;
    public static int NOT_PARTICIPATING_STATE = 0;
    public static int NOT_RESPONDED_STATE = 1;
    public static int RESPONDED = 2;
    public static int RATED = 5;

    @Id
    @SequenceGenerator(name = "CHALLENGE_PART_SEQ_GEN", sequenceName = "CHALLENGE_PART_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHALLENGE_PART_SEQ_GEN")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CHALLENGE")
    @NotNull
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "PARTICIPATOR")
    @NotNull
    private User participator;

    @Column(name = "JOINED" )
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date joined;

    @Column(name = "ENDING_DATE" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingDate;

    @Column(name = "IS_CHALLENGE_RATED")
    private Character isChallengeRated;

    @Column(name = "IS_RESPONSE_SUBMITTED")
    private Character isResponseSubmitted;

    protected ChallengeParticipation() {
        //for jpa purposes...
        this.endingDate = DateUtils.addHours(new Date(), 24);
    }

    public ChallengeParticipation(Challenge challenge, User participator) {
        this.challenge = challenge;
        this.participator = participator;
        this.joined = new Date();
        this.endingDate = DateUtils.addHours(new Date(), 24);
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

    public String getJoined() {
        return new SimpleDateFormat("dd-MM-yyyy").format(this.joined);
    }

    public Date getEndingDate() {
        return endingDate;
    }

    public Boolean isOverdue(){
        return endingDate.getTime() <= new Date().getTime();
    }

    public void rateChallenge() {
        this.isChallengeRated = 'Y';
    }

    public boolean isChallengeRated() {
        return this.isChallengeRated != null;
    }

    public boolean isResponseSubmitted() {
        return this.isResponseSubmitted != null;
    }

    public void submit() {
        this.isResponseSubmitted = 'Y';
    }
}
