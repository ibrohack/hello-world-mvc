package tartanga.dami2.din.helloworldmvc.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a user of the application. It is the data model shared by the user
 * interface layer and the data access layer.
 *
 * <p>Instances are immutable. The password is deliberately not part of the model:
 * credentials are checked inside the data access layer, so the password hash never
 * reaches the user interface.
 *
 * @author Aritz Navarro
 * @author Brayan Romero
 * @author Ekaitz Rivero
 * @version 1.0.0
 */
public final class User {

    /** Unique name used to sign in, always stored in lowercase. */
    private final String login;

    /** First name of the user. */
    private final String firstName;

    /** Last name (surname) of the user. */
    private final String lastName;

    /** Email address of the user. */
    private final String email;

    /** Date of birth of the user. */
    private final LocalDate birthDate;

    /**
     * Creates a user with the given data.
     *
     * @param login     unique name used to sign in
     * @param firstName first name of the user
     * @param lastName  last name of the user
     * @param email     email address of the user
     * @param birthDate date of birth of the user
     * @throws NullPointerException if any argument is {@code null}
     */
    public User(String login, String firstName, String lastName, String email, LocalDate birthDate) {
        this.login = Objects.requireNonNull(login, "login");
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        this.email = Objects.requireNonNull(email, "email");
        this.birthDate = Objects.requireNonNull(birthDate, "birthDate");
    }

    /**
     * Returns the unique name used to sign in.
     *
     * @return the login, in lowercase
     */
    public String getLogin() {
        return login;
    }

    /**
     * Returns the first name of the user.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name (surname) of the user.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the full name of the user: the first name followed by the last name.
     *
     * @return the full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the email address of the user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the date of birth of the user.
     *
     * @return the date of birth
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Compares this user with another object. Two users are equal when they have
     * the same login, because the login identifies a user.
     *
     * @param obj the object to compare with
     * @return {@code true} if {@code obj} is a user with the same login
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof User other && login.equals(other.login);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}, based on the login.
     *
     * @return the hash code of the login
     */
    @Override
    public int hashCode() {
        return login.hashCode();
    }

    /**
     * Returns a short description of the user, meant for logs and debugging.
     *
     * @return the login and the full name of the user
     */
    @Override
    public String toString() {
        return "User[login=" + login + ", fullName=" + getFullName() + "]";
    }
}
