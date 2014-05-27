package controllers;

import com.restfb.*;
import com.restfb.json.JsonObject;
import domain.*;
import com.google.gson.Gson;
import play.Routes;
import play.data.Form;
import play.api.mvc.Request;
import play.db.jpa.Transactional;
import play.mvc.*;

import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;
import services.ChallengeService;
import services.FacebookNotificationService;
import services.FacebookService;
import services.UserService;
import views.html.*;

import java.io.*;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Application extends Controller {

    public static Result start(String code, String error) {

        if(!error.equals("")) { return ok(error_view.render("You rejected the permissions!"));}
        else if(code.equals("")) {
            return redirect("https://www.facebook.com/dialog/oauth?client_id=471463259622297&redirect_uri=" + routes.Application.start("", "").absoluteURL(request()) + "&scope=publish_stream");
        }
        else {
            String accessToken = FacebookService.generateAccessToken(code, controllers.routes.Application.start("", "").absoluteURL(request()));

            session("fb_user_token", accessToken);
            FacebookUser user = Application.getFacebookService().getFacebookUser();
            Application.getUsersService().createNewOrGetExistingUser(user.getUsername(), Application.getFacebookService().getProfilePictureUrl());

            session("username", user.getUsername());
            session("profilePictureUrl", Application.getFacebookService().getProfilePictureUrl());

            return redirect(routes.Application.index());
        }
    }

    public static Result index() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        return ok(index.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challengeForm));
    }

    public static Result ajaxCreateChallenge(){

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class).bindFromRequest();

        if(challengeForm.hasErrors()) {
            return ok(new Gson().toJson(challengeForm.errors()));
        } else {
            CreateChallengeForm challenge = challengeForm.get();
            String videoId = "";
            if(request().body().asMultipartFormData().getFile("video-description") !=null) {
                try {
                    Http.MultipartFormData.FilePart resourceFile = request().body().asMultipartFormData().getFile("video-description");
                    InputStream stream = new FileInputStream(resourceFile.getFile());
                    videoId = Application.getFacebookService().publishAVideo(challenge.getChallengeName(), stream, resourceFile.getFilename());

                } catch (FileNotFoundException e) {}
            }

            Challenge newChallenge = getChallengeService().createChallenge(getLoggedInUsername(), challenge.getChallengeName(), challenge.getChallengeCategory(), videoId, challenge.getChallengeVisibility());

            if (!challenge.getChallengeVisibility() && challenge.getParticipants() != null){
                for (String p : challenge.getParticipants()) {
                    getUsersService().createNewOrGetExistingUser(p);
                    getChallengeService().participateInChallenge(newChallenge, p);
                }
            }

            return ok("success");
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

    private static FacebookService getFacebookService() {
        return new FacebookService(Application.getAccessToken());
    }

    private static String getProfilePictureUrl() {
        return session("profilePictureUrl");
    }

    private static String getAccessToken() {
        return session("fb_user_token");
    }


    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesForCriteria(String phrase, String category){

        ChallengeService service =  Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");
        Expression<String> challengeNameField = filter.getField("challengeName");

        filter.andCond(filter.getBuilder().like(challengeNameField, "%" + phrase + "%"));
        filter.andCond(filter.excludeUser(currentUser));
        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());

        if(!(ChallengeCategory.valueOf(category).equals(ChallengeCategory.ALL))){
            Expression<String> categoryField = filter.getField("category");
            filter.andCond(filter.getBuilder().equal(categoryField, ChallengeCategory.valueOf(category)));
        }

        filter.prepareWhere();
        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesForCategory(String category){

        ChallengeService service =  Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");

        filter.andCond(filter.excludeUser(currentUser));
        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());

        if(!(ChallengeCategory.valueOf(category).equals(ChallengeCategory.ALL))){
            Expression<String> categoryField = filter.getField("category");
            filter.andCond(filter.getBuilder().equal(categoryField, ChallengeCategory.valueOf(category)));
        }

        filter.prepareWhere();
        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetLatestChallenges(){

        ChallengeService service =  Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(10);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");

        filter.andCond(filter.excludeUser(currentUser));
        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());
        filter.prepareWhere();

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

    @play.db.jpa.Transactional
    public static Result ajaxGetFacebookFriends(){

        FacebookService service =  Application.getFacebookService();

        List<FacebookUser> facebookFriends = service.getFacebookFriends();

        return ok(new Gson().toJson(facebookFriends));
    }

    /**
     * Will be removed after filling data is not necessary anymore.
     */
    @play.db.jpa.Transactional
    public static Result generateData(){

        UserService userService =  new UserService(new UsersRepository());
        ChallengeService service =  new ChallengeService(new ChallengesRepository(), new UsersRepository(), new FacebookNotificationService());

        User testUser   = userService.createNewOrGetExistingUser(getLoggedInUsername());
        User otherUser  = userService.createNewOrGetExistingUser("otherUser");
        User otherUser2 = userService.createNewOrGetExistingUser("otherUser2");

        Challenge challenge = service.createChallenge(testUser.getUsername(), "test challenge", ChallengeCategory.FOOD, "", true);
        service.createChallenge(testUser.getUsername(), "testce", ChallengeCategory.FOOD, "", true);
        service.createChallenge(testUser.getUsername(), "testchjhjgfallenge", ChallengeCategory.OTHER, "", true);
        service.createChallenge(testUser.getUsername(), "testgh", ChallengeCategory.GETTING_INVOLVED, "", true);
        service.createChallenge(testUser.getUsername(), "testchgfgfg", ChallengeCategory.FREAK_MODE, "", true);
        service.createChallenge(testUser.getUsername(), "testchgfgfgfae", ChallengeCategory.USING_A_BRAIN, "", true);

        service.submitChallengeResponse(service.participateInChallenge(challenge, getLoggedInUsername()));
        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser"));
        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser2"));

        service.createChallenge(otherUser.getUsername(), "test challenge2", ChallengeCategory.FOOD, "", true);
        service.createChallenge(otherUser2.getUsername(), "test challenge3", ChallengeCategory.FOOD, "", false);
        service.createChallenge(otherUser.getUsername(), "test challenge4", ChallengeCategory.FOOD, "", false);
        service.createChallenge(otherUser2.getUsername(), "test challenge5", ChallengeCategory.FOOD, "", true);

        return redirect(routes.Application.index());
    }

    public static Result showProfile(){
        return ok(profile.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getFacebookService().getFacebookUser().getLastName(), Application.getProfilePictureUrl(),
                Application.getLoggedInUser().getJoined(), getChallengeService().countCreatedChallengesForUser(Application.getLoggedInUsername()), getChallengeService().countCompletedChallenges(Application.getLoggedInUsername())));
    }

    @play.db.jpa.Transactional
    public static Result showMyChallenges(){

        ChallengeService service =  Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        return ok(my_challenges.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxCloseChallenge(String id){

        ChallengeService service =  Application.getChallengeService();

        service.closeChallenge(Long.parseLong(id));

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetResponsesForChallenge(String challengeId){

        ChallengeService service =  Application.getChallengeService();

        List<ChallengeResponse> responses = service.getResponsesForChallenge(Long.parseLong(challengeId));

        return ok(new Gson().toJson(responses));
    }

    @play.db.jpa.Transactional
    public static Result ajaxDeclineResponse(String responseId){

        ChallengeService service =  Application.getChallengeService();

        ChallengeResponse response = service.getChallengeResponse(Long.parseLong(responseId));
        service.refuseChallengeResponse(response);

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxAcceptResponse(String responseId){

        ChallengeService service =  Application.getChallengeService();

        ChallengeResponse response = service.getChallengeResponse(Long.parseLong(responseId));
        service.acceptChallengeResponse(response);

        return ok("success");
    }

    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        routes.javascript.Application.ajaxDeclineResponse(),
                        routes.javascript.Application.ajaxAcceptResponse()
                )
        );
    }

}
