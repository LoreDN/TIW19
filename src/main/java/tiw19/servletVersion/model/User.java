package tiw19.servletVersion.model;

/**
 * User Model Representation.
 * <br>This Class works as the User Model from the USERS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class User {
    private final String name;
    private final String surname;
    private final String username;
    private final String photo;
    private final String role;  // ADMIN, MANAGER, COLLABORATOR, MANAGER_COLLABORATOR

    /**
     * Constructor of the User.
     *
     * @param name Name of the User.
     * @param surname Surname of the User.
     * @param username Username of the User.
     * @param photo Path to the User profile picture.
     * @param role Role of the User (ADMIN vs TECHNICAL STUFF)
     */
    public User(String name, String surname, String username, String photo, String role) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.photo = photo;
        this.role = role;
    }

    /**
     * Getter for the attribute name.
     *
     * @return User.name
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the attribute surname.
     *
     * @return User.surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Getter for the attribute username.
     *
     * @return User.username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Getter for the attribute photo.
     *
     * @return User.photo
     */
    public String getPhoto() {
        return photo;
    }

    /**
     * Check if the User is an ADMIN.
     *
     * @return role == ADMIN ?
     */
    public boolean isAdmin() {
        return role.equals("ADMIN");
    }

    /**
     * Check if the User is an MANAGER.
     *
     * @return role == MANAGER ?
     */
    public boolean isManager() {
        return role.equals("MANAGER") || role.equals("MANAGER_COLLABORATOR");
    }

    /**
     * Check if the User is an COLLABORATOR.
     *
     * @return role == COLLABORATOR ?
     */
    public boolean isCollaborator() {
        return role.equals("COLLABORATOR") || role.equals("MANAGER_COLLABORATOR");
    }
}