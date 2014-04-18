package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.User;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.F;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.ArrayList;
import java.util.List;

public class ChallengeService {

    private static final boolean READ_ONLY = true;

    private final ChallengesRepository challengesRepository;
    private final UsersRepository usersRepository;
    private final NotificationService notificationService;

    public ChallengeService(ChallengesRepository challengesRepository, UsersRepository usersRepository, NotificationService notificationService) {
        this.challengesRepository = challengesRepository;
        this.usersRepository = usersRepository;
        this.notificationService = notificationService;
    }

    public Challenge createChallenge(final String creatorUsername, final String challengeName) {
        if(isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }

        return createAndPersistChallenge(creatorUsername, challengeName);
    }

    private Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName) {
        try {
            return JPA.withTransaction(new F.Function0<Challenge>() {
                @Override
                public Challenge apply() throws Throwable {
                    User creator = usersRepository.getUser(creatorUsername);
                    return challengesRepository.createChallenge(creator, challengeName);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public ChallengeParticipation participateInChallenge(Challenge challenge, String participatorUsername) {
        if(isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
        }
        User participator = usersRepository.getUser(participatorUsername);
        return challengesRepository.createChallengeParticipation(challenge, participator);
    }

    public boolean isUserParticipatingInChallenge(final Challenge challenge, final String user) {
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<Boolean>() {
                @Override
                public Boolean apply() throws Throwable {
                    return challengesRepository.isUserParticipatingInChallenge(challenge, user);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public boolean isUserCreatedChallengeWithName(final String challengeName, final String creator) {
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<Boolean>() {
                @Override
                public Boolean apply() throws Throwable {
                    return challengesRepository.isChallengeWithGivenNameExistsForUser(challengeName, creator);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public ChallengeResponse submitChallengeResponse(ChallengeParticipation challengeParticipation) {
        assertThatResponseCanBeSubmittedForParticipation(challengeParticipation);
        ChallengeResponse challengeResponse = challengesRepository.addChallengeResponse(challengeParticipation);
        notifyCreator(challengeParticipation.getCreator());
        notifyOtherChallengeParticipators(challengeParticipation.getChallenge());
        return challengeResponse;
    }

    private void notifyCreator(User creator) {
        notificationService.notifyUser(creator);
    }

    private void notifyOtherChallengeParticipators(Challenge challenge) {
        List<User> otherChallengeParticipators = usersRepository.getParticipatorsFor(challenge);
        notificationService.notifyUsers(otherChallengeParticipators);
    }

    private void assertThatResponseCanBeSubmittedForParticipation(ChallengeParticipation challengeParticipation) {
        if(isNotScoredResponseExistsFor(challengeParticipation)) {
            throw new IllegalStateException("User " + challengeParticipation.getParticipator() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        }
    }

    public boolean isNotScoredResponseExistsFor(final ChallengeParticipation challengeParticipation) {
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<Boolean>() {
                @Override
                public Boolean apply() throws Throwable {
                    return challengesRepository.isNotScoredChallengeResponseExistsFor(challengeParticipation);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public ChallengeParticipation getChallengeParticipation(final Challenge challenge, final String participatorUsername) {
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<ChallengeParticipation>() {
                @Override
                public ChallengeParticipation apply() throws Throwable {
                    return challengesRepository.getChallengeParticipation(challenge, participatorUsername);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List<Challenge> findChallenges(ChallengeFilter challengeFilter) {
        return new ArrayList<Challenge>();
    }

    public Challenge getChallenge(final long id){
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<Challenge>() {
                @Override
                public Challenge apply() throws Throwable {
                    return challengesRepository.getChallenge(id);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }
}
