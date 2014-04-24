package domain;

import com.restfb.Facebook;

/**
 * Created by jasbuber on 2014-04-10.
 */
public class FacebookUser {

    private String id;

    //private String email;

    @Facebook("first_name")
    private String firstName;

    @Facebook("username")
    private String username;

    private String gender;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Facebook User{" +
                "id='" + this.id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FacebookUser user = (FacebookUser) o;

        if (!this.id.equals(user.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
