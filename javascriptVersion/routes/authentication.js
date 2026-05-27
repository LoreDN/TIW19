'use strict';

// define the express router
const express = require('express');
const router = express.Router();

const userDAO = require('../dao/UserDAO');


// POST /api/authentication/login
router.post('/login', async (request, response) => {
    const { username, password } = request.body;

    // Server-side validation
    if (!username) {
        if (!password) return response.status(400).json({ error: "Username and password are required!!!" });
        else return response.status(400).json({ error: "Username is required!!!" });
    } else if (!password) return response.status(400).json({ error: "Password is required!!!" });

    // user authentication
    try {
        const user = await userDAO.checkCredentials(username, password);
        if (!user) return response.status(401).json({ error: "[Login Error] : Invalid username or password" });
        
        // save session
        request.session.user = user;
        response.json({ user: user });
    } catch (exception) {
        console.error(exception);
        response.status(500).json({ error: "[DB Error] : " + exception.message });
    }
});


// POST /api/authentication/logout
router.post('/logout', (request, response) => {
    request.session.destroy(error => {
        if (error) {
            console.error(error);
            response.status(500).json({ error: "[Logout Error] : " + error.message });
        } else response.json({ ok: true });
    });
});


// GET /api/authentication/user
router.get('/user', (request, response) => {
    const user = request.session.user;
    if (!user) return response.status(401).json({ error: "[Login Error] : Not authenticated" });
    response.json(user);
});


// export the router
module.exports = router;