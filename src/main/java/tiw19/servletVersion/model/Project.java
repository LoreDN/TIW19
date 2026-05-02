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
    private final String title;
    private final int monthsNumber;
    private final String status; // CREATED, ASSIGNED, CONCLUDED

    /**
     * Constructor of the Project.
     *
     * @param title         Title of the Project.
     * @param monthsNumber  Duration (in months) of the Project.
     * @param status        Status of the Project (CREATED vs ASSIGNED vs CONCLUDED).
     */
    public Project(String title, int monthsNumber, String status) {
        this.title = title;
        this.monthsNumber = monthsNumber;
        this.status = status;
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

    /**
     * @see Object Equals method override for Project attributes.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Project && (
                title.equals(((Project) obj).title) &&
                monthsNumber == ((Project) obj).monthsNumber &&
                status.equals(((Project) obj).status)
        ));
    }
}