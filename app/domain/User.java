package domain;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "USERS")
public class User {

    public static final int MINOR_REWARD = 5;
    public static final int NORMAL_REWARD = 10;
    public static final int MAJOR_REWARD = 15;
    public static final int SPECIAL_REWARD = 25;

    @Id
    @SequenceGenerator(name = "USERS_SEQ_GEN", sequenceName = "USERS_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_SEQ_GEN")
    private Long id;

    /**
     * Username is case insensitive
     */

    @Column(name = "USERNAME", unique = true)
    @NotNull
    private String username;

    @Column(name = "PROFILE_PICTURE_URL")
    private String profilePictureUrl;

    @Column(name = "JOINED" )
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date joined;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "CREATION_POINTS")
    private Integer creationPoints = 0;

    @Column(name = "PARTICIPATION_POINTS")
    private Integer participationPoints = 0;

    @Column(name = "OTHER_POINTS")
    private Integer otherPoints = 0;

    @Column(name = "TUTORIAL_COMPLETED")
    private Integer tutorialCompleted = 0;

    protected User() {
        //for jpa purposes...
        this.joined = new Date();
    }

    public User(String username) {
        assertUsername(username);
        this.username = username.toLowerCase();
        this.joined = new Date();
    }

    public User(String username, String profilePictureUrl, String firstName, String lastName) {
        assertUsername(username);
        this.username = username.toLowerCase();
        this.profilePictureUrl = profilePictureUrl;
        this.joined = new Date();
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
    }

    public User(FacebookUser user, String profilePictureUrl) {
        assertUsername(user.getId());
        this.username = user.getId().toLowerCase();
        this.profilePictureUrl = profilePictureUrl;
        this.joined = new Date();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.fullName = user.getName();
    }

    private void assertUsername(String username) {
        if(StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Username cannot be null");
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + getFormattedName() + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!username.equalsIgnoreCase(user.username)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getJoined() {
        return new SimpleDateFormat("dd-MM-yyyy").format(this.joined);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFormattedName(){
        if(StringUtils.isBlank(this.firstName) || StringUtils.isBlank(this.lastName)) {
            return this.username;
        }else {
            return this.firstName + " " + lastName.substring(0, 3);
        }
    }

    public Integer getCreationPoints() {
        return creationPoints;
    }

    public Integer getParticipationPoints() {
        return participationPoints;
    }

    public Integer getOtherPoints() {
        return otherPoints;
    }

    public Integer addCreationPoints(int points){
        if(points <= 0 || points > SPECIAL_REWARD){
            return this.creationPoints;
        }
        this.creationPoints += points;
        return this.creationPoints;
    }

    public Integer addParticipationPoints(int points){
        if(points <= 0 || points > SPECIAL_REWARD){
            return this.participationPoints;
        }
        this.participationPoints += points;
        return this.participationPoints;
    }

    public Integer addOtherPoints(int points){
        if(points <= 0 || points > SPECIAL_REWARD){
            return this.otherPoints;
        }
        this.otherPoints += points;
        return this.otherPoints;
    }

    public Integer getAllPoints(){
        return this.creationPoints + this.participationPoints + this.otherPoints;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Integer getTutorialCompleted() {
        return tutorialCompleted;
    }

    public void completeTutorial(){
        this.tutorialCompleted = 1;
    }
}
