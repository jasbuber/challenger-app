package repositories.dtos;

import java.util.Date;

public class ChallengeWithParticipantsNr {

    private final String challengeName;
    private final Date challengeCreationDate;
    private final Long challengeId;
    private final Long participantsNr;


    public ChallengeWithParticipantsNr(String challengeName, Long participantsNr, Long challengeId) {
        this(challengeName, null, participantsNr, challengeId);
    }

    public ChallengeWithParticipantsNr(String challengeName, Date challengeCreationDate, Long participantsNr, Long challengeId) {
        this.challengeName = challengeName;
        this.challengeCreationDate = challengeCreationDate;
        this.challengeId = challengeId;
        this.participantsNr = participantsNr;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public Date getChallengeCreationDate() {
        return challengeCreationDate;
    }

    public Long getParticipantsNr() {
        return participantsNr;
    }

    public Long getChallengeId() {
        return challengeId;
    }
}
