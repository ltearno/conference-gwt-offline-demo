OfflineDemo
===========

GWT Offline Application Demonstration, with SQL.js, Application Cache and Local Storage

## What is it ?

This project is a proof of concept for a web application supporting an offline mode, with persisted client storage.
It is written in Java for any web application container and for GWT on the client side.
The client side code uses HTML5 features like Application Cache and Local Storage. It also makes extensive use of [sql.js](https://github.com/kripken/sql.js), the Javascript port of [SQLite](http://www.sqlite.org/).

## Usage

First, you need to have a MySQL or MariaDB server running on your local host. Default credentials used are user root with no password. Of course those are defined in the [persistence.xml](src/META-INF/persistence.xml) file.

Then, do a 

```bash
mvn clean install tomcat7:run
```

to compile and run the project.

You can then access the [http://localhost:8080/offline-demo/](http://localhost:8080/offline-demo/) url to load the application in your browser.

## What is it more ?

Having SQL on the client side
sql.js
mini orm => get results on the left side, no callbacks ! make complex queries

Persisting the data
what to persist ? sql db

Persisting the application

Synchronizing
Asking and detecting changes
Conflict detection and resolution

