package controllers;

import com.google.gson.Gson;
import domain.Challenge;
import domain.ChallengeCategory;
import domain.User;
import play.data.Form;
import play.mvc.*;

import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.ChallengeService;
import services.FacebookNotificationService;
import views.html.*;

import java.util.List;

public class Application extends Controller {

    public static Result index() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        return ok(index.render(challengeForm));
    }

    public static Result createChallenge(){

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class).bindFromRequest();

        if(challengeForm.hasErrors()) {
            return badRequest(index.render(challengeForm));
        } else {
            CreateChallengeForm challenge = challengeForm.get();
            getChallengeService().createChallenge(getLoggedInUsername(), challenge.getChallengeName());
            return redirect(routes.Application.index());
        }

    }

    //need to exist until dependency injection framework is added
    private static ChallengeService getChallengeService() {
        return new ChallengeService(new ChallengesRepository(), new UsersRepository(), new FacebookNotificationService());
    }

    //username to be set in session during login
    private static String getLoggedInUsername() {
        return session("username");
    }


    public static Result ajaxGetChallengesForCriteria(String phrase, String category){

        ChallengeService service =  new ChallengeService(new ChallengesRepository(), new UsersRepository(), new FacebookNotificationService());
        List<Challenge> challenges = service.findChallenges(new ChallengeFilter());

        //test data
        challenges.add(new Challenge(new User("dfdfdf"), "jdfjdfd"));
        challenges.add(new Challenge(new User("dfdgfgffdf"), "jdfgfgfgfgfjdfd"));
        challenges.add(new Challenge(new User("dfdgfgffgfdf"), "jdfjddfd"));
        challenges.add(new Challenge(new User("fdf"), "jdfjd"));

        return ok(new Gson().toJson(challenges));
    }

}
