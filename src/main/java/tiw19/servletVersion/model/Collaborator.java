package tiw19.servletVersion.model;

/**
 * Collaborator Model Representation.
 * <br>This Class works as the Collaborator Model from the COLLABORATORS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class Collaborator {
    private final int userID;
    private final int taskID;
    private final int month;
    private final int WorkedHours;

    /**
     * Constructor of the Collaborator.
     *
     * @param userID        ID of the User in the triplet (Task, Month, Collaborator).
     * @param taskID        ID of the Task in the triplet (Task, Month,Collaborator).
     * @param month         Month in the triplet (Task, Month, Collaborator).
     * @param workedHours   Worked Hours of the Collaborator.
     */
    public Collaborator(int userID, int taskID, int month, int workedHours) {
        this.userID = userID;
        this.taskID = taskID;
        this.month = month;
        WorkedHours = workedHours;
    }

    /**
     * Getter for the attribute userID.
     *
     * @return Planning.userID
     */
    public int getUserID() {
        return userID;
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
     * Getter for the attribute workedHours.
     *
     * @return Planning.workedHours
     */
    public int getWorkedHours() {
        return WorkedHours;
    }
}
