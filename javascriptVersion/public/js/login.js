'use strict';

// handle Login form submission
document.getElementById('login-form').addEventListener('submit', async function(event) {
    event.preventDefault();

    // get form values
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value.trim();

    // client-side validation
    let errorMessage = "";
    if (!username) {
        if (!password) errorMessage = "Username and password are required!!!";
        else errorMessage = "Username is required!!!";
    } else if (!password) errorMessage = "Password is required!!!";

    // form error
    if (errorMessage) {
        showMessage('login-error', errorMessage);
        return;
    }

    // fetch login request
    try {
        const response = await fetch(BASE_URL + '/api/authentication/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username,
                password
            })
        });
        const data = await response.json();

        // wrong credentials
        if (!response.ok) {
            showMessage('login-error', data.error);
            return;
        }

        // role redirect
        const user = data.user;
        if (user.role === 'ADMIN') showPage('homeAdmin');
        else if (user.role === 'MANAGER_COLLABORATOR') {
            showSection('section-chooseRole');
            document.getElementById('chooseRole-welcome').textContent = "Welcome, " + user.name + " " + user.surname;
        } else if (user.role === 'MANAGER') showPage('homeManager');
        else if (user.role === 'COLLABORATOR') showPage('homeCollaborator');
    } catch (exception) {
        showMessage('login-error', "[DB Error] : " + exception.message);
    }
});


// handle Choose-Role links
document.getElementById('button-chooseRole-Manager').addEventListener('click', function() {
    showPage('homeManager');
});

document.getElementById('button-chooseRole-Collaborator').addEventListener('click', function() {
    showPage('homeCollaborator');
});

document.getElementById('button-chooseRole-Logout').addEventListener('click', function() {
    handleLogout();
});