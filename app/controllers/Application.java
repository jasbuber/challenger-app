package controllers;

import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultWebRequestor;
import com.restfb.FacebookClient;
import com.restfb.WebRequestor;
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

            session("fb_user_token", accessToken);
            //FacebookClient facebookClient = new DefaultFacebookClient(accessToken, secret);
            //User user = facebookClient.fetchObject("me", User.class);

            return redirect(routes.Application.index());
        }
    }

    public static Result index() {
       return ok(index.render("Default user"));
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
