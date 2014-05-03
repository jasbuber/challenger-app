package domain;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //@SequenceGenerator(name = "user_seq_gen", sequenceName = "USER_SEQ")
    private Long id;

    /**
     * Username is case insensitive
     */

    @Column(name = "USERNAME", unique = true)
    @NotNull
    private String username;

    @Column(name = "PROFILE_PICTURE_URL")
    private String profilePictureUrl;

    protected User() {
        //for jpa purposes...
    }

    public User(String username) {
        assertUsername(username);
        this.username = username.toLowerCase();
    }

    public User(String username, String profilePictureUrl) {
        assertUsername(username);
        this.username = username.toLowerCase();
        this.profilePictureUrl = profilePictureUrl;
    }

    private void assertUsername(String username) {
        if(StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Username cannot be null");
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
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
}
