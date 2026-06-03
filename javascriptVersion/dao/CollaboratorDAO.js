'use strict';

// DAO definition
const DBPool = require('./DBPool');
const UserDAO = require('./UserDAO');
const TaskDAO = require('./TaskDAO');


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


// export the DAO functions
module.exports = {
    isValid,
    addNew
};