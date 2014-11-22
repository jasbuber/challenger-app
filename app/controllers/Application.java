package controllers;

import com.google.gson.Gson;
import com.restfb.types.Video;
import domain.*;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;
import services.*;
import views.html.*;

import javax.persistence.criteria.Expression;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application extends Controller {

    public static Result start(String code, String error) {

        if (!error.equals("")) {
            return ok(error_view.render("You rejected the permissions!"));
        } else if (code.equals("")) {
            return redirect("https://www.facebook.com/dialog/oauth?client_id=471463259622297&redirect_uri=" + routes.Application.start("", "").absoluteURL(request()) + "&scope=publish_stream,user_videos");
        } else {
            String accessToken = FacebookService.generateAccessToken(code, controllers.routes.Application.start("", "").absoluteURL(request()));

            session("fb_user_token", accessToken);
            FacebookUser user = Application.getFacebookService().getFacebookUser();
            Application.getUsersService().createNewOrGetExistingUser(user, Application.getFacebookService().getProfilePictureUrl());

            session("username", user.getId());
            session("name", user.getFormattedName());
            session("profilePictureUrl", Application.getFacebookService().getProfilePictureUrl());

            return redirect(routes.Application.index());
        }
    }

    public static Result index() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        return ok(index.render(Application.getFacebookService().getFacebookUser().getFirstName(), getLoggedInUsername(), Application.getProfilePictureUrl(), challengeForm, (long) getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser()), new ArrayList<Challenge>()));
    }

    @play.db.jpa.Transactional
    public static Result showBrowseChallenges() {

        List<Challenge> challenges = prepareChallengesForCriteria("", ChallengeCategory.ALL.name());

        return ok(browse.render(Application.getFacebookService().getFacebookUser().getFirstName(), getLoggedInUsername(), Application.getProfilePictureUrl(), challenges, (long) getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser())));
    }

    @play.db.jpa.Transactional
    public static Result showBrowseChallengesWithData(String phrase) {

        List<Challenge> challenges = prepareChallengesForCriteria(phrase, ChallengeCategory.ALL.name());

        return ok(browse.render(Application.getFacebookService().getFacebookUser().getFirstName(), getLoggedInUsername(), Application.getProfilePictureUrl(), challenges, (long) getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser())));
    }

    @play.db.jpa.Transactional
    public static Result showCreateChallenge() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        return ok(new_challenge.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), (long) getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser()), challengeForm));
    }

    public static Result ajaxCreateChallenge() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class).bindFromRequest();

        if (challengeForm.hasErrors()) {
            return handleChallengeFormErrors(challengeForm);
        }

        CreateChallengeForm challenge = challengeForm.get();
        String videoId = "";

        validateChallengeFormData(challengeForm);

        if (challengeForm.hasErrors()) {
            return ok(new Gson().toJson(challengeForm.errors()));
        }


        Http.MultipartFormData.FilePart resourceFile = request().body().asMultipartFormData().getFile("video-description");
        InputStream stream = null;
        try {
            stream = new FileInputStream(resourceFile.getFile());
            if (!challenge.getChallengeVisibility()) {
                videoId = Application.getFacebookService().publishAPrivateVideo(challenge.getChallengeName(), stream, resourceFile.getFilename());
            } else {
                videoId = Application.getFacebookService().publishAVideo(challenge.getChallengeName(), stream, resourceFile.getFilename());
            }
        } catch (IOException e) {
            return handleErrorMsgDuringFileUploading(challengeForm, resourceFile, e);
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    return handleErrorMsgDuringFileUploading(challengeForm, resourceFile, e);
                }
            }
        }

        Challenge newChallenge = getChallengeService().createChallenge(getLoggedInUsername(), challenge.getChallengeName(), challenge.getChallengeCategory(), videoId, challenge.getChallengeVisibility());


        if (!challenge.getChallengeVisibility()) {
            List<User> participants = new ArrayList<User>();

            if (challenge.getParticipants() != null && challenge.getParticipants().size() > 0) {
                addParicipantToChallenge(challenge, newChallenge, participants);
            } else {
                challengeForm.reject("participants", "You didn't select any of your friends. Challenge someone or make the challenge public.");
                return ok(new Gson().toJson(challengeForm.errors()));
            }

            getChallengeNotificationService().notifyAboutNewPrivateChallenge(newChallenge, participants);
        }

        return ok("success");

    }

    private static Result handleErrorMsgDuringFileUploading(Form<CreateChallengeForm> challengeForm, Http.MultipartFormData.FilePart resourceFile, IOException e) {
        Logger.error("Error has occurred during uploading file: " + resourceFile.getFilename(), e);
        challengeForm.reject("participants", "An internal error occurred during file uploading");
        return ok(new Gson().toJson(challengeForm.errors()));
    }

    private static void addParicipantToChallenge(CreateChallengeForm challenge, Challenge newChallenge, List<User> participants) {
        for (String p : challenge.getParticipants()) {

            List<String> items = Arrays.asList(p.split("\\s*,\\s*"));

            String id = items.get(0);

            //TODO batch?
            User user = getUsersService().createNewOrGetExistingUser(id, items.get(1), items.get(2), items.get(3));
            participants.add(user);
            getChallengeService().participateInChallenge(newChallenge, id, user.getFormattedName());
        }
    }

    private static void validateChallengeFormData(Form<CreateChallengeForm> challengeForm) {
        if (request().body().asMultipartFormData().getFile("video-description") == null) {
            challengeForm.reject("videoDescriptionUrl", "Upload a video description...");
        }
        if (getChallengeService().isUserCreatedChallengeWithName(challengeForm.get().getChallengeName(), getLoggedInUsername())) {
            challengeForm.reject("challengeName", "You already created a challenge with that name. Pick another name, please.");
        }
    }

    private static Result handleChallengeFormErrors(Form<CreateChallengeForm> challengeForm) {
        if (request().body().asMultipartFormData().getFile("video-description") == null) {
            challengeForm.reject("videoDescriptionUrl", "Upload a video description...");
        }
        return ok(new Gson().toJson(challengeForm.errors()));
    }

    //need to exist until dependency injection framework is added
    private static ChallengeService getChallengeService() {
        return new ChallengeService(new ChallengesRepository(), new UsersRepository(), createNotificationService());
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
        return Application.getUsersService().getExistingUser(Application.getLoggedInUsername());
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

    private static String getLoggedInName() {
        return session("name");
    }

    private static InternalNotificationService getNotificationService() {
        return new InternalNotificationService(new InternalNotificationsRepository());
    }

    private static ChallengeNotificationsService getChallengeNotificationService() {
        return new ChallengeNotificationsService(getNotificationService());
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesForCriteria(String phrase, String category) {

        List<Challenge> challenges = prepareChallengesForCriteria(phrase, category);

        return ok(new Gson().toJson(challenges));
    }

    private static List<Challenge> prepareChallengesForCriteria(String phrase, String category) {
        ChallengeService service = Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");
        Expression<String> challengeNameField = filter.getField("challengeName");

        filter.andCond(filter.getBuilder().like(challengeNameField, "%" + phrase + "%"));
        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());

        if (!(ChallengeCategory.valueOf(category).equals(ChallengeCategory.ALL))) {
            Expression<String> categoryField = filter.getField("category");
            filter.andCond(filter.getBuilder().equal(categoryField, ChallengeCategory.valueOf(category)));
        }

        filter.prepareWhere();
        return service.findChallenges(filter);

    }

    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesForCategory(String category) {

        ChallengeService service = Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");

        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());

        if (!(ChallengeCategory.valueOf(category).equals(ChallengeCategory.ALL))) {
            Expression<String> categoryField = filter.getField("category");
            filter.andCond(filter.getBuilder().equal(categoryField, ChallengeCategory.valueOf(category)));
        }

        filter.prepareWhere();
        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetLatestChallenges() {

        ChallengeService service = Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(10);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");

        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());
        filter.prepareWhere();

        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    /*
    @play.db.jpa.Transactional
    public static Result ajaxChangeChallengeParticipation(String id, Boolean state) {

        ChallengeService service = Application.getChallengeService();
        Challenge challenge = service.getChallenge(Long.parseLong(id));

        if (state == true) {
            service.participateInChallenge(challenge, getLoggedInUsername(), getLoggedInName());
        } else {
            service.leaveChallenge(challenge, getLoggedInUsername(), getLoggedInName());
        }

        return ok("success");
    }*/

    @play.db.jpa.Transactional
    public static Result ajaxJoinChallenge(Long challengeId) {

        ChallengeService service = Application.getChallengeService();
        Challenge challenge = service.getChallenge(challengeId);

        service.participateInChallenge(challenge, getLoggedInUsername(), getLoggedInName());

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetFacebookUsers(String ids) {

        FacebookService service = Application.getFacebookService();

        List<String> userIds = Arrays.asList(ids.split("\\s*,\\s*"));

        return ok(new Gson().toJson(service.getFacebookUsers(userIds)));

    }

    /**
     * Will be removed after filling data is not necessary anymore.
     */
    @play.db.jpa.Transactional
    public static Result generateData() {

        UserService userService = new UserService(new UsersRepository());
        ChallengeService service = new ChallengeService(new ChallengesRepository(), new UsersRepository(), createNotificationService());
        InternalNotificationService notificationService = new InternalNotificationService(new InternalNotificationsRepository());

        User testUser = creteTestUser(userService, getLoggedInUsername());
        User otherUser = creteTestUser(userService, "12122112");
        User otherUser2 = creteTestUser(userService, "12122113");
        User otherUser3 = creteTestUser(userService, "12122114");
        User otherUser4 = creteTestUser(userService, "12122115");
        User otherUser5 = creteTestUser(userService, "12122116");

        Challenge challenge = service.createChallenge(testUser.getUsername(), "test challenge", ChallengeCategory.FOOD, "543763142406586", true);
        service.createChallenge(testUser.getUsername(), "testce", ChallengeCategory.FOOD, "543758585740375", true);
        service.createChallenge(testUser.getUsername(), "testchjhjgfallenge", ChallengeCategory.OTHER, "543763142406586", true);

        service.submitChallengeResponse(service.participateInChallenge(challenge, "12122112", "otherUser"), "fsfdsdss", "543763142406586");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "12122113", "otherUser2"), "fsdss", "544923992290501");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "12122114", "otherUser3"), "fsfdshhjhjjhjdss", "544923992290501");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "12122115", "otherUser4"), "fsfdss", "544923992290501");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "12122116", "otherUser5"), "fsfddfgfdgfddgfdgfgfgfsdss", "544923992290501");

        service.createChallenge(otherUser.getUsername(), "test challenge2", ChallengeCategory.FOOD, "543763142406586", true);
        service.createChallenge(otherUser2.getUsername(), "test challenge3", ChallengeCategory.FOOD, "543763142406586", false);
        service.createChallenge(otherUser.getUsername(), "test challenge4", ChallengeCategory.FOOD, "543763142406586", false);
        service.createChallenge(otherUser2.getUsername(), "test challenge5", ChallengeCategory.FOOD, "543763142406586", true);

        return redirect(routes.Application.index());
    }

    private static User creteTestUser(UserService userService, String id) {
        return userService.createNewOrGetExistingUser(id, null, null, null);
    }

    private static ChallengeNotificationsService createNotificationService() {
        return new ChallengeNotificationsService(new InternalNotificationService(new InternalNotificationsRepository()));
    }

    public static Result showCurrentProfile() {

        User currentUser = getLoggedInUser();

        ChallengeService service = Application.getChallengeService();
        FacebookService fbService = Application.getFacebookService();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);

        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(currentUser.getUsername());
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(currentUser.getUsername());
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(currentUser.getUsername());

        FacebookUser fbUser = fbService.getFacebookUser();

        String currentFirstName = fbUser.getFirstName();
        String currentName = fbUser.getFormattedName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);


        return ok(profile.render(currentFirstName, currentPicture, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, currentName, currentPicture, createdChallengesNr, joinedChallengesNr, completedChallengesNr));

    }

    @play.db.jpa.Transactional
    public static Result showMyChallenges() {

        ChallengeService service = Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> participations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(my_challenges.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challenges, participations, responseForm, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications));
    }

    @play.db.jpa.Transactional
    public static Result ajaxCloseChallenge(long id) {

        ChallengeService service = Application.getChallengeService();

        if (service.isUserCreatedAChallenge(id, getLoggedInUsername())) {
            service.closeChallenge(id);
        } else {
            return ok("false");
        }

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetResponsesForChallenge(long challengeId) {

        ChallengeService service = Application.getChallengeService();

        List<ChallengeResponse> responses = service.getResponsesForChallenge(challengeId);

        if (responses.isEmpty()) {
            return ok(new Gson().toJson(responses));
        } else {
            return ok(new Gson().toJson(getFacebookService().getThumbnailsForResponses(responses)));
        }

    }

    @play.db.jpa.Transactional
    public static Result ajaxDeclineResponse(long responseId) {

        ChallengeService service = Application.getChallengeService();

        ChallengeResponse response = service.getChallengeResponse(responseId);

        if (service.isUserCreatedAChallenge(response.getChallengeParticipation().getChallenge().getId(), getLoggedInUsername())) {
            service.refuseChallengeResponse(response);
        } else {
            return ok("false");
        }

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxAcceptResponse(long responseId) {

        ChallengeService service = Application.getChallengeService();

        ChallengeResponse response = service.getChallengeResponse(responseId);

        if (service.isUserCreatedAChallenge(response.getChallengeParticipation().getChallenge().getId(), getLoggedInUsername())) {
            service.acceptChallengeResponse(response);
        } else {
            return ok("false");
        }

        return ok("success");

    }

    public static Result javascriptRoutes() {
        response().setContentType("text/javascript");
        return ok(
                Routes.javascriptRouter("jsRoutes",
                        routes.javascript.Application.ajaxDeclineResponse(),
                        routes.javascript.Application.ajaxAcceptResponse(),
                        routes.javascript.Application.ajaxGetUserParticipations(),
                        routes.javascript.Application.ajaxLeaveChallenge(),
                        routes.javascript.Application.ajaxCloseChallenge(),
                        routes.javascript.Application.ajaxGetChallengesForCriteria(),
                        routes.javascript.Application.ajaxGetChallengesForCategory(),
                        routes.javascript.Application.ajaxGetLatestChallenges(),
                        routes.javascript.Application.ajaxGetResponsesForChallenge(),
                        routes.javascript.Application.ajaxGetFacebookUsers(),
                        routes.javascript.Application.ajaxGetCompletedChallenges(),
                        routes.javascript.Application.ajaxGetResponse(),
                        routes.javascript.Application.showProfile(),
                        routes.javascript.Application.ajaxGetChallengesContent(),
                        routes.javascript.Application.ajaxGetCurrentProfileContent(),
                        routes.javascript.Application.ajaxGetParticipationsContent(),
                        routes.javascript.Application.ajaxJoinChallenge(),
                        routes.javascript.Application.ajaxRemoveParticipantFromChallenge(),
                        routes.javascript.Application.showBrowseChallengesWithData(),
                        routes.javascript.Application.showBrowseChallenges(),
                        routes.javascript.Application.showMyParticipations(),
                        routes.javascript.Application.showChallenge()
                )
        );
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetUserParticipations() {

        ChallengeService service = Application.getChallengeService();

        List<ChallengeResponse> participations = service.getChallengeParticipationsForUser(getLoggedInUsername());

        return ok(new Gson().toJson(participations));
    }

    @play.db.jpa.Transactional
    public static Result ajaxLeaveChallenge(String challengeId) {

        ChallengeService service = Application.getChallengeService();

        Challenge challenge = service.getChallenge(Long.parseLong(challengeId));

        service.leaveChallenge(challenge, getLoggedInUsername(), getLoggedInName());

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxRemoveParticipantFromChallenge(String challengeId, String username, String name) {

        ChallengeService service = Application.getChallengeService();

        Challenge challenge = service.getChallenge(Long.parseLong(challengeId));

        if (challenge.getCreator().getUsername().compareTo(getLoggedInUsername()) == 0) {

            service.leaveChallenge(challenge, username, name);
        }

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxSubmitChallengeResponse() {

        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class).bindFromRequest();

        ChallengeService service = getChallengeService();

        if (responseForm.hasErrors()) {
            return ok(new Gson().toJson(responseForm.errors()));
        } else {
            CreateChallengeResponseForm response = responseForm.get();

            String videoId = "";
            if (request().body().asMultipartFormData().getFile("video-description") != null) {
                try {
                    Http.MultipartFormData.FilePart resourceFile = request().body().asMultipartFormData().getFile("video-description");
                    InputStream stream = new FileInputStream(resourceFile.getFile());
                    videoId = Application.getFacebookService().publishAVideo(resourceFile.getFilename(), stream, resourceFile.getFilename());

                } catch (FileNotFoundException e) {
                }
            } else {
                responseForm.reject("You need to choose a video response...");
                return ok(new Gson().toJson(responseForm.errors()));
            }

            Challenge challenge = service.getChallenge(Long.parseLong(response.getChallengeId()));

            ChallengeParticipation participation = service.getChallengeParticipation(challenge, getLoggedInUsername());
            ChallengeResponse newResponse = service.submitChallengeResponse(participation, response.getMessage(), videoId);

            return ok("success");
        }
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetCompletedChallenges() {

        ChallengeService service = Application.getChallengeService();

        List<Challenge> completedChallenges = service.getCompletedChallenges(getLoggedInUsername());

        return ok(new Gson().toJson(completedChallenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetResponse(Long responseId) {

        ChallengeService service = getChallengeService();
        FacebookService fbService = Application.getFacebookService();

        ChallengeResponse response = service.getChallengeResponse(responseId);

        Video video = fbService.getVideo(response.getVideoResponseUrl());

        response.setThumbnailUrl(video.getPicture());
        response.setVideoResponseUrl(video.getSource());
        return ok(new Gson().toJson(response));
    }

    public static Result showParticipators(Long challengeId) {

        ChallengeService service = getChallengeService();
        Challenge challenge = service.getChallenge(challengeId);
        User currentUser = getLoggedInUser();

        if (getChallengeService().isUserCreatedChallengeWithName(challenge.getChallengeName(), getLoggedInUsername())) {

            List<ChallengeParticipation> challengeParticipants = getChallengeService().getParticipantsForChallenge(challengeId);

            List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());
            List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());
            List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
            List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);
            Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

            String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
            String currentUsername = Application.getLoggedInUsername();
            String currentPicture = Application.getProfilePictureUrl();


            return ok(participants.render(currentFirstName, currentUsername, currentPicture, challengeParticipants, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, challenge));
        } else {
            return redirect(routes.Application.index());
        }
    }

    public static Result showProfile(String username) {

        User viewedUser = getUsersService().getExistingUser(username);

        User currentUser = getLoggedInUser();

        ChallengeService service = Application.getChallengeService();

        List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());
        List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());
        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);

        String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(username);
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(username);
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(username);

        String viewedUserName = viewedUser.getFormattedName();
        String viewedUserPicture = viewedUser.getProfilePictureUrl();

        return ok(profile.render(currentFirstName, currentPicture, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, viewedUserName, viewedUserPicture, createdChallengesNr, joinedChallengesNr, completedChallengesNr));

    }

    public static Result showMyNotifications() {

        InternalNotificationService notificationService = getNotificationService();
        ChallengeService service = getChallengeService();
        User currentUser = getLoggedInUser();

        List<Notification> myNotifications = notificationService.getAllNotifications(getLoggedInUser());

        List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());
        List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());
        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);

        String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(notifications.render(currentFirstName, currentPicture, myNotifications, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications));

    }

    public static Result followNotification(long notificationId) {
        InternalNotificationService service = getNotificationService();

        Notification notification = service.getNotification(notificationId);
        notification.read();
        service.readNotification(notification);

        return redirect(routes.Application.showMyChallenges());
    }

    public static Result followNotificationToChallengeDetails(long notificationId, long challengeId) {
        InternalNotificationService service = getNotificationService();
        Notification notification = service.getNotification(notificationId);

        if (notification.getUser().getUsername().compareTo(getLoggedInUsername()) == 0) {
            notification.read();
            service.readNotification(notification);

            return redirect(routes.Application.showChallenge(challengeId));

        }
        return redirect(routes.Application.showMyChallenges());
    }

    public static Result followNotificationToChallengeResponses(long notificationId, long challengeId) {
        InternalNotificationService service = getNotificationService();
        Notification notification = service.getNotification(notificationId);

        if (notification.getUser().getUsername().compareTo(getLoggedInUsername()) == 0) {
            notification.read();
            service.readNotification(notification);

            return redirect(routes.Application.showChallengeResponses(challengeId));

        }

        return redirect(routes.Application.showMyChallenges());
    }

    public static Result followNotificationToUserProfile(long notificationId, String username) {
        InternalNotificationService service = getNotificationService();
        Notification notification = service.getNotification(notificationId);

        if (notification.getUser().getUsername().compareTo(getLoggedInUsername()) == 0) {
            notification.read();
            service.readNotification(notification);

            return redirect(routes.Application.showProfile(username));

        }

        return redirect(routes.Application.showMyChallenges());
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesContent() {

        ChallengeService service = Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        return ok(challenges_content.render(Application.getProfilePictureUrl(), challenges));
    }

    public static Result ajaxGetCurrentProfileContent() {

        ChallengeService service = Application.getChallengeService();

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(getLoggedInUsername());
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(getLoggedInUsername());
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(getLoggedInUsername());

        return ok(profile_content.render(Application.getFacebookService().getFacebookUser().getName(), Application.getProfilePictureUrl(), createdChallengesNr, joinedChallengesNr, completedChallengesNr));

    }

    @play.db.jpa.Transactional
    public static Result showMyParticipations() {

        ChallengeService service = Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> challenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> myParticipations = service.getChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(participations.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), myParticipations, challenges, responseForm, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications));
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetParticipationsContent() {

        ChallengeService service = Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> participations = service.getChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        return ok(participations_content.render(Application.getProfilePictureUrl(), participations));
    }

    @play.db.jpa.Transactional
    public static Result showChallenge(long id) {

        ChallengeService service = Application.getChallengeService();
        FacebookService facebookService = Application.getFacebookService();

        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        User currentUser = Application.getLoggedInUser();
        Challenge currentChallenge = service.getChallenge(id);

        List<Object[]> challenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());
        List<Object[]> participations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        Long challengeResponsesNr = service.getResponsesNrForChallenge(id);
        Boolean isCurrentUserRespondedToChallenge = service.isUserRespondedToChallenge(currentChallenge, currentUser.getUsername());

        Video video = facebookService.getVideo(currentChallenge.getVideoDescriptionUrl());

        List<ChallengeParticipation> participants = service.getParticipantsForChallenge(id);

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(challenge_details.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challenges, participations, responseForm, currentUnreadNotificationsNr,
                latestNotifications, latestUnreadNotifications, currentChallenge, video, participants, challengeResponsesNr, isCurrentUserRespondedToChallenge, currentUser.getUsername()));
    }

    @play.db.jpa.Transactional
    public static Result showChallengeResponses(long id) {

        ChallengeService service = Application.getChallengeService();
        FacebookService fbService = Application.getFacebookService();

        User currentUser = Application.getLoggedInUser();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        Challenge currentChallenge = service.getChallenge(id);

        List<Object[]> participations = service.getChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());


        List<ChallengeResponse> responses = service.getResponsesForChallenge(id);

        Video video = null;

        if (!responses.isEmpty()) {
            responses = fbService.getThumbnailsForResponses(responses);

            video = fbService.getVideo(responses.get(0).getVideoResponseUrl());
        }

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(challenge_responses.render(Application.getFacebookService().getFacebookUser().getFirstName(), getLoggedInUsername(), Application.getProfilePictureUrl(), challenges, participations, responseForm, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, responses, currentChallenge, video));
    }

}
