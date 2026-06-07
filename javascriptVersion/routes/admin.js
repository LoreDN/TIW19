'use strict';

// define the express router
const express = require('express');
const router = express.Router();

const User = require('../model/User');
const ProjectDAO = require('../dao/ProjectDAO');


// POST /api/admin/createProject
router.post('/createProject', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const admin = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!admin || !admin.isAdmin()) return response.status(401).json({ error: "[Login Error] : Not authenticated as ADMIN!!!" });
    const project = request.body;

    // DB-side validation
    try {
        const projectValidity = await ProjectDAO.isValid(admin.username, project);
        if (projectValidity === true) {
            await ProjectDAO.addNew(admin.username, project);
            response.json({
                ok: true,
                success: "[Project Success] : Project has been created successfully!!!"
            });
        } else response.status(400).json({ error: projectValidity });
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// GET /api/admin/loadProjects
router.get('/loadProjects', async (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated!!!" });
    const admin = new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
    if (!admin || !admin.isAdmin()) return response.status(401).json({ error: "[Login Error] : Not authenticated as ADMIN!!!" });

    // query the DB
    try {
        const projects = await ProjectDAO.getAll(admin.username, 'ADMIN');
        response.json({
            ok: true,
            projects: projects
        })
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// export the router
module.exports = router;