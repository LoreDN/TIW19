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

// rendering functions
function renderProject(project) {
    const container = document.getElementById('homeAdmin-projects-container');
    const card = document.createElement('div');
    card.className = 'project-draft-card';
    card.id = `project-card-${project.clientId}`;
    card.innerHTML = `
        <strong>${escapeHtml(project.title)}</strong>
        &nbsp;|&nbsp; Duration: ${project.duration} month(s)
        &nbsp;|&nbsp; Manager: ${escapeHtml(project.manager)}
    `;
    container.appendChild(card);
}

function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}