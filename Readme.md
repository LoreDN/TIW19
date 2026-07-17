# Project Management and Reporting (TIW19)

_Hour-reporting and Project-tracking system, delivered as two independent implementations of the same specification._

---

## 🎓 About

This repository holds the exam project ("Progetto Tecnologie Informatiche per il Web", internally referred to as **TIW19**) for a university web-development course.<br>
The assignment specifies one system — a tool for planning and reporting hours worked on a Project — but requires it to be delivered as **two separate, independent web applications**.

1. **Servlet version** — server-rendered with Java Servlets (via Tomcat) + Thymeleaf, no client-side JavaScript; every interaction is a full page load, validated on both client and server, following a Post-Redirect-Get pattern.
2. **JavaScript version** — a single page per role, with a Node.js/Express backend exposing a JSON API; consumed by a vanilla-JS frontend that updates the DOM asynchronously instead of reloading the page.

Both versions satisfy the exact same spec — same three roles, same validation rules, same database schema — so the two folders below aren't a fork of one another.<br>
They're two independent answers to the same design problem, which was the assignment for this project.

---

## 🧩 The application

Three roles share one hierarchy of data:

```
Project - (title, duration, status: CREATED -> ASSIGNED -> CONCLUDED)
 |- Work Package - (order number, title, month range)
     |- Task - (order number, title, description, month range)
         |- planned hours - one value per (task, month)
         |- worked hours - one value per (task, collaborator, month)
```

| Role             | Responsibilities                                                                                                                                                                                                  |
| ---------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Admin**        | Defines a Project and its structure (Work-Packages, Tasks), assigns it to a Manager and sets it as `CREATED`.<br>Can only see and manage the Projects they created.                                          |
| **Manager**      | Assigns Collaborators and planned hours to each Task, then sets the Project as `ASSIGNED`.<br>Monitors progress and sets the project as `CONCLUDED` once worked hours meet or exceed planned hours on every Task. |
| **Collaborator** | Logs worked hours, month by month, on the Tasks they've been assigned to.                                                                                                                                         |


Users whose technical role covers both Manager and Collaborator duties pick the home page to use right after login.<br>
Status transitions such as concluding a Project are always re-validated server-side.

---

## 📁 Repository structure 

```
.
|-- servletVersion/          # Java Servlets + Thymeleaf
|   |-- src/main/
|   |   |-- java/tiw19/servletVersion/
|   |   |   |
|   |   |   |-- dao/         # DAO implementation
|   |   |   |-- model/       # Model implementation
|   |   |   |-- servlet/     # Servlets implementation
|   |   |   .
|   |   |-- resources/       # additional resources (DB)
|   |   |-- webapp/          # public folder for HTML / CSS files
|   .   .
|-- javascriptVersion/       # Node.js/Express + vanilla JS
|   |
|   |-- dao/                 # DAO implementation
|   |-- model/               # Model implementation
|   |-- routes/              # Routes implementation
|   |-- public/              # public folder for HTML / CSS / JS files
|   |-- server.js            # server entry-point
|   .
|-- db/
|	|-- data/                # container-restricted DB
|	|-- schema.sql           # schema for the MariaDB instance
|	.
|-- doc/                     # UML sequence diagrams (for both versions)
|-- .env                     # DB credentials
|-- docker-compose.yml       # compose file for deployment
.
```

---

## 🛠️ Tech stack

|                 | Servlet version                                     | JavaScript version                                                                   |
| --------------- | --------------------------------------------------- | ------------------------------------------------------------------------------------ |
| Backend         | Java Servlets on Tomcat 10.1                        | Node.js 18 + Express                                                                 |
| Views           | Thymeleaf 3.1.4<br>Server-rendered data             | Vanilla JS SPA<br>Main pages with hidden sections                                    |
| DB access       | JDBC + custom DAO classes                           | `mysql2` + custom DAO classes                                                        |
| Client ↔ server | Full page reloads<br>Post-Redirect-Get with session | Async `fetch` API calls<br>Partial DOM updates<br>URL update via `history.pushState` |
| Client-side JS  | None                                                | Vanilla JS only, no frameworks                                                       |


Shared by both versions:

- **Database** — MariaDB schema (`USERS`, `PROJECTS`, `WORK_PACKAGES`, `TASKS`, `COLLABORATORS`, `PLANNING`)
- **Containers** — Docker via docker-compose
- **Config** — a single `.env` as the source of truth for DB credentials (`MARIADB_*` variables), copied in the `../resources/` folder for the Servlet version.

---

## 🚀 Deployment and Development

```bash
docker-compose up
```

> - **mariadb:** shared database between the two versions, defined in the `db/` folder.
> - **tomcat:** Web-Server for the Servlet version, works on the `.war` file generated by the build process (via *Maven*).
> - **nodejs:** NodeJS/Express app which handles the deployment of the JavaScript version.

---

## 👤 Author

Built by **Lorenzo Di Napoli**.<br>
Born as the exam project for the class: `Informatic Technologies for the Web — 2026`.