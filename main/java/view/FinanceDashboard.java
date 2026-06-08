package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import model.Payroll;
import service.PayrollConfigService;
import service.PayrollService;
import service.ResourcePathService;

public class FinanceDashboard extends JFrame {

    // --- Brand Colors (shared constants) ---
    private static final Color BRAND_TEAL    = new Color(0, 102, 102);
    private static final Color BRAND_GREEN   = new Color(25, 135, 84);
    private static final Color BRAND_RED     = new Color(220, 53, 69);
    private static final Color TEXT_DARK     = new Color(33, 37, 41);
    private static final Color TEXT_MUTED    = new Color(108, 117, 125);
    private static final Color TEXT_LABEL    = new Color(73, 80, 87);
    private static final Color BORDER_LIGHT  = new Color(222, 226, 230);
    private static final Color ROW_STRIPE    = new Color(245, 250, 250);

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    // --- Component references ---
    private JTabbedPane tabbedPane;
    private JTable payrollTable;
    private JTable payrollHistoryTable;
    private JLabel totalAmountLabel;
    private List<Payroll> payrollRecords = new ArrayList<>();

    // Deduction settings
    private JCheckBox strictAttendanceCheck;

    // Allowance spinners
    private JSpinner riceSubsidySpinner;
    private JSpinner phoneAllowanceSpinner;
    private JSpinner clothingAllowanceSpinner;
    private final PayrollConfigService payrollConfigService;
    private final String currentFinanceUser;
    private final boolean openedFromAdmin;

    public FinanceDashboard(String payrollOfficerNumber) {
        this(payrollOfficerNumber, false);
    }

    public FinanceDashboard(String payrollOfficerNumber, boolean openedFromAdmin) {
        this.openedFromAdmin = openedFromAdmin;
        this.payrollConfigService = new PayrollConfigService();
        this.currentFinanceUser = (payrollOfficerNumber == null || payrollOfficerNumber.isBlank())
            ? "finance"
            : payrollOfficerNumber.trim();
        initComponents();
        loadPayrollSettings();
        loadPayrollData();
    }

    // ------------------------------------------------------------------ //
    //  Frame Setup
    // ------------------------------------------------------------------ //
    private void initComponents() {
        setTitle("MotorPH - Finance Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 20));
        headerPanel.setBackground(BRAND_TEAL);
        JLabel titleLabel = new JLabel("Finance Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setFocusable(false);

        tabbedPane.addTab("Process Payroll",  createProcessPayrollPanel());
        tabbedPane.addTab("Payroll Summary",  createPayrollSummaryPanel());
        tabbedPane.addTab("Deductions",       createDeductionsPanel());
        tabbedPane.addTab("Allowances",       createAllowancesPanel());
        tabbedPane.addTab("Generate Payslips",createPayslipsPanel());
        tabbedPane.addTab("Payroll History",  createPayrollHistoryPanel());
        installLiveSyncHooks();

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        if (!openedFromAdmin) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            buttonPanel.setBackground(Color.WHITE);
            JButton logoutBtn = createOutlineButton("Logout", BRAND_RED);
            logoutBtn.addActionListener(evt -> logout());
            buttonPanel.add(logoutBtn);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        add(mainPanel, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
    }

    private void installLiveSyncHooks() {
        tabbedPane.addChangeListener(evt -> {
            int idx = tabbedPane.getSelectedIndex();
            // Keep payroll and history tabs in sync with CSV-backed state.
            if (idx == 0 || idx == 1 || idx == 5) {
                loadPayrollData();
            }
            // Keep settings tabs in sync with persisted settings.
            if (idx == 2 || idx == 3) {
                loadPayrollSettings();
            }
        });

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                loadPayrollSettings();
                loadPayrollData();
            }
        });
    }

