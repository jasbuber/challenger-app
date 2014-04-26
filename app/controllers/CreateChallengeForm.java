package controllers;

import domain.ChallengeCategory;
import play.data.validation.Constraints;

public class CreateChallengeForm {

    @Constraints.Required
    @Constraints.MinLength(5)
    private String challengeName;

    private String creatorUsername;

    private String videoDescriptionUrl;

    private ChallengeCategory challengeCategory;

    public void setCreator(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public ChallengeCategory getChallengeCategory() {
        return challengeCategory;
    }

    public void setChallengeCategory(ChallengeCategory challengeCategory) {
        this.challengeCategory = challengeCategory;
    }

    public String getVideoDescriptionUrl() {
        return videoDescriptionUrl;
    }

    public void setVideoDescriptionUrl(String videoDescriptionUrl) {
        this.videoDescriptionUrl = videoDescriptionUrl;
    }
}
