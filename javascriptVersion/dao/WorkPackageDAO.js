'use strict';

// DAO definition
const DBPool = require('./DBPool');
const taskDAO = require('./TaskDAO');


// 1 ----- Check if a Work-Package is valid
function isValid(project, workPackage) {
    // sanity check
    if (!workPackage.title || !workPackage.startingMonth || !workPackage.endingMonth)
        return "All fields are required!!!";
    else if (isNaN(workPackage.startingMonth) || isNaN(workPackage.endingMonth))
        return "Work-Package starting/ending months must be a number!!!";
    // starting month
    else if (Number(workPackage.startingMonth) < 1)
        return "[Work-Package Error] : Work-Package starting month must be greater than 0!!!";
    else if (Number(workPackage.startingMonth) > Number(project.duration))
        return"[Work-Package Error] : Work-Package starting month cannot be greater than the Project duration!!!";
    // ending month
    else if (Number(workPackage.endingMonth) < 1)
        return "[Work-Package Error] : Work-Package ending month must be greater than 0!!!";
    else if (Number(workPackage.endingMonth) < Number(workPackage.startingMonth))
        return "[Work-Package Error] : Work-Package ending month cannot be smaller than the starting one!!!";
    else if (Number(workPackage.endingMonth) > Number(project.duration))
        return "[Work-Package Error] : Work-Package ending month cannot be greater than the Project duration!!!";

    // check Tasks validity
    for (const task of workPackage.tasks) {
        const taskValidity = taskDAO.isValid(workPackage, task);
        if (taskValidity !== true) return taskValidity;
    }

    // check months coverage
    for (let m = workPackage.startingMonth; m <= workPackage.endingMonth; m++) {
        const covered = workPackage.tasks.some(task => (task.startingMonth <= m && m <= task.endingMonth));
        if (!covered) return `[Work-Package Error] : Month M${m} is not covered by any Task!!!`;
    }

    // all checks passed
    return true;
}


// 2 ----- Add a new Work-Package
async function addNew(projectID, workPackage) {
    const [newRow] = await DBPool.query(
        "INSERT INTO WORK_PACKAGES " +
        "(orderNumber, title, startingMonth, endingMonth, projectID) " +
        "VALUES (?, ?, ?, ?, ?)",
        [workPackage.orderNumber, workPackage.title, workPackage.startingMonth, workPackage.endingMonth, projectID]
    );

    // add Tasks
    const workPackageID = newRow.insertId;
    for (const task of workPackage.tasks) {
        const decimalOrderNumber = parseFloat(
            `${workPackage.orderNumber}.${String(task.orderNumber).padStart(2, '0')}`
        );
        await taskDAO.addNew(workPackageID, decimalOrderNumber, task);
    }
}


// export the DAO functions
module.exports = {
    isValid,
    addNew
};