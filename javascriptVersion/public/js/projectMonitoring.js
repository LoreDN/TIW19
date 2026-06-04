'use strict';

// section links
document.getElementById('button-homeManager-projectMonitoring-back').onclick = () => initPage();
document.getElementById('button-projectMonitoring-logout').onclick = () => handleLogout();


// 1 ----- Page Initialization
async function showProjectMonitoring() {
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
    showSection('section-homeManager-projectMonitoring');
    history.pushState(null, '', BASE_URL + '/homeManager/projectMonitoring');
    await loadAssignedProjects();
}

// load all the Projects
async function loadAssignedProjects() {
    // clear previous containers
    document.getElementById('homeManager-projectMonitoring-projects-container').innerHTML = '';
    document.getElementById('homeManager-projectMonitoring-projects-empty').style.display = 'none';
    document.getElementById('homeManager-projectMonitoring-stats-container').innerHTML = '';
    document.getElementById('homeManager-projectMonitoring-stats-container').style.display = 'none';
    document.getElementById('homeManager-projectMonitoring-error').style.display = 'none';

    try {
        const response = await fetch(BASE_URL + '/api/manager/loadProjects?status=ASSIGNED', {
            method: 'GET'
        });
        const data = await response.json();

        // loading error
        if (!response.ok) {
            showMessage('homeManager-projectMonitoring-error', data.error);
            return;
        }

        // check for empty container
        if (data.projects.length === 0) {
            const empty = document.getElementById('homeManager-projectMonitoring-projects-empty');
            empty.style.display = '';
            return;
        }

        // load the Projects
        const container = document.getElementById('homeManager-projectMonitoring-projects-container');
        const projectsList = document.createElement('ul');
        for (const project of data.projects) {
            const projectItem = document.createElement('li');
            projectItem.id = `homeManager-projectMonitoring-projects-${project.id}`;
            const projectLink = document.createElement('a');
            projectLink.href = '#';
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
        showMessage('homeManager-projectMonitoring-error', '[DB Error] : ' + exception.message);
    }
}


// 2 ----- Show the stats of a Project
function showProjectStats(project) {
    // clear previous containers
    const container = document.getElementById('homeManager-projectMonitoring-stats-container');
    container.innerHTML = '';
    container.style.display = '';

    // define table wrapper
    const title = document.createElement('h2');
    title.textContent = `Project ${project.title}`;
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

    // conclude button
    if (canBeConcluded(project)) {
        // form
        const concludeForm = document.createElement('form');
        concludeForm.id = 'homeManager-projectMonitoring-form';
        concludeForm.className = 'conclude-form';
        concludeForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            clearMessages('homeManager-projectMonitoring');
            await handleProjectConclusion(project.id, project.title);
        });
        wrapper.appendChild(concludeForm);

        // button
        const concludeButton = document.createElement('button');
        concludeButton.type = 'submit';
        concludeButton.textContent = 'Set Project to CONCLUDED status';
        concludeForm.appendChild(concludeButton);
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
        const plannedHoursHeader = document.createElement('th');
        plannedHoursHeader.textContent = `M${month} Planned Hours`;
        const workedHoursHeader = document.createElement('th');
        workedHoursHeader.textContent = `M${month} Worked Hours`;
        tableHeaderRow.appendChild(plannedHoursHeader);
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
        const plannedHoursBody = document.createElement('td');
        const workedHoursBody = document.createElement('td');
        const monthPlanned = task.plannedHours.find(p => p.month === month);
        const monthWorked = task.workedHours.find(w => w.month === month);
        plannedHoursBody.textContent = (monthPlanned) ? monthPlanned.plannedHours : '/';
        workedHoursBody.textContent = (monthWorked) ? monthWorked.workedHours : '/';  
        tableBodyRow.appendChild(plannedHoursBody);
        tableBodyRow.appendChild(workedHoursBody);
    }
}


// 3 ----- Check if a Project can be concluded
function canBeConcluded(project) {
    return project.workPackages.flatMap(workPackage => workPackage.tasks).every(task => {
        for (let month = task.startingMonth; month <= task.endingMonth; month++) {
            const monthPlanned = task.plannedHours.find(p => p.month === month);
            const monthWorked = task.workedHours.find(w => w.month === month);
            if (!monthPlanned || isNaN(monthPlanned.plannedHours) ||
                !monthWorked || isNaN(monthWorked.workedHours) ||
                Number(monthWorked.workedHours) < Number(monthPlanned.plannedHours)
            ) return false;
        } return true;
    });
}


// 4 ----- Handle Project Conclusion
async function handleProjectConclusion(projectID, projectTitle) {
    try {
        const response = await fetch(BASE_URL + '/api/manager/concludeProject', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ projectID, projectTitle })
        });
        const data = await response.json();

        // conclusion error
        if (!response.ok) {
            showMessage('homeManager-project-error', data.error);
            return;
        }

        // update client-side state
        showMessage('homeManager-projectMonitoring-success', data.success);
        document.getElementById(`homeManager-projectMonitoring-projects-${projectID}`).remove();
        document.getElementById('homeManager-projectMonitoring-stats-container').style.display = 'none';
    } catch (exception) {
        showMessage('homeManager-projectMonitoring-error', "[DB Error] : " + exception.message);
    }
}
