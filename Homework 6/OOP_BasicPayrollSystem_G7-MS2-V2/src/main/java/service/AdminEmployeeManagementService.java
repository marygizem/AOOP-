package service;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVWriter;

public class AdminEmployeeManagementService {
    private static final int ADMIN_COLUMN_COUNT = 12;
    private static final String[] DEFAULT_HEADER = {
            "Employee #", "Last Name", "First Name", "SSS Number", "Philhealth Number", "TIN", "Pag-ibig Number",
        "Email Address", "Basic Salary", "Rice Allowance", "Phone Allowance", "Clothing Allowance"
    };

    private final EmployeeService employeeService;

    public AdminEmployeeManagementService() {
        this.employeeService = new EmployeeService();
    }

    public static class OperationResult {
        private final boolean success;
        private final String message;

        public OperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public List<String[]> loadAdminRows(String csvFile) throws Exception {
        List<String[]> rows = new ArrayList<>(employeeService.getEmployees(csvFile));
        normalizeRowsSchema(rows);
        return rows;
    }

    public OperationResult addEmployee(String csvFile, String empNum, String lastName, String firstName,
                                       String sss, String philhealth, String tin, String pagibig, String email,
                           String basicSalary, String riceAllowance,
                                       String phoneAllowance, String clothingAllowance) {
        ValidationResult validation = validateInputs(empNum, lastName, firstName, sss, philhealth, tin, pagibig, email,
            basicSalary, riceAllowance, phoneAllowance, clothingAllowance, true);
        if (!validation.valid) {
            return new OperationResult(false, validation.message);
        }

        try {
            List<String[]> rows = new ArrayList<>(employeeService.getEmployees(csvFile));
            normalizeRowsSchema(rows);

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length > 0 && row[0].trim().equals(empNum.trim())) {
                    return new OperationResult(false, "Employee number already exists.");
                }
            }

            rows.add(new String[]{
                    empNum.trim(),
                    capitalizeWords(lastName.trim()),
                    capitalizeWords(firstName.trim()),
                    sss.trim(),
                    philhealth.trim(),
                    tin.trim(),
                    pagibig.trim(),
                        email.trim(),
                    formatMoney(basicSalary),
                    formatMoney(riceAllowance),
                    formatMoney(phoneAllowance),
                    formatMoney(clothingAllowance)
            });

