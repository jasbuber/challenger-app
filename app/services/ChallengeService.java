package services;

import domain.Challenge;
import domain.ChallengeParticipation;
import repositories.ChallengesRepository;

public class ChallengeService {

    private final ChallengesRepository challengesRepository;

    public ChallengeService(ChallengesRepository challengesRepository) {
        this.challengesRepository = challengesRepository;
    }

    public Challenge createChallenge() {
        return challengesRepository.createChallenge();
    }

    public ChallengeParticipation participateInChallenge(Challenge challenge) {
        return new ChallengeParticipation(challenge, user);
    }
}
