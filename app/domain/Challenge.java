package domain;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "CHALLENGES")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Challenge is case insensitive
     */


    @Column(name = "NAME", unique = true)
    @NotNull
    private String challengeName;

    @Column(name = "CREATION_DATE" )
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "CREATOR")
    @NotNull
    private User creator;

    @Column(name = "CATEGORY")
    @Enumerated(EnumType.STRING)
    public ChallengeCategory category;

    @Column(name = "VIDEO_DESCRIPTION_URL")
    private String videoDescriptionUrl;



    protected Challenge(){}

    public Challenge(User creator, String challengeName, ChallengeCategory category) {
        assertCreatorAndName(creator, challengeName);
        this.creator = creator;
        this.challengeName = challengeName.toLowerCase();
        this.category = category;
        this.creationDate = new Date();
    }

    public Challenge(User creator, String challengeName, ChallengeCategory category, String videoDescriptionUrl) {
        assertCreatorAndName(creator, challengeName);
        this.creator = creator;
        this.challengeName = challengeName.toLowerCase();
        this.category = category;
        this.creationDate = new Date();
        this.videoDescriptionUrl = videoDescriptionUrl;
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

        if (!challengeName.equalsIgnoreCase(challenge.challengeName)) return false;
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

    public String getChallengeName() {
        return challengeName;
    }

    public User getCreator() {
        return creator;
    }

    public Long getId() { return id;}

    public Date getCreationDate() {
        return creationDate;
    }

    public String getVideoDescriptionUrl() {
        return videoDescriptionUrl;
    }
}