            writeRows(csvFile, rows);
            return new OperationResult(true, "Employee added successfully!");
        } catch (Exception e) {
            return new OperationResult(false, "Error adding employee: " + e.getMessage());
        }
    }

    /**
     * Validates add-employee input without writing any changes.
     */
    public OperationResult validateNewEmployee(String csvFile, String empNum, String lastName, String firstName,
                                               String sss, String philhealth, String tin, String pagibig, String email,
                                               String basicSalary, String riceAllowance,
                                               String phoneAllowance, String clothingAllowance) {
        ValidationResult validation = validateInputs(empNum, lastName, firstName, sss, philhealth, tin, pagibig, email,
            basicSalary, riceAllowance, phoneAllowance, clothingAllowance, true);
        if (!validation.valid) {
            return new OperationResult(false, validation.message);
        }

        try {
            List<String[]> rows = new ArrayList<>(employeeService.getEmployees(csvFile));
            normalizeRowsSchema(rows);
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length > 0 && row[0].trim().equals(empNum.trim())) {
                    return new OperationResult(false, "Employee number already exists.");
                }
            }
            return new OperationResult(true, "");
        } catch (Exception e) {
            return new OperationResult(false, "Unable to validate employee data: " + e.getMessage());
        }
    }

    /**
     * Validates update-employee input without writing changes.
     */
    public OperationResult validateUpdatedEmployee(String csvFile, String empNum, String lastName, String firstName,
                                                   String sss, String philhealth, String tin, String pagibig, String email,
                                                   String basicSalary, String riceAllowance,
                                                   String phoneAllowance, String clothingAllowance) {
        ValidationResult validation = validateInputs(empNum, lastName, firstName, sss, philhealth, tin, pagibig, email,
            basicSalary, riceAllowance, phoneAllowance, clothingAllowance, false);
        if (!validation.valid) {
            return new OperationResult(false, validation.message);
        }

        try {
            List<String[]> rows = new ArrayList<>(employeeService.getEmployees(csvFile));
            normalizeRowsSchema(rows);
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length > 0 && row[0].trim().equals(empNum.trim())) {
                    return new OperationResult(true, "");
                }
            }
            return new OperationResult(false, "Employee number not found.");
        } catch (Exception e) {
            return new OperationResult(false, "Unable to validate employee data: " + e.getMessage());
        }
    }

    public OperationResult updateEmployee(String csvFile, String empNum, String lastName, String firstName,
                                          String sss, String philhealth, String tin, String pagibig, String email,
                          String basicSalary, String riceAllowance,
                                          String phoneAllowance, String clothingAllowance) {
        ValidationResult validation = validateInputs(empNum, lastName, firstName, sss, philhealth, tin, pagibig, email,
            basicSalary, riceAllowance, phoneAllowance, clothingAllowance, false);
        if (!validation.valid) {
            return new OperationResult(false, validation.message);
        }

        try {
            List<String[]> rows = new ArrayList<>(employeeService.getEmployees(csvFile));
            normalizeRowsSchema(rows);
            boolean found = false;

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length > 0 && row[0].trim().equals(empNum.trim())) {
                    rows.set(i, new String[]{
                            empNum.trim(),
                            capitalizeWords(lastName.trim()),
                            capitalizeWords(firstName.trim()),
                            sss.trim(),
                            philhealth.trim(),
                            tin.trim(),
                            pagibig.trim(),
                            email.trim(),
                            formatMoney(basicSalary),
                            formatMoney(riceAllowance),
                            formatMoney(phoneAllowance),
                            formatMoney(clothingAllowance)
                    });
                    found = true;
                    break;
                }
            }

            if (!found) {
                return new OperationResult(false, "Employee number not found.");
            }

            writeRows(csvFile, rows);
            return new OperationResult(true, "Employee updated successfully!");
        } catch (Exception e) {
            return new OperationResult(false, "Error updating employee: " + e.getMessage());
        }
    }

    public OperationResult deleteEmployee(String csvFile, String empNum) {
        try {
            List<String[]> rows = new ArrayList<>(employeeService.getEmployees(csvFile));
            normalizeRowsSchema(rows);
            boolean removed = rows.removeIf(row -> row.length > 0 && row[0].trim().equals(empNum.trim()));

            if (!removed) {
                return new OperationResult(false, "Employee number not found.");
            }

            writeRows(csvFile, rows);
            return new OperationResult(true, "Employee deleted successfully!");
        } catch (Exception e) {
            return new OperationResult(false, "Error deleting employee: " + e.getMessage());
        }
    }

    private void writeRows(String csvFile, List<String[]> rows) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile, false))) {
            writer.writeAll(rows);
        }
    }

    private void normalizeRowsSchema(List<String[]> rows) {
        if (rows.isEmpty()) {
            rows.add(DEFAULT_HEADER);
            return;
        }

        rows.set(0, DEFAULT_HEADER);
        for (int i = 1; i < rows.size(); i++) {
            rows.set(i, toAdminSchemaRow(rows.get(i)));
        }
    }

    private String[] toAdminSchemaRow(String[] row) {
        String[] normalized = new String[ADMIN_COLUMN_COUNT];
        for (int i = 0; i < ADMIN_COLUMN_COUNT; i++) {
            normalized[i] = "";
        }

        if (row == null) {
            return normalized;
        }

        // Legacy/admin schema with email column.
        if (row.length >= 12) {
            normalized[0] = row[0].trim();
            normalized[1] = row[1].trim();
            normalized[2] = row[2].trim();
            normalized[3] = row[3].trim();
            normalized[4] = row[4].trim();
            normalized[5] = row[5].trim();
            normalized[6] = row[6].trim();
            normalized[7] = row[7].trim();
            normalized[8] = row[8].trim();
            normalized[9] = row[9].trim();
            normalized[10] = row[10].trim();
            normalized[11] = row[11].trim();
            return normalized;
        }

        // Current schema without email: place email as blank at index 7 and shift numeric fields.
        if (row.length == 11) {
            normalized[0] = row[0].trim();
            normalized[1] = row[1].trim();
            normalized[2] = row[2].trim();
            normalized[3] = row[3].trim();
            normalized[4] = row[4].trim();
            normalized[5] = row[5].trim();
            normalized[6] = row[6].trim();
            normalized[7] = "";
            normalized[8] = row[7].trim();
            normalized[9] = row[8].trim();
            normalized[10] = row[9].trim();
            normalized[11] = row[10].trim();
            return normalized;
        }

        for (int i = 0; i < row.length && i < ADMIN_COLUMN_COUNT; i++) {
            normalized[i] = row[i] == null ? "" : row[i].trim();
        }
        return normalized;
    }

    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
    }

    private ValidationResult validateInputs(String empNum, String lastName, String firstName,
                                            String sss, String philhealth, String tin, String pagibig, String email,
                                            String basicSalary, String riceAllowance,
                                            String phoneAllowance, String clothingAllowance,
                                            boolean requireEmployeeNumber) {
        if (requireEmployeeNumber && isBlank(empNum)) {
            return new ValidationResult(false, "Employee Number is required.");
        }
        if (isBlank(lastName) || isBlank(firstName)) {
            return new ValidationResult(false, "Last Name and First Name are required.");
        }
        if (isBlank(sss) || !sss.matches("^\\d{2}-\\d{7}-\\d{1}$")) {
            return new ValidationResult(false, "Invalid SSS format! Must be XX-XXXXXXX-X.");
        }
        if (isBlank(philhealth) || !philhealth.matches("^\\d{2}-\\d{9}-\\d{1}$")) {
            return new ValidationResult(false, "Invalid PhilHealth format! Must be XX-XXXXXXXXX-X.");
        }
        if (isBlank(tin)
                || !(tin.matches("^\\d{3}-\\d{3}-\\d{3}$") || tin.matches("^\\d{3}-\\d{3}-\\d{3}-\\d{3}$"))) {
            return new ValidationResult(false, "Invalid TIN format! Must be XXX-XXX-XXX or XXX-XXX-XXX-XXX.");
        }
        if (isBlank(pagibig) || !pagibig.matches("^\\d{4}-\\d{4}-\\d{4}$")) {
            return new ValidationResult(false, "Invalid Pag-IBIG format! Must be XXXX-XXXX-XXXX.");
        }
        if (isBlank(email) || !isValidEmail(email)) {
            return new ValidationResult(false, "Invalid email format.");
        }
        if (!isNumericNonNegative(basicSalary)) {
            return new ValidationResult(false, "Basic salary must be a valid non-negative number.");
        }
        if (!isNumericNonNegative(riceAllowance)
                || !isNumericNonNegative(phoneAllowance)
                || !isNumericNonNegative(clothingAllowance)) {
            return new ValidationResult(false, "All allowances must be valid non-negative numbers.");
        }
        return new ValidationResult(true, "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNumericNonNegative(String value) {
        if (isBlank(value)) {
            return false;
        }

        try {
            return Double.parseDouble(value.trim()) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        return email != null
            && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String formatMoney(String value) {
        double amount = Double.parseDouble(value.trim());
        return String.format("%.2f", amount);
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                capitalized.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    capitalized.append(word.substring(1).toLowerCase());
                }
            }
            if (i < words.length - 1) {
                capitalized.append(" ");
            }
        }

        return capitalized.toString();
    }
}