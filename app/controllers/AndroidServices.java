package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.Comment;
import domain.CustomResponse;
import domain.FacebookUser;
import domain.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;
import repositories.dtos.ChallengeWithParticipantsNr;
import services.ChallengeNotificationsService;
import services.ChallengeService;
import services.FacebookService;
import services.InternalNotificationService;
import services.UserService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.criteria.Expression;

/**
 * Created by Jasbuber on 14/08/2015.
 */
public class AndroidServices extends Controller {

    public enum SORTING_ORDER { RECENT, TOP, POPULAR, CATEGORIES }

    @play.db.jpa.Transactional
    public static Result createChallenge() {

        Http.Request request = request();

        String username = getPostData(request, "username");
        String name = getPostData(request, "challengeName");
        String category = getPostData(request, "category");
        int difficulty = Integer.parseInt(getPostData(request, "difficulty"));
        boolean visibility = !Boolean.getBoolean(getPostData(request, "visibility"));
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        Challenge newChallenge = getChallengeService()
                .createChallenge(username, name, ChallengeCategory.valueOf(category),
                        visibility, new ArrayList<String>(), difficulty);

        return ok(getGson().toJson(getResponseForCreatedChallenge(newChallenge, newChallenge.getId())));
    }

    //TO_DO
    private static boolean isChallengeParticipantSelected() {
        return true;
    }

    private static boolean isChallengePrivate(Challenge challenge) {
        return !challenge.getVisibility();
    }

    //need to exist until dependency injection framework is added
    private static ChallengeService getChallengeService() {
        return new ChallengeService(new ChallengesRepository(), new UserService(new UsersRepository()), createNotificationService());
    }

    //need to exist until dependency injection framework is added
    private static UserService getUsersService() {
        return new UserService(new UsersRepository());
    }

    private static FacebookService getFacebookService(String token) {
        return new FacebookService(token);
    }

    private static ChallengeNotificationsService createNotificationService() {
        return new ChallengeNotificationsService(new InternalNotificationService(new InternalNotificationsRepository()));
    }

    private static CustomResponse getResponseForCreatedChallenge(Challenge challenge, long challengeId) {

        String username = challenge.getCreator().getUsername();

        CustomResponse response = new CustomResponse();

        response.setChallengeId(challengeId);

        long challengeNr = getChallengeService().getCreatedChallengesNrForUser(username);

        if (isChallengePrivate(challenge)) {
            response.addMessage("first private challenge", User.MINOR_REWARD);
        }
        if (challengeNr == 1) {
            response.addMessage("first challenge", User.MINOR_REWARD);
        } else if (challengeNr % 5 == 0) {
            int rewardedPoints = (int) (challengeNr / 5) * User.NORMAL_REWARD;
            response.addMessage(challengeNr + " challenges created", rewardedPoints);
        }

        if (response.getRewardedPoints() > 0) {
            getUsersService().rewardCreationPoints(username, response.getRewardedPoints());
        }

        return response;
    }

