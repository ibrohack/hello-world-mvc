package tartanga.dami2.din.helloworldmvc.dao;

import tartanga.dami2.din.helloworldmvc.exception.DaoException;
import tartanga.dami2.din.helloworldmvc.exception.InvalidCredentialsException;
import tartanga.dami2.din.helloworldmvc.model.User;

/**
 * Defines the operations of the data access layer for {@link User} objects.
 *
 * <p>The user interface layer only depends on this interface, never on a specific
 * implementation. On each run, {@link DaoFactory} creates the implementation selected
 * with the {@code dao.implementation} key of {@code config.properties}:
 * {@link UserDaoDbImpl} or {@link UserDaoFileImpl}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public interface UserDao {

    /**
     * Returns the user whose login and password match the given credentials.
     *
     * <p>The login is compared ignoring case. The same exception is thrown when the
     * login does not exist and when the password is wrong, so callers cannot find out
     * which logins exist.
     *
     * @param login    the login typed by the user
     * @param password the password typed by the user, in plain text
     * @return the user identified by the credentials
     * @throws DaoException                if the data store cannot be accessed or contains invalid data
     * @throws InvalidCredentialsException if no user matches the login and password
     */
    User getUser(String login, String password) throws DaoException, InvalidCredentialsException;

    /**
     * Returns a short name of the data store used by this implementation. The user
     * interface shows it so that users can see where their data comes from.
     *
     * @return the name of the data store, for example {@code "MySQL database"}
     */
    String getDataSourceName();
}
