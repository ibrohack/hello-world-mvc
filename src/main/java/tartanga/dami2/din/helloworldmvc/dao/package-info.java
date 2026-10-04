/**
 * Contains the data access layer (DAO) of the application.
 *
 * <p>{@link UserDao} defines the operations used by the user interface layer.
 * {@link UserDaoDbImpl} implements them for a MySQL database and {@link UserDaoFileImpl}
 * for a properties file. {@link DaoFactory} creates the implementation selected in the
 * {@code config.properties} file, which is loaded and validated by {@link DaoConfig}.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
package tartanga.dami2.din.helloworldmvc.dao;
