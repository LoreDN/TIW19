'use strict';

// DAO definition
const DBPool = require('./DBPool');
const UserDAO = require('./UserDAO');
const ProjectDAO = require('./ProjectDAO');
const TaskDAO = require('./TaskDAO');
const PlanningDAO = require('./PlanningDAO');

const Collaborator = require('../model/Collaborator');
const Assignment = require('../model/Assignment');


// 1 ----- Check if a Collaborator-Planning is valid
async function isValid(user, taskID, month, collaborator) {
    if (!taskID || !month || !collaborator)
        return "All fields are required!!!";

    // sanity check
    const task = await TaskDAO.getFromID(taskID);
    const collaboratorUser = await UserDAO.getFromUsername(collaborator);
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
    // collaborator
    else if (!collaboratorUser)
        return "[Collaborator Error] : Collaborator not found!!!";
    else if (!collaboratorUser.isCollaborator())
        return "[Collaborator Error] : Collaborator must have the role COLLABORATOR!!!";
    else if (collaboratorUser.username === user.username)
        return "[Collaborator Error] : Manager cannot be assigned as a Collaborator of its own Project!!!";

    // all checks passed
    return true;
}


// 2 ----- Add a new Collaborator-Planning
async function addNew(taskID, month, collaboratorID) {
    const [newRow] = await DBPool.query(
        "INSERT INTO COLLABORATORS " +
        "(userID, taskID, month) " +
        "VALUES (?, ?, ?)",
        [collaboratorID, taskID, month]
    );
}


// 3 ----- Get all the Collaborators for a Manager
async function getCollaborators(manager) {
    const collaborators = [];

    // get Projects
    const projects = await ProjectDAO.getWithStatus(manager, 'MANAGER', 'ASSIGNED');
    if (!projects || projects.length === 0) return collaborators;

    // get Collaborators
    for (const project of projects) {
        for (const workPackage of project.workPackages) {
            for (const task of workPackage.tasks) {
                const [rows] = await DBPool.query(
                    "SELECT U.ID AS ID, C.month AS month, C.workedHours AS workedHours " +
                    "FROM USERS AS U " + 
                    "JOIN COLLABORATORS AS C ON U.ID = C.userID " + 
                    "WHERE C.taskID = ?",
                    [task.id]
                );
                for (const row of rows) {
                    if (!collaborators.find(c => c.id === row.ID)) {
                        collaborators.push( new Collaborator(
                            await UserDAO.getFromID(row.ID)
                        ));
                    }
                    collaborators.find(c => c.id === row.ID).update(
                        project.title, project.duration, workPackage.orderNumber, task.orderNumber,
                        row.month, row.workedHours
                    )
                }
            }
        }
    }
    return collaborators;
}


// 4 ----- Get all the Tasks' IDs assigned to a Collaborator
async function getTaskIDs(collaboratorID) {
    const tasks = [];
    if (!collaboratorID || isNaN(collaboratorID)) return tasks; 
    const [rows] = await DBPool.query(
        "SELECT taskID " + 
        "FROM COLLABORATORS " +
        "WHERE userID = ?",
        [collaboratorID]
    );
    for (const row of rows) tasks.push(row.taskID);
    return tasks;
}


// 5 ----- Get all the assignments of a Collaborator
async function getAssignments(collaboratorID) {
    const assignments = [];
    if (!collaboratorID || isNaN(collaboratorID)) return assignments;

    // get Projects
    const tasks = await getTaskIDs(collaboratorID);
    for (const taskID of tasks) {
        const project = await ProjectDAO.getFromTask(taskID);
        if (project.status !== 'ASSIGNED' || assignments.find(p => p.id === project.id)) continue;
        const newAssignment = new Assignment(project.id, project.title, project.duration, project.admin);

        // get Work-Packages
        for (const workPackage of project.workPackages) {
            newAssignment.addWorkPackage(workPackage.orderNumber);

            // get Tasks
            for (const task of workPackage.tasks) {
                newAssignment.addTask(workPackage.orderNumber, task.id, task.orderNumber);
                
                // filter months
                for (let month = task.startingMonth; month <= task.endingMonth; month++) {
                    const workedHours = await getWorkedHours(collaboratorID, task.id, month);
                    if (workedHours !== null) {
                        newAssignment.addMonth(
                            workPackage.orderNumber, task.id,
                            month, workedHours
                        );
                    }
                }
            }
        }
        assignments.push(newAssignment);
    }
    return assignments;
}


// 6 ----- Get the worked hours for Task on a given month by a Collaborator
async function getWorkedHours(collaboratorID, taskID, month) {
    if (!collaboratorID || isNaN(collaboratorID) ||
        !taskID || isNaN(taskID) ||
        !month || isNaN(month)
    ) return null;
    const [rows] = await DBPool.query(
        "SELECT workedHours " +
        "FROM COLLABORATORS " +
        "WHERE userID = ? AND taskID = ? AND month = ?",
        [collaboratorID, taskID, month]
    );
    if (rows.length === 0) return null;
    return rows[0].workedHours;
}


// 7 ----- Check if hours can be logged for a Task on a given month by a Collaborator
function canBeLogged(collaboratorID, taskID, month, hours) {
    // sanity check
    if (!collaboratorID || !taskID || !month || hours === undefined || hours === null)
        return "All fields are required!!!";
    else if (isNaN(collaboratorID) || isNaN(taskID) || isNaN(month) || isNaN(hours))
        return "All filed must be numbers!!!";
    // hours
    else if (hours < 0)
        return "[Hours Error] : Worked hours must be at least than 0!!!";

    // all checks passed
    return true;
}


// 8 ----- Log workedHours for a Task on a given month by a Collaborator
async function updateWorkedHours(collaboratorID, taskID, month, hours) {
    if (!taskID || isNaN(taskID) ||
        !month || isNaN(month) ||
        hours === undefined || hours === null || isNaN(hours)
    ) return;

    // update the DB
    const oldWorkedHours = await getWorkedHours(collaboratorID, taskID, month);
    const [rows] = await DBPool.query(
        "UPDATE COLLABORATORS " +
        "SET workedHours = ? " +
        "WHERE userID = ? AND taskID = ? AND month = ?",
        [hours, collaboratorID, taskID, month]
    );
    await PlanningDAO.updateWorkedHours(taskID, month, hours - oldWorkedHours);
}


// export the DAO functions
module.exports = {
    isValid,
    addNew,
    getCollaborators,
    getTaskIDs,
    getAssignments,
    getWorkedHours,
    canBeLogged,
    updateWorkedHours
};