package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.ChallengeResponse;
import domain.User;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

public class ChallengeService {

    private final ChallengesRepository challengesRepository;
    private final UsersRepository usersRepository;

    public ChallengeService(ChallengesRepository challengesRepository, UsersRepository usersRepository) {
        this.challengesRepository = challengesRepository;
        this.usersRepository = usersRepository;
    }

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
        return challengesRepository.addChallengeResponse(challengeParticipation);
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
}
