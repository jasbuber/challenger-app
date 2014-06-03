package services;

import domain.*;
import play.libs.F;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.List;

public class ChallengeService extends TransactionalBase {

    private final ChallengesRepository challengesRepository;
    private final UsersRepository usersRepository;
    private final NotificationService notificationService;

    public ChallengeService(ChallengesRepository challengesRepository, UsersRepository usersRepository, NotificationService notificationService) {
        this.challengesRepository = challengesRepository;
        this.usersRepository = usersRepository;
        this.notificationService = notificationService;
    }

    public Challenge createChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        if (isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }

        Challenge challenge = createAndPersistChallenge(creatorUsername, challengeName, category, videoId, visibility);

        notifyAboutChallengeCreation(challenge);

        return challenge;
    }

    private Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        return withTransaction(new F.Function0<Challenge>() {
            @Override
            public Challenge apply() throws Throwable {
                User creator = usersRepository.getUser(creatorUsername);
                return challengesRepository.createChallenge(new Challenge(creator, challengeName, category, videoId, visibility));
            }
        });
    }

    private void notifyAboutChallengeCreation(Challenge challenge) {
        String notificationMsg = "Challenge " + challenge.getChallengeName() + " was successfully created";
        notifyChallengeCreator(challenge, notificationMsg);
    }

    private void notifyChallengeCreator(Challenge challenge, String notificationMsg) {
        notificationService.notifyUser(challenge.getCreator(), notificationMsg);
    }

    private void notifyAllParticipators(final Challenge challenge, String notificationMsg) {
        List<User> participators = findAllParticipatorsOf(challenge);
        notificationService.notifyUsers(participators, notificationMsg);
    }

    private List<User> findAllParticipatorsOf(final Challenge challenge) {
        return withReadOnlyTransaction(new F.Function0<List<User>>() {
            @Override
            public List<User> apply() throws Throwable {
                return challengesRepository.getAllParticipatorsOf(challenge);
            }
        });
    }

    public ChallengeParticipation participateInChallenge(final Challenge challenge, final String participatorUsername) {
        if (isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
        }

        ChallengeParticipation challengeParticipation = withTransaction(new F.Function0<ChallengeParticipation>() {
            @Override
            public ChallengeParticipation apply() throws Throwable {
                User participator = usersRepository.getUser(participatorUsername);
                return challengesRepository.persistChallengeParticipation(new ChallengeParticipation(challenge, participator));
            }
        });

        notifyAboutNewChallengeParticipation(challenge, participatorUsername);

        return challengeParticipation;
    }

    private void notifyAboutNewChallengeParticipation(Challenge challenge, String participatorUsername) {
        String challengeCreatorMsg = "New participation was added to your challenge " + challenge.getChallengeName() + "." +
                " Participator username is " + participatorUsername;
        String challengeParticipatorsMsg = "New participation was added to the challenge " + challenge.getChallengeName() + ". " +
                "Participator username is " + participatorUsername + "." +
                " Challenge creator is " + challenge.getCreator().getUsername();

        notifyChallengeCreator(challenge, challengeCreatorMsg);
        notifyAllParticipators(challenge, challengeParticipatorsMsg);
    }

    public Boolean leaveChallenge(final Challenge challenge, final String participatorUsername) {

        if (!isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is not participating in challenge " + challenge);
        }
        Boolean challengeRemovalResult = withTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                User participator = usersRepository.getUser(participatorUsername);
                return challengesRepository.deleteChallengeParticipation(challenge, participator);
            }
        });

        notifyAboutChallengeLeaving(challenge, participatorUsername);

        return challengeRemovalResult;
    }

    private void notifyAboutChallengeLeaving(Challenge challenge, String participatorUsername) {
        String challengeCreatorMsg = "Participator " + participatorUsername + " has left your challenge " + challenge.getChallengeName();
        String challengeParticipatorsMsg = "Participator " + participatorUsername + " has left challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();

        notifyChallengeCreator(challenge, challengeCreatorMsg);
        notifyAllParticipators(challenge, challengeParticipatorsMsg);
    }

    public boolean isUserParticipatingInChallenge(final Challenge challenge, final String user) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isUserParticipatingInChallenge(challenge, user);
            }
        });
    }

    public boolean isUserCreatedChallengeWithName(final String challengeName, final String creator) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isChallengeWithGivenNameExistsForUser(challengeName, creator);
            }
        });
    }

    public ChallengeResponse submitChallengeResponse(final ChallengeParticipation challengeParticipation) {
        assertThatResponseCanBeSubmittedForParticipation(challengeParticipation);
        ChallengeResponse challengeResponse = withTransaction(new F.Function0<ChallengeResponse>() {

            @Override
            public ChallengeResponse apply() throws Throwable {
                ChallengeResponse challengeResponse = new ChallengeResponse(challengeParticipation);
                return challengesRepository.addChallengeResponse(challengeResponse);
            }
        });

        notifyAboutSubmittingChallengeResponse(challengeParticipation);

        return challengeResponse;
    }

    private void notifyAboutSubmittingChallengeResponse(ChallengeParticipation challengeParticipation) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeCreatorMsg = "User " + challengeParticipation.getParticipator() + " has just submitted response to your challenge " + challenge.getChallengeName();
        String challengeParticipatorsMsg = "User " + challengeParticipation.getParticipator() + " has just submitted response to the challenge " + challenge.getChallengeName()
                + " of user " + challenge.getCreator().getUsername();

        notifyChallengeCreator(challenge, challengeCreatorMsg);
        notifyAllParticipators(challenge, challengeParticipatorsMsg);

    }

    private void assertThatResponseCanBeSubmittedForParticipation(ChallengeParticipation challengeParticipation) {
        if (isNotEvaluatedResponseExistsFor(challengeParticipation)) {
            throw new IllegalStateException("User " + challengeParticipation.getParticipator() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        }
    }

    public boolean isNotEvaluatedResponseExistsFor(final ChallengeParticipation challengeParticipation) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isNotEvaluatedChallengeResponseExistsFor(challengeParticipation);
            }
        });
    }


    public ChallengeParticipation getChallengeParticipation(final Challenge challenge, final String participatorUsername) {
        return withReadOnlyTransaction(new F.Function0<ChallengeParticipation>() {
            @Override
            public ChallengeParticipation apply() throws Throwable {
                return challengesRepository.getChallengeParticipation(challenge, participatorUsername);
            }
        });
    }

    public List<Challenge> findChallenges(final ChallengeFilter challengeFilter) {
        return withReadOnlyTransaction(new F.Function0<List<Challenge>>() {
            @Override
            public List<Challenge> apply() throws Throwable {
                return challengesRepository.findChallenges(challengeFilter);
            }
        });
    }

    public Challenge getChallenge(final long id) {
        return withReadOnlyTransaction(new F.Function0<Challenge>() {
            @Override
            public Challenge apply() throws Throwable {
                return challengesRepository.getChallenge(id);
            }
        });
    }

    public ChallengeResponse acceptChallengeResponse(final ChallengeResponse challengeResponse) {
        ChallengeResponse acceptedResponse = withTransaction(new F.Function0<ChallengeResponse>() {

            @Override
            public ChallengeResponse apply() throws Throwable {
                assertThatResponseIsNotEvaluatedYet(challengeResponse);

                challengeResponse.accept();
                challengesRepository.updateChallengeResponse(challengeResponse);
                return challengeResponse;
            }
        });

        notifyAboutChallengeResponseAcceptance(acceptedResponse.getChallengeParticipation());
        return acceptedResponse;
    }

    private void notifyAboutChallengeResponseAcceptance(ChallengeParticipation challengeParticipation) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getUsername();
        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been accepted by " + challenge.getCreator().getUsername();

        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg);
        notifyAllParticipators(challenge, participatorsMsg);
    }

    public ChallengeResponse refuseChallengeResponse(final ChallengeResponse challengeResponse) {
        ChallengeResponse refusedResponse = withTransaction(new F.Function0<ChallengeResponse>() {

            @Override
            public ChallengeResponse apply() throws Throwable {
                assertThatResponseIsNotEvaluatedYet(challengeResponse);

                challengeResponse.refuse();
                challengesRepository.updateChallengeResponse(challengeResponse);
                return challengeResponse;
            }
        });

        notifyAboutChallengeResponseRefusal(refusedResponse.getChallengeParticipation());
        return refusedResponse;
    }

    private void notifyAboutChallengeResponseRefusal(ChallengeParticipation challengeParticipation) {
        Challenge challenge = challengeParticipation.getChallenge();

        String challengeParticipatorMsg = "Your challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getUsername();
        String participatorsMsg = "Challenge participation in challenge " + challenge.getChallengeName() +
                " has been refused by " + challenge.getCreator().getUsername();

        notificationService.notifyUser(challengeParticipation.getParticipator(), challengeParticipatorMsg);
        notifyAllParticipators(challenge, participatorsMsg);
    }

    private void assertThatResponseIsNotEvaluatedYet(ChallengeResponse challengeResponse) {
        if (challengeResponse.isDecided()) {
            throw new IllegalStateException("ChallengeResponse id: " + challengeResponse.getId() + " cannot be decided more than once");
        }
    }

    public Long countCreatedChallengesForUser(final String username) {
        return withReadOnlyTransaction(new F.Function0<Long>() {
            @Override
            public Long apply() throws Throwable {
                return challengesRepository.countCreatedChallengesForUser(username);
            }
        });
    }

    public Long countCompletedChallenges(final String username) {
        return withReadOnlyTransaction(new F.Function0<Long>() {
            @Override
            public Long apply() throws Throwable {
                return challengesRepository.countCompletedChallenges(username);
            }
        });
    }

    public List getChallengesWithParticipantsNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<List>() {
                @Override
                public List apply() throws Throwable {
                    return challengesRepository.getChallengesWithParticipantsNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Challenge closeChallenge(final long id){
        try {
            return withTransaction(new F.Function0<Challenge>() {
                @Override
                public Challenge apply() throws Throwable {
                    return challengesRepository.closeChallenge(id);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List<ChallengeResponse> getResponsesForChallenge(final long challengeId){
        try {
            return withReadOnlyTransaction(new F.Function0<List>() {
                @Override
                public List apply() throws Throwable {
                    return challengesRepository.getResponsesForChallenge(challengeId);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public ChallengeResponse getChallengeResponse(final long id){
        try {
            return withReadOnlyTransaction(new F.Function0<ChallengeResponse>() {
                @Override
                public ChallengeResponse apply() throws Throwable {
                    return challengesRepository.getChallengeResponse(id);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }
}
