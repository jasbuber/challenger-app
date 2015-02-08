package domain;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "CHALLENGES")
public class Challenge {

    public static enum DifficultyLevel { easy, medium, hard, insane }

    @Id
    @SequenceGenerator(name = "CHALLENGE_SEQ_GEN", sequenceName = "CHALLENGE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CHALLENGE_SEQ_GEN")
    private Long id;

    /**
     * Challenge is case insensitive
     */


    @Column(name = "NAME")
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

    @Column(name = "VIDEO_ID")
    private String videoId;

    @Column(name = "VISIBILITY")
    private Boolean visibility;

    @Column(name = "ACTIVE")
    private Boolean active;

    @Column(name = "ENDING_DATE" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date endingDate;

    @Column(name = "DIFFICULTY")
    private Integer difficulty = 0;

    @Column(name = "RATING")
    private Float rating = 0F;

    @Column(name = "PARTICIPATORS_RATED")
    private Integer participatorsRatedNr = 0;

    public static final int POPULARITY_LEVEL_1 = 3;
    public static final int POPULARITY_LEVEL_2 = 8;
    public static final int POPULARITY_LEVEL_3 = 15;
    public static final int POPULARITY_LEVEL_4 = 25;

    protected Challenge(){}

    public Challenge(User creator, String challengeName, ChallengeCategory category, Integer difficulty) {
        assertCreatorAndName(creator, challengeName);
        this.creator = creator;
        this.challengeName = challengeName.toLowerCase();
        this.category = category;
        this.creationDate = new Date();
        this.active = true;
        this.difficulty = difficulty;
    }

    public Challenge(User creator, String challengeName, ChallengeCategory category, String videoId, Boolean visibility, Integer difficulty) {
        assertCreatorAndName(creator, challengeName);
        this.creator = creator;
        this.challengeName = challengeName.toLowerCase();
        this.category = category;
        this.creationDate = new Date();
        this.videoId = videoId;
        this.visibility = visibility;
        this.active = true;
        this.difficulty = difficulty;
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

    public String getCreationDate() {
        return new SimpleDateFormat("dd-MM-yyyy").format(this.creationDate);
    }

    public String getVideoId() {
        return videoId;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public Boolean isActive() {
        return active;
    }

    public void setInactive(){
        this.active = false;
        this.endingDate = new Date();
    }

    public Date getEndingDate() {
        return endingDate;
    }

    public static Integer getPopularityLevel(Long participantsNr){

        if(participantsNr < POPULARITY_LEVEL_1){
            return 1;
        }else if(participantsNr < POPULARITY_LEVEL_2){
            return 2;
        }else if(participantsNr < POPULARITY_LEVEL_3){
            return 3;
        }else if(participantsNr < POPULARITY_LEVEL_4){
            return 4;
        }else{
            return 5;
        }

    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public String getFormattedDifficulty() {
        if(DifficultyLevel.values().length <= difficulty || difficulty < 0){
            return DifficultyLevel.easy.toString();
        }

        return DifficultyLevel.values()[difficulty].toString();
    }

    public float getRating() {
        return this.rating;
    }

    public void addRating(int rating){
        if(rating < 1 || rating > 5){
            return;
        }
        this.rating = ((this.rating * this.participatorsRatedNr) + (float) rating)/ ++this.participatorsRatedNr;
    }

    public Integer getParticipatorsRatedNr() {
        return participatorsRatedNr;
    }

}
