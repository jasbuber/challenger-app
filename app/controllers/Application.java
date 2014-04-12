package controllers;

import domain.Challenge;
import domain.User;
import play.*;
import play.data.Form;
import play.mvc.*;

import services.UserService;
import views.html.*;

public class Application extends Controller {

    public static Result index() {

        Form<Challenge> challengeForm = Form.form(Challenge.class);

        return ok(index.render(challengeForm));
    }

    public static Result createChallenge(){

        Form<Challenge> challengeForm = Form.form(Challenge.class).bindFromRequest();

        if(challengeForm.hasErrors()) {
            return badRequest(index.render(challengeForm));
        } else {
            Challenge challenge = challengeForm.get();
            challenge.setCreator(UserService.getCurrentUser());
            return redirect(routes.Application.index());
        }

    }

}
