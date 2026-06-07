'use strict';

// define the express router
const express = require('express');
const router = express.Router();

const User = require('../model/User');
const CollaboratorDAO = require('../dao/CollaboratorDAO');


// GET /api/collaborator/loadProjects
router.get('/loadProjects', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const collaborator = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!collaborator || !collaborator.isCollaborator()) return response.status(401).json({ error: "[Login Error] : Not authenticated as COLLABORATOR!!!" });

    // query the DB
    try {
        const assignments = await CollaboratorDAO.getAssignments(collaborator.id);
        response.json({
            ok: true,
            assignments: assignments
        })
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// POST /api/collaborator/logHours
router.post('/logHours', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const collaborator = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!collaborator || !collaborator.isCollaborator()) return response.status(401).json({ error: "[Login Error] : Not authenticated as COLLABORATOR!!!" });

    // update the DB
    const task = request.body.task;
    const month = request.body.month;
    const hours = request.body.hours;
    try {
        const logValidity = CollaboratorDAO.canBeLogged(collaborator.id, task.id, month, hours);
        if (logValidity === true) {
            await CollaboratorDAO.updateWorkedHours(collaborator.id, task.id, month, hours);
            response.json({
                ok: true,
                success: `[Hours Success] : Worked Hours have ben logged successfully for the Task ${task.orderNumber} on the month M${month}!!!`
            });
        } else response.status(400).json({ error: logValidity });
    }  catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// export the router
module.exports = router;