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


// export the DAO functions
module.exports = {
    getPlannedHours
};