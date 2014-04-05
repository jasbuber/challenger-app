package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import domain.User;
import repositories.ChallengesRepository;

public class ChallengeService {

    private final ChallengesRepository challengesRepository;

    public ChallengeService(ChallengesRepository challengesRepository) {
        this.challengesRepository = challengesRepository;
    }

    public Challenge createChallenge(User creator, String challengeName) {
        if(isUserCreatedChallengeWithName(challengeName, creator)) {
            throw new IllegalStateException("Challenge with given name: " + challengeName +
                    " has already been created by user " + creator);
        }
        return challengesRepository.createChallenge(creator, challengeName);
    }

    public ChallengeParticipation participateInChallenge(Challenge challenge, User participator) {
        if(isUserParticipatingInChallenge(challenge, participator)) {
            throw new IllegalStateException("User " + participator + " is participating in challenge " + challenge);
        }
        return challengesRepository.createChallengeParticipation(challenge, participator);
    }

    public boolean isUserParticipatingInChallenge(Challenge challenge, User user) {
        return challengesRepository.isUserParticipatingInChallenge(challenge, user);
    }

    public boolean isUserCreatedChallengeWithName(String challengeName, User creator) {
        return challengesRepository.isChallengeWithGivenNameExistsForUser(challengeName, creator);
    }
}
