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

    @Column(name = "VIDEO_RESPONSE_URL")
    private String videoResponseUrl;


    protected ChallengeResponse() {
        //for jpa purposes...
    }

    public ChallengeResponse(ChallengeParticipation challengeParticipation) {
        this.challengeParticipation = challengeParticipation;
    }

    public ChallengeResponse(ChallengeParticipation challengeParticipation, String videoResponseUrl) {
        this.challengeParticipation = challengeParticipation;
        this.videoResponseUrl = videoResponseUrl;
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

    public Character getIsAccepted() {
        return isAccepted;
    }

    public String getVideoResponseUrl() {
        return videoResponseUrl;
    }
}
