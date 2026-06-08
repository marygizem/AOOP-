Homework

Homework 6

For Homework 6,implemented JDBC-based persistence for employee data. I created a MySQL database and employee table, added a DatabaseConnection class for database connectivity, implemented EmployeeJDBCDAO to retrieve employee records using SQL queries, and updated EmployeeService to use the JDBC DAO instead of the CSV DAO. This keeps persistence logic isolated in the DAO layer while preserving the existing business logic and application behavior.


Homework 7 

For Homework 7, I continued the migration from CSV-based persistence to JDBC-based persistence across the MotorPH Payroll System. I updated additional modules, including authentication and employee data access, to use JDBC DAOs instead of CSV DAOs. I implemented CredentialJDBCDAO for database-backed credential retrieval and updated AuthService to use the JDBC implementation. I also verified that the application compiles and runs successfully while preserving the existing workflows, business logic, and user interface behavior. This completed a larger portion of the transition to database persistence and further reduced the system’s dependency on CSV files.

Homework 8

For Homework 8, I implemented report generation functionality using the existing JDBC-based persistence and payroll logic. I created a PayslipReport data class and a ReportService to generate employee payslips and payroll summary reports. The implementation reuses payroll data and calculations from PayrollService instead of duplicating business logic. Report generation remains separated from persistence and GUI code, following the required reporting flow while maintaining the existing system behavior.
