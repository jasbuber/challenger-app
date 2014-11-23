package controllers;

import com.restfb.types.Video;
import domain.*;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import play.Routes;
import play.data.Form;
import play.mvc.*;

import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;
import services.*;
import views.html.*;

import java.io.*;
import javax.persistence.criteria.Expression;
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
            Application.getUsersService().createNewOrGetExistingUser(user, Application.getFacebookService().getProfilePictureUrl());

            session("username", user.getId());
            session("name", user.getFormattedName());
            session("profilePictureUrl", Application.getFacebookService().getProfilePictureUrl());

            return redirect(routes.Application.index());
        }
    }

    public static Result index() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);

        User currentUser = getLoggedInUser();
        String firstName = currentUser.getFirstName();
        Integer points = currentUser.getAllPoints();
        long unreadNotificationNr = (long) getNotificationService().getNumberOfUnreadNotifications(currentUser);
        List<Notification> newestNotifications = getNotificationService().getNewestNotifications(currentUser);

        return ok(index.render(firstName, getLoggedInUsername(), Application.getProfilePictureUrl(), points, challengeForm, unreadNotificationNr, newestNotifications, new ArrayList<Challenge>()));
    }

    @play.db.jpa.Transactional
    public static Result showBrowseChallenges() {

        List<Challenge> challenges = prepareChallengesForCriteria("", ChallengeCategory.ALL.name());

        User currentUser = getLoggedInUser();
        String firstName = currentUser.getFirstName();
        Integer points = currentUser.getAllPoints();
        long unreadNotificationNr = (long) getNotificationService().getNumberOfUnreadNotifications(currentUser);
        List<Notification> newestNotifications = getNotificationService().getNewestNotifications(currentUser);

        return ok(browse.render(firstName, getLoggedInUsername(), Application.getProfilePictureUrl(), points, challenges, unreadNotificationNr, newestNotifications));
    }

    @play.db.jpa.Transactional
    public static Result showBrowseChallengesWithData(String phrase) {

        List<Challenge> challenges = prepareChallengesForCriteria(phrase, ChallengeCategory.ALL.name());
        User currentUser = getLoggedInUser();
        String firstName = currentUser.getFirstName();
        Integer points = currentUser.getAllPoints();
        long unreadNotificationNr = (long) getNotificationService().getNumberOfUnreadNotifications(currentUser);
        List<Notification> newestNotifications = getNotificationService().getNewestNotifications(currentUser);

        return ok(browse.render(firstName, getLoggedInUsername(), Application.getProfilePictureUrl(), points, challenges, unreadNotificationNr, newestNotifications));
    }

    @play.db.jpa.Transactional
    public static Result showCreateChallenge() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class);
        User currentUser = getLoggedInUser();
        String firstName = currentUser.getFirstName();
        Integer points = currentUser.getAllPoints();
        long unreadNotificationNr = (long) getNotificationService().getNumberOfUnreadNotifications(currentUser);
        List<Notification> newestNotifications = getNotificationService().getNewestNotifications(currentUser);

        return ok(new_challenge.render(firstName, Application.getProfilePictureUrl(), points, unreadNotificationNr, newestNotifications, challengeForm));
    }

    public static Result ajaxCreateChallenge() {

        Form<CreateChallengeForm> challengeForm = Form.form(CreateChallengeForm.class).bindFromRequest();

        String username = getLoggedInUsername();

        if (challengeForm.hasErrors()) {
            if( request().body().asMultipartFormData().getFile("video-description") == null) {
                challengeForm.reject("videoDescriptionUrl", "Upload a video description...");
            }
            return ok(new Gson().toJson(challengeForm.errors()));
        } else {
            CreateChallengeForm challenge = challengeForm.get();
            String videoId = "";

            if (request().body().asMultipartFormData().getFile("video-description") == null) {
                challengeForm.reject("videoDescriptionUrl", "Upload a video description...");
            }
            if(getChallengeService().isUserCreatedChallengeWithName(challengeForm.get().getChallengeName(), username)){
                challengeForm.reject("challengeName", "You already created a challenge with that name. Pick another name, please.");
            }

            if (!challengeForm.hasErrors()) {
                try {
                    Http.MultipartFormData.FilePart resourceFile = request().body().asMultipartFormData().getFile("video-description");
                    InputStream stream = new FileInputStream(resourceFile.getFile());

                    if(!challenge.getChallengeVisibility()){
                        videoId = Application.getFacebookService().publishAPrivateVideo(challenge.getChallengeName(), stream, resourceFile.getFilename());
                    }else{
                        videoId = Application.getFacebookService().publishAVideo(challenge.getChallengeName(), stream, resourceFile.getFilename());
                    }

                } catch (FileNotFoundException e) {
                }

                Challenge newChallenge = getChallengeService().createChallenge(username, challenge.getChallengeName(), challenge.getChallengeCategory(), videoId, challenge.getChallengeVisibility());

                List<User> participants = new ArrayList<User>();

                if (!challenge.getChallengeVisibility()) {
                    if(challenge.getParticipants() != null && challenge.getParticipants().size() > 0) {
                        for (String p : challenge.getParticipants()) {

                            List<String> items = Arrays.asList(p.split("\\s*,\\s*"));

                            String id = items.get(0);

                            User user = getUsersService().createNewOrGetExistingUser(id, items.get(1), items.get(2), items.get(3));
                            participants.add(user);
                            getChallengeService().participateInChallenge(newChallenge, id, user.getFormattedName());
                        }


                    }else{
                        challengeForm.reject("participants", "You didn't select any of your friends. Challenge someone or make the challenge public.");
                        return ok(new Gson().toJson(challengeForm.errors()));
                    }
                    getChallengeNotificationService().notifyAboutNewPrivateChallenge(newChallenge, participants);
                }

                long challengeNr = getChallengeService().getCreatedChallengesNrForUser(username);
                UserService userService = getUsersService();

                CustomResponse response = new CustomResponse();

                if(!newChallenge.getVisibility()){
                    response.addPoints(User.MINOR_REWARD);
                    response.addMessage("first private challenge");
                }
                if( challengeNr == 1){
                    response.addPoints(User.MINOR_REWARD);
                    response.addMessage("first challenge");

                }else if(challengeNr % 5 == 0) {
                    int rewardedPoints = (int)(challengeNr / 5) * User.NORMAL_REWARD;
                    response.addPoints(rewardedPoints);
                    response.addMessage(challengeNr + " challenges created");
                }

                if(response.getRewardedPoints() > 0) {
                    userService.rewardCreationPoints(username, response.getRewardedPoints());
                }

                return ok(new Gson().toJson(response));
            }else{
                return ok(new Gson().toJson(challengeForm.errors()));
            }
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

    private static List<Challenge> prepareChallengesForCriteria(String phrase, String category){
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
    public static Result ajaxGetFacebookFriends() {

        FacebookService service = Application.getFacebookService();

        List<FacebookUser> facebookFriends = service.getFacebookFriends();

        return ok(new Gson().toJson(facebookFriends));
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

        User testUser = userService.createNewOrGetExistingUser(getLoggedInUsername());
        User otherUser = userService.createNewOrGetExistingUser("12122112");
        User otherUser2 = userService.createNewOrGetExistingUser("12122113");
        User otherUser3 = userService.createNewOrGetExistingUser("12122114");
        User otherUser4 = userService.createNewOrGetExistingUser("12122115");
        User otherUser5 = userService.createNewOrGetExistingUser("12122116");

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

        FacebookUser fbUser =  fbService.getFacebookUser();

        String currentFirstName = fbUser.getFirstName();
        String currentName = fbUser.getFormattedName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);


        return ok(profile.render(currentFirstName, currentPicture, currentUser.getAllPoints(), latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, currentName, currentPicture, createdChallengesNr, joinedChallengesNr, completedChallengesNr, currentUser.getAllPoints()));

    }

    @play.db.jpa.Transactional
    public static Result showMyChallenges() {

        ChallengeService service = Application.getChallengeService();
        User currentUser = Application.getLoggedInUser();
        String firstName = currentUser.getFirstName();
        Integer points = currentUser.getAllPoints();
        Form<CreateChallengeResponseForm> responseForm = Form.form(CreateChallengeResponseForm.class);

        List<Object[]> challenges = service.getChallengesWithParticipantsNrForUser(currentUser.getUsername());

        List<Object[]> participations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());

        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(my_challenges.render(firstName, Application.getProfilePictureUrl(), points, challenges, participations, responseForm, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications));
    }

    @play.db.jpa.Transactional
    public static Result ajaxCloseChallenge(long id) {

        ChallengeService service = Application.getChallengeService();

        if(service.isUserCreatedAChallenge(id, getLoggedInUsername())) {
            service.closeChallenge(id);
        }else{
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
        ChallengeResponse challengeResponse = service.getChallengeResponse(responseId);
        String username = getLoggedInUsername();
        CustomResponse response = new CustomResponse();

        if(service.isUserCreatedAChallenge(challengeResponse.getChallengeParticipation().getChallenge().getId(), username)) {
            service.refuseChallengeResponse(challengeResponse);
            getUsersService().rewardCreationPoints(username, User.MINOR_REWARD);
            response.addPoints(User.MINOR_REWARD);
            response.addMessage("response rated");
        }else{
            response.setStatus(CustomResponse.ResponseStatus.failure);
        }

        return ok(new Gson().toJson(response));
    }

    @play.db.jpa.Transactional
    public static Result ajaxAcceptResponse(long responseId) {

        ChallengeService service = Application.getChallengeService();
        ChallengeResponse challengeResponse = service.getChallengeResponse(responseId);
        String username = getLoggedInUsername();
        CustomResponse response = new CustomResponse();

        if(service.isUserCreatedAChallenge(challengeResponse.getChallengeParticipation().getChallenge().getId(), username)) {

            long acceptedResponsesNr = service.getAcceptedResponsesNrForUser(challengeResponse.getChallengeParticipation().getParticipator().getUsername());
            service.acceptChallengeResponse(challengeResponse);
            String participantUsername = challengeResponse.getChallengeParticipation().getParticipator().getUsername();

            if(acceptedResponsesNr % 5 == 0){
                int rewardedPoints = (int)(acceptedResponsesNr / 5) * User.MAJOR_REWARD;
                getUsersService().rewardParticipationPoints(participantUsername, rewardedPoints);
            }else {
                getUsersService().rewardParticipationPoints(participantUsername, User.NORMAL_REWARD);

            }
            getUsersService().rewardCreationPoints(username, User.MINOR_REWARD);
            response.addPoints(User.MINOR_REWARD);
            response.addMessage("response rated");
        }else{
            response.setStatus(CustomResponse.ResponseStatus.failure);
        }

        return ok(new Gson().toJson(response));

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
                        routes.javascript.Application.ajaxGetFacebookFriends(),
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

        if(challenge.getCreator().getUsername().compareTo(getLoggedInUsername()) == 0) {

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

            CustomResponse customResponse = new CustomResponse();

            if(service.getResponsesNrForUser(getLoggedInUsername()) == 1) {
                customResponse.addPoints(User.MINOR_REWARD);
                customResponse.addMessage("first challenge response!");
                getUsersService().rewardParticipationPoints(getLoggedInUsername(), User.MINOR_REWARD);
            }

            return ok(new Gson().toJson(customResponse));
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

            String currentFirstName = currentUser.getFirstName();
            String currentUsername = Application.getLoggedInUsername();
            String currentPicture = Application.getProfilePictureUrl();
            Integer currentPoints = currentUser.getAllPoints();


            return ok(participants.render(currentFirstName, currentUsername, currentPicture, currentPoints, challengeParticipants, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, challenge));
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
        List<Notification> latestUnreadNotifications = getNotificationService().getNewestUnreadNotifications(currentUser);

        String currentFirstName = Application.getFacebookService().getFacebookUser().getFirstName();
        String currentPicture = Application.getProfilePictureUrl();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(username);
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(username);
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(username);

        String viewedUserName = viewedUser.getFormattedName();
        String viewedUserPicture = viewedUser.getProfilePictureUrl();

        return ok(profile.render(currentFirstName, currentPicture, currentUser.getAllPoints(), latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, viewedUserName, viewedUserPicture, createdChallengesNr, joinedChallengesNr, completedChallengesNr, viewedUser.getAllPoints()));

    }

    public static Result showMyNotifications() {

        InternalNotificationService notificationService = getNotificationService();
        ChallengeService service = getChallengeService();
        User currentUser = getLoggedInUser();

        List<Notification> myNotifications = notificationService.getAllNotifications(getLoggedInUser());

        List<Object[]> latestChallenges = service.getLatestChallengesWithParticipantsNrForUser(currentUser.getUsername());
        List<Object[]> latestParticipations = service.getLatestChallengeParticipationsWithParticipantsNrForUser(currentUser.getUsername());
        List<Notification> latestNotifications = getNotificationService().getNewestNotifications(currentUser);

        String currentFirstName = currentUser.getFirstName();
        String currentPicture = Application.getProfilePictureUrl();
        Integer currentPoints = currentUser.getAllPoints();
        Long currentUnreadNotificationsNr = getNotificationService().getNumberOfUnreadNotifications(currentUser);

        return ok(notifications.render(currentFirstName, currentPicture, currentPoints, myNotifications, latestChallenges, latestParticipations, currentUnreadNotificationsNr, latestNotifications));

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

        return ok(profile_content.render(Application.getFacebookService().getFacebookUser().getName(), Application.getProfilePictureUrl(), createdChallengesNr, joinedChallengesNr, completedChallengesNr, getLoggedInUser().getAllPoints()));

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

        return ok(participations.render(currentUser.getFirstName(), Application.getProfilePictureUrl(), currentUser.getAllPoints(), myParticipations, challenges, responseForm, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications));
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

        return ok(challenge_details.render(currentUser.getFirstName(), Application.getProfilePictureUrl(), currentUser.getAllPoints(), challenges, participations, responseForm, currentUnreadNotificationsNr,
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

        return ok(challenge_responses.render(currentUser.getFirstName(), getLoggedInUsername(), Application.getProfilePictureUrl(), currentUser.getAllPoints(), challenges, participations, responseForm, currentUnreadNotificationsNr, latestNotifications, latestUnreadNotifications, responses, currentChallenge, video));
    }

}
