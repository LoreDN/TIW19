'use strict';

// Class Prototype for WORK_PACKAGES table
class WorkPackage {
    constructor(id, orderNumber, title, startingMonth, endingMonth, tasks) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.title = title;
        this.startingMonth = startingMonth;
        this.endingMonth = endingMonth;
        this.tasks = tasks;
    }
}


// export the class
module.exports = WorkPackage;