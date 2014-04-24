package services;

import com.restfb.*;
import com.restfb.json.JsonObject;
import domain.FacebookUser;

import java.io.IOException;

/**
 * Eventually to be merged with FacebookNotificationService
 */
public class FacebookService {

    private final static String secret = "a8d1db17c5add29872d79dd35bf793dc";

    private final static String applicationId = "471463259622297";

    private static String token;

    private final DefaultFacebookClient client;

    public FacebookService(){

        this.client = new DefaultFacebookClient(token, this.secret);

    }

    public DefaultFacebookClient getClient() {
        return client;
    }

    public static String generateAccessToken(String code, String redirectUrl){
        try {
            WebRequestor wr = new DefaultWebRequestor();
            WebRequestor.Response accessTokenResponse = wr.executeGet(
                "https://graph.facebook.com/oauth/access_token?client_id=" + FacebookService.applicationId + "&redirect_uri=" + redirectUrl
                        + "&client_secret=" + FacebookService.secret + "&code=" + code);

            FacebookService.token =  DefaultFacebookClient.AccessToken.fromQueryString(accessTokenResponse.getBody()).getAccessToken();
            return FacebookService.token;
        } catch (IOException e) {
        }

        return null;
    }

    public FacebookUser getFacebookUser() {
        return this.client.fetchObject("me", FacebookUser.class, Parameter.with("fields", "id, first_name, username"));
    }

    public String getProfilePictureUrl(){
        JsonObject photo = this.client.fetchObject("me/picture", JsonObject.class, Parameter.with("redirect","0"), Parameter.with("width","32"), Parameter.with("height","32"));
        return photo.getJsonObject("data").getString("url");
    }

    public static String getToken() {
        return token;
    }
}
