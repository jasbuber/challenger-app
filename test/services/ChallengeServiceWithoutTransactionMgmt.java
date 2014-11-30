package services;

import play.libs.F;
import repositories.ChallengesRepository;
import repositories.UsersRepository;

public class ChallengeServiceWithoutTransactionMgmt extends ChallengeService {

    public ChallengeServiceWithoutTransactionMgmt(ChallengesRepository challengesRepository, UsersRepository usersRepository,
                                                  ChallengeNotificationsService challengeNotificationsService) {
        super(challengesRepository, null, challengeNotificationsService, null);
    }

    @Override
    protected <T> T withTransaction(F.Function0<T> function) {
        try {
            return function.apply();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    @Override
    protected <T> T withReadOnlyTransaction(F.Function0<T> function) {
        return withTransaction(function);
    }
}
