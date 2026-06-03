'use strict';

// DAO definition
const DBPool = require('./DBPool');
const PlanningDAO = require('./PlanningDAO');

const Task = require('../model/Task');


// 1 ----- Check if a Task is valid
function isValid(workPackage, task) {
    // sanity check
    if (!task.title || !task.description || !task.startingMonth || !task.endingMonth)
        return "All fields are required!!!";
    else if (isNaN(task.startingMonth) || isNaN(task.endingMonth))
        return "Task starting/ending months must be a number!!!";
    // starting month
    else if (Number(task.startingMonth) < 1)
        return "[Task Error] : Task starting month must be greater than 0!!!";
    else if (Number(task.startingMonth) < Number(workPackage.startingMonth))
        return"[Task Error] : Task starting month cannot be smaller than the Work-Package starting month!!!";
    // ending month
    else if (Number(task.endingMonth) < 1)
        return "[Task Error] : Task ending month must be greater than 0!!!";
    else if (Number(task.endingMonth) < Number(task.startingMonth))
        return "[Task Error] : Task ending month cannot be smaller than the starting one!!!";
    else if (Number(task.endingMonth) > Number(workPackage.endingMonth))
        return "[Task Error] : Task ending month cannot be greater than the Work-Package ending month!!!";

    // all checks passed
    return true;
}


// 2 ----- Add a new Task
async function addNew(workPackageID, decimalOrderNumber, task) {
    await DBPool.query(
        "INSERT INTO TASKS " +
        "(orderNumber, title, description, startingMonth, endingMonth, workPackageID) " +
        "VALUES (?, ?, ?, ?, ?, ?)",
        [decimalOrderNumber, task.title, task.description, task.startingMonth, task.endingMonth, workPackageID]
    );
}


// 3 ----- Get all Tasks for a Work-Package
async function getAll(workPackageID) {
    const tasks = [];
    if (!workPackageID) return tasks;
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM TASKS AS T " +
        "WHERE workPackageID = ?",
        [workPackageID]
    );
    for (const row of rows) {
        tasks.push( new Task(
            row.ID, row.orderNumber, row.title, row.description, row.startingMonth, row.endingMonth, 
            await PlanningDAO.getPlannedHours(row.ID)
        ));
    }
    return tasks;
}


// 4 ----- Get a Task from its ID
async function getFromID(taskID) {
    if (!taskID || isNaN(taskID)) return null;
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM TASKS " +
        "WHERE ID = ?",
        [taskID]
    );
    if (rows.length === 0) return null;
    const row = rows[0];
    return new Task(
        row.ID, row.orderNumber, row.title, row.description, row.startingMonth, row.endingMonth, 
        await PlanningDAO.getPlannedHours(row.ID)
    );
}


// 5 ----- Check if a Task is fully assigned
async function isAssigned(taskID) {
    if (!taskID || isNaN(taskID)) return false;
    const task = await getFromID(taskID);
    if (!task) return false;

    // check Planning
    const [hoursRows] = await DBPool.query(
        "SELECT count(*) AS count " +
        "FROM PLANNING " +
        "WHERE taskID = ?",
        [taskID]
    );
    if (hoursRows.length === 0 ||
        Number(hoursRows[0].count) !== (Number(task.endingMonth) - Number(task.startingMonth) + 1)
    ) return false;

    // check collaborators
    for (let month = task.startingMonth; month <= task.endingMonth; month++) {
        const [collaboratorRows] = await DBPool.query(
            "SELECT count(*) AS count " +
            "FROM COLLABORATORS " +
            "WHERE taskID = ? AND month = ?",
            [taskID, month]
        );
        if (collaboratorRows.length === 0 || Number(collaboratorRows[0].count) === 0) return false;
    }

    // all checks passed
    return true;
}


// export the DAO functions
module.exports = {
    isValid,
    addNew,
    getAll,
    getFromID,
    isAssigned
};