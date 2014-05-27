package services;

import play.libs.F;
import repositories.InternalNotificationsRepository;

public class InternalNotificationServiceWithoutTransactionMgmt extends InternalNotificationService {

    public InternalNotificationServiceWithoutTransactionMgmt(InternalNotificationsRepository internalNotificationsRepository) {
        super(internalNotificationsRepository);
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
