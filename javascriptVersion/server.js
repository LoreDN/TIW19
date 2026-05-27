'use strict';

// define the express app
const express = require('express');
const session = require('express-session');
const path = require('path');

const app = express();
const PORT = 3000;
const BASE_URL = '/TIW19-javascript';

// define routes path
const pageRoutes = require('./routes/pages');
const authRoutes = require('./routes/authentication');
/* 
const adminRoutes = require('./routes/admin');
const managerRoutes = require('./routes/manager');
const collaboratorRoutes = require('./routes/collaborator');
*/


// set parsing middleware
app.use(express.json());
app.use(express.urlencoded(
    {
        "extended": false
    }
));

// set the session
app.use(session({
    "secret": 'TIW19-secret-key',
    "resave": false,
    "saveUninitialized": false
}));


// set base routes
app.use(BASE_URL, pageRoutes);
app.get('/', (request, response) => response.redirect(BASE_URL + '/login'));
app.get(BASE_URL, (request, response) => response.redirect(BASE_URL + '/login'));

// set the static files
app.use(BASE_URL, express.static(path.join(__dirname, 'public')));

// set the Fetch-API routes
app.use(BASE_URL + '/api/authentication', authRoutes);
/*
app.use(BASE_URL + '/api/admin', adminRoutes);
app.use(BASE_URL + '/api/manager', managerRoutes);
app.use(BASE_URL + '/api/collaborator', collaboratorRoutes);
*/


// start the server
app.listen(PORT, () => {
    console.log(`Server listening at http://localhost:${PORT}` + BASE_URL);
});