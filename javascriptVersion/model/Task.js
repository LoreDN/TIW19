'use strict';

// Class Prototype for TASKS table
class Task {
    constructor(id, orderNumber, title, description, startingMonth, endingMonth, plannedHours, workedHours) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.title = title;
        this.description = description;
        this.startingMonth = startingMonth;
        this.endingMonth = endingMonth;
        this.plannedHours = plannedHours;
        this.workedHours = workedHours;
    }
}


// export the class
module.exports = Task;