package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.User;
import play.db.jpa.Transactional;
import repositories.ChallengeFilter;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

import java.util.ArrayList;
import java.util.List;

public class ChallengeService {

    private final ChallengesRepository challengesRepository;
    private final UsersRepository usersRepository;
    private final NotificationService notificationService;

    public ChallengeService(ChallengesRepository challengesRepository, UsersRepository usersRepository, NotificationService notificationService) {
        this.challengesRepository = challengesRepository;
        this.usersRepository = usersRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Challenge createChallenge(String creatorUsername, String challengeName) {
        if(isUserCreatedChallengeWithName(challengeName, creatorUsername)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creatorUsername);
        }
        User creator = usersRepository.getUser(creatorUsername);
        return challengesRepository.createChallenge(creator, challengeName);
    }

    public ChallengeParticipation participateInChallenge(Challenge challenge, String participatorUsername) {
        if(isUserParticipatingInChallenge(challenge, participatorUsername)) {
            throw new IllegalStateException("User " + participatorUsername + " is participating in challenge " + challenge);
        }
        User participator = usersRepository.getUser(participatorUsername);
        return challengesRepository.createChallengeParticipation(challenge, participator);
    }

    public boolean isUserParticipatingInChallenge(Challenge challenge, String user) {
        return challengesRepository.isUserParticipatingInChallenge(challenge, user);
    }

    public boolean isUserCreatedChallengeWithName(String challengeName, String creator) {
        return challengesRepository.isChallengeWithGivenNameExistsForUser(challengeName, creator);
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
            throw new IllegalStateException("User " + challengeParticipation.getUser() + " has already submitted response that is not scored yet for challenge " + challengeParticipation.getChallenge());
        }
    }

    public boolean isNotScoredResponseExistsFor(ChallengeParticipation challengeParticipation) {
        return challengesRepository.isNotScoredChallengeResponseExistsFor(challengeParticipation);
    }

    public ChallengeParticipation getChallengeParticipation(Challenge challenge, String participatorUsername) {
        return challengesRepository.getChallengeParticipation(challenge, participatorUsername);
    }

    public List<Challenge> findChallenges(ChallengeFilter challengeFilter) {
        return new ArrayList<Challenge>();
    }
}
