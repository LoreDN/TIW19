'use strict';

// cached data
let allProjects;

// section links
document.getElementById('button-homeManager-logout').onclick = () => handleLogout();


// 1 ----- Page Initialization
async function initPage() {
    const response = await fetch(BASE_URL + '/api/authentication/user', {
        method: 'GET'
    });
    const user = await response.json();

    // loading error
    if (!response.ok) {
        handleLogout();
        return;
    }

    // load the page
    showSection('section-homeManager');
    history.pushState(null, '', BASE_URL + '/homeManager');
    document.getElementById('homeManager-welcome').textContent = `Welcome, ${user.name} ${user.surname}`;
    document.getElementById('homeManager-profile-photo').src = `${BASE_URL}/images/${user.photo}`;
    await loadCreatedProjects();
}
initPage();

// load all the Projects 
async function loadCreatedProjects() {
    const forms = ['hours', 'collaborators', 'project'];
    const response = await fetch(BASE_URL + '/api/manager/loadProjects?status=CREATED', {
        method: 'GET'
    });
    const data = await response.json();

    // loading error
    if (!response.ok) {
        forms.forEach(form => {
            showMessage(`homeManager-${form}-error`, data.error);
        });
        return;
    }

    // populate the forms selection
    allProjects = data.projects;
    forms.forEach(form => {
        const selection = document.getElementById(`homeManager-${form}-selection`);

        // create label
        const label = document.createElement('label');
        label.htmlFor = `homeManager-${form}-selection-projects`;
        label.innerHTML = 'Reference Project Title:';
        selection.appendChild(label);

        // create select
        const select = document.createElement('select');
        select.id = `homeManager-${form}-selection-projects`;
        if (form !== 'project') select.addEventListener('change', () => projectSelected(form, select.value));
        selection.appendChild(select);
        
        // populate options
        allProjects.forEach(project => {
            const option = document.createElement('option');
            option.value = project.id;
            option.textContent = `Admin: ${project.admin} | Title: ${project.title}`;
            select.appendChild(option);
        });
        if (form !== 'project') projectSelected(form, select.value);
    });
}


// 2 ----- Project Selection
function projectSelected(form, projectId) {
    const selection = document.getElementById(`homeManager-${form}-selection`);
    const project = allProjects.find(p => p.id === parseInt(projectId, 10));
    if (!project) return;

    // clear previous selections
    ['tasks', 'workPackages'].forEach(level => {
        const oldMessage = document.getElementById(`homeManager-${form}-selection-${level}-empty`);
        if (oldMessage) oldMessage.remove();
        const oldSelect = document.getElementById(`homeManager-${form}-selection-${level}`);
        if (oldSelect) {
            oldSelect.previousElementSibling.remove();
            oldSelect.remove();
        }
    });

    // check for empty Project
    if (project.workPackages.length === 0) {
        const message = document.createElement('span');
        message.id = `homeManager-${form}-selection-workPackages-empty`;
        message.textContent = 'No Work-Packages have been created yet for this Project!';
        selection.appendChild(message);
        return;
    }

    // create label
    const label = document.createElement('label');
    label.htmlFor = `homeManager-${form}-selection-workPackages`;
    label.innerHTML = 'Work-Package Title:';
    selection.appendChild(label);

    // create select
    const select = document.createElement('select');
    select.id = `homeManager-${form}-selection-workPackages`;
    select.addEventListener('change', () => workPackageSelected(form, select.value));
    selection.appendChild(select);

    // populate options
    project.workPackages.forEach(workPackage => {
        const option = document.createElement('option');
        option.value = workPackage.id;
        option.textContent = `Title: ${workPackage.title} | Duration: M${workPackage.startingMonth} -> M${workPackage.endingMonth}`;
        select.appendChild(option);
    });
    workPackageSelected(form, select.value);
}

