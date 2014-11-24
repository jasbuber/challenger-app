package services;

import domain.User;
import integration.EmTestsBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import repositories.UsersRepository;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class UserServiceMainApiTest {

    private static final String EXISTING_USER_USERNAME = "existingUser";
    private static final User EXISTING_USER = new User(EXISTING_USER_USERNAME);

    private final UsersRepository usersRepository = new UserRepositoryStub();
    private final UserService userService = new UserServiceWithoutTransactionMgmt(usersRepository);


    @Test
    public void shouldCreateNewUser() throws Exception {
        //given
        String username = "username";

        //when
        User newUser = userService.createNewOrGetExistingUser(username, null, null, null);

        //then
        assertTrue(!newUser.equals(EXISTING_USER));
    }

    @Test
    public void shouldNotCreateNewUserIfOneWithUsernameAlreadyExists() throws Exception {
        //when
        User returnedExistingUser = userService.createNewOrGetExistingUser(EXISTING_USER_USERNAME, null, null, null);

        //then
        assertEquals(EXISTING_USER, returnedExistingUser);
    }

    private static class UserRepositoryStub extends UsersRepository {

        @Override
        public User createUser(String username) {
            return new User(username);
        }

        @Override
        public boolean isUserExist(String username) {
            return EXISTING_USER_USERNAME.equals(username);
        }

        @Override
        public User getUser(String username) {
            return EXISTING_USER;
        }
    }

    private static class UserServiceWithoutTransactionMgmt extends UserService {

        public UserServiceWithoutTransactionMgmt(UsersRepository usersRepository) {
            super(usersRepository);
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
}
