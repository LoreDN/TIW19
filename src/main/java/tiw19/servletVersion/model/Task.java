package tiw19.servletVersion.model;

public class Task {
    private final int ID;
    private final float orderNumber;
    private final String title;
    private final int startingMonth;
    private final int endingMonth;
    private final Project project;
    private final WorkPackage workPackage;

    /**
     * Constructor of the Task.
     *
     * @param ID            ID of the Task.
     * @param OrderNumber   Order Number of the Task.
     * @param title         Title of the Task.
     * @param startingMonth Starting month of the Task.
     * @param endingMonth   Ending month of the Task.
     * @param project       Reference Project.
     * @param workPackage   Reference Work-Package.
     */
    public Task(int ID, float OrderNumber, String title, int startingMonth, int endingMonth, Project project, WorkPackage workPackage) {
        this.ID = ID;
        this.orderNumber = OrderNumber;
        this.title = title;
        this.startingMonth = startingMonth;
        this.endingMonth = endingMonth;
        this.project = project;
        this.workPackage = workPackage;
    }

    /**
     * Getter for the attribute ID.
     *
     * @return Task.ID
     */
    public int getID() {
        return ID;
    }

    /**
     * Getter for the attribute orderNumber.
     *
     * @return Task.orderNumber
     */
    public float getOrderNumber() {
        return orderNumber;
    }

    /**
     * Getter for the attribute title.
     *
     * @return Task.title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the attribute startingMonth.
     *
     * @return Task.startingMonth
     */
    public int getStartingMonth() {
        return startingMonth;
    }

    /**
     * Getter for the attribute endingMonth.
     *
     * @return Task.endingMonth
     */
    public int getEndingMonth() {
        return endingMonth;
    }

    /**
     * Getter for the attribute project.
     *
     * @return Reference Project for the Task
     */
    public Project getProject() {
        return project;
    }

    /**
     * Getter for the attribute workPackage.
     *
     * @return Reference Work-Package for the Task
     */
    public WorkPackage getWorkPackage() {
        return workPackage;
    }
}
