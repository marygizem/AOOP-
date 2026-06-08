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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import model.Employee;
import model.Payroll;
import service.AttendanceService;
import service.BonusService;
import service.EmployeeService;
import service.LeaveService;
import service.PayrollService;
import service.ResourcePathService;

public class HRDashboard extends JFrame {

    // --- Brand Colors ---
    private static final Color BRAND_TEAL   = new Color(0, 102, 102);
    private static final Color BRAND_GREEN  = new Color(25, 135, 84);
    private static final Color BRAND_RED    = new Color(220, 53, 69);
    private static final Color BRAND_GRAY   = new Color(108, 117, 125);
    private static final Color TEXT_DARK    = new Color(33, 37, 41);
    private static final Color TEXT_MUTED   = new Color(108, 117, 125);
    private static final Color BORDER_LIGHT = new Color(222, 226, 230);
    private static final Color ROW_STRIPE   = new Color(245, 250, 250);

    // --- State ---
    private List<Payroll> managerPayrollRecords = new ArrayList<>();
    private List<String[]> attendanceRows       = new ArrayList<>();

    // --- Tables ---
    private JTable teamTable;
    private JTable leaveTable;
    private JTable leaveHistoryTable;
    private JTable attendanceTable;
    private JTable payrollTable;

    // --- Misc ---
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;
    private final boolean openedFromAdmin;

    public HRDashboard(String managerNumber) {
        this(managerNumber, false);
    }

    public HRDashboard(String managerNumber, boolean openedFromAdmin) {
        this.openedFromAdmin = openedFromAdmin;
        initComponents();
        loadManagerDataAsync();
    }

    // ------------------------------------------------------------------ //
    //  Frame Setup
    // ------------------------------------------------------------------ //
    private void initComponents() {
        setTitle("MotorPH - HR Dashboard");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setSize(new Dimension(980, 660));
        setMinimumSize(new Dimension(980, 660));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ //
    //  Header 
    // ------------------------------------------------------------------ //
    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BRAND_TEAL);
        headerPanel.setBorder(new EmptyBorder(22, 30, 22, 30));

        JLabel titleLabel = new JLabel("HR DASHBOARD", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    // ------------------------------------------------------------------ //
    //  Main Content
    // ------------------------------------------------------------------ //
    private JPanel buildMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 10, 30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setFocusable(false);

        tabbedPane.addTab("Team Members",      createTeamPanel());
        tabbedPane.addTab("Leave Requests",    createLeavePanel());
        tabbedPane.addTab("Leave History",     createLeaveHistoryPanel());
        tabbedPane.addTab("Attendance",        createAttendancePanel());
        tabbedPane.addTab("Payroll Summary",   createPayrollPanel());
        tabbedPane.addTab("Reports & Bonuses", createReportsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        if (!openedFromAdmin) {
            mainPanel.add(buildLogoutPanel(), BorderLayout.SOUTH);
        }

        return mainPanel;
    }

    private JPanel buildLogoutPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        panel.setBackground(Color.WHITE);

        JButton logoutBtn = createButton("Logout", BRAND_RED, Color.WHITE);
        logoutBtn.addActionListener(evt -> logout());
        panel.add(logoutBtn);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Status Bar
    // ------------------------------------------------------------------ //
    private JLabel buildStatusBar() {
        statusLabel = new JLabel(" Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(BRAND_GRAY);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LIGHT),
            BorderFactory.createEmptyBorder(6, 15, 6, 15)
        ));
        return statusLabel;
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(" " + message);
        statusLabel.setForeground(color);
    }

    // ------------------------------------------------------------------ //
    //  Tab: Team Members 
    // ------------------------------------------------------------------ //
    private JPanel createTeamPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Your Team Members"), BorderLayout.NORTH);

