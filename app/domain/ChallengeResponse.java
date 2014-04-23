package domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "CHALLENGE_RESPONSES")
public class ChallengeResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CHALLENGE_PARTICIPATION")
    @NotNull
    private ChallengeParticipation challengeParticipation;

    @Column(name = "ACCEPTANCE")
    private Character isAccepted;


    protected ChallengeResponse() {
        //for jpa purposes...
    }

    public ChallengeResponse(ChallengeParticipation challengeParticipation) {
        this.challengeParticipation = challengeParticipation;
    }

    public ChallengeParticipation getChallengeParticipation() {
        return challengeParticipation;
    }

    public boolean isAccepted() {
        return isAccepted != null && isAccepted == 'Y';
    }

    public void accept() {
        this.isAccepted = 'Y';
    }

    public void refuse() {
        this.isAccepted = 'N';
    }

    public boolean isDecided() {
        return this.isAccepted != null;
    }

    public Long getId() {
        return id;
    }
}
