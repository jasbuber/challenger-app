package controllers;

import com.google.gson.Gson;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
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
import services.InternalNotificationService;
import services.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.criteria.Expression;

/**
 * Created by Jasbuber on 14/08/2015.
 */
public class AndroidServices extends Controller {

    @play.db.jpa.Transactional
    public static Result createChallenge() {

        Http.Request request = request();

        String username = getPostData(request, "username");
        String name = getPostData(request, "challengeName");
        String category = getPostData(request, "category");
        int difficulty = Integer.parseInt(getPostData(request, "difficulty"));
        boolean visibility = !Boolean.getBoolean(getPostData(request, "visibility"));

        if (!visibility && !isChallengeParticipantSelected()) {

            CustomResponse response = new CustomResponse();
            response.setStatus(CustomResponse.ResponseStatus.failure);
            response.addMessage("no_participants");

            return ok(new Gson().toJson(response));
        }

        Challenge newChallenge = getChallengeService()
                .createChallenge(username, name, ChallengeCategory.valueOf(category),
                        visibility, new ArrayList<>(), difficulty);

        return ok(new Gson().toJson(getResponseForCreatedChallenge(newChallenge, newChallenge.getId())));
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
    public static Result getLatestChallenges(String username){

        ChallengeService service = getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(11);
        User currentUser = getUsersService().getExistingUser(username);

        filter.orderDescBy("creationDate");

        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());
        filter.prepareWhere();

        List<Challenge> challenges = service.findChallenges(filter, 0);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getChallengesByCriteria(String username, String phrase, String category, int page, int scope){

        ChallengeService service = getChallengeService();
        ChallengeFilter filter = new ChallengeFilter(11);
        User currentUser = getUsersService().getExistingUser(username);

        filter.orderDescBy("creationDate");
        Expression<String> challengeNameField = filter.getBuilder().lower(filter.getField("challengeName"));

        phrase = phrase.trim();

        if(phrase.length() >=3) {
            if (phrase.length() > 25) {
                phrase = phrase.substring(0, 24);
            }

            if(scope == 2) {
                Expression<String> userNameField = filter.getRoot().join("creator").get("fullName");
                filter.andCond(filter.getBuilder().like(filter.getBuilder().lower(userNameField), "%" + phrase.toLowerCase() + "%"));
                Logger.error(filter.getQuery().toString());
            }else{
                filter.andCond(filter.getBuilder().like(challengeNameField, "%" + phrase.toLowerCase() + "%"));
            }
        }

        filter.andCond(filter.excludeChallengesThatUserParticipatesIn(currentUser));
        filter.andCond(filter.excludePrivateChallenges());

        if (!(ChallengeCategory.valueOf(category).equals(ChallengeCategory.ALL))) {
            Expression<String> categoryField = filter.getField("category");
            filter.andCond(filter.getBuilder().equal(categoryField, ChallengeCategory.valueOf(category)));
        }

        filter.prepareWhere();

        return ok(new Gson().toJson(service.findChallenges(filter, page)));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getResponsesForChallenge(Long challengeId, String username, String token) {

        ChallengeService service = getChallengeService();

        List<ChallengeResponse> responses = service.getResponsesForChallenge(challengeId);

        return ok(new Gson().toJson(responses));
    }

    @play.db.jpa.Transactional
    public static Result joinChallenge() {

        Http.Request request = request();

        String username = getPostData(request, "username");
        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String fullName = getPostData(request, "fullName");

        ChallengeService service = getChallengeService();
        Challenge challenge = service.getChallenge(challengeId);

        service.participateInChallenge(challenge, username, fullName);

        return ok(new Gson().toJson(new CustomResponse()));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getMyChallenges(String username, int page) {

        ChallengeService service = getChallengeService();

        List<ChallengeWithParticipantsNr> challenges = service.getChallengesWithParticipantsNrForUser(username, page);

        return ok(new Gson().toJson(challenges));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getChallenge(long id, String username) {

        ChallengeService service = getChallengeService();

        Challenge currentChallenge = service.getChallenge(id);
        int participationState = 3;

        if(!currentChallenge.getCreator().getUsername().equals(username)){
            participationState = service.getChallengeParticipationStateForUser(currentChallenge, username);
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("challenge", currentChallenge);
        response.put("participationState", participationState);

        return ok(new Gson().toJson(response));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getParticipationState(long id, String username) {

        ChallengeService service = getChallengeService();

        Challenge currentChallenge = service.getChallenge(id);

        int participationState = ChallengeParticipation.CREATOR_STATE;

        if(!currentChallenge.getCreator().getUsername().equals(username)){
            participationState = service.getChallengeParticipationStateForUser(currentChallenge, username);
        }

        return ok(new Gson().toJson(participationState));
    }

    @play.db.jpa.Transactional(readOnly = true)
    public static Result getMyParticipations(String username, int page) {

        ChallengeService service = getChallengeService();

        List<ChallengeWithParticipantsNr> challenges = service.getChallengeParticipationsWithParticipantsNrForUser(username, page);

        return ok(new Gson().toJson(challenges));
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

        return ok(new Gson().toJson(response));
    }

    @play.db.jpa.Transactional
    public static Result createUser(){

        UserService service = getUsersService();
        Http.Request request = request();

        String username = getPostData(request, "username");
        String firstName = getPostData(request, "firstName");
        String lastName = getPostData(request, "lastName");
        String profilePictureUrl = getPostData(request, "profilePictureUrl");

        FacebookUser user = new FacebookUser( username, profilePictureUrl, firstName, lastName);

        User appUser = service.createNewOrGetExistingUser(user, profilePictureUrl);

        if(!appUser.getProfilePictureUrl().equals(profilePictureUrl)){
            appUser.setProfilePictureUrl(profilePictureUrl);
            service.updateUser(appUser);
        }

        return ok("success");
    }

    @play.db.jpa.Transactional
    public static Result updateChallengeVideo(){

        ChallengeService service = getChallengeService();
        Http.Request request = request();

        long challengeId = Long.parseLong(getPostData(request, "challengeId"));
        String videoId = getPostData(request, "videoId");
        String username = getPostData(request, "username");

        Challenge challenge = service.getChallenge(challengeId);

        if(challenge.getCreator().getUsername().equals(username) && challenge.getVideoId() == null) {
            challenge.setVideoId(videoId);
            service.updateChallenge(challenge);

            return ok("success");
        }

        return ok("failure");
    }
}
