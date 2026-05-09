package tiw19.servletVersion.model;

/**
 * Work-Package Model Representation.
 * <br>This Class works as the Work-Package Model from the WORK_PACKAGES Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class WorkPackage {
    private final int ID;
    private final int orderNumber;
    private final String title;
    private final int startingMonth;
    private final int endingMonth;
    private final Project project;

    /**
     * Constructor of the Work-Package.
     *
     * @param ID            ID of the Work-Package.
     * @param orderNumber   Order Number of the Work-Package.
     * @param title         Title of the Work-Package.
     * @param startingMonth Starting month of the Work-Package.
     * @param endingMonth   Ending month of the Work-Package.
     * @param project       Reference Project.
     */
    public WorkPackage(int ID, int orderNumber, String title, int startingMonth, int endingMonth,  Project project) {
        this.ID = ID;
        this.orderNumber = orderNumber;
        this.title = title;
        this.startingMonth = startingMonth;
        this.endingMonth = endingMonth;
        this.project = project;
    }

    /**
     * Getter for the attribute ID.
     *
     * @return WorkPackage.ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Getter for the attribute orderNumber.
     *
     * @return WorkPackage.orderNumber
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    /**
     * Getter for the attribute title.
     *
     * @return WorkPackage.title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the attribute startingMonth.
     *
     * @return WorkPackage.startingMonth
     */
    public int getStartingMonth() {
        return startingMonth;
    }

    /**
     * Getter for the attribute endingMonth.
     *
     * @return WorkPackage.endingMonth
     */
    public int getEndingMonth() {
        return endingMonth;
    }

    /**
     * Getter for the attribute project.
     *
     * @return Reference Project for the Work-Package
     */
    public Project getProject() {
        return project;
    }

    // ----- Object Overrides

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WorkPackage workPackage)) return false;
        return this.ID == workPackage.ID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.ID);
    }
}
