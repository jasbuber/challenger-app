package integration;

import domain.User;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.F;
import repositories.UsersRepository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static play.test.Helpers.*;

public class GettingUserFromRepositoryTest {

    private UsersRepository usersRepository = new UsersRepository();

    private void runForFakeApplicationWithInMemoryDb(final F.Function0<?> function) {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
            @Override
            public void run() {
                try {
                    JPA.withTransaction(function);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        });
    }

    @Test
    public void shouldThrowExceptionWhenNoUserWithUsernameInDb() throws Throwable {
        runForFakeApplicationWithInMemoryDb(new F.Function0<User>() {
                                                @Override
                                                public User apply() throws Throwable {
                                                    try {
                                                        User user = usersRepository.getUser("username");
                                                        fail();

                                                    } catch (IllegalStateException exc) {

                                                    }
                                                    return null;
                                                }
                                            }
        );
    }

    @Test
    public void shouldAddAndGetUser() throws Throwable {
        running(fakeApplication(inMemoryDatabase()), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JPA.withTransaction(new F.Callback0() {

                                @Override
                                public void invoke() throws Throwable {
                                    usersRepository.createUser("user");
                                }
                            });
                            User user = JPA.withTransaction(new F.Function0<User>() {

                                @Override
                                public User apply() throws Throwable {
                                    return usersRepository.getUser("user");
                                }
                            });

                            assertNotNull(user);

                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    }
                }
        );
    }

}
