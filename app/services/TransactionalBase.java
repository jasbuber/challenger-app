package services;

import play.db.jpa.JPA;
import play.libs.F;

/**
 * Created for now to be able to decouple transaction logic from business logic in servcies.
 * Not good solution as the base class but for now must be enough -> to be changed to annotated solution (Spring?)
 */
public class TransactionalBase {

    private static final boolean READ_ONLY = true;
    private static final String TRANSACTION_NAME = "default";


    protected <T> T withTransaction(F.Function0<T> function) {
        try {
            return JPA.withTransaction(function);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    protected <T> T withReadOnlyTransaction(F.Function0<T> function) {
        try {
            return JPA.withTransaction(TRANSACTION_NAME, READ_ONLY, function);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
