package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dao.CredentialCSVDAO;
import dao.CredentialDAO;
import model.CredentialRecord;

/**
 * IT-owned application service for credential account administration.
 */
public class ITCredentialService {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";

    public static class AccountRow {
        private final String employeeNumber;
        private final String email;
        private final String username;
        private final String role;
        private final String status;

        public AccountRow(String employeeNumber, String email, String username, String role, String status) {
            this.employeeNumber = employeeNumber;
            this.email = email;
            this.username = username;
            this.role = role;
            this.status = status;
        }

        public String getEmployeeNumber() {
            return employeeNumber;
        }

        public String getEmail() {
            return email;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public String getStatus() {
            return status;
        }
    }

    private final CredentialDAO credentialDAO;

    public ITCredentialService() {
        this.credentialDAO = new CredentialCSVDAO();
    }

    public List<AccountRow> loadAccounts(String csvPath) throws Exception {
        List<CredentialRecord> credentials = credentialDAO.loadAllCredentials(csvPath);
        List<AccountRow> rows = new ArrayList<>();

        for (CredentialRecord record : credentials) {
            String role = resolveRole(record);
            String status = normalizeStatus(record.getStatus());
            rows.add(new AccountRow(
                safe(record.getEmployeeNumber()),
                safe(record.getEmailAddress()),
                safe(record.getUserName()),
                role,
                status
            ));
        }

        return rows;
    }

    public void resetPassword(String csvPath, String username, String newPassword) throws Exception {
        String hashedPassword = PasswordService.hash(newPassword);
        mutateByUsername(csvPath, username, record -> new CredentialRecord(
            record.getEmployeeNumber(),
            record.getEmailAddress(),
            record.getUserName(),
            hashedPassword,
            resolveRole(record),
            normalizeStatus(record.getStatus())
        ));
    }

    public void updateRole(String csvPath, String username, AuthService.AuthRole newRole) throws Exception {
        mutateByUsername(csvPath, username, record -> new CredentialRecord(
            record.getEmployeeNumber(),
            record.getEmailAddress(),
            record.getUserName(),
            record.getPassword(),
            newRole.name(),
            normalizeStatus(record.getStatus())
        ));
    }

    public void setAccountLocked(String csvPath, String username, boolean locked) throws Exception {
        mutateByUsername(csvPath, username, record -> new CredentialRecord(
            record.getEmployeeNumber(),
            record.getEmailAddress(),
            record.getUserName(),
            record.getPassword(),
            resolveRole(record),
            locked ? STATUS_LOCKED : STATUS_ACTIVE
        ));
    }

    private interface Mutator {
        CredentialRecord map(CredentialRecord record);
    }

    private void mutateByUsername(String csvPath, String username, Mutator mutator) throws Exception {
        List<CredentialRecord> credentials = credentialDAO.loadAllCredentials(csvPath);
        boolean updated = false;

        for (int i = 0; i < credentials.size(); i++) {
            CredentialRecord record = credentials.get(i);
            if (safe(record.getUserName()).equalsIgnoreCase(safe(username))) {
                credentials.set(i, mutator.map(record));
                updated = true;
                break;
            }
        }

        if (!updated) {
            throw new IllegalArgumentException("Account not found: " + username);
        }

        credentialDAO.saveAllCredentials(csvPath, credentials);
    }

    private String resolveRole(CredentialRecord record) {
        String role = safe(record.getRole());
        if (!role.isBlank()) {
            return role.toUpperCase(Locale.ENGLISH);
        }

        String username = safe(record.getUserName()).toLowerCase(Locale.ENGLISH);
        if ("admin".equals(username)) {
            return AuthService.AuthRole.ADMIN.name();
        }
        if ("hr".equals(username) || "manager".equals(username)) {
            return AuthService.AuthRole.HR.name();
        }
        if ("finance".equals(username) || "payroll".equals(username) || "payrollofficer".equals(username)) {
            return AuthService.AuthRole.FINANCE.name();
        }
        if ("it".equals(username)) {
            return AuthService.AuthRole.IT.name();
        }
        return AuthService.AuthRole.EMPLOYEE.name();
    }

    private String normalizeStatus(String status) {
        String normalized = safe(status).toUpperCase(Locale.ENGLISH);
        if (STATUS_LOCKED.equals(normalized)) {
            return STATUS_LOCKED;
        }
        return STATUS_ACTIVE;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
