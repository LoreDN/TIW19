'use strict';

// cached data
const allProjects = [];
let nextProjectID = 1;

// section links
document.getElementById('button-homeAdmin-projectVerification').onclick = () => showProjectVerification();
document.getElementById('button-homeAdmin-logout').onclick = () => handleLogout();


// 1 ----- Page Initialization
async function initPage() {
    const response = await fetch(BASE_URL + '/api/authentication/user');
    const user = await response.json();

    // loading error
    if (!response.ok) {
        handleLogout();
        return;
    }

    document.getElementById('homeAdmin-welcome').textContent = `Welcome, ${user.name} ${user.surname}`;
    document.getElementById('homeAdmin-profile-photo').src = `${BASE_URL}/images/${user.photo}`;
}
initPage();


// 2 ----- Project Creation form
document.getElementById('homeAdmin-project-form').addEventListener('submit', function (event) {
    event.preventDefault();
    clearMessages('homeAdmin-project');

    // get the form data
    const title = document.getElementById('homeAdmin-project-title').value.trim();
    const duration = parseInt(document.getElementById('homeAdmin-project-duration').value, 10);
    const manager = document.getElementById('homeAdmin-project-manager').value.trim();

    // client-side validation
    let errorMessage = "";
    // title
    if (!title || !duration || !manager)
        errorMessage = "All fields are required!!!";
    else if (allProjects.some(project => project.title === title))
        errorMessage = "Project title must be unique!!!";
    // duration
    else if (isNaN(duration))
        errorMessage = "Project duration must be a number!!!";
    else if (duration < 1)
        errorMessage = "[Project Error] : Project duration (in months) must be greater than 0!!!";

    // form error
    if (errorMessage) {
        showMessage('homeAdmin-project-error', errorMessage);
        return;
    }

    // update client-side state
    const project = { "clientId": nextProjectID++, "title": title, "duration": duration, "manager": manager, "workPackages": [] };
    allProjects.push(project);
    renderProject(project);
    showMessage('homeAdmin-project-success', "[Project Success] : Project has been added successfully!!!");
    document.getElementById('homeAdmin-project-form').reset();
});


// 3 ----- Render a Project Draft
function renderProject(project) {
    const container = document.getElementById('homeAdmin-projects-container');

    // Project Draft card
    const card = document.createElement('div');
    card.className = 'project-draft-card';
    card.id = `project-card-${project.clientId}`;
    container.appendChild(card);

    // card content
    const header = document.createElement('div');
    header.className = 'project-draft-header';
    header.innerHTML = `
        <strong>${escapeHtml(project.title)}</strong>
        &nbsp;|&nbsp; Duration: ${project.duration} month(s)
        &nbsp;|&nbsp; Manager: ${escapeHtml(project.manager)}
    `;
    card.appendChild(header);

    // add button
    const addButton = document.createElement('button');
    addButton.type = 'button';
    addButton.textContent = '+';
    addButton.className = 'button-draft button-add';
    addButton.title = 'Add Work-Package';
    addButton.onclick = () => handleAddWorkPackage(project);
    header.appendChild(addButton);

    // remove button
    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.textContent = '-';
    removeButton.className = 'button-draft button-remove';
    removeButton.title = 'Remove Project';
    removeButton.onclick = () => handleRemoveProject(project.clientId);
    header.appendChild(removeButton);

    // Work-Packages list
    const workPackagesList = document.createElement('ul');
    workPackagesList.className = 'workPackages-draft-list';
    workPackagesList.id = `workPackages-draft-list-${project.clientId}`;
    card.appendChild(workPackagesList);

    // create button
    const createButton = document.createElement('button');
    createButton.type = 'button';
    createButton.textContent = 'Create Project';
    createButton.className = 'button-create';
    createButton.title = 'Create Project';
    createButton.onclick = () => handleCreateProject(project);
    card.appendChild(createButton);
}

// remove a Project Draft
function handleRemoveProject(clientId) {
    allProjects.splice(allProjects.findIndex(project => project.clientId === clientId), 1);
    document.getElementById(`project-card-${clientId}`).remove();
}

// add a Work-Package to a Project Draft
function handleAddWorkPackage(project) {
    const workPackage = {
        orderNumber:   project.workPackages.length + 1,
        title:         'Work-Package title',
        startingMonth: 1,
        endingMonth:   project.duration,
        tasks:         []
    };
    project.workPackages.push(workPackage);
    renderWorkPackage(workPackage, project);
}


