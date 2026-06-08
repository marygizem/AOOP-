package service;

import java.util.List;
import java.util.Locale;

import dao.CredentialJDBCDAO;
import dao.CredentialDAO;
import model.CredentialRecord;

public class AuthService {
    public enum AuthRole {
        ADMIN,
        HR,
        FINANCE,
        IT,
        EMPLOYEE
    }

    public static class AuthResult {
        private final AuthRole role;
        private final String employeeNumber;
        private final String username;

        public AuthResult(AuthRole role, String employeeNumber, String username) {
            this.role = role;
            this.employeeNumber = employeeNumber;
            this.username = username;
        }

        public AuthRole getRole() {
            return role;
        }

        public String getEmployeeNumber() {
            return employeeNumber;
        }

        public String getUsername() {
            return username;
        }
    }

    private final CredentialDAO credentialDAO;
    private final EmployeeService employeeService;

    public AuthService() {
        this.credentialDAO = new CredentialJDBCDAO();
        this.employeeService = new EmployeeService();
    }

    public AuthResult authenticate(String loginInput, String password, String credentialCsvFile) throws Exception {
        List<CredentialRecord> credentials = credentialDAO.loadAllCredentials(credentialCsvFile);
        if (credentials == null || credentials.isEmpty()) {
            return null;
        }

        for (int i = 0; i < credentials.size(); i++) {
            CredentialRecord record = credentials.get(i);
            String employeeNumber = safeTrim(record.getEmployeeNumber());
            String email = safeTrim(record.getEmailAddress());
            String username = safeTrim(record.getUserName());
            String storedPassword = safeTrim(record.getPassword());
            String status = safeTrim(record.getStatus());

            boolean loginMatched = equalsIgnoreCase(loginInput, username)
                    || equalsIgnoreCase(loginInput, email)
                    || loginInput.equals(employeeNumber);

            if (!"".equals(status) && !"ACTIVE".equalsIgnoreCase(status)) {
                continue;
            }

            if (loginMatched && PasswordService.matches(password, storedPassword)) {
                if (!PasswordService.isHashed(storedPassword)) {
                    upgradePasswordHash(credentials, i, password, credentialCsvFile);
                }
                AuthRole role = resolveRole(record.getRole(), username, employeeNumber);
                return new AuthResult(role, employeeNumber, username);
            }
        }

        return null;
    }

    public AuthResult authenticateLegacy(String loginInput, String password, String employeeCsvFile) {
        if ("admin".equals(loginInput) && "123".equals(password)) {
            return new AuthResult(AuthRole.ADMIN, "", "admin");
        }
        if (("manager".equals(loginInput) || "hr".equals(loginInput)) && "123".equals(password)) {
            return new AuthResult(AuthRole.HR, "", loginInput);
        }
        if (("payroll".equals(loginInput) || "payrollofficer".equals(loginInput)
                || "finance".equals(loginInput)) && "123".equals(password)) {
            return new AuthResult(AuthRole.FINANCE, "", loginInput);
        }
        if ("it".equals(loginInput) && "123".equals(password)) {
            return new AuthResult(AuthRole.IT, "", "it");
        }
        if ("123".equals(password) && isExistingEmployeeNumber(loginInput, employeeCsvFile)) {
            return new AuthResult(AuthRole.EMPLOYEE, loginInput, loginInput);
        }
        return null;
    }

    public boolean isExistingEmployeeNumber(String employeeNumber, String employeeCsvFile) {
        if (employeeNumber == null || employeeNumber.isBlank()) {
            return false;
        }

        try {
            List<String[]> data = employeeService.getEmployees(employeeCsvFile);
            for (int i = 1; i < data.size(); i++) {
                String[] row = data.get(i);
                if (row.length > 0 && row[0].trim().equals(employeeNumber.trim())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private AuthRole resolveRole(String explicitRole, String username, String employeeNumber) {
        if (explicitRole != null && !explicitRole.isBlank()) {
            try {
                return AuthRole.valueOf(explicitRole.trim().toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String u = username == null ? "" : username.trim().toLowerCase();
        if ("admin".equals(u)) {
            return AuthRole.ADMIN;
        }
        if ("hr".equals(u) || "manager".equals(u)) {
            return AuthRole.HR;
        }
        if ("payroll".equals(u) || "payrollofficer".equals(u) || "finance".equals(u)) {
            return AuthRole.FINANCE;
        }
        if ("it".equals(u)) {
            return AuthRole.IT;
        }
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            return AuthRole.EMPLOYEE;
        }
        return AuthRole.EMPLOYEE;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void upgradePasswordHash(List<CredentialRecord> credentials,
                                     int index,
                                     String plainPassword,
                                     String credentialCsvFile) {
        try {
            CredentialRecord old = credentials.get(index);
            credentials.set(index, new CredentialRecord(
                old.getEmployeeNumber(),
                old.getEmailAddress(),
                old.getUserName(),
                PasswordService.hash(plainPassword),
                old.getRole(),
                old.getStatus()
            ));
            credentialDAO.saveAllCredentials(credentialCsvFile, credentials);
        } catch (Exception ignored) {
            // Do not block login if this persistence step fails.
        }
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
    }
}