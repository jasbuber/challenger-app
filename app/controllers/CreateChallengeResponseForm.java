package controllers;

import play.data.validation.Constraints;

/**
 * Created by jasbuber on 2014-06-02.
 */
public class CreateChallengeResponseForm {

    private long challengeParticipationId;

    private String challengeId;

    private Character isAccepted;

    private String message;

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
}
