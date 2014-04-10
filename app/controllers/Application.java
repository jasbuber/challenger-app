package controllers;

import com.restfb.*;
import com.restfb.json.JsonObject;
import domain.FacebookUser;
import domain.User;
import play.*;
import play.api.mvc.Request;
import play.mvc.*;

import views.html.*;

import java.io.IOException;
import java.util.Date;

public class Application extends Controller {

    public static Result start(String code, String error) {

        if(!error.equals("")) { return ok(error_view.render("You rejected the permissions!"));}
        else if(code.equals("")) {
            return redirect("https://www.facebook.com/dialog/oauth?client_id=471463259622297&redirect_uri=" + routes.Application.start("", "").absoluteURL(request()));
        }
        else {
            FacebookClient.AccessToken token = null;
            try {
                token = getFacebookUserToken(code, controllers.routes.Application.start("", "").absoluteURL(request()));
            } catch (IOException e) {
            }
            String accessToken = token.getAccessToken();
            Date expires = token.getExpires();
            //String secret = Play.application().configuration().getString("secretkey");

            String secret = "a8d1db17c5add29872d79dd35bf793dc";
            session("fb_user_token", accessToken);
            FacebookClient facebookClient = new DefaultFacebookClient(accessToken, secret);
            FacebookUser user = facebookClient.fetchObject("me", FacebookUser.class);

            return redirect(routes.Application.index());
        }
    }

    public static Result index() {

        String secret = "a8d1db17c5add29872d79dd35bf793dc";
        FacebookClient facebookClient = new DefaultFacebookClient(session("fb_user_token"), secret);
        FacebookUser user = facebookClient.fetchObject("me", FacebookUser.class, Parameter.with("fields", "id, first_name"));

        JsonObject photo = facebookClient.fetchObject("me/picture", JsonObject.class, Parameter.with("redirect","0"), Parameter.with("width","32"), Parameter.with("height","32"));

        String photoUrl = photo.getJsonObject("data").getString("url");

        return ok(index.render(user.getFirstName(), photoUrl));
    }

    private static FacebookClient.AccessToken getFacebookUserToken(String code, String redirectUrl) throws IOException {
        String appId = "471463259622297";
        String secretKey = "a8d1db17c5add29872d79dd35bf793dc";

        WebRequestor wr = new DefaultWebRequestor();
        WebRequestor.Response accessTokenResponse = wr.executeGet(
                "https://graph.facebook.com/oauth/access_token?client_id=" + appId + "&redirect_uri=" + redirectUrl
                        + "&client_secret=" + secretKey + "&code=" + code);

        return DefaultFacebookClient.AccessToken.fromQueryString(accessTokenResponse.getBody());
    }
}
