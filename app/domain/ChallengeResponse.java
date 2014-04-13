package domain;

public class ChallengeResponse {

    private final ChallengeParticipation challengeParticipation;


    public ChallengeResponse(ChallengeParticipation challengeParticipation) {
        this.challengeParticipation = challengeParticipation;
    }

    public ChallengeParticipation getChallengeParticipation() {
        return challengeParticipation;
    }
}
