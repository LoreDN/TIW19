'use strict';

// DAO definition
const DBPool = require('./DBPool');
const UserDAO = require('./UserDAO');
const WorkPackageDAO = require('./WorkPackageDAO');
const TaskDAO = require('./TaskDAO');

const Project = require('../model/Project');


// 1 ----- Check if a Project already exists
async function existsProject(admin, title) {
    if (!admin || !title) return true;
    const [rows] = await DBPool.query(
        "SELECT count(*) AS count " +
        "FROM PROJECTS " +
        "WHERE title = ? AND admin = ?",
        [title, admin]
    );
    if (rows.length === 0) return true;
    return rows[0].count > 0;
}


// 2 ----- Check if a Project is valid
async function isValid(admin, project) {
    if (!project.title || !project.duration || !project.manager)
        return "All fields are required!!!";
    
    // sanity check
    const manager = await UserDAO.getFromUsername(project.manager);
    // unique title
    if (await existsProject(admin, project.title))
        return "[Project Error] : Project with title \"" + project.title + "\" already exists!!!";
    // manager
    else if (!manager)
        return "[Project Error] : User not found!!!";
    else if (!manager.isManager())
        return "[Project Error] : User is not manager!!!";
    // duration
    else if (isNaN(project.duration))
        return "Project duration must be a number!!!";
    else if (Number(project.duration) < 1)
        return "[Project Error] : Project duration (in months) must be greater than 0!!!";

    // check Work-Packages validity
    for (const workPackage of project.workPackages) {
        const wpValidity = WorkPackageDAO.isValid(project, workPackage);
        if (wpValidity !== true) return wpValidity;
    }

    // check months coverage
    for (let m = 1; m <= project.duration; m++) {
        const covered = project.workPackages.some(workPackage => (workPackage.startingMonth <= m && m <= workPackage.endingMonth));
        if (!covered) return `[Project Error] : Month M${m} is not covered by any Work-Package!!!`;
    }

    // all checks passed
    return true;
}


// 3 ----- Add a new Project
async function addNew(admin, project) {
    const [newRow] = await DBPool.query(
        "INSERT INTO PROJECTS " +
        "(title, monthsNumber, admin, manager) " +
        "VALUES (?, ?, ?, ?)",
        [project.title, project.duration, admin, project.manager]
    );

    // add Work-Packages
    const projectID = newRow.insertId;
    for (const workPackage of project.workPackages) {
        await WorkPackageDAO.addNew(projectID, workPackage);
    }
}


// 4 ----- Get all Projects for an admin
async function getAll(username, role) {
    const projects = [];
    if (!username || !role || role !== 'ADMIN') return projects;

    // query the DB
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM PROJECTS " +
        "WHERE admin = ?",
        [username]
    );
    for (const row of rows) {
        projects.push( new Project(
            row.ID, row.title, row.monthsNumber, row.status, row.admin, row.manager,
            await WorkPackageDAO.getAll(row.ID)
        ));
    }
    return projects;
}


// 5 ----- Get all created Projects with a specific status for a manager
async function getWithStatus(username, role, status) {
    const projects = [];
    if (!username || !role || role !== 'MANAGER' || !status) return projects;

    // query the DB
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM PROJECTS " +
        "WHERE status = ? AND manager = ?",
        [status, username]
    );
    for (const row of rows) {
        projects.push( new Project(
            row.ID, row.title, row.monthsNumber, row.status, row.admin, row.manager,
            await WorkPackageDAO.getAll(row.ID)
        ));
    }
    return projects;
}


// 6 ----- Get a Project from its ID
async function getFromID(projectID) {
    if (!projectID || isNaN(projectID)) return null;
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM PROJECTS " +
        "WHERE ID = ?",
        [projectID]
    );
    if (rows.length === 0) return null;
    const row = rows[0];
    return new Project(
        row.ID, row.title, row.monthsNumber, row.status, row.admin, row.manager,
        await WorkPackageDAO.getAll(row.ID)
    );
}


// 7 ----- Check if a Project can be assigned
async function canBeAssigned(projectID, manager) {
    if (!projectID || isNaN(projectID) || !manager)
        return "All fields are required!!!";
    
    // sanity check
    const project = await getFromID(projectID);
    // project
    if (!project)
        return "[Project Error] : Project not found!!!";
    else if (project.manager !== manager)
        return "[Project Error] : Project not assigned to the logged MANAGER!!!";
    else if (project.status !== 'CREATED')
        return "[Project Error] : Project status must be CREATED to be assigned!!!";
    // tasks
    else {
        for (let month = 1; month <= project.duration; month++) {
            const workPackageCovered = project.workPackages.some(workPackage => (workPackage.startingMonth <= month && month <= workPackage.endingMonth));
            if (!workPackageCovered) return `[Project Error] : Month M${month} is not covered by any Work-Package!!!`;
            const taskCovered = project.workPackages.flatMap(workPackage => workPackage.tasks).some(task => (task.startingMonth <= month && month <= task.endingMonth));
            if (!taskCovered) return `[Project Error] : Month M${month} is not covered by any Task!!!`;
        }
        for (const task of project.workPackages.flatMap(workPackage => workPackage.tasks).values()) {
            if (!(await TaskDAO.isAssigned(task.id))) {
                return "[Project Error] : Task " + task.orderNumber + " is not assigned!!!";
            }
        }
    }

    // all checks passed
    return true;
}


// 8 ----- Update a Project status
async function updateStatus(projectID, newStatus) {
    if (!projectID || isNaN(projectID) || !newStatus) return;
    await DBPool.query(
        "UPDATE PROJECTS " +
        "SET status = ? " +
        "WHERE ID = ?",
        [newStatus, projectID]
    );
}


// export the DAO functions
module.exports = {
    existsProject,
    isValid,
    addNew,
    getAll,
    getWithStatus,
    getFromID,
    canBeAssigned,
    updateStatus
};