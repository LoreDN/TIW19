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


// handle HTML escaping
function escapeHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

// make an editable field (inline editing)
function makeEditable(object, field) {
    const span = document.createElement('span');
    span.className = 'editable';
    span.textContent = object[field];

    // inline editing
    span.onclick = () => {
        const input = document.createElement('input');
        input.type = (typeof object[field] === 'number') ? 'number' : 'text';
        input.value = object[field];
        input.className = 'editable-input';
        span.replaceWith(input);
        input.focus();

        // update value
        const save = () => {
            const value = (input.type === 'number') ? parseInt(input.value, 10) : input.value.trim();
            if (value && value !== '') object[field] = value;
            span.textContent = object[field];
            input.replaceWith(span);
        };
        input.onblur = save;
        input.onkeydown = (event) => {
            if (event.key === 'Enter') input.blur();
        };
    };
    return span;
}