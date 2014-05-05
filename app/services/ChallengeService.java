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

        return createAndPersistChallenge(creatorUsername, challengeName, category, videoId, visibility);
    }

    private Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        return withTransaction(new F.Function0<Challenge>() {
            @Override
            public Challenge apply() throws Throwable {
                User creator = usersRepository.getUser(creatorUsername);
                return challengesRepository.createChallenge(creator, challengeName, category, videoId, visibility);
            }
        });
    }

    private void notifyChallengeCreator(Challenge challenge) {
        notificationService.notifyUser(challenge.getCreator());
    }

    private void notifyAllParticipators(final Challenge challenge) {
        List<User> participators = findAllParticipatorsOf(challenge);
        notificationService.notifyUsers(participators);
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
                return challengesRepository.createChallengeParticipation(challenge, participator);
            }
        });

        notifyChallengeCreator(challenge);
        notifyAllParticipators(challenge);

        return challengeParticipation;
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

        notifyChallengeCreator(challenge);
        notifyAllParticipators(challenge);

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

        notifyChallengeCreator(challengeParticipation.getChallenge());
        notifyAllParticipators(challengeParticipation.getChallenge());

        return challengeResponse;
    }

    private void assertThatResponseCanBeSubmittedForParticipation(ChallengeParticipation challengeParticipation) {
        if (isNotScoredResponseExistsFor(challengeParticipation)) {
            throw new IllegalStateException("User " + challengeParticipation.getParticipator() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        }
    }

    public boolean isNotScoredResponseExistsFor(final ChallengeParticipation challengeParticipation) {
        return withReadOnlyTransaction(new F.Function0<Boolean>() {
            @Override
            public Boolean apply() throws Throwable {
                return challengesRepository.isNotScoredChallengeResponseExistsFor(challengeParticipation);
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
        return withTransaction(new F.Function0<ChallengeResponse>() {

            @Override
            public ChallengeResponse apply() throws Throwable {
                assertThatResponseIsNotDecidedYet(challengeResponse);

                challengeResponse.accept();
                challengesRepository.updateChallengeResponse(challengeResponse);
                return challengeResponse;
            }
        });
    }

    public ChallengeResponse refuseChallengeResponse(final ChallengeResponse challengeResponse) {
        return withTransaction(new F.Function0<ChallengeResponse>() {

            @Override
            public ChallengeResponse apply() throws Throwable {
                assertThatResponseIsNotDecidedYet(challengeResponse);

                challengeResponse.refuse();
                challengesRepository.updateChallengeResponse(challengeResponse);
                return challengeResponse;
            }
        });
    }

    private void assertThatResponseIsNotDecidedYet(ChallengeResponse challengeResponse) {
        if (challengeResponse.isDecided()) {
            throw new IllegalStateException("ChallengeResponse id: " + challengeResponse.getId() + " cannot be decided more than once");
        }
    }
}
