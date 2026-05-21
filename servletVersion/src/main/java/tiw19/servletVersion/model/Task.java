package tiw19.servletVersion.model;

import tiw19.servletVersion.dao.PlanningDAO;

import java.sql.SQLException;

/**
 * Task Model Representation.
 * <br>This Class works as the Task Model from the TASKS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class Task {
    private final int ID;
    private final float orderNumber;
    private final String title;
    private final String description;
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
     * @param description   Description of the Task.
     * @param startingMonth Starting month of the Task.
     * @param endingMonth   Ending month of the Task.
     * @param project       Reference Project.
     * @param workPackage   Reference Work-Package.
     */
    public Task(int ID, float OrderNumber, String title, String description, int startingMonth, int endingMonth, Project project, WorkPackage workPackage) {
        this.ID = ID;
        this.orderNumber = OrderNumber;
        this.title = title;
        this.description = description;
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
     * Getter for the attribute description.
     *
     * @return Task.description
     */
    public String getDescription() {
        return description;
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

    /**
     * Get the Total Planned Hours for the Task.
     *
     * @return Total Planned Hours for the Task.
     */
    public int getPlannedHours() {
        try {
            return PlanningDAO.getPlannedHours(this);
        } catch (SQLException exception) {
            return -1;
        }
    }

    /**
     * Get the Total Worked Hours for the Task.
     *
     * @return Total Worked Hours for the Task.
     */
    public int getWorkedHours() {
        try {
            return PlanningDAO.getWorkedHours(this);
        } catch (SQLException exception) {
            return -1;
        }
    }
}
