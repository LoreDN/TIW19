'use strict';

// Class Prototype for COLLABORATORS table
class Collaborator {
    constructor(user) {
        this.id = user.id;
        this.username = user.username;
        this.name = user.name;
        this.surname = user.surname;
        this.projects = [];
    }

    // update Projects info
    update(projecTitle, projectDuration, workPackageOrderNumber, taskOrderNumber, month, workedHours) {
        // Project
        if (!this.projects.find(p => p.title === projecTitle)) {
            this.projects.push({
                title: projecTitle,
                duration: projectDuration,
                workPackages: []
            })
        }

        // Work-Package
        if (!this.projects.find(p => p.title === projecTitle)
            .workPackages.find(wp => wp.orderNumber === workPackageOrderNumber)
        ) {
            this.projects.find(p => p.title === projecTitle)
                .workPackages.push({
                    orderNumber: workPackageOrderNumber,
                    tasks: []
                })
        }

        // Task
        if (!this.projects.find(p => p.title === projecTitle)
            .workPackages.find(wp => wp.orderNumber === workPackageOrderNumber)
            .tasks.find(t => t.orderNumber === taskOrderNumber)
        ) {
            this.projects.find(p => p.title === projecTitle)
                .workPackages.find(wp => wp.orderNumber === workPackageOrderNumber)
                .tasks.push({
                    orderNumber: taskOrderNumber,
                    work: []
                })
        }

        // update
        this.projects.find(p => p.title === projecTitle)
            .workPackages.find(wp => wp.orderNumber === workPackageOrderNumber)
            .tasks.find(t => t.orderNumber === taskOrderNumber)
            .work.push({
                month: month,
                hours: workedHours
            })
    }
}


// export the class
module.exports = Collaborator;