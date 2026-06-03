'use strict';

// DAO definition
const DBPool = require('./DBPool');


// 1 ----- Get total planned hours for a Task
async function getPlannedHours(taskID) {
    if (!taskID) return 0;
    const [rows] = await DBPool.query(
        "SELECT sum(plannedHours) AS sum " +
        "FROM PLANNING " +
        "WHERE taskID = ?",
        [taskID]
    );
    if (rows.length === 0) return 0;
    return rows[0].sum || 0;
}


// 2 ----- Check if a Planning is valid
async function isValid(taskID, month, plannedHours) {
    if (!taskID || !month || (!plannedHours && plannedHours !== 0))
        return "All fields are required!!!";
    
    // sanity check
    const TaskDAO = require('./TaskDAO');
    const task = await TaskDAO.getFromID(taskID);
    // task
    if (!task)
        return "[Hours Error] : Task not found!!!";
    // month
    else if (isNaN(month))
        return "Month must be a number!!!";
    else if (month < 1)
        return "[Hours Error] : Month must be greater than 0!!!";
    else if (month < task.startingMonth)
        return "[Hours Error] : Month cannot be smaller than the Task starting one!!!";
    else if (month > task.endingMonth)
        return "[Hours Error] : Month cannot be greater than the Task ending one!!!";
    // planned hours
    else if (isNaN(plannedHours))
        return "Planned hours must be a number!!!";
    else if (plannedHours < 0)
        return "[Hours Error] : Planned hours must be greater than 0!!!";

    // all checks passed
    return true;
}


// 3 ----- Check if a Planning already exists
async function existsPlanning(taskID, month) {
    if (!taskID || isNaN(taskID) || !month || isNaN(month)) return true;
    const [rows] = await DBPool.query(
        "SELECT count(*) AS count " +
        "FROM PLANNING " +
        "WHERE taskID = ? AND month = ?",
        [taskID, month]
    );
    if (rows.length === 0) return true;
    return rows[0].count > 0;
}


// 4 ----- Add a new Planning
async function addNew(taskID, month, plannedHours) {
    const [newRow] = await DBPool.query(
        "INSERT INTO PLANNING " +
        "(taskID, month, plannedHours) " +
        "VALUES (?, ?, ?)",
        [taskID, month, plannedHours]
    );
}


// 5 ----- Update an already existing Planning
async function updatePlanning(taskID, month, plannedHours) {
    if (!taskID || isNaN(taskID) ||
        !month || isNaN(month) ||
        !plannedHours || isNaN(plannedHours)
    ) return;
    const [rows] = await DBPool.query(
        "UPDATE PLANNING " +
        "SET plannedHours = ? " +
        "WHERE taskID = ? AND month = ?",
        [plannedHours, taskID, month]
    );
}


// export the DAO functions
module.exports = {
    getPlannedHours,
    isValid,
    existsPlanning,
    addNew,
    updatePlanning
};