'use strict';

// get mariaDB driver
const mariadb = require('mysql2/promise');

// create the connection pool
const DBPool = mariadb.createPool({
    "host": 'mariadb',
    "port": 3306,
    "user": process.env.MARIADB_USER,
    "password": process.env.MARIADB_PASSWORD,
    "database": process.env.MARIADB_DATABASE,
    "connectionLimit": 10
});
module.exports = DBPool;