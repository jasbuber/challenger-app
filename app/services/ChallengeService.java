package services;

import domain.Challenge;
import domain.ChallengeParticipation;
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
}
