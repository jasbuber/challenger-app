package controllers;

/**
 * Created by jasbuber on 2014-06-02.
 */
public class CreateChallengeResponseForm {

    private long challengeParticipationId;

    private String challengeId;

    private Character isAccepted;

    private String message;

    private String videoId;

    public long getChallengeParticipationId() {
        return challengeParticipationId;
    }

    public void setChallengeParticipationId(long challengeParticipationId) {
        this.challengeParticipationId = challengeParticipationId;
    }

    public Character getIsAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(Character isAccepted) {
        this.isAccepted = isAccepted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
