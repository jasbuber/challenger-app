package domain;

import com.google.gson.annotations.Expose;
import com.restfb.Facebook;
import com.restfb.types.Photo;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by jasbuber on 2014-04-10.
 */
public class FacebookUser {

    @Facebook("id")
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

    @Facebook("url")
    private String url;

    private String gender;

    public FacebookUser(String id, String picture, String firstName, String lastName){
        this.id = id;
        this.picture = picture;
        this.firstName = firstName;
        this.lastName = lastName;
    }

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

    public String getFormattedName(){
        if(StringUtils.isBlank(this.firstName) || StringUtils.isBlank(this.lastName)) {
            return this.id;
        }else {
            return this.firstName + " " + lastName.substring(0, 3);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