// Work-Package Selection
function workPackageSelected(form, workPackageId) {
    const selection = document.getElementById(`homeManager-${form}-selection`);
    const workPackage = allProjects.flatMap(p => p.workPackages).find(wp => wp.id === parseInt(workPackageId, 10));
    if (!workPackage) return;

    // clear previous selections
    const oldMessage = document.getElementById(`homeManager-${form}-selection-tasks-empty`);
    if (oldMessage) oldMessage.remove();
    const oldSelect = document.getElementById(`homeManager-${form}-selection-tasks`);
    if (oldSelect) {
        oldSelect.previousElementSibling.remove();
        oldSelect.remove();
    }

    // check for empty Work-Package
    if (workPackage.tasks.length === 0) {
        const message = document.createElement('span');
        message.id = `homeManager-${form}-selection-tasks-empty`;
        message.textContent = 'No Tasks have been created yet for this Work-Package!';
        selection.appendChild(message);
        return;
    }

    // create label
    const label = document.createElement('label');
    label.htmlFor = `homeManager-${form}-selection-tasks`;
    label.innerHTML = 'Task Title:';
    selection.appendChild(label);

    // create select
    const select = document.createElement('select');
    select.id = `homeManager-${form}-selection-tasks`;
    selection.appendChild(select);

    // populate options
    workPackage.tasks.forEach(task => {
        const option = document.createElement('option');
        option.value = task.id;
        option.textContent = `Title: ${task.title} | Duration: M${task.startingMonth} -> M${task.endingMonth}`;
        select.appendChild(option);
    });
}


// 3 ----- Handle Hours Assignment
document.getElementById('homeManager-hours-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    clearMessages('homeManager-hours');

    // get the form data
    const taskID = parseInt(document.getElementById('homeManager-hours-selection-tasks').value, 10);
    const month = parseInt(document.getElementById('homeManager-hours-month').value, 10);
    const plannedHours = parseInt(document.getElementById('homeManager-hours-plannedHours').value, 10);

    // client-side validation
    const task = allProjects.flatMap(project => project.workPackages)
        .flatMap(workPackage => workPackage.tasks)
        .find(task => task.id === taskID);
    let errorMessage = "";
    if (!taskID || !month || (!plannedHours && plannedHours !== 0))
        errorMessage = "All fields are required!!!";
    // task
    else if (!task)
        errorMessage = "[Hours Error] : Task not found!!!";
    // month
    else if (isNaN(month))
        errorMessage = "Month must be a number!!!";
    else if (month < 1)
        errorMessage = "[Hours Error] : Month must be greater than 0!!!";
    else if (month < task.startingMonth)
        errorMessage = "[Hours Error] : Month cannot be smaller than the Task starting one!!!";
    else if (month > task.endingMonth)
        errorMessage = "[Hours Error] : Month cannot be greater than the Task ending one!!!";
    // planned hours
    else if (isNaN(plannedHours))
        errorMessage = "Planned hours must be a number!!!";
    else if (plannedHours < 0)
        errorMessage = "[Hours Error] : Planned hours must be greater than 0!!!";

    // form error
    if (errorMessage) {
        showMessage('homeManager-hours-error', errorMessage);
        return;
    }

    // update client-side state
    const assignment = { "taskID": taskID, "month": month, "plannedHours": plannedHours };
    await handleHoursAssignment(assignment);
});

// fetch assignment
async function handleHoursAssignment(assignment) {
    try {
        const response = await fetch(BASE_URL + '/api/manager/assignHours', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ assignment })
        });
        const data = await response.json();
        
        // assignment error
        if (!response.ok) {
            showMessage('homeManager-hours-error', data.error);
            return;
        }

        // update client-side state
        showMessage('homeManager-hours-success', data.success);
        document.getElementById('homeManager-hours-form').reset();
    } catch (exception) {
        showMessage('homeManager-hours-error', "[DB Error] : " + exception.message);
    }
}

