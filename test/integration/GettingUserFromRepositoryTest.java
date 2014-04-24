package integration;

import domain.User;
import org.junit.Test;
import repositories.UsersRepository;

import static org.junit.Assert.assertNotNull;

public class GettingUserFromRepositoryTest extends EmTestsBase {

    private UsersRepository usersRepository = new UsersRepository();

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
