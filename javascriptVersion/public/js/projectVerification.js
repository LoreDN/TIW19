'use strict';

// section links
document.getElementById('button-homeAdmin-back').onclick = () => initPage();
document.getElementById('button-projectVerification-logout').onclick = () => handleLogout();


// 1 ----- Page Initialization
async function showProjectVerification() {
    const response = await fetch(BASE_URL + '/api/authentication/user', {
        method: 'GET'
    });
    const user = await response.json();

    // loading error
    if (!response.ok) {
        handleLogout();
        return;
    }

    // load the projects
    showSection('section-homeAdmin-projectVerification');
    history.pushState(null, '', BASE_URL + '/homeAdmin/projectVerification');
    await loadProjects();
}


// 2 ----- Load all the Projects 
async function loadProjects() {
    // clear previous containers
    ['created', 'assigned', 'concluded'].forEach(status => {
        document.getElementById(`projectVerification-${status}-container`).innerHTML = '';
        document.getElementById(`projectVerification-${status}-empty`).style.display = 'none';
    });

    try {
        const response = await fetch(BASE_URL + '/api/admin/loadProjects', {
            method: 'GET'
        });
        const data = await response.json();

        // loading error
        if (!response.ok) {
            showMessage('projectVerification-error', data.error);
            return;
        }

        // load the Projects for each category
        for (const project of data.projects) {
            const status = project.status.toLowerCase();
            const container = document.getElementById(`projectVerification-${status}-container`);
            renderProject(container, project, status)
        }

        // check for empty containers
        ['created', 'assigned', 'concluded'].forEach(status => {
            const container = document.getElementById(`projectVerification-${status}-container`);
            const empty = document.getElementById(`projectVerification-${status}-empty`);
            if (container.children.length === 0) empty.style.display = '';
        });
    } catch (exception) {
        showMessage('projectVerification-error', "[DB Error] : " + exception.message);
    }
}

// render a Project
function renderProject(container, project, status) {
    const projectCard = document.createElement('div');
    projectCard.className = `project-card status-${status}`;
    container.appendChild(projectCard);

    // card content
    const header = document.createElement('h3');
    header.innerHTML = `
        <strong>${escapeHtml(project.title)}</strong>
        &nbsp;|&nbsp; Duration: ${project.duration} month(s)
        &nbsp;|&nbsp; Manager: ${escapeHtml(project.manager)}
    `;
    projectCard.appendChild(header);

    // Work-Packages cards
    if (project.workPackages.length === 0) {
        const empty = document.createElement('span');
        empty.innerHTML = 'No Work-Packages have been created yet for this Project!';
        projectCard.appendChild(empty);
    } else {
        for (const workPackage of project.workPackages) {
            renderWorkPackage(projectCard, workPackage);
        }
    }
}

// render a Work-Package
function renderWorkPackage(container, workPackage) {
    const workPackageCard = document.createElement('ul');
    workPackageCard.className = 'workPackage-card';
    container.appendChild(workPackageCard);

    // card content
    const header = document.createElement('div');
    header.className = 'workPackage-header';
    header.innerHTML = `
        <strong>WP ${workPackage.orderNumber}:</strong> ${escapeHtml(workPackage.title)}
        &nbsp;|&nbsp; Duration: M${workPackage.startingMonth} -> M${workPackage.endingMonth}
    `;
    workPackageCard.appendChild(header);

    // Tasks list
    if (workPackage.tasks.length === 0) {
        const empty = document.createElement('span');
        empty.innerHTML = 'No Tasks have been created yet for this Work-Package!';
        workPackageCard.appendChild(empty);
    } else {
        const tasksList = document.createElement('ul');
        workPackageCard.appendChild(tasksList);
        for (const task of workPackage.tasks) {
            renderTask(tasksList, task);
        }
    }
}

// render a Task
function renderTask(container, task) {
    const taskElement = document.createElement('li');
    taskElement.innerHTML = `
        <strong>T ${task.orderNumber}:</strong> ${escapeHtml(task.title)}
        &nbsp;|&nbsp; Duration: M${task.startingMonth} -> M${task.endingMonth}
        &nbsp;|&nbsp; Total Planned Hours: ${task.plannedHours.reduce((sum, p) => sum + p.plannedHours, 0)}
    `;
    container.appendChild(taskElement);
}