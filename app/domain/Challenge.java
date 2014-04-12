package domain;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;
import services.UserService;

public class Challenge {

    @Constraints.Required
    @Constraints.MinLength(5)
    private String challengeName;

    @Constraints.Required
    private User creator;

    public ChallengeCategory category;

    public Challenge(){ this.creator = UserService.getCurrentUser(); }

    public Challenge(User creator, String challengeName) {
        assertCreatorAndName(creator, challengeName);
        this.creator = creator;
        this.challengeName = challengeName;
    }

    private void assertCreatorAndName(User creator, String challengeName) {
        if(creator == null || StringUtils.isBlank(challengeName)) {
            throw new IllegalArgumentException("Creator and challengeName must be given and be not empty");
        }
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "challengeName='" + challengeName + '\'' +
                ", creator=" + creator +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Challenge challenge = (Challenge) o;

        if (!challengeName.equals(challenge.challengeName)) return false;
        if (!creator.equals(challenge.creator)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = challengeName.hashCode();
        result = 31 * result + creator.hashCode();
        return result;
    }

    public ChallengeCategory getCategory() {
        return category;
    }

    public void setCategory(ChallengeCategory category) {
        this.category = category;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

}
