package controllers;

import com.google.gson.Gson;
import domain.Challenge;
import domain.ChallengeCategory;
import domain.User;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.*;

import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.ChallengeService;
import services.FacebookNotificationService;
import services.UserService;
import views.html.*;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

public class Application extends Controller {

    public static Result index() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        return ok(index.render(session("username"), challengeForm));
    }

    public static Result createChallenge(){

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class).bindFromRequest();

        if(challengeForm.hasErrors()) {
            return badRequest(index.render(session("username"), challengeForm));
        } else {
            CreateChallengeForm challenge = challengeForm.get();
            getChallengeService().createChallenge(getLoggedInUsername(), challenge.getChallengeName(), challenge.getChallengeCategory());
            return redirect(routes.Application.index());
        }

    }

    //need to exist until dependency injection framework is added
    private static ChallengeService getChallengeService() {
        return new ChallengeService(new ChallengesRepository(), new UsersRepository(), new FacebookNotificationService());
    }

    //need to exist until dependency injection framework is added
    private static UserService getUsersService() {
        return new UserService(new UsersRepository());
    }

    //username to be set in session during login
    private static String getLoggedInUsername() {
        return session("username");
    }

    private static User getLoggedInUser() {
        return Application.getUsersService().createNewOrGetExistingUser(Application.getLoggedInUsername());
    }


    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesForCriteria(String phrase, String category){

        ChallengeService service =  Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");
        Expression<String> path = filter.getRoot().get("challengeName");
        Predicate phraseCond = filter.getBuilder().like(path, "%" + phrase + "%");

        Predicate phraseCondWithoutUser = filter.getBuilder().and(filter.excludeUser(currentUser), phraseCond);
        Predicate phraseCondWithoutUserFull = filter.getBuilder().and(phraseCondWithoutUser, filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.getCriteriaQuery().where(phraseCondWithoutUserFull);

        if(!(ChallengeCategory.valueOf(category).equals(ChallengeCategory.ALL))){
            Expression<String> categoryPath = filter.getRoot().get("category");
            Predicate categoryCond = filter.getBuilder().equal(categoryPath, ChallengeCategory.valueOf(category));
            Predicate bothCond = filter.getBuilder().and(phraseCondWithoutUserFull, categoryCond);
            filter.getCriteriaQuery().where(bothCond);
        }

        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetLatestChallenges(){

        ChallengeService service =  Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(10);
        User currentUser = Application.getLoggedInUser();

        Predicate bothCond = filter.getBuilder().and(filter.excludeUser(currentUser), filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.getCriteriaQuery().where(bothCond);

        filter.orderDescBy("creationDate");
        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxChangeChallengeParticipation(String id, Boolean state){

        ChallengeService service =  Application.getChallengeService();
        Challenge challenge = service.getChallenge(Long.parseLong(id));

        if(state == true) {
            service.participateInChallenge(challenge, Application.getLoggedInUsername());
        }else{
            service.leaveChallenge(challenge, Application.getLoggedInUsername());
        }

        return ok("success");
    }

    /**
     * Will be removed after filling data is not necessary anymore.
     */
    @play.db.jpa.Transactional
    public static Result generateData(){

        UserService userService =  new UserService(new UsersRepository());
        ChallengeService service =  new ChallengeService(new ChallengesRepository(), new UsersRepository(), new FacebookNotificationService());

        User testUser   = userService.createNewOrGetExistingUser("testuser");
        User otherUser  = userService.createNewOrGetExistingUser("otherUser");
        User otherUser2 = userService.createNewOrGetExistingUser("otherUser2");

        session("username", testUser.getUsername());

        service.createChallenge(testUser.getUsername(), "testchgfgfgfallenge", ChallengeCategory.FOOD);
        service.createChallenge(otherUser.getUsername(), "test challenge2", ChallengeCategory.FOOD);
        service.createChallenge(otherUser2.getUsername(), "test challenge3", ChallengeCategory.FOOD);
        service.createChallenge(otherUser.getUsername(), "test challenge4", ChallengeCategory.FOOD);
        service.createChallenge(otherUser2.getUsername(), "test challenge5", ChallengeCategory.FOOD);

        return redirect(routes.Application.index());
    }

}
