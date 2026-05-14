package tiw19.servletVersion.model;

/**
 * Planning Model Representation.
 * <br>This Class works as the Planning Model from the PLANNING Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class Planning {
    private final int taskID;
    private final int month;
    private final int plannedHours;
    private final int WorkedHours;

    /**
     * Constructor of the Planning.
     *
     * @param taskID        ID of the Task in the couple (Task, Month).
     * @param month         Month in the couple (Task, Month).
     * @param plannedHours  Planned Hours of work.
     * @param workedHours   Total Worked Hours.
     */
    public Planning(int taskID, int month, int plannedHours, int workedHours) {
        this.taskID = taskID;
        this.month = month;
        this.plannedHours = plannedHours;
        WorkedHours = workedHours;
    }

    /**
     * Getter for the attribute taskID.
     *
     * @return Planning.taskID
     */
    public int getTaskID() {
        return taskID;
    }

    /**
     * Getter for the attribute month.
     *
     * @return Planning.month
     */
    public int getMonth() {
        return month;
    }

    /**
     * Getter for the attribute plannedHours.
     *
     * @return Planning.plannedHours
     */
    public int getPlannedHours() {
        return plannedHours;
    }

    /**
     * Getter for the attribute workedHours.
     *
     * @return Planning.workedHours
     */
    public int getWorkedHours() {
        return WorkedHours;
    }
}
