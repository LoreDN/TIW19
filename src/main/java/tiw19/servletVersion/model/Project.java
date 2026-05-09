package tiw19.servletVersion.model;

/**
 * Project Model Representation.
 * <br>This Class works as the Project Model from the PROJECTS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class Project {
    private final int ID;
    private final String title;
    private final int monthsNumber;
    private final String admin;
    private final String status; // CREATED, ASSIGNED, CONCLUDED

    /**
     * Constructor of the Project.
     *
     * @param ID            ID of the Project;
     * @param title         Title of the Project.
     * @param monthsNumber  Duration (in months) of the Project.
     * @param admin         Amin of the project.
     * @param status        Status of the Project (CREATED vs ASSIGNED vs CONCLUDED).
     */
    public Project(int ID, String title, int monthsNumber, String admin, String status) {
        this.ID = ID;
        this.title = title;
        this.monthsNumber = monthsNumber;
        this.admin = admin;
        this.status = status;
    }

    /**
     * Getter for the attribute ID.
     *
     * @return Project.ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Getter for the attribute title.
     *
     * @return Project.title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the attribute monthsNumber.
     *
     * @return Project.monthsNumber
     */
    public int getMonthsNumber() {
        return monthsNumber;
    }

    /**
     * Getter for the attribute admin.
     *
     * @return Project.admin
     */
    public String getAdmin() {
        return admin;
    }

    /**
     * Check if the Project status is ASSIGNED.
     *
     * @return status == ASSIGNED ?
     */
    public boolean isAssigned() {
        return status.equals("ASSIGNED");
    }

    /**
     * Check if the Project status is CONCLUDED.
     *
     * @return status == CONCLUDED ?
     */
    public boolean isConcluded() {
        return status.equals("CONCLUDED");
    }
}