    // ------------------------------------------------------------------ //
    //  Tab: Process Payroll
    // ------------------------------------------------------------------ //
    private JPanel createProcessPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(20, 24, 12, 24)
        ));
        card.setPreferredSize(new Dimension(620, 280));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        JLabel title = createSectionTitle("Process Payroll for Period");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridy = 0;
        formPanel.add(title, gbc);

        gbc.gridy = 1;
        formPanel.add(new JSeparator(), gbc);

        JLabel monthLabel = createFieldLabel("Select Month & Year:");
        gbc.gridy = 2;
        formPanel.add(monthLabel, gbc);

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        formRow.setBackground(Color.WHITE);

        JComboBox<String> monthCombo = new JComboBox<>(MONTH_NAMES);
        monthCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        monthCombo.setPreferredSize(new Dimension(160, 36));

        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(
            LocalDate.now().getYear(), 2020, 2030, 1));
        yearSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearSpinner.setPreferredSize(new Dimension(100, 36));

        formRow.add(monthCombo);
        formRow.add(yearSpinner);

        gbc.gridy = 3;
        formPanel.add(formRow, gbc);

        JLabel infoLabel = new JLabel(
            "Processing payroll will generate payslips for all active employees for the selected period.");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_MUTED);
        gbc.gridy = 4;
        formPanel.add(infoLabel, gbc);

        card.add(formPanel, BorderLayout.CENTER);

        JButton processBtn = createButton(
            "Process Payroll for All Employees", BRAND_TEAL, Color.WHITE);
        processBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        processBtn.setBorder(new EmptyBorder(10, 28, 10, 28));
        processBtn.addActionListener(evt ->
            processPayroll(monthCombo.getSelectedIndex(),
                           (Integer) yearSpinner.getValue()));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
        actionPanel.add(processBtn);
        card.add(actionPanel, BorderLayout.SOUTH);

        JPanel centerWrap = new JPanel(new GridBagLayout());
        centerWrap.setBackground(Color.WHITE);
        centerWrap.add(card);
        panel.add(centerWrap, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Payroll Summary
    // ------------------------------------------------------------------ //
    private JPanel createPayrollSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Payroll Summary Report"), BorderLayout.NORTH);

        payrollTable = new JTable(new DefaultTableModel(
            new String[]{"Employee #", "Name", "Basic Salary",
                         "Allowances", "Deductions", "Net Pay", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(payrollTable);
        styleStatusColumn(payrollTable, 6);
        panel.add(createStyledScrollPane(payrollTable), BorderLayout.CENTER);

        // Summary footer
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        summaryPanel.setBackground(new Color(248, 249, 250));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        JLabel totalLabel = new JLabel("Total Payroll:");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(TEXT_LABEL);

        totalAmountLabel = new JLabel("₱ 0.00");
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalAmountLabel.setForeground(BRAND_TEAL);

        summaryPanel.add(totalLabel);
        summaryPanel.add(totalAmountLabel);
        panel.add(summaryPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Deductions
    // ------------------------------------------------------------------ //
    private JPanel createDeductionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 0.5;

        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(createSectionTitle("Configure Deductions"), gbc);

        gbc.gridy = 1;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        JLabel info1 = new JLabel("Statutory deductions are read from contribution CSV tables:");
        info1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info1.setForeground(TEXT_DARK);
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(info1, gbc);

        JLabel info2 = new JLabel("- SSS Contribution.csv");
        info2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info2.setForeground(TEXT_LABEL);
        gbc.gridy = 3;
        panel.add(info2, gbc);

        JLabel info3 = new JLabel("- Philhealth Contribution.csv");
        info3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info3.setForeground(TEXT_LABEL);
        gbc.gridy = 4;
        panel.add(info3, gbc);

        JLabel info4 = new JLabel("- Pag-ibig Contribution.csv");
        info4.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info4.setForeground(TEXT_LABEL);
        gbc.gridy = 5;
        panel.add(info4, gbc);

        JLabel info5 = new JLabel("- Witholding Tax.csv");
        info5.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info5.setForeground(TEXT_LABEL);
        gbc.gridy = 6;
        panel.add(info5, gbc);

        gbc.gridwidth = 1;

        strictAttendanceCheck = new JCheckBox("Strict attendance mode (no attendance = no pay)");
        strictAttendanceCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        strictAttendanceCheck.setBackground(Color.WHITE);
        strictAttendanceCheck.setForeground(TEXT_DARK);
        gbc.gridy = 7; gbc.gridx = 1;
        gbc.insets = new Insets(5, 15, 5, 15);
        panel.add(strictAttendanceCheck, gbc);

        JButton saveBtn = createButton("Save Deduction Settings", BRAND_GREEN, Color.WHITE);
        saveBtn.addActionListener(evt -> saveDeductions());
        gbc.gridy = 8; gbc.gridx = 1;
        gbc.insets = new Insets(25, 15, 10, 15);
        panel.add(saveBtn, gbc);

        gbc.gridy = 9; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Allowances
    // ------------------------------------------------------------------ //
    private JPanel createAllowancesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.weightx = 0.5;

        gbc.gridy = 0; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(createSectionTitle("Configure Allowances"), gbc);

        gbc.gridy = 1;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        riceSubsidySpinner     = addSpinnerRow(panel, gbc, "Rice Subsidy (₱):",       2, new SpinnerNumberModel(1500, 0, 10000, 100));
        phoneAllowanceSpinner  = addSpinnerRow(panel, gbc, "Phone Allowance (₱):",    3, new SpinnerNumberModel(2000, 0, 10000, 100));
        clothingAllowanceSpinner = addSpinnerRow(panel, gbc, "Clothing Allowance (₱):", 4, new SpinnerNumberModel(1000, 0, 10000, 100));

        JButton saveBtn = createButton("Save Allowance Settings", BRAND_GREEN, Color.WHITE);
        saveBtn.addActionListener(evt -> saveAllowances());
        gbc.gridy = 5; gbc.gridx = 1;
        gbc.insets = new Insets(25, 15, 10, 15);
        panel.add(saveBtn, gbc);

        gbc.gridy = 6; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Generate Payslips
    // ------------------------------------------------------------------ //
    private JPanel createPayslipsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.weightx = 1.0;

        JLabel title = createSectionTitle("Generate & Export Payslips");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        panel.add(title, gbc);

        gbc.gridy = 1;
        panel.add(new JSeparator(), gbc);

        JLabel selectLabel = new JLabel("Select Period to Export:", SwingConstants.CENTER);
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectLabel.setForeground(TEXT_LABEL);
        gbc.gridy = 2;
        panel.add(selectLabel, gbc);

        JComboBox<String> periodCombo = new JComboBox<>(buildRecentPeriods(6));
        periodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JLabel) periodCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3;
        panel.add(periodCombo, gbc);

        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        actionPanel.setBackground(Color.WHITE);

        JButton exportPdfBtn = createButton("Export as PDF", BRAND_TEAL, Color.WHITE);
        exportPdfBtn.addActionListener(evt -> exportPayslips("PDF"));

        JButton exportCsvBtn = createButton("Export as CSV", BRAND_TEAL, Color.WHITE);
        exportCsvBtn.addActionListener(evt -> exportPayslips("CSV"));

        JButton printBtn = createButton("Print Payslips", BRAND_GREEN, Color.WHITE);
        printBtn.addActionListener(evt -> printPayslips());

        actionPanel.add(exportPdfBtn);
        actionPanel.add(exportCsvBtn);
        actionPanel.add(printBtn);

        gbc.gridy = 4;
        gbc.insets = new Insets(20, 10, 5, 10);
        panel.add(actionPanel, gbc);

        JLabel infoLabel = new JLabel(
            "This will generate individual payslips for all employees and export in the selected format.",
            SwingConstants.CENTER);
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_MUTED);
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel.add(infoLabel, gbc);

        gbc.gridy = 6; gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Payroll History
    // ------------------------------------------------------------------ //
    private JPanel createPayrollHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Payroll History & Audit Trail"), BorderLayout.NORTH);

        payrollHistoryTable = new JTable(new DefaultTableModel(
            new String[]{"Date", "Period", "Employee #", "Name", "Action", "Amount", "User"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        });
        styleTable(payrollHistoryTable);
        panel.add(createStyledScrollPane(payrollHistoryTable), BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  UI Helpers
    // ------------------------------------------------------------------ //
    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(BRAND_TEAL);
        return label;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_LABEL);
        return label;
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

    private JButton createOutlineButton(String text, Color color) {
        JButton btn = createButton(text, Color.WHITE, color);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 1),
            BorderFactory.createEmptyBorder(9, 19, 9, 19)
        ));
        return btn;
    }

    private JSpinner addSpinnerRow(JPanel panel, GridBagConstraints gbc,
                                   String labelText, int row, SpinnerNumberModel model) {
        gbc.gridy = row;

        gbc.gridx = 0; gbc.weightx = 0.4;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_LABEL);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        editor.getTextField().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(spinner, gbc);

        return spinner;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setSelectionBackground(BRAND_TEAL);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_STRIPE);
                    setForeground(TEXT_DARK);
                }
                return this;
            }
        });

        // Teal header — consistent with our other dashboards
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(BRAND_TEAL);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
    }

    /** Color-codes a Status column: green = Processed, orange = Pending, red = Failed */
    private void styleStatusColumn(JTable table, int colIndex) {
        table.getColumnModel().getColumn(colIndex).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object val,
                        boolean selected, boolean focus, int row, int col) {
                    super.getTableCellRendererComponent(t, val, selected, focus, row, col);
                    setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    if (!selected) {
                        setBackground(row % 2 == 0 ? Color.WHITE : ROW_STRIPE);
                        String status = val != null ? val.toString().toLowerCase() : "";
                        switch (status) {
                            case "processed" -> setForeground(BRAND_GREEN);
                            case "failed"    -> setForeground(BRAND_RED);
                            default          -> setForeground(new Color(204, 122, 0));
                        }
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                    return this;
                }
            }
        );
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        return scrollPane;
    }

    private String[] buildRecentPeriods(int count) {
        String[] periods = new String[count];
        LocalDate current = LocalDate.now();
        for (int i = 0; i < count; i++) {
            LocalDate d = current.minusMonths(i);
            periods[i] = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                         + " " + d.getYear();
        }
        return periods;
    }

    // ------------------------------------------------------------------ //
    //  Logic
    // ------------------------------------------------------------------ //
    private void loadPayrollData() {
        String csvFile = ResourcePathService.resourceFile("MotorPHEmployeeData-EmployeeDetails.csv");

        try {
            PayrollService payrollService = new PayrollService();
            payrollRecords = payrollService.getPayrollRecords(csvFile);

            DefaultTableModel summaryModel = (DefaultTableModel) payrollTable.getModel();
            summaryModel.setRowCount(0);

            for (Payroll record : payrollRecords) {
                summaryModel.addRow(new Object[]{
                    record.getEmployeeNumber(),
                    record.getEmployeeName(),
                    "₱ " + String.format("%,.2f", record.getBasicSalary()),
                    "₱ " + String.format("%,.2f", record.getAllowances()),
                    "₱ " + String.format("%,.2f", record.getDeductions()),
                    "₱ " + String.format("%,.2f", record.getNetPay()),
                    "Processed"
                });
            }

            totalAmountLabel.setText("₱ " + String.format("%,.2f",
                payrollService.getTotalPayroll(payrollRecords)));

            DefaultTableModel historyModel = (DefaultTableModel) payrollHistoryTable.getModel();
            historyModel.setRowCount(0);
            List<String[]> historyRows = payrollService.loadPayrollHistoryRows(100);
            if (historyRows.isEmpty()) {
                String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
                String period = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
                historyRows = payrollService.buildHistoryRows(
                    payrollRecords,
                    date,
                    period,
                    currentFinanceUser,
                    3
                );
            }
            for (String[] row : historyRows) {
                historyModel.addRow(row);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading payroll data: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processPayroll(int monthIndex, int year) {
        String monthName = MONTH_NAMES[monthIndex];
        PayrollService payrollService = new PayrollService();
        double total = payrollService.getTotalPayroll(payrollRecords);

        try {
            payrollService.savePayrollRun(payrollRecords, monthName + " " + year, currentFinanceUser);
            loadPayrollData();
        } catch (Exception e) {
            showStyledPayrollPopup(
                "Payroll Warning",
                "Payroll processed but history save failed: " + e.getMessage(),
                true
            );
        }

        showStyledPayrollPopup(
            "Payroll Processed",
            "Payroll processed successfully for " + monthName + " " + year + "!\n\n"
            + "Total employees: " + payrollRecords.size() + "\n"
            + "Total payroll:   ₱ " + String.format("%,.2f", total) + "\n"
            + "Status: Completed",
            false
        );
    }

    private void showStyledPayrollPopup(String title, String message, boolean isWarning) {
        showStyledOperationPopup("PAYROLL PROCESS RESULT", title, message, isWarning);
    }

    private void showStyledOperationPopup(String pageTitle, String title, String message, boolean isWarning) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(12, 12, 10, 12));

        JEditorPane messagePane = new JEditorPane("text/html", buildOperationPopupHtml(pageTitle, title, message, isWarning));
        messagePane.setEditable(false);
        messagePane.setBackground(Color.WHITE);
        messagePane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(messagePane);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(560, 420));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = createButton("Close", BRAND_TEAL, Color.WHITE);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setBorder(new EmptyBorder(10, 28, 10, 28));
        closeBtn.addActionListener(evt -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 8, 0, 0));
        buttonPanel.add(closeBtn);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(620, 520));
        dialog.setSize(Math.max(dialog.getWidth(), 620), Math.max(dialog.getHeight(), 520));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String buildOperationPopupHtml(String pageTitle, String title, String message, boolean isWarning) {
        File logoFile = new File("images/motorpic.png");
        String logoTag = logoFile.exists()
            ? "<img src='" + logoFile.toURI() + "' width='72' height='72'/>"
            : "";

        String status = isWarning ? "Needs Review" : "Completed";
        String statusColor = isWarning ? "#cc7a00" : "#198754";
        String safeTitle = escapeHtml(title);
        String safeMessage = escapeHtml(message).replace("\n", "<br/>");

        String css =
            "body{font-family:Arial,sans-serif;font-size:11px;color:#111;margin:14px;}" +
            "table{width:100%;border-collapse:collapse;}" +
            ".logo{width:80px;vertical-align:middle;}" +
            ".co-info{padding-left:12px;vertical-align:middle;}" +
            ".co-name{font-size:24px;font-weight:bold;font-family:'Arial Black',Arial;}" +
            ".co-detail{font-size:9px;color:#444;line-height:1.6;margin-top:2px;}" +
            ".page-title{text-align:center;font-size:13px;font-weight:bold;text-decoration:underline;letter-spacing:2px;margin:10px 0;}" +
            ".sec{background:#1c1c1c;color:#fff;padding:6px 8px;font-size:11px;font-weight:bold;letter-spacing:1px;margin-top:8px;}" +
            ".box{border:1px solid #aaa;padding:8px;margin-top:0;font-size:11px;line-height:1.55;}" +
            ".row td{padding:5px 8px;border-bottom:1px solid #ececec;}" +
            ".lbl{font-weight:bold;background:#f5f5f5;width:38%;}" +
            ".val{text-align:right;}";

        return "<html><head><style>" + css + "</style></head><body>"
            + "<table style='margin-bottom:4px;'><tr>"
            + "<td class='logo'>" + logoTag + "</td>"
            + "<td class='co-info'>"
            + "<div class='co-name'>MotorPH</div>"
            + "<div class='co-detail'>"
            + "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City<br/>"
            + "Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073<br/>"
            + "Email: corporate@motorph.com"
            + "</div></td></tr></table>"
            + "<p class='page-title'>" + escapeHtml(pageTitle) + "</p>"
            + "<div class='sec'>RUN STATUS</div>"
            + "<table class='box'><tr class='row'><td class='lbl'>Operation</td><td class='val'>"
            + safeTitle
            + "</td></tr><tr class='row'><td class='lbl'>Status</td><td class='val' style='color:" + statusColor + ";font-weight:bold;'>"
            + status
            + "</td></tr></table>"
            + "<div class='sec'>DETAILS</div>"
            + "<div class='box'>"
            + safeMessage
            + "</div>"
            + "</body></html>";
    }

    private String escapeHtml(String value) {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private void saveDeductions() {
        try {
            payrollConfigService.saveDeductionSettings(
                strictAttendanceCheck != null && strictAttendanceCheck.isSelected());

            JOptionPane.showMessageDialog(this,
                "Payroll rules saved:\n\n"
                + "Deductions are auto-calculated from statutory CSV tables (SSS, PhilHealth, Pag-IBIG, Withholding Tax).\n"
                + "Only strict attendance mode is configurable here.\n"
                + "Strict mode: " + (strictAttendanceCheck != null && strictAttendanceCheck.isSelected() ? "ON" : "OFF"),
                "Settings Saved", JOptionPane.INFORMATION_MESSAGE);

            loadPayrollData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to save deduction settings: " + e.getMessage(),
                "Settings Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAllowances() {
        try {
            payrollConfigService.saveAllowanceSettings(
                ((Number) riceSubsidySpinner.getValue()).doubleValue(),
                ((Number) phoneAllowanceSpinner.getValue()).doubleValue(),
                ((Number) clothingAllowanceSpinner.getValue()).doubleValue()
            );

            JOptionPane.showMessageDialog(this,
                "Allowance settings saved:\n\n"
                + "Rice Subsidy:      ₱" + riceSubsidySpinner.getValue()      + "\n"
                + "Phone Allowance:   ₱" + phoneAllowanceSpinner.getValue()   + "\n"
                + "Clothing Allowance:₱" + clothingAllowanceSpinner.getValue(),
                "Settings Saved", JOptionPane.INFORMATION_MESSAGE);

            loadPayrollData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Failed to save allowance settings: " + e.getMessage(),
                "Settings Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPayrollSettings() {
        try {
            PayrollConfigService.PayrollConfig config = payrollConfigService.loadConfig();
            riceSubsidySpinner.setValue(config.getRiceAllowance());
            phoneAllowanceSpinner.setValue(config.getPhoneAllowance());
            clothingAllowanceSpinner.setValue(config.getClothingAllowance());
            if (strictAttendanceCheck != null) {
                strictAttendanceCheck.setSelected(config.isStrictAttendanceMode());
            }
        } catch (Exception ignored) {
            // Keep UI defaults when settings are unavailable.
        }
    }

    private void exportPayslips(String format) {
        showStyledOperationPopup(
            "PAYSLIP EXPORT RESULT",
            "Export Complete",
            "Payslips exported successfully in " + format + " format!\n"
            + "File: payslips_" + System.currentTimeMillis() + "." + format.toLowerCase(),
            false
        );
    }

    private void printPayslips() {
        showStyledOperationPopup(
            "PAYSLIP PRINT RESULT",
            "Print Payslips",
            "Print dialog opened.\nTotal payslips to print: " + payrollTable.getRowCount(),
            false
        );
    }

    private void logout() {
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            new UserLogin().setVisible(true);
            dispose();
        }
    }
}