// 4 ----- Handle Collaborator Assignment
document.getElementById('homeManager-collaborators-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    clearMessages('homeManager-collaborators');

    // get the form data
    const taskID = parseInt(document.getElementById('homeManager-collaborators-selection-tasks').value, 10);
    const month = parseInt(document.getElementById('homeManager-collaborators-month').value, 10);
    const collaborator = document.getElementById('homeManager-collaborators-username').value;

    // client-side validation
    const task = allProjects.flatMap(project => project.workPackages)
        .flatMap(workPackage => workPackage.tasks)
        .find(task => task.id === taskID);
    let errorMessage = "";
    if (!taskID || !month || !collaborator)
        errorMessage = "All fields are required!!!";
    // task
    else if (!task)
        errorMessage = "[Collaborator Error] : Task not found!!!";
    // month
    else if (isNaN(month))
        errorMessage = "Month must be a number!!!";
    else if (month < 1)
        errorMessage = "[Collaborator Error] : Month must be greater than 0!!!";
    else if (month < task.startingMonth)
        errorMessage = "[Collaborator Error] : Month cannot be smaller than the Task starting one!!!";
    else if (month > task.endingMonth)
        errorMessage = "[Collaborator Error] : Month cannot be greater than the Task ending one!!!";

    // form error
    if (errorMessage) {
        showMessage('homeManager-collaborators-error', errorMessage);
        return;
    }

    // update client-side state
    const assignment = { "taskID": taskID, "month": month, "collaborator": collaborator };
    await handleCollaboratorAssignment(assignment);
});

// fetch assignment
async function handleCollaboratorAssignment(assignment) {
    try {
        const response = await fetch(BASE_URL + '/api/manager/assignCollaborator', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ assignment })
        });
        const data = await response.json();
        
        // assignment error
        if (!response.ok) {
            showMessage('homeManager-collaborators-error', data.error);
            return;
        }

        // update client-side state
        showMessage('homeManager-collaborators-success', data.success);
        document.getElementById('homeManager-collaborators-form').reset();
    } catch (exception) {
        showMessage('homeManager-collaborators-error', "[DB Error] : " + exception.message);
    }
}


// 4 ----- Handle Project Assignment
document.getElementById('homeManager-project-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    clearMessages('homeManager-project');

    // get the form data
    const projectID = parseInt(document.getElementById('homeManager-project-selection-projects').value, 10);

    // client-side validation
    const project = allProjects.find(project => project.id === projectID);
    let errorMessage = "";
    if (!projectID || !project)
        errorMessage = "Project not found!!!";

    // form error
    if (errorMessage) {
        showMessage('homeManager-project-error', errorMessage);
        return;
    }

    // update client-side state
    await handleProjectAssignment(projectID);
});

// fetch assignment
async function handleProjectAssignment(projectID) {
    try {
        const response = await fetch(BASE_URL + '/api/manager/assignProject', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ projectID })
        });
        const data = await response.json();
        
        // assignment error
        if (!response.ok) {
            showMessage('homeManager-project-error', data.error);
            return;
        }

        // update client-side state
        allProjects = allProjects.filter(project => project.id !== projectID);
        showMessage('homeManager-project-success', data.success);
        ['hours', 'collaborators', 'project'].forEach(form => {
            const select = document.getElementById(`homeManager-${form}-selection-projects`);
            
            // clear sibling selections if the removed project was selected
            if (parseInt(select.value, 10) === projectID) {
                ['tasks', 'workPackages'].forEach(level => {
                    const oldMessage = document.getElementById(`homeManager-${form}-selection-${level}-empty`);
                    if (oldMessage) oldMessage.remove();
                    const oldSelect = document.getElementById(`homeManager-${form}-selection-${level}`);
                    if (oldSelect) {
                        oldSelect.previousElementSibling.remove();
                        oldSelect.remove();
                    }
                });
            }
            select.querySelector(`option[value="${projectID}"]`).remove();
            if (form !== 'project') projectSelected(form, select.value);
        });
    } catch (exception) {
        showMessage('homeManager-project-error', "[DB Error] : " + exception.message);
    }
}