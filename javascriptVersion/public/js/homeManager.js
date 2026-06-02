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
    document.getElementById('homeManager-welcome').textContent = `Welcome, ${user.name} ${user.surname}`;
    document.getElementById('homeManager-profile-photo').src = `${BASE_URL}/images/${user.photo}`;
    await loadProjects();
}
initPage();

// load all the Projects 
async function loadProjects() {
    const forms = ['hours', 'collaborators', 'project'];
    const response = await fetch(BASE_URL + '/api/manager/loadProjects', {
        method: 'GET'
    })
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