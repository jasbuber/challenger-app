package services;

import domain.*;
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

    public Challenge createChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        if(isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }

        return createAndPersistChallenge(creatorUsername, challengeName, category, videoId, visibility);
    }

    private Challenge createAndPersistChallenge(final String creatorUsername, final String challengeName, final ChallengeCategory category, final String videoId, final Boolean visibility) {
        try {
            return JPA.withTransaction(new F.Function0<Challenge>() {
                @Override
                public Challenge apply() throws Throwable {
                    User creator = usersRepository.getUser(creatorUsername);
                    return challengesRepository.createChallenge(creator, challengeName, category, videoId, visibility);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public ChallengeParticipation participateInChallenge(final Challenge challenge, final String participatorUsername) {

        try {
            if(isUserParticipatingInChallenge(challenge, participatorUsername)) {
                throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
            }
            return JPA.withTransaction(new F.Function0<ChallengeParticipation>() {
                @Override
                public ChallengeParticipation apply() throws Throwable {
                    User participator = usersRepository.getUser(participatorUsername);
                    return challengesRepository.createChallengeParticipation(challenge, participator);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Boolean leaveChallenge(final Challenge challenge, final String participatorUsername) {

        try {
            if(!isUserParticipatingInChallenge(challenge, participatorUsername)) {
                throw new IllegalStateException("User " + participatorUsername + " is not participating in challenge " + challenge);
            }
            return JPA.withTransaction(new F.Function0<Boolean>() {
                @Override
                public Boolean apply() throws Throwable {
                    User participator = usersRepository.getUser(participatorUsername);
                    return challengesRepository.deleteChallengeParticipation(challenge, participator);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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

    public ChallengeResponse submitChallengeResponse(final ChallengeParticipation challengeParticipation) {

        try {
            return JPA.withTransaction(new F.Function0<ChallengeResponse>() {

                @Override
                public ChallengeResponse apply() throws Throwable {
                    assertThatResponseCanBeSubmittedForParticipation(challengeParticipation);
                    ChallengeResponse challengeResponse = challengesRepository.addChallengeResponse(challengeParticipation);
                    notifyCreator(challengeParticipation.getCreator());
                    notifyOtherChallengeParticipators(challengeParticipation.getChallenge());
                    return challengeResponse;
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Boolean notifyCreator(final User creator) {
        try {
            return JPA.withTransaction(new F.Function0<Boolean>() {

                @Override
                public Boolean apply() throws Throwable {
                    notificationService.notifyUser(creator);
                    return true;
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private Boolean notifyOtherChallengeParticipators(final Challenge challenge) {

        try {
            return JPA.withTransaction(new F.Function0<Boolean>() {

                @Override
                public Boolean apply() throws Throwable {
                    List<User> otherChallengeParticipators = usersRepository.getParticipatorsFor(challenge);
                    notificationService.notifyUsers(otherChallengeParticipators);
                    return true;
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void assertThatResponseCanBeSubmittedForParticipation(ChallengeParticipation challengeParticipation) {
        if(isNotScoredResponseExistsFor(challengeParticipation)) {
            throw new IllegalStateException("User " + challengeParticipation.getParticipator() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        }
    }

    public boolean isNotScoredResponseExistsFor(final ChallengeParticipation challengeParticipation) {
        return challengesRepository.isNotScoredChallengeResponseExistsFor(challengeParticipation);
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

    public List<Challenge> findChallenges(final ChallengeFilter challengeFilter) {

        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<List<Challenge>>() {
                @Override
                public List<Challenge> apply() throws Throwable {
                    return challengesRepository.findChallenges(challengeFilter);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
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

    public ChallengeResponse acceptChallengeResponse(final ChallengeResponse challengeResponse) {
        try {
            return JPA.withTransaction(new F.Function0<ChallengeResponse>() {

                @Override
                public ChallengeResponse apply() throws Throwable {
                    assertThatResponseIsNotDecidedYet(challengeResponse);

                    challengeResponse.accept();
                    challengesRepository.updateChallengeResponse(challengeResponse);
                    return challengeResponse;
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public ChallengeResponse refuseChallengeResponse(final ChallengeResponse challengeResponse) {
        try {
            return JPA.withTransaction(new F.Function0<ChallengeResponse>() {

                @Override
                public ChallengeResponse apply() throws Throwable {
                    assertThatResponseIsNotDecidedYet(challengeResponse);

                    challengeResponse.refuse();
                    challengesRepository.updateChallengeResponse(challengeResponse);
                    return challengeResponse;
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private void assertThatResponseIsNotDecidedYet(ChallengeResponse challengeResponse) {
        if(challengeResponse.isDecided()) {
            throw new IllegalStateException("ChallengeResponse id: " + challengeResponse.getId() + " cannot be decided more than once");
        }
    }

    public Long countCreatedChallengesForUser(final String username){
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.countCreatedChallengesForUser(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public Long countCompletedChallenges(final String username){
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<Long>() {
                @Override
                public Long apply() throws Throwable {
                    return challengesRepository.countCompletedChallenges(username);
                }
            });
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public List getChallengesWithParticipantsNrForUser(final String username){
        try {
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<List>() {
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
            return JPA.withTransaction(new F.Function0<Challenge>() {
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
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<List>() {
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
            return JPA.withTransaction("default", READ_ONLY, new F.Function0<ChallengeResponse>() {
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
