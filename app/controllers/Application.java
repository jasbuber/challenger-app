package controllers;

import com.restfb.*;
import com.restfb.batch.BatchResponse;
import com.restfb.json.JsonObject;
import com.restfb.types.FacebookType;
import com.restfb.types.Video;
import domain.*;
import com.google.gson.Gson;
import play.Routes;
import play.data.Form;
import play.api.mvc.Request;
import play.db.jpa.Transactional;
import play.mvc.*;

import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;
import services.*;
import views.html.*;

import java.io.*;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import java.text.SimpleDateFormat;
import java.util.*;

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
            Application.getUsersService().createNewOrGetExistingUser(user.getUsername(), Application.getFacebookService().getProfilePictureUrl());

            session("username", user.getUsername());
            session("profilePictureUrl", Application.getFacebookService().getProfilePictureUrl());

            return redirect(routes.Application.index());
        }
    }

    public static Result index() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        return ok(index.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challengeForm, (long) getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser())));
    }

    public static Result ajaxCreateChallenge() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class).bindFromRequest();

        if (challengeForm.hasErrors()) {
            return ok(new Gson().toJson(challengeForm.errors()));
        } else {
            CreateChallengeForm challenge = challengeForm.get();
            String videoId = "";
            if (request().body().asMultipartFormData().getFile("video-description") != null) {
                try {
                    Http.MultipartFormData.FilePart resourceFile = request().body().asMultipartFormData().getFile("video-description");
                    InputStream stream = new FileInputStream(resourceFile.getFile());
                    videoId = Application.getFacebookService().publishAVideo(challenge.getChallengeName(), stream, resourceFile.getFilename());

                } catch (FileNotFoundException e) {
                }
            }

            Challenge newChallenge = getChallengeService().createChallenge(getLoggedInUsername(), challenge.getChallengeName(), challenge.getChallengeCategory(), videoId, challenge.getChallengeVisibility());

            if (!challenge.getChallengeVisibility() && challenge.getParticipants() != null) {
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

    private static InternalNotificationService getNotificationService() {
        return new InternalNotificationService(new InternalNotificationsRepository());
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetChallengesForCriteria(String phrase, String category) {

        ChallengeService service = Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");
        Expression<String> challengeNameField = filter.getField("challengeName");

        filter.andCond(filter.getBuilder().like(challengeNameField, "%" + phrase + "%"));
        filter.andCond(filter.excludeUser(currentUser));
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
    public static Result ajaxGetChallengesForCategory(String category) {

        ChallengeService service = Application.getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(20);
        User currentUser = Application.getLoggedInUser();

        filter.orderDescBy("creationDate");

        filter.andCond(filter.excludeUser(currentUser));
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

        filter.andCond(filter.excludeUser(currentUser));
        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());
        filter.prepareWhere();

        List<Challenge> challenges = service.findChallenges(filter);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional
    public static Result ajaxChangeChallengeParticipation(String id, Boolean state) {

        ChallengeService service = Application.getChallengeService();
        Challenge challenge = service.getChallenge(Long.parseLong(id));

        if (state == true) {
            service.participateInChallenge(challenge, Application.getLoggedInUsername());
        } else {
            service.leaveChallenge(challenge, Application.getLoggedInUsername());
        }

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxJoinChallenge(Long challengeId) {

        ChallengeService service = Application.getChallengeService();
        Challenge challenge = service.getChallenge(challengeId);

        service.participateInChallenge(challenge, Application.getLoggedInUsername());

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetFacebookFriends() {

        FacebookService service = Application.getFacebookService();

        List<FacebookUser> facebookFriends = service.getFacebookFriends();

        return ok(new Gson().toJson(facebookFriends));
    }

    /**
     * Will be removed after filling data is not necessary anymore.
     */
    @play.db.jpa.Transactional
    public static Result generateData() {

        UserService userService = new UserService(new UsersRepository());
        ChallengeService service = new ChallengeService(new ChallengesRepository(), new UsersRepository(), createNotificationService());
        InternalNotificationService notificationService = new InternalNotificationService(new InternalNotificationsRepository());

        User testUser = userService.createNewOrGetExistingUser(getLoggedInUsername());
        User otherUser = userService.createNewOrGetExistingUser("otherUser");
        User otherUser2 = userService.createNewOrGetExistingUser("otherUser2");
        User otherUser3 = userService.createNewOrGetExistingUser("otherUser3");
        User otherUser4 = userService.createNewOrGetExistingUser("otherUser4");
        User otherUser5 = userService.createNewOrGetExistingUser("otherUser5");

        Challenge challenge = service.createChallenge(testUser.getUsername(), "test challenge", ChallengeCategory.FOOD, "543763142406586", true);
        service.createChallenge(testUser.getUsername(), "testce", ChallengeCategory.FOOD, "543758585740375", true);
        service.createChallenge(testUser.getUsername(), "testchjhjgfallenge", ChallengeCategory.OTHER, "543763142406586", true);

        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser"), "fsfdsdss", "543763142406586");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser2"), "fsfdsdss", "544923992290501");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser3"), "fsfdsdss", "544923992290501");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser4"), "fsfdsdss", "544923992290501");
        service.submitChallengeResponse(service.participateInChallenge(challenge, "otherUser5"), "fsfdsdss", "544923992290501");

        service.createChallenge(otherUser.getUsername(), "test challenge2", ChallengeCategory.FOOD, "543763142406586", true);
        service.createChallenge(otherUser2.getUsername(), "test challenge3", ChallengeCategory.FOOD, "543763142406586", false);
        service.createChallenge(otherUser.getUsername(), "test challenge4", ChallengeCategory.FOOD, "543763142406586", false);
        service.createChallenge(otherUser2.getUsername(), "test challenge5", ChallengeCategory.FOOD, "543763142406586", true);

        return redirect(routes.Application.index());
    }

    private static ChallengeNotificationsService createNotificationService() {
        return new ChallengeNotificationsService(new InternalNotificationService(new InternalNotificationsRepository()));
    }

    public static Result showCurrentProfile() {

        User currentUser = getLoggedInUser();

        ChallengeService service = Application.getChallengeService();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(currentUser.getUsername());
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(currentUser.getUsername());
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(currentUser.getUsername());

        String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
        String currentName = Application.getFacebookService().getFacebookUser().getName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);


        return ok(profile.render(currentFirstName, currentPicture, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, currentName, currentPicture, createdChallengesNr, joinedChallengesNr, completedChallengesNr));

    }

    @play.db.jpa.Transactional
    public static Result showMyChallenges() {

        ChallengeService service = Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> participations = service.getChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        return ok(my_challenges.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challenges, participations, responseForm, getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser())));
    }

    @play.db.jpa.Transactional
    public static Result ajaxCloseChallenge(String id) {

        ChallengeService service = Application.getChallengeService();

        service.closeChallenge(Long.parseLong(id));

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxGetResponsesForChallenge(String challengeId) {

        ChallengeService service = Application.getChallengeService();

        List<ChallengeResponse> responses = service.getResponsesForChallenge(Long.parseLong(challengeId));

        if (responses.isEmpty()) {
            return ok(new Gson().toJson(responses));
        } else {
            return ok(new Gson().toJson(getFacebookService().getThumbnailsForResponses(responses)));
        }

    }

    @play.db.jpa.Transactional
    public static Result ajaxDeclineResponse(String responseId) {

        ChallengeService service = Application.getChallengeService();

        ChallengeResponse response = service.getChallengeResponse(Long.parseLong(responseId));
        service.refuseChallengeResponse(response);

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxAcceptResponse(String responseId) {

        ChallengeService service = Application.getChallengeService();

        ChallengeResponse response = service.getChallengeResponse(Long.parseLong(responseId));
        service.acceptChallengeResponse(response);

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
                        routes.javascript.Application.ajaxChangeChallengeParticipation(),
                        routes.javascript.Application.ajaxCloseChallenge(),
                        routes.javascript.Application.ajaxGetChallengesForCriteria(),
                        routes.javascript.Application.ajaxGetChallengesForCategory(),
                        routes.javascript.Application.ajaxGetLatestChallenges(),
                        routes.javascript.Application.ajaxGetResponsesForChallenge(),
                        routes.javascript.Application.ajaxGetFacebookFriends(),
                        routes.javascript.Application.ajaxGetCompletedChallenges(),
                        routes.javascript.Application.ajaxGetResponse(),
                        routes.javascript.Application.showProfile(),
                        routes.javascript.Application.ajaxGetChallengesContent(),
                        routes.javascript.Application.ajaxGetCurrentProfileContent(),
                        routes.javascript.Application.ajaxGetParticipationsContent(),
                        routes.javascript.Application.ajaxJoinChallenge(),
                        routes.javascript.Application.ajaxRemoveParticipantFromChallenge()
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

        service.leaveChallenge(challenge, getLoggedInUsername());

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result ajaxRemoveParticipantFromChallenge(String challengeId, String username) {

        ChallengeService service = Application.getChallengeService();

        Challenge challenge = service.getChallenge(Long.parseLong(challengeId));

        if(challenge.getCreator().getUsername().compareTo(getLoggedInUsername()) == 0) {

            service.leaveChallenge(challenge, username);
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

            String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
            String currentUsername = Application.getLoggedInUsername();
            String currentPicture = Application.getProfilePictureUrl();
            Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

            return ok(participants.render(currentFirstName, currentUsername, currentPicture, challengeParticipants, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, challenge));
        } else {
            return redirect(routes.Application.index());
        }
    }

    public static Result showProfile(String username) {

        User viewedUser = getUsersService().createNewOrGetExistingUser(username);

        User currentUser = getLoggedInUser();

        ChallengeService service = Application.getChallengeService();

        List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());
        List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());
        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);

        String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(username);
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(username);
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(username);

        String viewedUserName = viewedUser.getUsername();
        String viewedUserPicture = viewedUser.getProfilePictureUrl();

        return ok(profile.render(currentFirstName, currentPicture, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, viewedUserName, viewedUserPicture, createdChallengesNr, joinedChallengesNr, completedChallengesNr));

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

        if(notification.getUser().getUsername().compareTo(getLoggedInUsername()) == 0) {
            notification.read();
            service.readNotification(notification);

            return redirect(routes.Application.showChallenge(challengeId));

        }
        return redirect(routes.Application.showMyChallenges());
    }

    public static Result followNotificationToChallengeResponses(long notificationId, long challengeId) {
        InternalNotificationService service = getNotificationService();
        Notification notification = service.getNotification(notificationId);

        if(notification.getUser().getUsername().compareTo(getLoggedInUsername()) == 0) {
            notification.read();
            service.readNotification(notification);

            return redirect(routes.Application.showChallengeResponses(challengeId));

        }

        return redirect(routes.Application.showMyChallenges());
    }

    public static Result followNotificationToUserProfile(long notificationId, String username) {
        InternalNotificationService service = getNotificationService();
        Notification notification = service.getNotification(notificationId);

        if(notification.getUser().getUsername().compareTo(getLoggedInUsername()) == 0) {
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

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> myParticipations = service.getChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        return ok(participations.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), myParticipations, challenges, responseForm, getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser())));
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

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());
        List<Object[]> participations = service.getChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        Long challengeResponsesNr = service.getResponsesNrForChallenge(id);
        Boolean isCurrentUserRespondedToChallenge = service.isUserRespondedToChallenge(currentChallenge, currentUser.getUsername());

        Video video = facebookService.getVideo(currentChallenge.getVideoDescriptionUrl());

        List<ChallengeParticipation> participants = service.getParticipantsForChallenge(id);

        return ok(challenge_details.render(Application.getFacebookService().getFacebookUser().getFirstName(), Application.getProfilePictureUrl(), challenges, participations, responseForm, getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()),
                getNotificationService().getNewestNotifications(getLoggedInUser()), currentChallenge, video, participants, challengeResponsesNr, isCurrentUserRespondedToChallenge, currentUser.getUsername()));
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


        return ok(challenge_responses.render(Application.getFacebookService().getFacebookUser().getFirstName(), getLoggedInUsername(), Application.getProfilePictureUrl(), challenges, participations, responseForm, getNotificationService().getNumberOfUnreadNotifications(getLoggedInUser()), getNotificationService().getNewestNotifications(getLoggedInUser()), responses, currentChallenge, video));
    }

}
