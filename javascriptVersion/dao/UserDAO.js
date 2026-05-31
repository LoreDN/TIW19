'use strict';

// DAO definition
const DBPool = require('./DBPool');
const User = require('../model/User');


// 1 ----- Get a logged User by its username and password
async function checkCredentials(username, password) {
    if (!username || !password) return null;
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM USERS " +
        "WHERE username = ? AND password = ?",
        [username, password]
    );
    if (rows.length === 0) return null;
    const r = rows[0];
    return new User(r.ID, r.name, r.surname, r.username, r.photo, r.role);
}


// 2 ----- Get an User by its username
async function getFromUsername(username) {
    if (!username) return null;
    const [rows] = await DBPool.query(
        "SELECT * " +
        "FROM USERS " +
        "WHERE username = ?",
        [username]
    );
    if (rows.length === 0) return null;
    const r = rows[0];
    return new User(r.ID, r.name, r.surname, r.username, r.photo, r.role);
}


// export the DAO functions
module.exports = {
    checkCredentials, 
    getFromUsername
};