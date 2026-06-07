package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import model.Deduction;
import model.Employee;
import model.Payroll;

/**
 * Application service for payroll read models consumed by GUI dashboards.
 */
public class PayrollService {
    private static final double STANDARD_MONTHLY_HOURS = 160.0;
    private static final String PAYROLL_HISTORY_FILE = "PayrollRunHistory.csv";
    private static final String[] PAYROLL_HISTORY_HEADER = {
        "Date", "Period", "Employee #", "Name", "Action", "Amount", "User"
    };
    private static final DateTimeFormatter DATE_FMT_MOTORPH = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter DATE_FMT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT_12 = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT_12_SHORT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT_24 = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME_FMT_24_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final PayrollConfigService configService;
    private final StatutoryDeductionService deductionService;

    public PayrollService() {
        this.employeeService = new EmployeeService();
        this.attendanceService = new AttendanceService();
        this.configService = new PayrollConfigService();
        this.deductionService = new StatutoryDeductionService();
    }

    public List<Payroll> getPayrollRecords(String employeeCsvFilePath) throws Exception {
        List<Employee> employees = employeeService.loadAllEmployees(employeeCsvFilePath);
        List<Payroll> records = new ArrayList<>();
        PayrollConfigService.PayrollConfig config = configService.loadConfig();

        String attendanceCsvPath = ResourcePathService.resourceFile("MotorPH_Attendance.csv");
        Map<String, Double> workedHoursByEmployee = loadWorkedHours(employees, attendanceCsvPath);

        for (Employee emp : employees) {
            if (emp == null) {
                continue;
            }

            double fullBasicSalary = emp.getBasicSalary();
            double fullAllowances = config.isAllowanceOverrideEnabled()
                ? config.getTotalAllowance()
                : emp.computeGross() - fullBasicSalary;

            double hoursWorked = workedHoursByEmployee.getOrDefault(
                emp.getEmployeeNumber(), config.isStrictAttendanceMode() ? 0.0 : STANDARD_MONTHLY_HOURS);

            // In non-strict mode, missing attendance keeps full monthly base.
            if (!config.isStrictAttendanceMode() && hoursWorked <= 0) {
                hoursWorked = STANDARD_MONTHLY_HOURS;
            }
            double attendanceFactor = Math.min(1.0, Math.max(0.0, hoursWorked / STANDARD_MONTHLY_HOURS));

            double basicSalary = fullBasicSalary * attendanceFactor;
            double allowances = fullAllowances * attendanceFactor;
            double grossPay = basicSalary + allowances;
            Deduction statutoryDeduction = deductionService.computeMonthlyDeduction(grossPay);
            double deductions = statutoryDeduction.getTotalDeduction();

            if (grossPay <= 0.0) {
                deductions = 0.0;
            } else {
                deductions = Math.min(deductions, grossPay);
            }

            double netPay = Math.max(0.0, grossPay - deductions);

            Payroll record = new Payroll(
                emp.getEmployeeNumber(),
                emp.getLastName() + ", " + emp.getFirstName(),
                basicSalary,
                allowances,
                deductions,
                netPay);
            record.setTotalHoursWorked(hoursWorked);
            records.add(record);
        }

        return records;
    }

    private Map<String, Double> loadWorkedHours(List<Employee> employees, String attendanceCsvPath) {
        Map<String, Double> hoursByEmployee = new HashMap<>();

        for (Employee employee : employees) {
            if (employee == null || employee.getEmployeeNumber() == null || employee.getEmployeeNumber().isBlank()) {
                continue;
            }

            try {
                List<AttendanceService.AttendanceLog> logs = attendanceService.getEmployeeLogs(
                    attendanceCsvPath,
                    employee.getEmployeeNumber());
                double hours = computeLatestPeriodHours(logs);
                if (hours > 0) {
                    hoursByEmployee.put(employee.getEmployeeNumber().trim(), hours);
                }
            }
            catch (Exception ignored) {
            }
        }

        return hoursByEmployee;
    }

