package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import model.Allowance;
import model.Deduction;
import model.Employee;
import model.Payroll;
import model.RegularEmployee;
import service.AttendanceService;
import service.EmployeeService;
import service.PayrollService;
import service.ResourcePathService;

public class EmployeeDashboard extends JFrame {

    private final String employeeNumber;
    private String firstName;
    private String lastName;
    private Employee currentEmployee;

    // Labels
    private JLabel headerLabel;
    private JLabel empNumberLabel, firstNameLabel, lastNameLabel;
    private JLabel sssLabel, philhealthLabel, tinLabel, pagibigLabel;

    // Text Fields
    private JTextField empNumberField, firstNameField, lastNameField;
    private JTextField sssField, philhealthField, tinField, pagibigField;

    // Buttons
    private JButton viewPayslipBtn, fileLeaveBtn, leaveHistoryBtn, timeInBtn, timeOutBtn, attendanceLogBtn, logoutBtn;

    public EmployeeDashboard(String employeeNumber) {
        this.employeeNumber = employeeNumber;
        initComponents();
        loadEmployeeData();
    }

    private void initComponents() {
        setTitle("MotorPH - Employee Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 102));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        headerLabel = new JLabel("Employee Profile");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);

        timeInBtn = createButton("Time In", new Color(25, 135, 84), Color.WHITE);
        timeOutBtn = createButton("Time Out", new Color(220, 53, 69), Color.WHITE);

        JPanel headerRightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRightButtons.setBackground(new Color(0, 102, 102));
        headerRightButtons.add(timeInBtn);
        headerRightButtons.add(timeOutBtn);

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(headerRightButtons, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT ---
        JPanel mainContentPanel = new JPanel(new BorderLayout(0, 30));
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(new EmptyBorder(30, 50, 30, 50));

        // --- FORM ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);

        empNumberLabel   = createLabel("Employee Number:");
        firstNameLabel   = createLabel("First Name:");
        lastNameLabel    = createLabel("Last Name:");
        sssLabel         = createLabel("SSS Number:");
        philhealthLabel  = createLabel("Philhealth #:");
        tinLabel         = createLabel("TIN #:");
        pagibigLabel     = createLabel("Pag-ibig #:");

        empNumberField   = createReadOnlyTextField();
        firstNameField   = createReadOnlyTextField();
        lastNameField    = createReadOnlyTextField();
        sssField         = createReadOnlyTextField();
        philhealthField  = createReadOnlyTextField();
        tinField         = createReadOnlyTextField();
        pagibigField     = createReadOnlyTextField();

        addFormRow(formPanel, gbc, empNumberLabel,  empNumberField,  0);
        addFormRow(formPanel, gbc, firstNameLabel,  firstNameField,  1);
        addFormRow(formPanel, gbc, lastNameLabel,   lastNameField,   2);
        addFormRow(formPanel, gbc, sssLabel,        sssField,        3);
        addFormRow(formPanel, gbc, philhealthLabel, philhealthField, 4);
        addFormRow(formPanel, gbc, tinLabel,        tinField,        5);
        addFormRow(formPanel, gbc, pagibigLabel,    pagibigField,    6);

        mainContentPanel.add(formPanel, BorderLayout.CENTER);

        // --- BUTTONS ---
        // Left side: action buttons
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftBtns.setBackground(Color.WHITE);

        viewPayslipBtn  = createButton("View Payslip", new Color(0, 102, 102), Color.WHITE);
        fileLeaveBtn    = createButton("File Leave", new Color(233, 236, 239), new Color(33, 37, 41));
        leaveHistoryBtn = createButton("Leave History", new Color(233, 236, 239), new Color(33, 37, 41));
        attendanceLogBtn = createButton("My Attendance", new Color(233, 236, 239), new Color(33, 37, 41));

        leftBtns.add(viewPayslipBtn);
        leftBtns.add(fileLeaveBtn);
        leftBtns.add(leaveHistoryBtn);
        leftBtns.add(attendanceLogBtn);

        // Right side: logout
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightBtns.setBackground(Color.WHITE);

        logoutBtn = createButton("Logout", Color.WHITE, new Color(220, 53, 69));
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 53, 69), 1),
            BorderFactory.createEmptyBorder(9, 19, 9, 19)
        ));
        rightBtns.add(logoutBtn);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(leftBtns, BorderLayout.WEST);
        buttonPanel.add(rightBtns, BorderLayout.EAST);

        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainContentPanel, BorderLayout.CENTER);

        // --- ACTIONS ---
        viewPayslipBtn.addActionListener(evt -> viewPayslip());
        fileLeaveBtn.addActionListener(evt -> fileLeaveDlg());
        leaveHistoryBtn.addActionListener(evt -> viewLeaveHistory());
        timeInBtn.addActionListener(evt -> timeIn());
        timeOutBtn.addActionListener(evt -> timeOut());
        attendanceLogBtn.addActionListener(evt -> viewAttendanceLogs());
        logoutBtn.addActionListener(evt -> logout());

        pack();
        setMinimumSize(new Dimension(700, 600));
        setLocationRelativeTo(null);
    }

    // --- UI HELPERS ---

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(73, 80, 87));
        return label;
    }

    private JTextField createReadOnlyTextField() {
        JTextField tf = new JTextField(20);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setEditable(false);
        tf.setBackground(new Color(248, 249, 250));
        tf.setForeground(new Color(33, 37, 41));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return tf;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc,
                            JLabel label, JTextField field, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    // --- DATA & ACTIONS ---

    private void loadEmployeeData() {
        String csvFile = ResourcePathService.resourceFile("MotorPHEmployeeData-EmployeeDetails.csv");

        try {
            EmployeeService service = new EmployeeService();
            Employee emp = service.findEmployeeByNumber(csvFile, employeeNumber);

            if (emp != null) {
                currentEmployee = emp;
                firstName = emp.getFirstName() != null ? emp.getFirstName() : "";
                lastName  = emp.getLastName()  != null ? emp.getLastName()  : "";

                empNumberField.setText(emp.getEmployeeNumber());
                firstNameField.setText(firstName);
                lastNameField.setText(lastName);

                // Update header with employee name
                headerLabel.setText("Employee Profile — " + firstName + " " + lastName);

                if (emp.getGovernmentDetails() != null) {
                    sssField.setText(emp.getGovernmentDetails().getSssNumber() != null
                        ? emp.getGovernmentDetails().getSssNumber() : "");
                    philhealthField.setText(emp.getGovernmentDetails().getPhilhealthNumber() != null
                        ? emp.getGovernmentDetails().getPhilhealthNumber() : "");
                    tinField.setText(emp.getGovernmentDetails().getTinNumber() != null
                        ? emp.getGovernmentDetails().getTinNumber() : "");
                    pagibigField.setText(emp.getGovernmentDetails().getPagibigNumber() != null
                        ? emp.getGovernmentDetails().getPagibigNumber() : "");
                }
                return;
            }

            JOptionPane.showMessageDialog(this, "Employee data not found.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading employee data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewPayslip() {
        if (currentEmployee == null) {
            JOptionPane.showMessageDialog(this,
                "Employee data not loaded. Please refresh.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            PayrollService payrollService = new PayrollService();
            String employeeCsv = ResourcePathService.resourceFile("MotorPHEmployeeData-EmployeeDetails.csv");

            Payroll currentPayroll = null;
            for (Payroll record : payrollService.getPayrollRecords(employeeCsv)) {
                if (record.getEmployeeNumber() != null
                        && record.getEmployeeNumber().trim().equals(employeeNumber.trim())) {
                    currentPayroll = record;
                    break;
                }
            }

            Employee displayEmployee = currentEmployee;
            double totalDeductions;
            double totalHoursWorked = 0.0;

            if (currentPayroll != null) {
                double grossPay = currentPayroll.getBasicSalary() + currentPayroll.getAllowances();
                if (grossPay <= 0 || currentPayroll.getNetPay() <= 0) {
                    totalDeductions = currentEmployee.computeTotalDeductions();
                } else {
                    displayEmployee = buildPayslipDisplayEmployee(currentEmployee, currentPayroll);
                    totalDeductions = currentPayroll.getDeductions();
                    totalHoursWorked = currentPayroll.getTotalHoursWorked();
                }
            } else {
                totalDeductions = currentEmployee.computeTotalDeductions();
            }

            LocalDate[] actualPeriod = resolveActualPayslipPeriod(employeeNumber);
            LocalDate periodStartDate = actualPeriod[0];
            LocalDate periodEndDate = actualPeriod[1];
            YearMonth ym = YearMonth.from(periodEndDate);
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String payslipNo = "SN-" + employeeNumber + "-" + ym.format(DateTimeFormatter.ofPattern("yyyyMM"));
            String periodStart = periodStartDate.format(dateFmt);
            String periodEnd = periodEndDate.format(dateFmt);
            String employeeName = displayEmployee.getFullName();
            String position = displayEmployee.getPosition() != null ? displayEmployee.getPosition() : "N/A";
            String department = displayEmployee.getDepartment() != null ? displayEmployee.getDepartment() : "N/A";
            String positionDept = position + " / " + department;

            double basicSalary = displayEmployee.getBasicSalary();
            double dailyRate = basicSalary / 22.0;
            int daysWorked = (int) Math.round(Math.max(0.0, totalHoursWorked) / 8.0);
            double overtime = 0.0;

            Allowance allowance = displayEmployee.getAllowance();
            double rice = allowance != null ? allowance.getRiceSubsidy() : 0.0;
            double phone = allowance != null ? allowance.getPhoneAllowance() : 0.0;
            double clothing = allowance != null ? allowance.getClothingAllowance() : 0.0;

            Deduction deduction = displayEmployee.getDeduction();
            double sss = deduction != null ? deduction.getSss() : 0.0;
            double philhealth = deduction != null ? deduction.getPhilhealth() : 0.0;
            double pagibig = deduction != null ? deduction.getPagibig() : 0.0;
            double tax = deduction != null ? deduction.getTax() : 0.0;

            // Keep totals coherent with computed payroll result.
            if (deduction != null && totalDeductions > 0.0) {
                double componentTotal = sss + philhealth + pagibig + tax;
                if (componentTotal > 0.0) {
                    double scale = totalDeductions / componentTotal;
                    sss *= scale;
                    philhealth *= scale;
                    pagibig *= scale;
                    tax *= scale;
                }
            }

            String payslipHtml = PayslipDialog.generateHtml(
                payslipNo,
                periodStart,
                periodEnd,
                displayEmployee.getEmployeeNumber(),
                employeeName,
                positionDept,
                basicSalary,
                dailyRate,
                daysWorked,
                overtime,
                rice,
                phone,
                clothing,
                sss,
                philhealth,
                pagibig,
                tax
            );

            PayslipDialog dialog = new PayslipDialog(this, payslipHtml);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading payslip: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Employee buildPayslipDisplayEmployee(Employee sourceEmployee, Payroll payrollRecord) {
        if (sourceEmployee == null || payrollRecord == null) {
            return sourceEmployee;
        }

        double fullBasic = sourceEmployee.getBasicSalary();
        double factor = fullBasic > 0 ? payrollRecord.getBasicSalary() / fullBasic : 1.0;
        factor = Math.max(0.0, Math.min(1.0, factor));

        Allowance srcAllowance = sourceEmployee.getAllowance();
        Deduction srcDeduction = sourceEmployee.getDeduction();

        double rice = srcAllowance != null ? srcAllowance.getRiceSubsidy() * factor : 0.0;
        double phone = srcAllowance != null ? srcAllowance.getPhoneAllowance() * factor : 0.0;
        double clothing = srcAllowance != null ? srcAllowance.getClothingAllowance() * factor : 0.0;

        double sss = srcDeduction != null ? srcDeduction.getSss() * factor : 0.0;
        double philhealth = srcDeduction != null ? srcDeduction.getPhilhealth() * factor : 0.0;
        double pagibig = srcDeduction != null ? srcDeduction.getPagibig() * factor : 0.0;
        double tax = srcDeduction != null ? srcDeduction.getTax() * factor : 0.0;

        Employee snapshot = new RegularEmployee(
            payrollRecord.getBasicSalary(),
            sourceEmployee.getEmpID(),
            sourceEmployee.getLastName(),
            sourceEmployee.getFirstName(),
            null,
            "",
            "",
            "",
            sourceEmployee.getGovernmentDetails(),
            new Allowance(rice, phone, clothing),
            new Deduction(sss, philhealth, pagibig, tax)
        );

        if (sourceEmployee.getPosition() != null && !sourceEmployee.getPosition().isBlank()) {
            snapshot.setPosition(sourceEmployee.getPosition());
        }
        if (sourceEmployee.getDepartment() != null && !sourceEmployee.getDepartment().isBlank()) {
            snapshot.setDepartment(sourceEmployee.getDepartment());
        }

        return snapshot;
    }

    private LocalDate[] resolveActualPayslipPeriod(String empNum) {
        String attendanceCsv = ResourcePathService.resourceFile("MotorPH_Attendance.csv");
        AttendanceService service = new AttendanceService();
        List<AttendanceService.AttendanceLog> logs = service.getEmployeeLogs(attendanceCsv, empNum);

        LocalDate latestDate = null;
        for (AttendanceService.AttendanceLog log : logs) {
            LocalDate parsed = parseAttendanceDate(log.getDate());
            if (parsed != null && (latestDate == null || parsed.isAfter(latestDate))) {
                latestDate = parsed;
            }
        }

        YearMonth targetMonth = latestDate != null ? YearMonth.from(latestDate) : YearMonth.now();
        LocalDate start = null;
        LocalDate end = null;

        for (AttendanceService.AttendanceLog log : logs) {
            LocalDate parsed = parseAttendanceDate(log.getDate());
            if (parsed == null || !YearMonth.from(parsed).equals(targetMonth)) {
                continue;
            }
            if (start == null || parsed.isBefore(start)) {
                start = parsed;
            }
            if (end == null || parsed.isAfter(end)) {
                end = parsed;
            }
        }

        if (start == null || end == null) {
            start = targetMonth.atDay(1);
            end = targetMonth.atEndOfMonth();
        }

        return new LocalDate[]{start, end};
    }

    private LocalDate parseAttendanceDate(String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(dateText.trim(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(dateText.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private void fileLeaveDlg() {
        LeaveFilingDialog dlg = new LeaveFilingDialog(this, employeeNumber);
        dlg.setVisible(true);
    }

    private void viewLeaveHistory() {
        LeaveHistoryDialog dlg = new LeaveHistoryDialog(this, employeeNumber);
        dlg.setVisible(true);
    }

    private void timeIn() {
        AttendanceService service = new AttendanceService();
        String logCsv = ResourcePathService.resourceFile("MotorPH_Attendance.csv");
        boolean ok = service.timeIn(logCsv, employeeNumber);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                "Time In recorded successfully.",
                "Attendance", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Unable to record Time In.",
                "Attendance Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void timeOut() {
        AttendanceService service = new AttendanceService();
        String logCsv = ResourcePathService.resourceFile("MotorPH_Attendance.csv");
        boolean ok = service.timeOut(logCsv, employeeNumber);

        if (ok) {
            JOptionPane.showMessageDialog(this,
                "Time Out recorded successfully.",
                "Attendance", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Unable to record Time Out.",
                "Attendance Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAttendanceLogs() {
        AttendanceService service = new AttendanceService();
        String logCsv = ResourcePathService.resourceFile("MotorPH_Attendance.csv");
        List<AttendanceService.AttendanceLog> logs = service.getEmployeeLogs(logCsv, employeeNumber);

        if (logs.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No attendance logs found yet.",
                "Attendance", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build table data (latest 10)
        int start = Math.max(0, logs.size() - 10);
        String[] columns = {"Date", "Time In", "Time Out"};
        String[][] data = new String[Math.min(10, logs.size())][3];
        int row = 0;
        for (int i = logs.size() - 1; i >= start; i--) {
            AttendanceService.AttendanceLog log = logs.get(i);
            data[row][0] = log.getDate() != null ? log.getDate() : "-";
            data[row][1] = log.getTimeIn().isBlank() ? "-" : log.getTimeIn();
            data[row][2] = log.getTimeOut().isBlank() ? "-" : log.getTimeOut();
            row++;
        }

        // Build dialog
        javax.swing.JDialog dlg = new javax.swing.JDialog(this, "My Attendance Logs (Latest 10)", true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(0, 102, 102));
        hdr.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel hdrLabel = new JLabel("My Attendance Logs");
        hdrLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        hdrLabel.setForeground(Color.WHITE);
        hdr.add(hdrLabel, BorderLayout.WEST);
        dlg.add(hdr, BorderLayout.NORTH);

        // Table
        javax.swing.JTable table = new javax.swing.JTable(data, columns) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0, 102, 102, 40));
        table.setFillsViewportHeight(true);

        // Header style
        javax.swing.table.JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tableHeader.setBackground(new Color(240, 240, 240));
        tableHeader.setForeground(new Color(33, 37, 41));
        tableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 102, 102)));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(130);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object val, boolean sel, boolean focus, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, focus, r, c);
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!sel) {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 249, 250));
                    setForeground(new Color(33, 37, 41));
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 16, 0, 16));
        scroll.getViewport().setBackground(Color.WHITE);
        dlg.add(scroll, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = createButton("Close", new Color(0, 102, 102), Color.WHITE);
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnPanel.add(closeBtn);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        dlg.setSize(430, 420);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void logout() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            new UserLogin().setVisible(true);
            dispose();
        }
    }
}
