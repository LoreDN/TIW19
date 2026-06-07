'use strict';

// define the express router
const express = require('express');
const router = express.Router();

const User = require('../model/User');
const UserDAO = require('../dao/UserDAO');
const ProjectDAO = require('../dao/ProjectDAO');
const PlanningDAO = require('../dao/PlanningDAO');
const CollaboratorDAO = require('../dao/CollaboratorDAO');


// GET /api/manager/loadProjects
router.get('/loadProjects', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const manager = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!manager || !manager.isManager()) return response.status(401).json({ error: "[Login Error] : Not authenticated as MANAGER!!!" });
    const projectStatus = request.query.status;

    // query the DB
    try {
        const projects = await ProjectDAO.getWithStatus(manager.username, 'MANAGER', projectStatus);
        response.json({
            ok: true,
            projects: projects
        })
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// POST /api/manager/assignHours
router.post('/assignHours', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const manager = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!manager || !manager.isManager()) return response.status(401).json({ error: "[Login Error] : Not authenticated as MANAGER!!!" });
    const taskID = request.body.assignment.taskID;
    const month = request.body.assignment.month;
    const plannedHours = request.body.assignment.plannedHours;

    // DB-side validation
    try {
        const assignmentValidity = await PlanningDAO.isValid(taskID, month, plannedHours);
        if (assignmentValidity === true) {
            if (await PlanningDAO.existsPlanning(taskID, month)) await PlanningDAO.updatePlanning(taskID, month, plannedHours);
            else await PlanningDAO.addNew(taskID, month, plannedHours);
            response.json({
                ok: true,
                success: "[Hours Success] : Planning has been added successfully!!!"
            });
        } else response.status(400).json({ error: assignmentValidity });
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// POST /api/manager/assignCollaborator
router.post('/assignCollaborator', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const manager = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!manager || !manager.isManager()) return response.status(401).json({ error: "[Login Error] : Not authenticated as MANAGER!!!" });
    const taskID = request.body.assignment.taskID;
    const month = request.body.assignment.month;
    const collaborator = request.body.assignment.collaborator;

    // DB-side validation
    try {
        const assignmentValidity = await CollaboratorDAO.isValid(user, taskID, month, collaborator);
        if (assignmentValidity === true) {
            await CollaboratorDAO.addNew(taskID, month, (await UserDAO.getFromUsername(collaborator)).id);
            response.json({
                ok: true,
                success: "[Collaborator Success] : Collaborator has been assigned successfully!!!"
            });
        } else response.status(400).json({ error: assignmentValidity });
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// POST /api/manager/assignProject
router.post('/assignProject', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const manager = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!manager || !manager.isManager()) return response.status(401).json({ error: "[Login Error] : Not authenticated as MANAGER!!!" });
    const projectID = request.body.projectID;

    // DB-side validation
    try {
        const assignmentValidity = await ProjectDAO.canBeAssigned(projectID, manager.username);
        if (assignmentValidity === true) {
            await ProjectDAO.updateStatus(projectID, 'ASSIGNED');
            response.json({
                ok: true,
                success: "[Project Success] : Project has been assigned successfully!!!"
            });
        } else response.status(400).json({ error: assignmentValidity });
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// POST /api/manager/concludeProject
router.post('/concludeProject', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const manager = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!manager || !manager.isManager()) return response.status(401).json({ error: "[Login Error] : Not authenticated as MANAGER!!!" });
    const projectID = request.body.projectID;
    const projectTitle = request.body.projectTitle;

    // DB-side validation
    try {
        const conclusionValidity = await ProjectDAO.canBeConcluded(projectID, manager.username);
        if (conclusionValidity === true) {
            await ProjectDAO.updateStatus(projectID, 'CONCLUDED');
            response.json({
                ok: true,
                success: `[Conclusion Success] : Project ${projectTitle} has been concluded successfully!!!`
            });
        } else response.status(400).json({ error: conclusionValidity });
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// GET /api/manager/loadCollaborators
router.get('/loadCollaborators', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const manager = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!manager || !manager.isManager()) return response.status(401).json({ error: "[Login Error] : Not authenticated as MANAGER!!!" });

    // query the DB
    try {
        const collaborators = await CollaboratorDAO.getCollaborators(manager.username);
        response.json({
            ok: true,
            collaborators: collaborators
        })
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// export the router
module.exports = router;