'use strict';

// Utility Class Prototype for COLLABORATORS & PROJECTS
class Assignment {
    constructor(projectID, projectTitle, projectDuration, projectAdmin) {
        this.id = projectID;
        this.title = projectTitle;
        this.duration = projectDuration;
        this.admin = projectAdmin;
        this.workPackages = []
    }

    // update Work-Packages info
    addWorkPackage(workPackageOrderNumber) {
        this.workPackages.push({
            orderNumber: workPackageOrderNumber,
            tasks: []
        });
    }

    // update Task info
    addTask(workPackageOrderNumber, taskID, taskOrderNumber) {
        this.workPackages.find(wp => wp.orderNumber === workPackageOrderNumber)
            .tasks.push({
                id: taskID,
                orderNumber: taskOrderNumber,
                work: []
            });
    }

    // update month info
    addMonth(workPackageOrderNumber, taskID, month, workedHours) {
        this.workPackages.find(wp => wp.orderNumber === workPackageOrderNumber)
            .tasks.find(t => t.id === taskID)
            .work.push({
                month: month,
                hours: workedHours
            });
    }
}


// export the class
module.exports = Assignment;