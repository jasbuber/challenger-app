package controllers;

import domain.ChallengeCategory;
import domain.User;
import play.data.validation.Constraints;

import java.util.Collection;
import java.util.List;

public class CreateChallengeForm {

    @Constraints.Required
    @Constraints.MinLength(5)
    private String challengeName;

    private String creatorUsername;

    private String videoDescriptionUrl;

    private Boolean challengeVisibility;

    private List<String> participants;

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

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Boolean getChallengeVisibility() {
        return challengeVisibility;
    }

    public void setChallengeVisibility(Boolean challengeVisibility) {
        this.challengeVisibility = challengeVisibility;
    }
}