        teamTable = buildTable(new String[]{"Employee #", "Last Name", "First Name", "Position", "Department"});
        setColumnWidths(teamTable, 110, 160, 160, 180, 160);
        panel.add(createStyledScrollPane(teamTable), BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Leave Requests
    // ------------------------------------------------------------------ //
    private JPanel createLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Pending Leave Requests"), BorderLayout.NORTH);

        leaveTable = buildTable(
            new String[]{"Employee #", "Name", "Leave Type", "Start Date", "End Date", "Status"});
        styleStatusColumn(leaveTable, 5);
        setColumnWidths(leaveTable, 110, 190, 140, 120, 120, 110);
        panel.add(createStyledScrollPane(leaveTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        btnPanel.setBackground(Color.WHITE);

        JButton refreshBtn = createButton("Refresh",          BRAND_TEAL,  Color.WHITE);
        JButton approveBtn = createButton("Approve Selected", BRAND_GREEN, Color.WHITE);
        JButton rejectBtn  = createButton("Reject Selected",  BRAND_RED,   Color.WHITE);

        refreshBtn.addActionListener(evt -> loadLeaveRequests());
        approveBtn.addActionListener(evt -> approveLeave());
        rejectBtn.addActionListener(evt  -> rejectLeave());

        btnPanel.add(refreshBtn);
        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Leave History
    // ------------------------------------------------------------------ //
    private JPanel createLeaveHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Leave Request History"), BorderLayout.NORTH);

        leaveHistoryTable = buildTable(
            new String[]{"Employee #", "Name", "Leave Type", "Start Date", "End Date", "Status"});
        styleStatusColumn(leaveHistoryTable, 5);
        setColumnWidths(leaveHistoryTable, 110, 190, 140, 120, 120, 110);
        panel.add(createStyledScrollPane(leaveHistoryTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        btnPanel.setBackground(Color.WHITE);
        JButton refreshBtn = createButton("Refresh", BRAND_TEAL, Color.WHITE);
        refreshBtn.addActionListener(evt -> loadLeaveHistory());
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Attendance
    // ------------------------------------------------------------------ //
    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Team Attendance Records"), BorderLayout.NORTH);

        attendanceTable = buildTable(
            new String[]{"Employee #", "Name", "Hours Worked", "Absences", "Attendance %"});
        setColumnWidths(attendanceTable, 110, 220, 140, 120, 130);
        panel.add(createStyledScrollPane(attendanceTable), BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Payroll Summary
    // ------------------------------------------------------------------ //
    private JPanel createPayrollPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        panel.add(createSectionTitle("Team Payroll Summary"), BorderLayout.NORTH);

        payrollTable = buildTable(
            new String[]{"Employee #", "Name", "Basic Salary", "Allowances", "Deductions", "Net Pay"});
        setColumnWidths(payrollTable, 110, 200, 140, 130, 130, 140);
        panel.add(createStyledScrollPane(payrollTable), BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  Tab: Reports & Bonuses
    // ------------------------------------------------------------------ //
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        JLabel title = new JLabel("Generate Reports & View Bonuses", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_DARK);
        gbc.gridy = 0;
        panel.add(title, gbc);

        JSeparator sep = new JSeparator();
        sep.setPreferredSize(new Dimension(360, 1));
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 20, 0);
        panel.add(sep, gbc);

        Dimension btnSize = new Dimension(340, 46);

        JButton payrollReportBtn = createButton("Generate Payroll Report",     BRAND_TEAL,  Color.WHITE);
        JButton attendReportBtn  = createButton("Generate Attendance Report",  BRAND_TEAL,  Color.WHITE);
        JButton bonusBtn         = createButton("View Team Bonuses/Incentives",BRAND_GREEN, Color.WHITE);

        payrollReportBtn.setPreferredSize(btnSize);
        attendReportBtn.setPreferredSize(btnSize);
        bonusBtn.setPreferredSize(btnSize);

        payrollReportBtn.addActionListener(evt -> generatePayrollReport());
        attendReportBtn.addActionListener(evt  -> generateAttendanceReport());
        bonusBtn.addActionListener(evt         -> viewBonuses());

        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridy = 2; panel.add(payrollReportBtn, gbc);
        gbc.gridy = 3; panel.add(attendReportBtn,  gbc);
        gbc.gridy = 4; panel.add(bonusBtn,         gbc);

        gbc.gridy = 5;
        gbc.weighty = 1.0;
        JPanel filler = new JPanel();
        filler.setBackground(Color.WHITE);
        panel.add(filler, gbc);

        return panel;
    }

    // ------------------------------------------------------------------ //
    //  UI Helpers
    // ------------------------------------------------------------------ //
    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(BRAND_TEAL);
        label.setBorder(new EmptyBorder(0, 0, 8, 0));
        return label;
    }

    private JTable buildTable(String[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        });

        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setSelectionBackground(BRAND_TEAL);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

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

        // Teal header 
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(BRAND_TEAL);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);   // disable drag reorder
        header.setResizingAllowed(false);     // disable column resize

        // Center-align header text
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        return table;
    }

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
                            case "approved" -> { setForeground(BRAND_GREEN); }
                            case "rejected" -> { setForeground(BRAND_RED); }
                            default         -> { setForeground(new Color(204, 122, 0)); }
                        }
                        setFont(getFont().deriveFont(Font.BOLD));
                    }
                    return this;
                }
            }
        );
    }

    private void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(widths[i]);   // prevent shrinking
            table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);   // prevent growing
        }
    }

    private JScrollPane createStyledScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table) {
            @Override
            public void paint(java.awt.Graphics g) {
                super.paint(g);
                if (table.getRowCount() == 0) {
                    g.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                    g.setColor(new Color(180, 180, 180));
                    String msg = "No records to display";
                    int x = (getWidth() - g.getFontMetrics().stringWidth(msg)) / 2;
                    int y = getHeight() / 2 + 20;
                    g.drawString(msg, x, y);
                }
            }
        };
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void showCustomReportDialog(String title, String headerText, JPanel contentPanel) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        header.setBackground(BRAND_TEAL);
        JLabel lblHeader = new JLabel(headerText);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(Color.WHITE);
        header.add(lblHeader);
        dialog.add(header, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(520, 340));
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_LIGHT));
        JButton closeBtn = createButton("Close", BRAND_GRAY, Color.WHITE);
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(closeBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 430));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ------------------------------------------------------------------ //
    //  Data Loading
    // ------------------------------------------------------------------ //
    private void loadManagerDataAsync() {
        setStatus("Loading team data...", BRAND_GRAY);

        new SwingWorker<Void, Void>() {
            List<Employee> employees;
            List<Payroll>  payrollList;
            List<String[]> teamAttendance;

            @Override
            protected Void doInBackground() throws Exception {
                String empCsv           = ResourcePathService.resourceFile("MotorPHEmployeeData-EmployeeDetails.csv");
                String attendanceLogCsv = ResourcePathService.resourceFile("MotorPH_Attendance.csv");

                EmployeeService empService = new EmployeeService();
                employees = empService.loadAllEmployees(empCsv);

                AttendanceService attendanceService = new AttendanceService();
                teamAttendance = attendanceService.buildTeamAttendanceRows(empCsv, attendanceLogCsv);

                PayrollService payService = new PayrollService();
                payrollList = payService.getPayrollRecords(empCsv);

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();

                    // Team table — removed email since we have no actual data from the motorph
                    DefaultTableModel teamModel = (DefaultTableModel) teamTable.getModel();
                    teamModel.setRowCount(0);
                    for (Employee emp : employees) {
                        if (emp != null) {
                            teamModel.addRow(new String[]{
                                emp.getEmployeeNumber(),
                                emp.getLastName(),
                                emp.getFirstName(),
                                emp.getPosition()   != null ? emp.getPosition()   : "N/A",
                                emp.getDepartment() != null ? emp.getDepartment() : "N/A"
                            });
                        }
                    }

                    // Payroll table
                    managerPayrollRecords = payrollList;
                    DefaultTableModel payModel = (DefaultTableModel) payrollTable.getModel();
                    payModel.setRowCount(0);
                    for (Payroll r : payrollList) {
                        payModel.addRow(new String[]{
                            r.getEmployeeNumber(),
                            r.getEmployeeName(),
                            String.format("₱%,.2f", r.getBasicSalary()),
                            String.format("₱%,.2f", r.getAllowances()),
                            String.format("₱%,.2f", r.getDeductions()),
                            String.format("₱%,.2f", r.getNetPay())
                        });
                    }

                    // Attendance table
                    attendanceRows = new ArrayList<>();
                    DefaultTableModel attModel = (DefaultTableModel) attendanceTable.getModel();
                    attModel.setRowCount(0);
                    for (String[] row : teamAttendance) {
                        if (row.length >= 6) {
                            attendanceRows.add(new String[]{row[0], row[1], row[2], row[4], row[5]});
                            attModel.addRow(new String[]{
                                row[0], row[1] + ", " + row[2], row[3], row[4], row[5]
                            });
                        }
                    }

                    loadLeaveRequests();
                    loadLeaveHistory();

                    setStatus("Loaded " + employees.size() + " team members.", BRAND_TEAL);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setStatus("Loading interrupted.", BRAND_RED);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = cause != null && cause.getMessage() != null
                        ? cause.getMessage()
                        : "Some data files may be missing.";
                    setStatus(message, BRAND_RED);
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------ //
    //  Leave Loading
    // ------------------------------------------------------------------ //
    private void loadLeaveRequests() {
        String leaveCsv = ResourcePathService.resourceFile("LeaveRequests.csv");
        String empCsv   = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
        try {
            LeaveService leaveService = new LeaveService();
            DefaultTableModel model = (DefaultTableModel) leaveTable.getModel();
            model.setRowCount(0);
            for (String[] row : leaveService.getPendingLeaveRows(leaveCsv, empCsv)) {
                model.addRow(row);
            }
            setStatus("Leave requests loaded.", BRAND_TEAL);
        } catch (Exception e) {
            setStatus("Error loading leave requests.", BRAND_RED);
        }
    }

    private void loadLeaveHistory() {
        String leaveCsv = ResourcePathService.resourceFile("LeaveRequests.csv");
        String empCsv   = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
        try {
            LeaveService leaveService = new LeaveService();
            DefaultTableModel model = (DefaultTableModel) leaveHistoryTable.getModel();
            model.setRowCount(0);
            for (String[] row : leaveService.getLeaveHistoryRows(leaveCsv, empCsv)) {
                model.addRow(row);
            }
        } catch (Exception e) {
            setStatus("Error loading leave history.", BRAND_RED);
        }
    }

    // ------------------------------------------------------------------ //
    //  Leave Actions
    // ------------------------------------------------------------------ //
    private void approveLeave() { processLeaveAction("Approved", "approved"); }
    private void rejectLeave()  { processLeaveAction("Rejected", "rejected"); }

    private void processLeaveAction(String newStatus, String verb) {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a leave request first.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String empNum    = leaveTable.getValueAt(selectedRow, 0).toString();
        String leaveType = leaveTable.getValueAt(selectedRow, 2).toString();
        String startDate = leaveTable.getValueAt(selectedRow, 3).toString();
        String endDate   = leaveTable.getValueAt(selectedRow, 4).toString();
        String empName   = leaveTable.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark leave for " + empName + " as " + newStatus + "?",
            "Confirm Action", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String leaveCsv = ResourcePathService.resourceFile("LeaveRequests.csv");
        LeaveService leaveService = new LeaveService();
        boolean success = leaveService.updateLeaveStatus(
            leaveCsv, empNum, leaveType, startDate, endDate, newStatus);

        if (success) {
            setStatus("Leave for " + empName + " marked as " + newStatus + ".", BRAND_GREEN);
            JOptionPane.showMessageDialog(this, "Leave request " + verb + " successfully.",
                newStatus, JOptionPane.INFORMATION_MESSAGE);
            loadLeaveRequests();
            loadLeaveHistory();
        } else {
            setStatus("Failed to update leave status.", BRAND_RED);
            JOptionPane.showMessageDialog(this, "Failed to update leave status.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------------------------------------------------ //
    //  Reports
    // ------------------------------------------------------------------ //
    private void generatePayrollReport() {
        PayrollService payrollService = new PayrollService();
        double totalPayroll = payrollService.getTotalPayroll(managerPayrollRecords);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        content.add(createMetricCard(
            "Total Team Members",
            String.valueOf(teamTable.getRowCount()),
            new Color(13, 110, 253)));
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(createMetricCard(
            "Total Payroll Processed",
            "₱" + String.format("%,.2f", totalPayroll),
            BRAND_TEAL));
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(new JSeparator());
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(styledReportHint("payroll_report_" + System.currentTimeMillis() + ".pdf"));

        wrapAndShow(content, "Payroll Report", "Payroll Summary");
    }

    private void generateAttendanceReport() {
        AttendanceService attendanceService = new AttendanceService();
        AttendanceService.AttendanceSummary summary = attendanceService.summarize(attendanceRows);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        content.add(createMetricCard(
            "Team Attendance Average",
            String.format("%.1f%%", summary.getAverageAttendance()),
            BRAND_TEAL));
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(createMetricCard(
            "Total Team Absences",
            String.valueOf(summary.getTotalAbsences()),
            new Color(220, 53, 69)));
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(new JSeparator());
        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(styledReportHint("attendance_report_" + System.currentTimeMillis() + ".pdf"));

        wrapAndShow(content, "Attendance Report", "Attendance Summary");
    }

    private void viewBonuses() {
        String bonusCsv = ResourcePathService.resourceFile("BonusData.csv");

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(15, 20, 15, 20));

        try {
            BonusService bonusService = new BonusService();
            BonusService.BonusSummary summary = bonusService.getBonusSummary(bonusCsv);

            for (Map.Entry<String, List<BonusService.BonusEntry>> entry
                    : summary.getGroupedBonuses().entrySet()) {

                JPanel groupHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
                groupHeader.setBackground(Color.WHITE);
                groupHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
                JLabel typeLabel = new JLabel(entry.getKey());
                typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
                typeLabel.setForeground(BRAND_TEAL);
                groupHeader.add(typeLabel);
                content.add(groupHeader);

                for (BonusService.BonusEntry bonusEntry : entry.getValue()) {
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(new Color(248, 249, 250));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_LIGHT),
                        new EmptyBorder(10, 15, 10, 15)
                    ));
                    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
                    card.setAlignmentX(Component.LEFT_ALIGNMENT);

                    JLabel nameLabel = new JLabel(bonusEntry.getEmployeeName());
                    nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    nameLabel.setForeground(TEXT_DARK);

                    JLabel amountLabel = new JLabel("₱" + String.format("%,.0f", bonusEntry.getAmount()));
                    amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    amountLabel.setForeground(BRAND_GREEN);

                    card.add(nameLabel,   BorderLayout.WEST);
                    card.add(amountLabel, BorderLayout.EAST);
                    content.add(card);
                    content.add(Box.createRigidArea(new Dimension(0, 6)));
                }
                content.add(Box.createRigidArea(new Dimension(0, 14)));
            }

            content.add(new JSeparator());
            content.add(Box.createRigidArea(new Dimension(0, 14)));

            JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            totalPanel.setBackground(Color.WHITE);
            totalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel totalLabel = new JLabel("Total Bonus Pool:  ₱"
                + String.format("%,.0f", summary.getGrandTotal()));
            totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
            totalLabel.setForeground(BRAND_TEAL);
            totalPanel.add(totalLabel);
            content.add(totalPanel);

        } catch (Exception e) {
            JLabel error = new JLabel("No bonus data file found.");
            error.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            error.setForeground(TEXT_MUTED);
            content.add(error);
        }

        wrapAndShow(content, "Team Bonuses", "Bonuses & Incentives");
    }

    // ------------------------------------------------------------------ //
    //  Report Panel Helpers
    // ------------------------------------------------------------------ //
    private JLabel styledReportHint(String filename) {
        JLabel label = new JLabel("File saved: " + filename);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(BRAND_GRAY);
        return label;
    }

    private JPanel createMetricCard(String title, String value, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(248, 249, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(valueColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void wrapAndShow(JPanel content, String title, String header) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(4, 4, 4, 4));
        wrapper.add(content, BorderLayout.CENTER);
        showCustomReportDialog(title, header, wrapper);
    }

    // ------------------------------------------------------------------ //
    //  Logout
    // ------------------------------------------------------------------ //
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


