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

    @Facebook("last_name")
    private String lastName;

    @Facebook("username")
    private String username;

    @Facebook("picture")
    private String picture;

    @Facebook("name")
    private String name;

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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
