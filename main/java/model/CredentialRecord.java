package model;

public class CredentialRecord {
    private final String employeeNumber;
    private final String emailAddress;
    private final String userName;
    private final String password;
    private final String role;
    private final String status;

    public CredentialRecord(String employeeNumber, String emailAddress, String userName, String password) {
        this(employeeNumber, emailAddress, userName, password, "", "ACTIVE");
    }

    public CredentialRecord(
            String employeeNumber,
            String emailAddress,
            String userName,
            String password,
            String role,
            String status) {
        this.employeeNumber = employeeNumber;
        this.emailAddress = emailAddress;
        this.userName = userName;
        this.password = password;
        this.role = role == null ? "" : role;
        this.status = status == null || status.isBlank() ? "ACTIVE" : status;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}
