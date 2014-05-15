OfflineDemo
===========

GWT Offline Application Demonstration, with SQL.js, Application Cache and Local Storage

## What is it ?

This project is a proof of concept for a web application supporting an offline mode, with persisted client storage.
It is written in Java for any web application container and for GWT on the client side.
The client side code uses HTML5 features like Application Cache and Local Storage. It also makes extensive use of [sql.js](https://github.com/kripken/sql.js), the Javascript port of [SQLite](http://www.sqlite.org/).

## Usage

do a 

```bash
mvn clean install tomcat7:run
```

to compile and run the project.

You can then access the [http://localhost:8080/offline-demo/](http://localhost:8080/offline-demo/) url to load the application in your browser.

## What is it more ?

