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

    @Column(name = "MESSAGE")
    private String message;

    @Transient
    private String thumbnailUrl;

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

    public ChallengeResponse(ChallengeParticipation challengeParticipation, String videoResponseUrl, String message) {
        this.challengeParticipation = challengeParticipation;
        this.videoResponseUrl = videoResponseUrl;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
