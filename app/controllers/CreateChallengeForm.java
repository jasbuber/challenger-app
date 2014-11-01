package controllers;

import domain.ChallengeCategory;
import play.data.validation.Constraints;

import java.util.List;

public class CreateChallengeForm {

    @Constraints.Required(message = "You forgot to type the challenge name, you know...")
    @Constraints.MinLength(value = 5, message = "Challenge name should be at least 5 characters long. Just because :P")
    private String challengeName;

    private String creatorUsername;

    private String videoDescriptionUrl;

    private Boolean challengeVisibility;

    private List<String> participants;

    private ChallengeCategory challengeCategory;

    private Boolean isInviteFriends;

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

    public Boolean getIsInviteFriends() {
        return isInviteFriends;
    }

    public void setIsInviteFriends(Boolean isInviteFriends) {
        this.isInviteFriends = isInviteFriends;
    }
}