// 3 ----- Render a Work-Package Draft item
function renderWorkPackage(workPackage, project) {
    clearMessages('homeAdmin-project');
    const workPackagesList = document.getElementById(`workPackages-draft-list-${project.clientId}`);

    // Work-Package Draft item
    const item = document.createElement('li');
    item.className = 'workPackage-draft-item';
    item.id = `workPackage-draft-item-${project.clientId}-${workPackage.orderNumber}`;
    workPackagesList.appendChild(item);

    // order number label
    const orderSpan = document.createElement('span');
    orderSpan.textContent = `WP ${workPackage.orderNumber}:`;
    orderSpan.className = 'workPackage-orderNumber';
    item.appendChild(orderSpan);

    // inline-editable fields
    item.appendChild(makeEditable(workPackage, 'title'));
    item.appendChild(document.createTextNode(' | M'));
    item.appendChild(makeEditable(workPackage, 'startingMonth'));
    item.appendChild(document.createTextNode(' → M'));
    item.appendChild(makeEditable(workPackage, 'endingMonth'));

    // add button
    const addButton = document.createElement('button');
    addButton.type = 'button';
    addButton.textContent = '+';
    addButton.className = 'button-draft button-add';
    addButton.title = 'Add Task';
    addButton.onclick = () => handleAddTask(workPackage, project);
    item.appendChild(addButton);

    // remove button
    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.textContent = '-';
    removeButton.className = 'button-draft button-remove';
    removeButton.title = 'Remove Work Package';
    removeButton.onclick = () => handleRemoveWorkPackage(workPackage, project);
    item.appendChild(removeButton);

    // Task list
    const TaskList = document.createElement('ul');
    TaskList.className = 'tasks-draft-list';
    TaskList.id = `tasks-draft-list-${project.clientId}-${workPackage.orderNumber}`;
    item.appendChild(TaskList);
}

// remove a Work-Package from a Project Draft
function handleRemoveWorkPackage(workPackage, project) {
    project.workPackages.splice(project.workPackages.indexOf(workPackage), 1);
    document.getElementById(`workPackage-draft-item-${project.clientId}-${workPackage.orderNumber}`).remove();
    project.workPackages.forEach((wp) => {
        if (wp.orderNumber > workPackage.orderNumber) {
            const item = document.getElementById(`workPackage-draft-item-${project.clientId}-${wp.orderNumber}`);
            wp.orderNumber--;
            item.id = `workPackage-draft-item-${project.clientId}-${wp.orderNumber}`;
            item.querySelector('.workPackage-orderNumber').textContent = `WP ${wp.orderNumber}:`;
            item.querySelector('.tasks-draft-list').id = `tasks-draft-list-${project.clientId}-${wp.orderNumber}`;
        }
    });
}

// add a Task to a Work-Package Draft
function handleAddTask(workPackage, project) {
    clearMessages('homeAdmin-project');
    const task = {
        orderNumber:   workPackage.tasks.length + 1,
        title:         'Task title',
        description:   'Task description',
        startingMonth: workPackage.startingMonth,
        endingMonth:   workPackage.endingMonth
    };
    workPackage.tasks.push(task);
    renderTask(task, workPackage, project);
} 


// 4 ----- Render a Task Draft item
function renderTask(task, workPackage, project) {
    const tasksList = document.getElementById(`tasks-draft-list-${project.clientId}-${workPackage.orderNumber}`);

    // Task Draft item
    const item = document.createElement('li');
    item.className = 'task-draft-item';
    item.id = `task-draft-item-${project.clientId}-${workPackage.orderNumber}-${task.orderNumber}`;
    tasksList.appendChild(item);

    // order number label
    const orderSpan = document.createElement('span');
    orderSpan.textContent = `Task ${task.orderNumber}:`;
    orderSpan.className = 'task-orderNumber';
    item.appendChild(orderSpan);

    // inline-editable fields
    item.appendChild(makeEditable(task, 'title'));
    item.appendChild(document.createTextNode(' | '));
    item.appendChild(makeEditable(task, 'description'));
    item.appendChild(document.createTextNode(' | M'));
    item.appendChild(makeEditable(task, 'startingMonth'));
    item.appendChild(document.createTextNode(' → M'));
    item.appendChild(makeEditable(task, 'endingMonth'));

    // remove button
    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.textContent = '-';
    removeButton.className = 'button-draft button-remove';
    removeButton.title = 'Remove Task';
    removeButton.onclick = () => handleRemoveTask(task, workPackage, project);
    item.appendChild(removeButton);
}

// remove a Task from a Work-Package Draft
function handleRemoveTask(task, workPackage, project) {
    workPackage.tasks.splice(workPackage.tasks.indexOf(task), 1);
    document.getElementById(`task-draft-item-${project.clientId}-${workPackage.orderNumber}-${task.orderNumber}`).remove();
    workPackage.tasks.forEach((t) => {
        if (t.orderNumber > task.orderNumber) {
            const item = document.getElementById(`task-draft-item-${project.clientId}-${workPackage.orderNumber}-${t.orderNumber}`);
            t.orderNumber--;
            item.id = `task-draft-item-${project.clientId}-${workPackage.orderNumber}-${t.orderNumber}`;
            item.querySelector('.task-orderNumber').textContent = `Task ${t.orderNumber}:`;
        }
    });
}


// 5 ----- Create a Project
async function handleCreateProject(project) {
    try {
        const response = await fetch(BASE_URL + '/api/admin/createProject', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(project)
        });
        const data = await response.json();
        
        // creation error
        if (!response.ok) {
            showMessage('homeAdmin-project-error', data.error);
            return;
        }
        
        // update client-side state
        handleRemoveProject(project.clientId);
        clearMessages('homeAdmin-project');
        showMessage('homeAdmin-project-success', data.success);
    } catch (exception) {
        showMessage('homeAdmin-project-error', "[DB Error] : " + exception.message);
    }
}