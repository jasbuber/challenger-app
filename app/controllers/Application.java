package controllers;

import play.data.Form;
import play.mvc.*;

import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.ChallengeService;
import services.FacebookNotificationService;
import views.html.*;

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

}