    private static String getPostData(Http.Request request, String key) {
        return request.body().asFormUrlEncoded().get(key)[0];
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getLatestChallenges() {

        ChallengeService service = getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(11);

        filter.orderDescBy("creationDate");
        filter.andCond(filter.excludePrivateChallenges());
        filter.andCond(filter.excludeChallengesWithoutVideo());
        filter.prepareWhere();

        List<Challenge> challenges = service.findChallenges(filter, 0);

        return ok(getGson().toJson(challenges));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getChallengesByCriteria(String phrase, String category, int page, String order) {

        ChallengeService service = getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(11);

        phrase = phrase.trim();
        if (phrase.length() > 25) {
            phrase = phrase.substring(0, 24);
        }

        switch (SORTING_ORDER.valueOf(order)){
            case POPULAR : {
                return ok(getGson().toJson(service.getPopularChallengesByPhrase(phrase, page)));
            }
            case CATEGORIES:
                filter.orderDescBy("creationDate");
                ChallengeCategory challengeCategory = ChallengeCategory.valueOf(category);
                if(!challengeCategory.equals(ChallengeCategory.ALL)) {
                    Expression<String> categoryField = filter.getField("category");
                    filter.andCond(filter.getBuilder().equal(categoryField, challengeCategory));
                }
                break;
            case TOP:
                filter.orderDescBy("rating");
                break;
            case RECENT:
                filter.orderDescBy("creationDate");
                break;
        }

        Expression<String> challengeNameField = filter.getBuilder().lower(filter.getField("challengeName"));

        if (phrase.length() >= 3) {
            filter.andCond(filter.getBuilder().like(challengeNameField, "%" + phrase.toLowerCase() + "%"));
        }

        filter.andCond(filter.excludePrivateChallenges());
        filter.andCond(filter.excludeChallengesWithoutVideo());

        filter.prepareWhere();

        return ok(getGson().toJson(service.findChallenges(filter, page)));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getResponsesForChallenge(Long challengeId, String username, String token) {

        ChallengeService service = getChallengeService();

        List<ChallengeResponse> responses = service.getResponsesForChallenge(challengeId);

        if (!responses.isEmpty()) {
            responses = getFacebookService(token).getThumbnailsForResponses(responses);
        }

        return ok(getGson().toJson(responses));
    }

    @play.db.jpa.Transactional
    public static Result joinChallenge() {

        Http.Request request = request();

        String username = getPostData(request, "username");
        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String fullName = getPostData(request, "fullName");
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        ChallengeService service = getChallengeService();
        Challenge challenge = service.getChallenge(challengeId);

        service.participateInChallenge(challenge, username, fullName);

        return ok(getGson().toJson(new CustomResponse()));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getMyChallenges(String username, int page) {

        ChallengeService service = getChallengeService();

        List<ChallengeWithParticipantsNr> challenges = service.getChallengesWithParticipantsNrForUser(username, page);

        return ok(getGson().toJson(challenges));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getChallenge(long id, String username) {

        ChallengeService service = getChallengeService();

        Challenge currentChallenge = service.getChallenge(id);

        int participationState = 3;

        if (!currentChallenge.getCreator().getUsername().equals(username)) {
            participationState = service.getChallengeParticipationStateForUser(currentChallenge, username);
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("challenge", currentChallenge);
        response.put("participationState", participationState);
        response.put("comments", service.getCommentsForChallenge(id, 0));

        return ok(getGson().toJson(response));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getParticipationState(long id, String username) {

        ChallengeService service = getChallengeService();

        Challenge currentChallenge = service.getChallenge(id);

        int participationState = ChallengeParticipation.CREATOR_STATE;

        if (!currentChallenge.getCreator().getUsername().equals(username)) {
            participationState = service.getChallengeParticipationStateForUser(currentChallenge, username);
        }

        return ok(getGson().toJson(participationState));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getMyParticipations(String username, int page) {

        ChallengeService service = getChallengeService();

        List<ChallengeWithParticipantsNr> challenges = service.getChallengeParticipationsWithParticipantsNrForUser(username, page);

        return ok(getGson().toJson(challenges));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getRankings() {

        List<User> topRatedUsers = getUsersService().getTopRatedUsers();
        List<Challenge> topRatedChallenges = getChallengeService().getTopRatedChallenges();
        List<Challenge> trendingChallenges = getChallengeService().getTrendingChallenges();
        List<ChallengeWithParticipantsNr> mostPopularChallenges = getChallengeService().getMostPopularChallenges();

        HashMap<String, List> response = new HashMap<>();
        response.put("topRatedUsers", topRatedUsers);
        response.put("topRatedChallenges", topRatedChallenges);
        response.put("trendingChallenges", trendingChallenges);
        response.put("mostPopularChallenges", mostPopularChallenges);

        return ok(getGson().toJson(response));
    }

    @play.db.jpa.Transactional
    public static Result createUser() {

        UserService service = getUsersService();
        Http.Request request = request();

        String username = getPostData(request, "username");
        String firstName = getPostData(request, "firstName");
        String lastName = getPostData(request, "lastName");
        String profilePictureUrl = getPostData(request, "profilePictureUrl");
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        FacebookUser user = new FacebookUser();
        user.setId(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPicture(profilePictureUrl);

        User appUser = service.createNewOrGetExistingUser(user, profilePictureUrl);

        if (!appUser.getProfilePictureUrl().equals(profilePictureUrl)) {
            appUser.setProfilePictureUrl(profilePictureUrl);
            service.updateUser(appUser);
        }

        return ok(String.valueOf(appUser.getTutorialCompleted()));
    }

    @play.db.jpa.Transactional
    public static Result updateChallengeVideo() {

        ChallengeService service = getChallengeService();
        Http.Request request = request();

        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String videoId = getPostData(request, "videoId");
        String username = getPostData(request, "username");

        Challenge challenge = service.getChallenge(challengeId);

        if (challenge.getCreator().getUsername().equals(username) && challenge.getVideoId() == null) {
            challenge.setVideoId(videoId);
            service.updateChallenge(challenge);

            return ok("success");
        }

        return ok("failure");
    }

    @play.db.jpa.Transactional
    public static Result submitChallengeResponse() {

        ChallengeService service = getChallengeService();
        Http.Request request = request();

        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String videoId = getPostData(request, "videoId");
        String username = getPostData(request, "username");
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        Challenge challenge = service.getChallenge(challengeId);

        ChallengeParticipation participation = service.getChallengeParticipation(challenge, username);
        ChallengeResponse newResponse = service.submitChallengeResponse(participation, null, videoId);
        participation.submit();
        service.updateChallengeParticipation(participation);

        CustomResponse customResponse = new CustomResponse();

        if (service.getResponsesNrForUser(username) == 1) {
            customResponse.addPoints(User.MINOR_REWARD);
            customResponse.addMessage("first challenge response!");
            getUsersService().rewardParticipationPoints(username, User.MINOR_REWARD);
        }

        customResponse.setChallengeId(challenge.getId());

        return ok(getGson().toJson(customResponse));
    }

    @play.db.jpa.Transactional
    public static Result rateResponse() {

        ChallengeService service = getChallengeService();
        Http.Request request = request();

        long responseId = Long.parseLong(getPostData(request, "responseId"));
        String username = getPostData(request, "username");
        String isAccepted = getPostData(request, "isAccepted");
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        ChallengeResponse challengeResponse = service.getChallengeResponse(responseId);

        CustomResponse response;
        if (isAccepted.equals("Y")) {
            response = getResponseForAcceptChallengeResponse(username, challengeResponse);
        } else {
            response = getResponseForRejectChallengeResponse(username, challengeResponse);
        }

        return ok(getGson().toJson(response));

    }

    private static CustomResponse getResponseForAcceptChallengeResponse(String username, ChallengeResponse challengeResponse) {

        ChallengeService service = getChallengeService();
        CustomResponse response = new CustomResponse();

        if (service.isUserCreatedAChallenge(challengeResponse.getChallengeParticipation().getChallenge().getId(), username)) {

            long acceptedResponsesNr = service.getAcceptedResponsesNrForUser(challengeResponse.getChallengeParticipation().getParticipator().getUsername());
            service.acceptChallengeResponse(challengeResponse);
            String participantUsername = challengeResponse.getChallengeParticipation().getParticipator().getUsername();

            if (acceptedResponsesNr % 5 == 0) {
                int rewardedPoints = (int) (acceptedResponsesNr / 5) * User.MAJOR_REWARD;
                getUsersService().rewardParticipationPoints(participantUsername, rewardedPoints);
            } else {
                getUsersService().rewardParticipationPoints(participantUsername, User.NORMAL_REWARD);

            }
            getUsersService().rewardCreationPoints(username, User.MINOR_REWARD);
            response.addPoints(User.MINOR_REWARD);
            response.addMessage("response accepted");
        } else {
            response.setStatus(CustomResponse.ResponseStatus.failure);
        }

        return response;
    }

    private static CustomResponse getResponseForRejectChallengeResponse(String username, ChallengeResponse challengeResponse) {

        ChallengeService service = getChallengeService();
        CustomResponse response = new CustomResponse();

        if (service.isUserCreatedAChallenge(challengeResponse.getChallengeParticipation().getChallenge().getId(), username)) {
            service.refuseChallengeResponse(challengeResponse);
            getUsersService().rewardCreationPoints(username, User.MINOR_REWARD);
            response.addPoints(User.MINOR_REWARD);
            response.addMessage("response declined");
        } else {
            response.setStatus(CustomResponse.ResponseStatus.failure);
        }

        return response;
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getProfile(String username) {

        ChallengeService service = getChallengeService();

        Long completedChallengesNr = service.getCompletedChallengesNrForUser(username);
        Long joinedChallengesNr = service.getJoinedChallengesNrForUser(username);
        Long createdChallengesNr = service.getCreatedChallengesNrForUser(username);

        User user = getUsersService().getExistingUser(username);

        HashMap<String, Object> response = new HashMap<>();
        response.put("completedChallengesNr", completedChallengesNr);
        response.put("joinedChallengesNr", joinedChallengesNr);
        response.put("createdChallengesNr", createdChallengesNr);
        response.put("user", user);

        return ok(getGson().toJson(response));
    }

    @play.db.jpa.Transactional
    public static Result rateChallenge() {

        Http.Request request = request();

        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String username = getPostData(request, "username");
        int rating = Integer.parseInt(getPostData(request, "rating"));
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        ChallengeService service = getChallengeService();

        Challenge challenge = service.getChallenge(challengeId);

        ChallengeParticipation participation = service.getChallengeParticipation(challenge, username);

        if (!participation.isChallengeRated()) {
            participation.getChallenge().addRating(rating);
            service.updateChallenge(participation.getChallenge());
            participation.rateChallenge();
            service.updateChallengeParticipation(participation);
        }

        return ok("success");
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
    }

    private static boolean isAccessTokenValid(String username, String token) {
        return new FacebookService(token).getFacebookUser().getId().equals(username);
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getVideoUrl(String token, String videoId) {
        return ok(getGson().toJson(getFacebookService(token).getVideo(videoId).getSource()));
    }

    @play.db.jpa.Transactional
    public static Result createComment() {

        Http.Request request = request();

        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String username = getPostData(request, "username");
        String message = getPostData(request, "message");
        String token = getPostData(request, "token");

        ChallengeService service = getChallengeService();

        if (!isAccessTokenValid(username, token) || message.isEmpty()) {
            return ok("failure");
        }

        Comment c = service.createComment(username, message, challengeId);

        return ok(getGson().toJson("success"));

    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getComments(long challengeId, int offset) {

        ChallengeService service = getChallengeService();

        List<Comment> comments = service.getCommentsForChallenge(challengeId, offset);

        return ok(getGson().toJson(comments));
    }

    @play.db.jpa.Transactional
    public static Result completeTutorial() {

        Http.Request request = request();

        String username = getPostData(request, "username");
        String token = getPostData(request, "token");

        if (!isAccessTokenValid(username, token)) {
            return ok("failure");
        }

        User currentUser = getUsersService().getExistingUser(username);

        currentUser.completeTutorial();
        getUsersService().updateUser(currentUser);

        return ok("success");
    }

}
