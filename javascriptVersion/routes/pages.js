'use strict';

// define the express router
const express = require('express');
const router = express.Router();
const path = require('path');

const User = require('../model/User');

// get the Session User 
function getSessionUser(request) {
    const user = request.session.user;
    if (!user) return null;
    return new User(user.id, user.name, user.surname, user.username, user.photo, user.role);
}

// logout and redirect to login
function logoutRedirect(request, response) {
    request.session.destroy(() => {
        response.redirect('/TIW19-javascript/login');
    });
}


// GET /login
router.get('/login', (request, response) => {
    response.sendFile(path.join(__dirname, '../public', 'login.html'));
});

// GET /homeAdmin
router.get('/homeAdmin', (request, response) => {
    const user = getSessionUser(request);
    if (!user || !user.isAdmin()) return logoutRedirect(request, response);
    response.sendFile(path.join(__dirname, '../public', 'homeAdmin.html'));
});

// GET /homeManager
router.get('/homeManager', (request, response) => {
    const user = getSessionUser(request);
    if (!user || !user.isManager()) return logoutRedirect(request, response);
    response.sendFile(path.join(__dirname, '../public', 'homeManager.html'));
});

// GET /homeCollaborator
router.get('/homeCollaborator', (request, response) => {
    const user = getSessionUser(request);
    if (!user || !user.isCollaborator()) return logoutRedirect(request, response);
    response.sendFile(path.join(__dirname, '../public', 'homeCollaborator.html'));
});


// export the router
module.exports = router;