package integration;

import domain.User;
import org.junit.After;
import org.junit.Test;
import play.db.jpa.JPA;
import repositories.UsersRepository;

import static org.junit.Assert.assertNotNull;

public class GettingUserFromRepositoryTest extends EmTestsBase {

    private UsersRepository usersRepository = new UsersRepository();

    @After
    public void tearDown() {
        clearDatabase();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenNoUserWithUsernameInDb() throws Throwable {
       openTransaction();
       usersRepository.getUser("username");
       closeTransaction();
    }

    @Test
    public void shouldAddAndGetUser() throws Throwable {
        openTransaction();
        usersRepository.createUser("user");
        closeTransaction();

        openTransaction();
        User user = usersRepository.getUser("user");
        closeTransaction();

        assertNotNull(user);
    }

}
