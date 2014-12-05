package services;

import domain.*;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChallengeService extends TransactionalBase {

    public static final int POPULARITY_INDICATOR = 10;

    private final ChallengesRepository challengesRepository;
    private final UserService userService;
    private final ChallengeNotificationsService notificationService;
    private final FacebookService facebookService;

    public ChallengeService(ChallengesRepository challengesRepository, UserService userService,
                            ChallengeNotificationsService notificationService, FacebookService facebookService) {
        this.challengesRepository = challengesRepository;
        this.userService = userService;
        this.notificationService = notificationService;
        this.facebookService = facebookService;
    }

    public Challenge createChallenge(final String creatorUsername, final String challengeName,
                                     final ChallengeCategory category, final Boolean visibility,
                                     final List<String> challengeParticipants, final File resourceFile, String filename,
                                     final Integer difficulty) throws UploadVideoFileException {


        if (isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }

        Challenge challenge = createAndPersistChallenge(creatorUsername, challengeName, category, null, visibility, difficulty);

        if (isChallengePrivate(challenge)) {

            if (!isAnyParticipantSelect(challengeParticipants)) {
                throw new IllegalArgumentException("Participants must be selected for private challenge creation. Challenge name: " + challengeName);
            }

            List<User> participants = addParicipantToChallenge(challenge, challengeParticipants);
            notificationService.notifyAboutNewPrivateChallenge(challenge, participants);

        }

        //upload video to fb
        String videoId = null;
        InputStream stream = null;
        try {
            stream = new FileInputStream(resourceFile);
            if (isChallengePrivate(challenge)) {
                videoId = facebookService.publishAPrivateVideo(challenge.getChallengeName(), stream, filename);
            } else {
                videoId = facebookService.publishAVideo(challenge.getChallengeName(), stream, filename);
            }
        } catch (IOException e) {
            throw new UploadVideoFileException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new UploadVideoFileException(e);
                }
            }
        }

        challenge.setVideoId(videoId);
        updateChallenge(challenge);
        return challenge;
    }

    private boolean isAnyParticipantSelect(List<String> challengeParticipants) {
        return challengeParticipants != null && challengeParticipants.size() > 0;
    }

    private List<User> addParicipantToChallenge(Challenge newChallenge, List<String> participants) {
        List<User> userParticipants = new ArrayList<User>();
        for (String p : participants) {

            List<String> items = Arrays.asList(p.split("\\s*,\\s*"));

            String id = items.get(0);

            //TODO batch?
            User user = userService.createNewOrGetExistingUser(id, items.get(1), items.get(2), items.get(3));
            userParticipants.add(user);
            participateInChallenge(newChallenge, id, user.getFormattedName());
        }

        return userParticipants;
    }

    private boolean isChallengePrivate(Challenge challenge) {
        return !challenge.getVisibility();
    }

    public Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility, Integer difficulty) {
        User creator = userService.getExistingUser(creatorUsername);
        return challengesRepository.createChallenge(new Challenge(creator, challengeName, category, videoId, visibility, difficulty));
    }

    private List<User> findAllParticipatorsOf(final Challenge challenge) {
        return challengesRepository.getAllParticipatorsOf(challenge);
    }

    public ChallengeParticipation participateInChallenge(final Challenge challenge, final String participatorUsername, final String participatorName) {
        if (isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
        }

        User participator = userService.getExistingUser(participatorUsername);
        Challenge refreshedChallenge = challengesRepository.getChallenge(challenge.getId());
        ChallengeParticipation challengeParticipation = challengesRepository.persistChallengeParticipation(new ChallengeParticipation(refreshedChallenge, participator));

        if (hasChallengeBecamePopular(challenge)) {
            notificationService.notifyAboutNewChallengeParticipation(challenge, participatorUsername, participatorName, findAllParticipatorsOf(challenge));
        }

        return challengeParticipation;
    }

    /**
     * Less effective, because sql statement is called each time, even after
     * limit has been reached. However it keeps logic to manage limit's
     * exceeding in one place. To be changed if it won't be effective enough to
     * more dynamic approach (keep it as a state, change the state, if necessary
     * during creating/removing new challenge participation)
     */
    private boolean hasChallengeBecamePopular(final Challenge challenge) {
        return challengesRepository.getNrOfParticipationsOf(challenge) == POPULARITY_INDICATOR;
    }

    public Boolean leaveChallenge(final Challenge challenge, final String participatorUsername, String participatorName) {

        if (!isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is not participating in challenge " + challenge);
        }

        User participator = userService.getExistingUser(participatorUsername);
        Boolean challengeRemovalResult = challengesRepository.deleteChallengeParticipation(challenge, participator);

        notificationService.notifyAboutChallengeLeaving(challenge, participatorUsername, participatorName, findAllParticipatorsOf(challenge));

        return challengeRemovalResult;
    }

    public boolean isUserParticipatingInChallenge(final Challenge challenge, final String user) {
        return challengesRepository.isUserParticipatingInChallenge(challenge, user);
    }

    public boolean isUserRespondedToChallenge(final Challenge challenge, final String user) {
        return challengesRepository.isUserRespondedToChallenge(challenge, user);
    }

    public boolean isUserCreatedChallengeWithName(final String challengeName, final String creator) {
        return challengesRepository.isChallengeWithGivenNameExistsForUser(challengeName, creator);
    }

    public boolean isUserCreatedAChallenge(final Long challengeId, final String creator) {
        return challengesRepository.isUserCreatedAChallenge(challengeId, creator);
    }

    public ChallengeResponse submitChallengeResponse(final ChallengeParticipation challengeParticipation, final String message, final String videoDescriptionUrl) {
        assertThatResponseCanBeSubmittedForParticipation(challengeParticipation);

        ChallengeResponse challengeResponse = new ChallengeResponse(challengeParticipation, videoDescriptionUrl, message);
        challengeResponse = challengesRepository.addChallengeResponse(challengeResponse);

        notificationService.notifyAboutSubmittingChallengeResponse(challengeParticipation, findAllParticipatorsOf(challengeParticipation.getChallenge()));

        return challengeResponse;
    }

    private void assertThatResponseCanBeSubmittedForParticipation(ChallengeParticipation challengeParticipation) {
        if (isNotEvaluatedResponseExistsFor(challengeParticipation)) {
            throw new IllegalStateException("User " + challengeParticipation.getParticipator() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        } else if (challengeParticipation.isOverdue()) {
            throw new IllegalStateException(challengeParticipation.getChallenge().getChallengeName() + "has ended on " + challengeParticipation.getChallenge().getEndingDate());
        }
    }

    public boolean isNotEvaluatedResponseExistsFor(final ChallengeParticipation challengeParticipation) {
        return challengesRepository.isNotEvaluatedChallengeResponseExistsFor(challengeParticipation);
    }


    public ChallengeParticipation getChallengeParticipation(final Challenge challenge, final String participatorUsername) {
        return challengesRepository.getChallengeParticipation(challenge, participatorUsername);
    }

    public List<Challenge> findChallenges(final ChallengeFilter challengeFilter) {
        return challengesRepository.findChallenges(challengeFilter);
    }

    public Challenge getChallenge(final long id) {
        return challengesRepository.getChallenge(id);
    }

    public ChallengeResponse acceptChallengeResponse(final ChallengeResponse challengeResponse) {

        assertThatResponseIsNotEvaluatedYet(challengeResponse);

        challengeResponse.accept();
        ChallengeResponse acceptedResponse = challengesRepository.updateChallengeResponse(challengeResponse);

        ChallengeParticipation challengeParticipation = acceptedResponse.getChallengeParticipation();
        notificationService.notifyAboutChallengeResponseAcceptance(challengeParticipation, findAllParticipatorsOf(challengeParticipation.getChallenge()));
        return acceptedResponse;
    }

    public ChallengeResponse refuseChallengeResponse(final ChallengeResponse challengeResponse) {

        assertThatResponseIsNotEvaluatedYet(challengeResponse);

        challengeResponse.refuse();
        ChallengeResponse refusedResponse = challengesRepository.updateChallengeResponse(challengeResponse);

        ChallengeParticipation challengeParticipation = refusedResponse.getChallengeParticipation();
        notificationService.notifyAboutChallengeResponseRefusal(challengeParticipation, findAllParticipatorsOf(challengeParticipation.getChallenge()));
        return refusedResponse;
    }

    private void assertThatResponseIsNotEvaluatedYet(ChallengeResponse challengeResponse) {
        if (challengeResponse.isDecided()) {
            throw new IllegalStateException("ChallengeResponse id: " + challengeResponse.getId() + " cannot be decided more than once");
        }
    }

    public Long countCreatedChallengesForUser(final String username) {
        return challengesRepository.countCreatedChallengesForUser(username);
    }

    public Long countCompletedChallenges(final String username) {
        return challengesRepository.countCompletedChallenges(username);
    }

    public List getChallengeParticipationsWithParticipantsNrForUser(final String username) {
        return challengesRepository.getChallengeParticipationsWithParticipantsNrForUser(username);
    }

    public List getLatestChallengeParticipationsWithParticipantsNrForUser(final String username) {
        return challengesRepository.getLastestParticipationsWithParticipantsNrForUser(username);
    }

    public List getChallengesWithParticipantsNrForUser(final String username) {
        return challengesRepository.getChallengesWithParticipantsNrForUser(username);
    }

    public List getLatestChallengesWithParticipantsNrForUser(final String username) {
        return challengesRepository.getLatestChallengesWithParticipantsNrForUser(username);
    }

    public Challenge closeChallenge(final long id) {
        return challengesRepository.closeChallenge(id);
    }

    public List<ChallengeResponse> getResponsesForChallenge(final long challengeId) {
        return challengesRepository.getResponsesForChallenge(challengeId);
    }

    public Long getResponsesNrForChallenge(final long challengeId) {
        return challengesRepository.getResponsesNrForChallenge(challengeId);
    }

    public ChallengeResponse getChallengeResponse(final long id) {
        return challengesRepository.getChallengeResponse(id);
    }

    public List<ChallengeResponse> getChallengeParticipationsForUser(final String participatorUsername) {
        return challengesRepository.getChallengeParticipationsForUser(participatorUsername);
    }

    public List<Challenge> getCompletedChallenges(final String username) {
        return challengesRepository.getCompletedChallenges(username);
    }

    public List<ChallengeParticipation> getParticipantsForChallenge(final long challengeId) {
        return challengesRepository.getParticipantsForChallenge(challengeId);
    }

    public Long getCreatedChallengesNrForUser(final String username) {
        return challengesRepository.getCreatedChallengesNrForUser(username);
    }

    public Long getCompletedChallengesNrForUser(final String username) {
        return challengesRepository.getCompletedChallengesNrForUser(username);
    }

    public Long getJoinedChallengesNrForUser(final String username) {
        return challengesRepository.getJoinedChallengesNrForUser(username);
    }

    public Long getResponsesNrForUser(final String username){
        return challengesRepository.getResponsesNrForUser(username);
    }

    public Long getAcceptedResponsesNrForUser(final String username){
        return challengesRepository.getAcceptedResponsesNrForUser(username);
    }

    public Challenge updateChallenge(final Challenge challenge) {
        return challengesRepository.updateChallenge(challenge);
    }

    public ChallengeParticipation updateChallengeParticipation(final ChallengeParticipation challengeParticipation) {
        return challengesRepository.updateChallengeParticipation(challengeParticipation);

    }


}
