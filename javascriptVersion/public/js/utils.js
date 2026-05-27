'use strict';

// define the base URL for API calls
const BASE_URL = '/TIW19-javascript';


// show a message
function showMessage(elementId, message) {
    const element = document.getElementById(elementId);
    element.textContent = message;
    element.style.display = 'block';
}

// clear messages
function clearMessages(prefix) {
    [prefix + '-error', prefix + '-success'].forEach(id => {
        const element = document.getElementById(id);
        element.textContent = '';
        element.style.display = 'none';
    });
}

// show the specified section
function showSection(sectionId) {
    document.querySelectorAll('[id^="section-"]').forEach(section => section.style.display = 'none');
    const section = document.getElementById(sectionId);
    if (section) section.style.display = '';
}


// show the specified Web-Page
async function showPage(route) {
    window.location.href = BASE_URL + '/' + route;
}

// handle Logout
async function handleLogout() {
    const response = await fetch(BASE_URL + '/api/authentication/logout', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    });
    const data = await response.json();

    // redirect to Login
    if (response.ok) showPage('login');
    else showMessage('logout-error', data.error);    
}