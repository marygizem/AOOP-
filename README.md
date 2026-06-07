# Homework 6

For Homework 6,implemented JDBC-based persistence for employee data. I created a MySQL database and employee table, added a DatabaseConnection class for database connectivity, implemented EmployeeJDBCDAO to retrieve employee records using SQL queries, and updated EmployeeService to use the JDBC DAO instead of the CSV DAO. This keeps persistence logic isolated in the DAO layer while preserving the existing business logic and application behavior.
