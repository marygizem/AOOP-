# MotorPH Basic Payroll System (Group 6)

## Project Overview
This project is a Java desktop payroll system built with Swing and Maven.
It implements a layered architecture (`model`, `dao`, `service`, `view`) and applies core Object-Oriented Programming (OOP) principles:
- Encapsulation
- Abstraction
- Inheritance
- Polymorphism

The system supports role-based access for Admin, HR, Finance, IT, and Employee users, with CSV-backed data storage.

---

## Objectives
- Build a maintainable payroll system using OOP and clean separation of concerns.
- Implement employee management, attendance, leave filing, payroll computation, and payslip generation.
- Ensure deductions are based on statutory CSV contribution tables.
- Demonstrate role-based workflows in a desktop UI.

---

## Tech Stack
- Language: Java
- UI: Java Swing
- Build Tool: Maven (`mvnw.cmd`)
- Data Storage: CSV files in `src/main/resource/`

---

## Project Structure
```text
src/main/java/
  model/      -> Domain models and OOP entities
  dao/        -> CSV data access interfaces and implementations
  service/    -> Business logic and application services
  view/       -> Swing dashboards and dialogs

src/main/resource/
  *.csv       -> Employee, attendance, credentials, leave, settings, payroll history, contribution tables
```

---

## User Roles and Major Features

### 1. Admin
- View and manage employee records.
- Add, update, delete employee data with validation.
- Access admin operations and navigate to modules.

### 2. HR (Manager)
- View team members.
- Approve/reject leave requests.
- View leave history and attendance summaries.
- View payroll summaries and reports.

### 3. Finance (Payroll Officer)
- Process payroll per selected month/year.
- View payroll summary and payroll history.
- Configure strict attendance mode and allowance overrides.
- Generate/export payslips.

### 4. IT
- Manage account roles.
- Lock/unlock accounts.
- Reset passwords.

### 5. Employee
- View personal profile details.
- Time in/time out and view attendance logs.
- File leave requests and check leave history.
- View payslip details.

---

## Data Sources (CSV)
Core files in `src/main/resource/`:
- `MotorPHEmployeeData-EmployeeDetails.csv`
- `EmployeeDetails_AdminView.csv`
- `MotorPHcredentialLogin.csv`
- `MotorPH_Attendance.csv`
- `LeaveRequests.csv`
- `PayrollRunHistory.csv`
- `PayrollSettings.csv`

Statutory contribution tables:
- `SSS Contribution.csv`
- `Philhealth Contribution.csv`
- `Pag-ibig Contribution.csv`
- `Witholding Tax.csv`

---

## Payroll and Deduction Logic
- Payroll computation uses attendance-adjusted earnings in `PayrollService`.
- Net pay is guarded so it cannot become negative.
- Statutory deductions are loaded from contribution CSVs in `StatutoryDeductionService`:
  - SSS
  - PhilHealth
  - Pag-IBIG
  - Withholding Tax
- Finance deduction settings do not manually override statutory percentages.
- Strict attendance mode is configurable (`no attendance = no pay`).

---

## Authentication
- Login uses CSV credentials from `MotorPHcredentialLogin.csv` via `AuthService.authenticate(...)`.
- Role routing is handled in `UserLogin`.
- Legacy hardcoded fallback login is disabled for final compliance.

---

## OOP and Layered Design Evidence

### Encapsulation
- Model classes keep fields private/protected and expose controlled getters/setters.
- Validation logic is encapsulated in service/model methods.

### Abstraction
- `Employee` is an abstract base class for employee behavior.
- DAO interfaces (`EmployeeDAO`, `CredentialDAO`, `LeaveDAO`) define abstract data contracts.

### Inheritance
- `RegularEmployee`, `AdminEmployee`, `ManagerEmployee`, `PayrollOfficerEmployee` derive from `Employee` hierarchy.

### Polymorphism
- DAO interfaces are consumed through implementations (`*CSVDAO`).
- Employee subtypes are handled through common base references where applicable.

### Layered Architecture
- `view` only handles UI events and rendering.
- `service` contains business logic.
- `dao` handles data persistence.
- `model` represents domain state.

---

## Class-by-Class Summary

### model/
- `Employee`: Abstract base employee model.
- `RegularEmployee`: Standard employee type.
- `AdminEmployee`: Admin employee subtype.
- `ManagerEmployee`: HR/manager subtype.
- `PayrollOfficerEmployee`: Finance/payroll subtype.
- `Payroll`: Payroll result model (basic, allowances, deductions, net).
- `Allowance`: Allowance components.
- `Deduction`: Deduction components.
- `GovernmentDetails`: SSS/PhilHealth/TIN/Pag-IBIG fields.
- `CredentialRecord`: Credential row model.

### dao/
- `EmployeeDAO`, `CredentialDAO`, `LeaveDAO`: Data access interfaces.
- `EmployeeCSVDAO`: Employee CSV load/parse.
- `CredentialCSVDAO`: Credential CSV load/save.
- `LeaveCSVDAO`: Leave CSV append operations.

### service/
- `EmployeeService`: Employee retrieval and search.
- `AuthService`: Credential authentication and role resolution.
- `AttendanceService`: Time in/out and attendance retrieval.
- `LeaveService`: Leave submit/review/history flows.
- `PayrollService`: Payroll computation and history operations.
- `StatutoryDeductionService`: Statutory deduction calculation from CSV tables.
- `PayrollConfigService`: Payroll settings persistence (allowances/strict mode).
- `AdminEmployeeManagementService`: Admin CRUD and validation.
- `ITCredentialService`: IT account admin actions.
- `BonusService`: Bonus grouping and totals.
- `ResourcePathService`: Centralized resource path helper.

### view/
- `APPmain`: Application launcher.
- `UserLogin`: Login and role-based navigation.
- `AdminUserInterface`: Admin dashboard.
- `HRDashboard`: HR dashboard and reports.
- `FinanceDashboard`: Payroll processing and finance tools.
- `ITDashboard`: IT account operations.
- `EmployeeDashboard`: Employee self-service dashboard.
- `EmployeeFormDialog`: Employee create/edit dialog.
- `LeaveFilingDialog`: Leave request dialog.
- `LeaveHistoryDialog`: Leave history dialog.
- `PayslipDialog`: Payslip view dialog.
- `Registration`: Registration form UI.

---

## Build and Run

### Compile
```powershell
.\mvnw.cmd -q -DskipTests compile
```

### Run Tests
```powershell
.\mvnw.cmd -q test
```

### Run App
Run `view.APPmain` from your IDE or configured Java run profile.

---

## Current Validation Status
- Build status: PASS
- Test status: PASS
- Payroll negative-history issue: FIXED
- Duplicate employee numbers: CLEANED
- CSV-only authentication path: ENFORCED

---

## Notes for Evaluation
- Contribution and withholding tables are read from CSV resources.
- Payroll history is persisted in CSV for audit tracking.
- Role-based dashboards map directly to project requirement scenarios.
- The codebase demonstrates OOP principles and layered architecture in a practical payroll workflow.
