package integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import play.db.jpa.JPA;
import play.test.FakeApplication;
import play.test.Helpers;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class EmTestsBase {


    private static FakeApplication app;
    EntityManager em = null;
    EntityTransaction tx = null;

    @BeforeClass
    public static void beforeEachTestsSuite() {
        startFakeApp();
    }

    @AfterClass
    public static void afterEachTestsSuite() {
        stopFakeApp();
    }

    private static void startFakeApp() {
        app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
        Helpers.start(app);
    }

    private static void stopFakeApp() {
        Helpers.stop(app);
    }


    public void openTransaction() {
        em = JPA.em("default");
        JPA.bindForCurrentThread(em);
        tx = em.getTransaction();
        tx.begin();
    }

    public void closeTransaction() {
        if (tx != null) {
            if (tx.isActive()) {
                if (tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }
            }

        }
        JPA.bindForCurrentThread(null);
        if (em != null) {
            em.close();
        }
    }

}
