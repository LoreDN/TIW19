'use strict';

// DAO definition
const DBPool = require('./DBPool');


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


// export the DAO functions
module.exports = {
    isValid,
    addNew
};