    private double computeLatestPeriodHours(List<AttendanceService.AttendanceLog> logs) {
        YearMonth latestMonth = null;
        for (AttendanceService.AttendanceLog log : logs) {
            LocalDate date = parseLogDate(log.getDate());
            if (date == null) {
                continue;
            }
            YearMonth ym = YearMonth.from(date);
            if (latestMonth == null || ym.isAfter(latestMonth)) {
                latestMonth = ym;
            }
        }

        if (latestMonth == null) {
            return 0.0;
        }

        double totalHours = 0.0;
        for (AttendanceService.AttendanceLog log : logs) {
            LocalDate date = parseLogDate(log.getDate());
            if (date == null || !YearMonth.from(date).equals(latestMonth)) {
                continue;
            }

            LocalTime in = parseLogTime(log.getTimeIn());
            LocalTime out = parseLogTime(log.getTimeOut());
            if (in == null || out == null || out.isBefore(in)) {
                continue;
            }

            totalHours += Duration.between(in, out).toMinutes() / 60.0;
        }

        return totalHours;
    }

    private LocalDate parseLogDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim(), DATE_FMT_MOTORPH);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value.trim(), DATE_FMT_ISO);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private LocalTime parseLogTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalTime.parse(value.trim(), TIME_FMT_12);
        } catch (DateTimeParseException e) {
            try {
                return LocalTime.parse(value.trim(), TIME_FMT_12_SHORT);
            } catch (DateTimeParseException e2) {
                try {
                    return LocalTime.parse(value.trim(), TIME_FMT_24);
                } catch (DateTimeParseException e3) {
                    try {
                        return LocalTime.parse(value.trim(), TIME_FMT_24_SHORT);
                    } catch (DateTimeParseException ignored) {
                        return null;
                    }
                }
            }
        }
    }

    public double getTotalPayroll(List<Payroll> records) {
        double total = 0.0;
        for (Payroll record : records) {
            total += record.getNetPay();
        }
        return total;
    }

    public List<String[]> buildHistoryRows(List<Payroll> records, String date, String period, String user, int limit) {
        List<String[]> rows = new ArrayList<>();
        int count = Math.min(records.size(), limit);

        for (int i = 0; i < count; i++) {
            Payroll record = records.get(i);
            rows.add(new String[]{
                    date,
                    period,
                    record.getEmployeeNumber(),
                    record.getEmployeeName(),
                    "Payroll Processed",
                    "₱ " + String.format("%,.2f", record.getNetPay()),
                    user
            });
        }

        return rows;
    }

    // Overload with sensible defaults so UI does not hardcode period/date values.
    public List<String[]> buildHistoryRows(List<Payroll> records, int limit) {
        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ISO_DATE);
        String period = now.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        return buildHistoryRows(records, date, period, "payroll_officer", limit);
    }

    public void savePayrollRun(List<Payroll> records, String period, String user) throws Exception {
        if (records == null || records.isEmpty()) {
            return;
        }

        String historyCsv = ResourcePathService.resourceFile(PAYROLL_HISTORY_FILE);
        File file = new File(historyCsv);
        boolean append = file.exists() && file.length() > 0;

        try (CSVWriter writer = new CSVWriter(new FileWriter(historyCsv, true))) {
            if (!append) {
                writer.writeNext(PAYROLL_HISTORY_HEADER);
            }

            String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            for (Payroll record : records) {
                writer.writeNext(new String[]{
                    date,
                    period,
                    record.getEmployeeNumber(),
                    record.getEmployeeName(),
                    "Payroll Processed",
                    "₱ " + String.format("%,.2f", record.getNetPay()),
                    user
                });
            }
        }
    }

    public List<String[]> loadPayrollHistoryRows(int limit) throws Exception {
        List<String[]> rows = new ArrayList<>();
        String historyCsv = ResourcePathService.resourceFile(PAYROLL_HISTORY_FILE);
        File file = new File(historyCsv);
        if (!file.exists() || file.length() == 0) {
            return rows;
        }

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> all = reader.readAll();
            if (all.size() <= 1) {
                return rows;
            }

            for (int i = all.size() - 1; i >= 1 && rows.size() < limit; i--) {
                String[] row = all.get(i);
                if (row.length >= 7) {
                    if (isNegativeAmount(row[5])) {
                        continue;
                    }
                    rows.add(new String[]{
                        row[0], row[1], row[2], row[3], row[4], row[5], row[6]
                    });
                }
            }
        }

        return rows;
    }

    private boolean isNegativeAmount(String amountText) {
        if (amountText == null || amountText.isBlank()) {
            return false;
        }

        String cleaned = amountText.replace("₱", "").replace(",", "").trim();
        try {
            return Double.parseDouble(cleaned) < 0.0;
        } catch (NumberFormatException e) {
            return cleaned.startsWith("-");
        }
    }

}