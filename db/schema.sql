# --------------- create Tables ---------------

# ----- 1. USERS
CREATE TABLE USERS(
    # attributes
    ID          int NOT NULL AUTO_INCREMENT,
    name        varchar(30) NOT NULL,
    surname     varchar(30) NOT NULL,
    username    varchar(30) NOT NULL,
    password    varchar(255) NOT NULL,
    photo       varchar(255) NOT NULL,
    role        enum(
                    'ADMIN',
                    'MANAGER',
                    'COLLABORATOR',
                    'MANAGER_COLLABORATOR'
                ),

    # constraints
    CONSTRAINT pk_USERS PRIMARY KEY (ID),
    CONSTRAINT uq_USERS_username UNIQUE (username)
);

# ----- 2. PROJECTS
CREATE TABLE PROJECTS(
    # attributes
    ID              int NOT NULL AUTO_INCREMENT,
    title           varchar(50) NOT NULL,
    monthsNumber    int NOT NULL CHECK ( monthsNumber >= 1 ),
    status          enum(
                        'CREATED',
                        'ASSIGNED',
                        'CONCLUDED'
                    ) DEFAULT 'CREATED' NOT NULL,
    admin           varchar(30) NOT NULL,
    manager         varchar(30),

    # constraints
    CONSTRAINT pk_PROJECTS PRIMARY KEY (ID),
    CONSTRAINT uq_PROJECT_title_ADMIN UNIQUE (admin, title),
    CONSTRAINT uq_PROJECT_title_MANAGER UNIQUE (manager, title),
    CONSTRAINT fk_PROJECTS_ADMIN FOREIGN KEY (admin) REFERENCES USERS(username),
    CONSTRAINT fk_PROJECTS_MANAGER FOREIGN KEY (manager) REFERENCES USERS(username)
);

# ----- 3.WORK_PACKAGES
CREATE TABLE WORK_PACKAGES(
    # attributes
    ID              int NOT NULL AUTO_INCREMENT,
    orderNumber     int NOT NULL CHECK ( orderNumber >= 1 ),
    title           varchar(50) NOT NULL,
    startingMonth   int NOT NULL CHECK ( startingMonth >= 1 ),
    endingMonth     int NOT NULL CHECK ( endingMonth >= startingMonth ),
    projectID       int NOT NULL,

    # constraints
    CONSTRAINT pk_WORK_PACKAGES PRIMARY KEY (ID),
    CONSTRAINT fk_WORK_PACKAGES_PROJECTS FOREIGN KEY (projectID) REFERENCES PROJECTS(ID),
    CONSTRAINT uq_WORK_PACKAGES_orderNumber UNIQUE (projectID, orderNumber)
);

# ----- 4.TASKS
CREATE TABLE TASKS(
    # attributes
    ID              int NOT NULL AUTO_INCREMENT,
    orderNumber     decimal(10,2) NOT NULL CHECK ( orderNumber >= 1.01 ),
    title           varchar(50) NOT NULL,
    description     TEXT,
    startingMonth   int NOT NULL CHECK ( startingMonth >= 1),
    endingMonth     int NOT NULL CHECK ( endingMonth >= startingMonth ),
    workPackageID   int NOT NULL,

    # constraints
    CONSTRAINT pk_TASKS PRIMARY KEY (ID),
    CONSTRAINT fk_TASKS_WORK_PACKAGES FOREIGN KEY (workPackageID) REFERENCES WORK_PACKAGES(ID),
    CONSTRAINT uq_TASKS_orderNumber UNIQUE (workPackageID, orderNumber)
);

# ----- 5.COLLABORATORS
CREATE TABLE COLLABORATORS(
    # attributes
    userID          int NOT NULL,
    taskID          int NOT NULL,
    month           int NOT NULL CHECK ( month >= 1 ),
    workedHours     int NOT NULL DEFAULT 0 CHECK ( workedHours >= 0 ),

    # constraints
    CONSTRAINT pk_COLLABORATORS PRIMARY KEY (userID, taskID, month),
    CONSTRAINT fk_COLLABORATORS_USERS FOREIGN KEY (userID) REFERENCES USERS(ID),
    CONSTRAINT fk_COLLABORATORS_TASKS FOREIGN KEY (taskID) REFERENCES TASKS(ID)
);

# ----- 6.PLANNING
CREATE TABLE PLANNING(
    # attributes
    taskID          int NOT NULL,
    month           int NOT NULL CHECK ( month >= 1 ),
    plannedHours    int NOT NULL DEFAULT 0 CHECK ( plannedHours >= 0 ),
    workedHours     int NOT NULL DEFAULT 0 check ( workedHours >= 0 ),

    # constraints
    CONSTRAINT pk_PLANNING PRIMARY KEY (taskID, month),
    CONSTRAINT fk_PLANNING_TASKS FOREIGN KEY (taskID) REFERENCES TASKS(ID)
);