package controllers;

import com.google.gson.Gson;

import domain.Challenge;
import domain.ChallengeCategory;
import domain.CustomResponse;
import domain.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import repositories.ChallengesRepository;
import repositories.InternalNotificationsRepository;
import repositories.UsersRepository;
import services.ChallengeNotificationsService;
import services.ChallengeService;
import services.InternalNotificationService;
import services.UserService;

import java.util.ArrayList;

/**
 * Created by Jasbuber on 14/08/2015.
 */
public class AndroidServices extends Controller {

    @play.db.jpa.Transactional
    public static Result createChallenge() {

        Http.Request request = request();

        String username = getPostData(request, "username");
        String name = getPostData(request, "name");
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
}
