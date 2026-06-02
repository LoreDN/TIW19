'use strict';

// Class Prototype for PROJECTS table
class Project {
    constructor(id, title, duration, status, admin, manager, workPackages) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.status = status;
        this.admin = admin;
        this.manager = manager;
        this.workPackages = workPackages;
    }
}


// export the class
module.exports = Project;