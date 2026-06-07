'use strict';

// section links
document.getElementById('button-homeCollaborator-logout').onclick = () => handleLogout();


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
    document.getElementById('homeCollaborator-welcome').textContent = `Welcome, ${user.name} ${user.surname}`;
    document.getElementById('homeCollaborator-profile-photo').src = `${BASE_URL}/images/${user.photo}`;
    await loadProjects();
}
initPage();

// load all the Projects
async function loadProjects() {
    // clear previous containers
    document.getElementById('homeCollaborator-projects-container').innerHTML = '';
    document.getElementById('homeCollaborator-projects-empty').style.display = 'none';
    document.getElementById('homeCollaborator-error').style.display = 'none';
    document.getElementById('homeCollaborator-stats-container').innerHTML = '';
    document.getElementById('homeCollaborator-stats-container').style.display = 'none';

    try {
        const response = await fetch(BASE_URL + '/api/collaborator/loadProjects', {
            method: 'GET'
        });
        const data = await response.json();

        // loading error
        if (!response.ok) {
            showMessage(`homeCollaborator-error`, data.error);
            return;
        }

        // load the Projects
        const container = document.getElementById('homeCollaborator-projects-container');
        const projectsList = document.createElement('ul');
        for (const project of data.assignments) {
            const projectItem = document.createElement('li');
            const projectLink = document.createElement('a');
            projectLink.innerHTML = `
                <strong>${escapeHtml(project.title)}</strong>
                &nbsp;|&nbsp; Duration: ${project.duration} month(s)
                &nbsp;|&nbsp; Admin: ${escapeHtml(project.admin)}
            `;
            projectLink.onclick = () => showProjectStats(project);
            projectItem.appendChild(projectLink);
            projectsList.appendChild(projectItem);
        }
        container.appendChild(projectsList);
    } catch (exception) {
        showMessage('homeCollaborator-error', '[DB Error] : ' + exception.message);
    }
}


// 2 ----- Show the stats of a Project
function showProjectStats(project) {
    // clear previous containers
    clearMessages('homeCollaborator');
    const container = document.getElementById('homeCollaborator-stats-container');
    container.innerHTML = '';
    container.style.display = '';

    // define table wrapper
    const title = document.createElement('h2');
    title.innerHTML = `
        Project ${escapeHtml(project.title)}
        &nbsp;|&nbsp; Admin ${project.admin}
    `;
    const wrapper = document.createElement('div');
    wrapper.className = 'table-wrapper';
    container.appendChild(title);
    container.appendChild(wrapper);

    // create table
    const table = document.createElement('table');
    const tableHeader = document.createElement('thead');
    const tableBody = document.createElement('tbody');
    wrapper.appendChild(table);
    table.appendChild(tableHeader);
    table.appendChild(tableBody);
        
    // populate table
    createTableHeader(tableHeader, project);
    for (const workPackage of project.workPackages) {
        for (const task of workPackage.tasks) {
            createTableBody(tableBody, project, workPackage, task);
        }
    }
}

// create table header
function createTableHeader(tableHeader, project) {
    const tableHeaderRow = document.createElement('tr');
    tableHeader.appendChild(tableHeaderRow);

    // define Work-Package column
    const workPackageHeader = document.createElement('th');
    workPackageHeader.textContent = 'WP';
    tableHeaderRow.appendChild(workPackageHeader);
    
    // define Task column
    const taskHeader = document.createElement('th');
    taskHeader.textContent = 'Task';
    tableHeaderRow.appendChild(taskHeader);
    
    // define months columns
    for (let month = 1; month <= project.duration; month++) {
        const workedHoursHeader = document.createElement('th');
        workedHoursHeader.textContent = `M${month} Worked Hours`;
        tableHeaderRow.appendChild(workedHoursHeader);
    }
}

// create table body
function createTableBody(tableBody, project, workPackage, task) {    
    const tableBodyRow = document.createElement('tr');
    tableBody.appendChild(tableBodyRow);
    
    // populate Work-Package column
    const workPackageBody = document.createElement('td');
    workPackageBody.textContent = `WP ${workPackage.orderNumber}`;
    tableBodyRow.appendChild(workPackageBody);

    // populate Task column
    const taskBody = document.createElement('td');
    taskBody.textContent = `T ${task.orderNumber}`;
    tableBodyRow.appendChild(taskBody);

    // populate months columns
    for (let month = 1; month <= project.duration; month++) {
        const workedHoursBody = document.createElement('td');
        const monthWorked = task.work.find(w => w.month === month);
        if (monthWorked !== undefined) {
            workedHoursBody.textContent = monthWorked.hours;
            workedHoursBody.contentEditable = 'true';
            workedHoursBody.addEventListener('mouseleave', () => updateWorkedHours(workedHoursBody, task, month));
        } else {
            workedHoursBody.textContent = '/';
        }
        tableBodyRow.appendChild(workedHoursBody);
    }
}


// 3 ----- Update worked hours for a cell
async function updateWorkedHours(cell, task, month) {
    clearMessages('homeCollaborator');
    const workedHours = Number(cell.textContent.trim());
    if (!Number.isInteger(workedHours) || workedHours < 0) {
        showMessage('homeCollaborator-error', "[Hours Error] : Worked hours must be at least 0!!!");
        cell.textContent = task.work.find(w => w.month === month).hours;
        return;
    }

    // fetch update
    try {
        const response = await fetch(BASE_URL + '/api/collaborator/logHours', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ task: task, month: month, hours: workedHours })
        });
        const data = await response.json();

        // update error
        if (!response.ok) {
            showMessage('homeCollaborator-error', data.error);
            cell.textContent = task.work.find(w => w.month === month).hours;
            return;
        }

        // update client-side
        task.work.find(w => w.month === month).hours = workedHours;
        showMessage('homeCollaborator-success', data.success);
    } catch (exception) {
        showMessage('homeCollaborator-error', '[DB Error] : ' + exception.message);
    }
}