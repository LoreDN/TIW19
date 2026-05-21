'use strict';

// Class Prototype for USERS table
class User {
    constructor(id, name, surname, username, photo, role) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.photo = photo;
        this.role = role;  // ADMIN, MANAGER, COLLABORATOR, MANAGER_COLLABORATOR
    }

    // check User role
    isAdmin() {
        return this.role === 'ADMIN';
    }

    isManager() {
        return this.role === 'MANAGER' || this.role === 'MANAGER_COLLABORATOR';
    }

    isCollaborator() {
        return this.role === 'COLLABORATOR' || this.role === 'MANAGER_COLLABORATOR';
    }
}


// export the class
module.exports = User;