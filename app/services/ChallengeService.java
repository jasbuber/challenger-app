package services;

import controllers.CreateChallengeForm;
import domain.*;
import play.libs.F;
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

    public Challenge createChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility, final Integer difficulty) {
        if (isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }

        Challenge newChallenge = createAndPersistChallenge(creatorUsername, challengeName, category, videoId, visibility, difficulty);


        return newChallenge;
    }

    private Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility, final Integer difficulty) {
        return withTransaction(new F.Function0<Challenge>() {
            @Override
            public Challenge apply() throws Throwable {
                User creator = usersRepository.getUser(creatorUsername);
                return challengesRepository.createChallenge(new Challenge(creator, challengeName, category, videoId, visibility, difficulty));
            }
        });
    }

    private List<User> findAllParticipatorsOf(final Challenge challenge) {
        return withReadOnlyTransaction(new F.Function0<List<User>>() {
            @Override
            public List<User> apply() throws Throwable {
                return challengesRepository.getAllParticipatorsOf(challenge);
            }
        });
    }

    public ChallengeParticipation participateInChallenge(final Challenge challenge, final String participatorUsername, final String participatorName) {
        if (isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
        }

        ChallengeParticipation challengeParticipation = withTransaction(new F.Function0<ChallengeParticipation>() {
            @Override
            public ChallengeParticipation apply() throws Throwable {
                User participator = usersRepository.getUser(participatorUsername);
                Challenge refreshedChallenge = challengesRepository.getChallenge(challenge.getId());
                return challengesRepository.persistChallengeParticipation(new ChallengeParticipation(refreshedChallenge, participator));
            }
        });

        if(hasChallengeBecamePopular(challenge)) {
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
     *
     */
    public boolean hasChallengeBecamePopular(final Challenge challenge) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.getNrOfParticipationsOf(challenge) == POPULARITY_INDICATOR;
            }
        });
    }

    public Boolean leaveChallenge(final Challenge challenge, final String participatorUsername, String participatorName) {

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

        notificationService.notifyAboutChallengeLeaving(challenge, participatorUsername, participatorName, findAllParticipatorsOf(challenge));

        return challengeRemovalResult;
    }

    public boolean isUserParticipatingInChallenge(final Challenge challenge, final String user) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isUserParticipatingInChallenge(challenge, user);
            }
        });
    }

    public boolean isUserRespondedToChallenge(final Challenge challenge, final String user) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isUserRespondedToChallenge(challenge, user);
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

    public boolean isUserCreatedAChallenge(final Long challengeId, final String creator) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isUserCreatedAChallenge(challengeId, creator);
            }
        });
    }

    public ChallengeResponse submitChallengeResponse(final ChallengeParticipation challengeParticipation, final String message, final String videoDescriptionUrl) {
        assertThatResponseCanBeSubmittedForParticipation(challengeParticipation);
        ChallengeResponse challengeResponse = withTransaction(new F.Function0<ChallengeResponse>() {

            @Override
            public ChallengeResponse apply() throws Throwable {
                ChallengeResponse challengeResponse = new ChallengeResponse(challengeParticipation, videoDescriptionUrl, message);
                return challengesRepository.addChallengeResponse(challengeResponse);
            }
        });

        notificationService.notifyAboutSubmittingChallengeResponse(challengeParticipation, findAllParticipatorsOf(challengeParticipation.getChallenge()));

        return challengeResponse;
    }

    private void assertThatResponseCanBeSubmittedForParticipation(ChallengeParticipation challengeParticipation) {
        if (isNotEvaluatedResponseExistsFor(challengeParticipation)) {
            throw new IllegalStateException("User " + challengeParticipation.getParticipator() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        }else if(challengeParticipation.isOverdue()){
            throw new IllegalStateException(challengeParticipation.getChallenge().getChallengeName() + "has ended on " + challengeParticipation.getChallenge().getEndingDate());
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

        ChallengeParticipation challengeParticipation = acceptedResponse.getChallengeParticipation();
        notificationService.notifyAboutChallengeResponseAcceptance(challengeParticipation, findAllParticipatorsOf(challengeParticipation.getChallenge()));
        return acceptedResponse;
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

    public List getChallengeParticipationsWithParticipantsNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<List>() {
                @Override
                public List apply() throws Throwable {
                    return challengesRepository.getChallengeParticipationsWithParticipantsNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List getLatestChallengeParticipationsWithParticipantsNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<List>() {
                @Override
                public List apply() throws Throwable {
                    return challengesRepository.getLastestParticipationsWithParticipantsNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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

    public List getLatestChallengesWithParticipantsNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<List>() {
                @Override
                public List apply() throws Throwable {
                    return challengesRepository.getLatestChallengesWithParticipantsNrForUser(username);
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
            return withReadOnlyTransaction(new F.Function0<List<ChallengeResponse>>() {
                @Override
                public List<ChallengeResponse> apply() throws Throwable {
                    return challengesRepository.getResponsesForChallenge(challengeId);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long getResponsesNrForChallenge(final long challengeId){
        try {
            return withReadOnlyTransaction(new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.getResponsesNrForChallenge(challengeId);
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

    public List<ChallengeResponse> getChallengeParticipationsForUser(final String participatorUsername) {
        try {
            return withReadOnlyTransaction(new F.Function0<List<ChallengeResponse>>() {
                @Override
                public List<ChallengeResponse> apply() throws Throwable {
                    return challengesRepository.getChallengeParticipationsForUser(participatorUsername);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List<Challenge> getCompletedChallenges(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<List<Challenge>>() {
                @Override
                public List<Challenge> apply() throws Throwable {
                    return challengesRepository.getCompletedChallenges(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List<ChallengeParticipation> getParticipantsForChallenge(final long challengeId){
        try {
            return withReadOnlyTransaction(new F.Function0<List<ChallengeParticipation>>() {
                @Override
                public List<ChallengeParticipation> apply() throws Throwable {
                    return challengesRepository.getParticipantsForChallenge(challengeId);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long getCreatedChallengesNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.getCreatedChallengesNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long getCompletedChallengesNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.getCompletedChallengesNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long getJoinedChallengesNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.getJoinedChallengesNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long getResponsesNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.getResponsesNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long getAcceptedResponsesNrForUser(final String username){
        try {
            return withReadOnlyTransaction(new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.getAcceptedResponsesNrForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Challenge updateChallenge(final Challenge challenge) {
        return withTransaction(new F.Function0<Challenge>() {
            @Override
            public Challenge apply() throws Throwable {
                return challengesRepository.updateChallenge(challenge);
            }
        });
    }
}
