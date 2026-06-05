'use strict';

// section links
document.getElementById('button-homeManager-collaboratorsMonitoring-back').onclick = () => initPage();
document.getElementById('button-collaboratorsMonitoring-logout').onclick = () => handleLogout();


// 1 ----- Page Initialization
async function showCollaboratorsMonitoring() {
    const response = await fetch(BASE_URL + '/api/authentication/user', {
        method: 'GET'
    });
    const user = await response.json();

    // loading error
    if (!response.ok) {
        handleLogout();
        return;
    }
    
    // load the Collaborators
    showSection('section-homeManager-collaboratorsMonitoring');
    history.pushState(null, '', BASE_URL + '/homeManager/collaboratorsMonitoring');
    await loadAssignedCollaborators();
}

// load all the Collaborators
async function loadAssignedCollaborators() {
    // clear previous containers
    document.getElementById('homeManager-collaboratorsMonitoring-collaborators-container').innerHTML = '';
    document.getElementById('homeManager-collaboratorsMonitoring-collaborators-empty').style.display = 'none';
    document.getElementById('homeManager-collaboratorsMonitoring-stats-container').innerHTML = '';
    document.getElementById('homeManager-collaboratorsMonitoring-stats-container').style.display = 'none';
    document.getElementById('homeManager-collaboratorsMonitoring-error').style.display = 'none';

    try {
        const response = await fetch(BASE_URL + '/api/manager/loadCollaborators', {
            method: 'GET'
        });
        const data = await response.json();

        // loading error
        if (!response.ok) {
            showMessage('homeManager-collaboratorsMonitoring-error', data.error);
            return;
        }

        // check for empty container
        if (data.collaborators.length === 0) {
            const empty = document.getElementById('homeManager-collaboratorsMonitoring-collaborators-empty');
            empty.style.display = '';
            return;
        }

        // load the Collaborators
        const container = document.getElementById('homeManager-collaboratorsMonitoring-collaborators-container');
        const collaboratotorsList = document.createElement('ul');
        for (const collaborator of data.collaborators) {
            // load new Collaborator
            const collaboratotorItem = document.createElement('li');
            collaboratotorItem.id = `homeManager-collaboratorsMonitoring-collaborators-${collaborator.id}`;
            const collaboratorLink = document.createElement('a');
            collaboratorLink.innerHTML = `
                <strong>${escapeHtml(collaborator.username)}</strong>
                &nbsp;|&nbsp; Full name: ${escapeHtml(collaborator.name)} ${escapeHtml(collaborator.surname)}
            `;
            collaboratorLink.onclick = () => showCollaboratorStats(collaborator);
            collaboratotorItem.appendChild(collaboratorLink);
            collaboratotorsList.appendChild(collaboratotorItem);
        }
        container.appendChild(collaboratotorsList);
    } catch (exception) {
        showMessage('homeManager-collaboratorsMonitoring-error', '[DB Error] : ' + exception.message);
    }
}


// 2 ----- Show the stats of a Collaborator
function showCollaboratorStats(collaborator) {
    // clear previous containers
    const container = document.getElementById('homeManager-collaboratorsMonitoring-stats-container');
    container.innerHTML = '';
    container.style.display = '';

    // define table wrapper
    const title = document.createElement('h2');
    title.textContent = `Collaborator ${collaborator.username}`;
    const wrapper = document.createElement('div');
    wrapper.className = 'table-wrapper';
    container.appendChild(title);
    container.appendChild(wrapper);

    for (let i = 0; i < collaborator.projects.length; i++) {
        const project = collaborator.projects[i];

        // create table
        const table = document.createElement('table');
        const tableHeader = document.createElement('thead');
        const tableBody = document.createElement('tbody');
        wrapper.appendChild(table);
        table.appendChild(tableHeader);
        table.appendChild(tableBody);
        
        // populate table
        createCollaboratorTableHeader(tableHeader, project, i + 1);
        for (const workPackage of project.workPackages) {
            for (const task of workPackage.tasks) {
                createCollaboratorTableBody(tableBody, project, workPackage, task);
            }
        }
    }
}

// create table header
function createCollaboratorTableHeader(tableHeader, project, counter) {
    const projectHeaderRow = document.createElement('tr');
    const tableHeaderRow = document.createElement('tr');
    tableHeader.appendChild(projectHeaderRow);
    tableHeader.appendChild(tableHeaderRow);

    // define Project header
    const projectHeader = document.createElement('th');
    projectHeader.textContent = `Project N${counter}: ${project.title}`;
    projectHeader.colSpan = project.duration + 2;
    projectHeaderRow.appendChild(projectHeader);

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
function createCollaboratorTableBody(tableBody, project, workPackage, task) {    
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
        workedHoursBody.textContent = (monthWorked) ? monthWorked.hours : '/';
        tableBodyRow.appendChild(workedHoursBody);
    }
}