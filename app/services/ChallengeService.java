package services;

import domain.*;
import play.db.jpa.Transactional;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.List;

public class ChallengeService extends TransactionalBase {

    public static final int POPULARITY_INDICATOR = 10;

    private final ChallengesRepository challengesRepository;
    private final UsersRepository usersRepository;
    private final ChallengeNotificationsService notificationService;

    public ChallengeService(ChallengesRepository challengesRepository, UsersRepository usersRepository, ChallengeNotificationsService notificationService) {
        this.challengesRepository = challengesRepository;
        this.usersRepository = usersRepository;
        this.notificationService = notificationService;
    }

    public Challenge createChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        if (isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }

        return createAndPersistChallenge(creatorUsername, challengeName, category, videoId, visibility);
    }

    private Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        User creator = usersRepository.getUser(creatorUsername);
        return challengesRepository.createChallenge(new Challenge(creator, challengeName, category, videoId, visibility));
    }

    private List<User> findAllParticipatorsOf(final Challenge challenge) {
        return challengesRepository.getAllParticipatorsOf(challenge);
    }

    public ChallengeParticipation participateInChallenge(final Challenge challenge, final String participatorUsername, final String participatorName) {
        if (isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
        }


        User participator = usersRepository.getUser(participatorUsername);
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

        User participator = usersRepository.getUser(participatorUsername);
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

    public Challenge updateChallenge(final Challenge challenge) {
        return challengesRepository.updateChallenge(challenge);
    }
}
