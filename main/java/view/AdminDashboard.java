package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import service.AdminEmployeeManagementService;
import service.ResourcePathService;

public class AdminDashboard extends JFrame {

    // --- Brand Colors ---
    private static final Color BRAND_TEAL   = new Color(0, 102, 102);
    private static final Color BRAND_GREEN  = new Color(25, 135, 84);
    private static final Color BRAND_BLUE   = new Color(13, 110, 253);
    private static final Color BRAND_RED    = new Color(220, 53, 69);
    private static final Color BRAND_GRAY   = new Color(108, 117, 125);
    private static final Color TEXT_DARK    = new Color(33, 37, 41);
    private static final Color BORDER_LIGHT = new Color(222, 226, 230);
    private static final Color ROW_STRIPE   = new Color(245, 250, 250);

    // --- Services ---
    private final CurrentUser currentUser;
    private final AdminEmployeeManagementService adminService;

    // --- Components ---
    private JTable employeeTable;
    private JScrollPane tableScrollPane;
    private JLabel statusLabel;

    private JButton reloadBtn;
    private JButton addBtn;
    private JButton updateBtn;
    private JButton deleteBtn;
    private JButton openHROpsBtn;
    private JButton openFinanceBtn;
    private JButton openITBtn;
    private JButton logoutBtn;

    // --- Constructors ---
    public AdminDashboard() {
        this(new CurrentUser("HR"));
    }

    public AdminDashboard(CurrentUser currentUser) {
        this.currentUser = currentUser;
        this.adminService = new AdminEmployeeManagementService();
        initComponents();
        loadDataAsync();
    }

    // ------------------------------------------------------------------ //
    //  Frame Setup
    // ------------------------------------------------------------------ //
    private void initComponents() {
        setTitle("MotorPH - Employee Payroll Administrator");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(true);                          //  allow resize
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        applyRolePermissions();

        pack();
        setMinimumSize(new Dimension(1280, 650));    // wider to show all columns
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ //
    //  Header
    // ------------------------------------------------------------------ //
    private JPanel buildHeader() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        headerPanel.setBackground(BRAND_TEAL);

        JLabel titleLabel = new JLabel("EMPLOYEE PAYROLL ADMINISTRATOR");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);

        return headerPanel;
    }

    // ------------------------------------------------------------------ //
    //  Main Content (Table + Buttons)
    // ------------------------------------------------------------------ //
    private JPanel buildMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(25, 40, 20, 40));

        mainPanel.add(buildTable(), BorderLayout.CENTER);
        mainPanel.add(buildButtonPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    // ------------------------------------------------------------------ //
    //  Table 
    // ------------------------------------------------------------------ //
    private JScrollPane buildTable() {
        employeeTable = new JTable(new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        });

        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        employeeTable.setRowHeight(36);
        employeeTable.setSelectionBackground(BRAND_TEAL);
        employeeTable.setSelectionForeground(Color.WHITE);
        employeeTable.setShowGrid(false);
        employeeTable.setIntercellSpacing(new Dimension(0, 0));
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.setAutoCreateRowSorter(true);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 

        // Alternating row renderer
        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        // Teal table header
        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(BRAND_TEAL);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 42));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        tableScrollPane = new JScrollPane(employeeTable) {
            @Override
            public void paint(java.awt.Graphics g) {
                super.paint(g);
                if (employeeTable.getRowCount() == 0) {
                    g.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    g.setColor(new Color(150, 150, 150));
                    String msg = "No employee records to display";
                    int x = (getWidth() - g.getFontMetrics().stringWidth(msg)) / 2;
                    int y = getHeight() / 2 + 20;
                    g.drawString(msg, x, y);
                }
            }
        };
        tableScrollPane.getViewport().setBackground(Color.WHITE);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_LIGHT));
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        return tableScrollPane;
    }

    private JPanel buildButtonPanel() {
        // Left: action buttons
        JPanel leftBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftBtns.setBackground(Color.WHITE);

        reloadBtn          = createButton("Reload Data",      BRAND_GRAY,  Color.WHITE);
        addBtn             = createButton("Add",              BRAND_GREEN, Color.WHITE);
        updateBtn          = createButton("Update",           BRAND_BLUE,  Color.WHITE);
        deleteBtn          = createButton("Delete",           BRAND_RED,   Color.WHITE);

        leftBtns.add(reloadBtn);
        leftBtns.add(addBtn);
        leftBtns.add(updateBtn);
        leftBtns.add(deleteBtn);

        if ("ADMIN".equals(currentUser.getRole())) {
            openHROpsBtn       = createButton("Open HR Module",      BRAND_BLUE,  Color.WHITE);
            openFinanceBtn     = createButton("Open Finance Module", BRAND_TEAL,  Color.WHITE);
            openITBtn          = createButton("Open IT Module",      BRAND_GREEN, Color.WHITE);

            leftBtns.add(openHROpsBtn);
            leftBtns.add(openFinanceBtn);
            leftBtns.add(openITBtn);
        }

        // Right: logout
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBtns.setBackground(Color.WHITE);

        logoutBtn = createButton("Logout", BRAND_RED, Color.WHITE);
        rightBtns.add(logoutBtn);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(leftBtns,  BorderLayout.WEST);
        buttonPanel.add(rightBtns, BorderLayout.EAST);

        // Wire up actions
        reloadBtn.addActionListener(evt -> loadDataAsync());
        addBtn.addActionListener(evt -> addEmployee());
        updateBtn.addActionListener(evt -> updateEmployee());
        deleteBtn.addActionListener(evt -> deleteEmployee());
        if (openHROpsBtn != null) {
            openHROpsBtn.addActionListener(evt -> openHROps());
        }
        if (openFinanceBtn != null) {
            openFinanceBtn.addActionListener(evt -> openFinanceOps());
        }
        if (openITBtn != null) {
            openITBtn.addActionListener(evt -> openITOps());
        }
        logoutBtn.addActionListener(evt -> logout());

        return buttonPanel;
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
    //  Role-Based Access
    // ------------------------------------------------------------------ //
    private void applyRolePermissions() {
        boolean isHR = "HR".equals(currentUser.getRole());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());
        boolean canManageEmployees = isHR || isAdmin;

        addBtn.setEnabled(canManageEmployees);
        updateBtn.setEnabled(canManageEmployees);
        deleteBtn.setEnabled(canManageEmployees);

        if (!canManageEmployees) {
            addBtn.setToolTipText("HR access required");
            updateBtn.setToolTipText("HR access required");
            deleteBtn.setToolTipText("HR access required");
        }
    }

    // ------------------------------------------------------------------ //
    //  Data Loading 
    // ------------------------------------------------------------------ //
    private void loadDataAsync() {
        setStatus("Loading employee data...", BRAND_GRAY);
        reloadBtn.setEnabled(false);

        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                String csvFile = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
                return adminService.loadAdminRows(csvFile);
            }

            @Override
            protected void done() {
                reloadBtn.setEnabled(true);
                try {
                    List<String[]> data = get();
                    if (data == null || data.isEmpty()) {
                        setStatus("No employee data found.", BRAND_GRAY);
                        return;
                    }

                    DefaultTableModel model = (DefaultTableModel) employeeTable.getModel();
                    model.setColumnIdentifiers(data.get(0));
                    model.setRowCount(0);
                    for (int i = 1; i < data.size(); i++) {
                        model.addRow(data.get(i));
                    }

                    setColumnWidths();
                    setStatus("Loaded " + (data.size() - 1) + " employee records.", BRAND_TEAL);

                } catch (Exception e) {
                    setStatus("Error loading data: " + e.getMessage(), BRAND_RED);
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Error loading employee data.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // ------------------------------------------------------------------ //
    //  Column Widths 
    // ------------------------------------------------------------------ //
    private void setColumnWidths() {
        // Employee#, Last, First, SSS, Philhealth, TIN, Pagibig, Email, Basic, Rice, Phone, Clothing
        int[] widths = {100, 120, 130, 120, 150, 130, 130, 210, 110, 120, 110, 120};
        for (int i = 0; i < widths.length && i < employeeTable.getColumnCount(); i++) {
            employeeTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            
        }
    }

    // ------------------------------------------------------------------ //
    //  CRUD Actions
    // ------------------------------------------------------------------ //
    private void addEmployee() {
        String csvFile = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
        EmployeeFormDialog dialog = new EmployeeFormDialog(
            this,
            "Add New Employee",
            null,
            candidate -> {
                AdminEmployeeManagementService.OperationResult validation = adminService.validateNewEmployee(
                    csvFile,
                    candidate.getEmpNumber(), candidate.getLastName(), candidate.getFirstName(),
                    candidate.getSss(), candidate.getPhilhealth(), candidate.getTin(), candidate.getPagibig(), candidate.getEmail(),
                    candidate.getBasicSalary(), candidate.getRiceAllowance(),
                    candidate.getPhoneAllowance(), candidate.getClothingAllowance()
                );
                return validation.isSuccess() ? null : validation.getMessage();
            }
        );
        dialog.setVisible(true);

        EmployeeFormDialog.FormResult result = dialog.getResult();
        if (result == null) return;

        AdminEmployeeManagementService.OperationResult opResult = adminService.addEmployee(
            csvFile,
            result.getEmpNumber(), result.getLastName(), result.getFirstName(),
            result.getSss(), result.getPhilhealth(), result.getTin(), result.getPagibig(), result.getEmail(),
            result.getBasicSalary(), result.getRiceAllowance(),
            result.getPhoneAllowance(), result.getClothingAllowance()
        );

        if (opResult.isSuccess()) {
            setStatus("Employee " + result.getEmpNumber() + " added successfully.", BRAND_GREEN);
            JOptionPane.showMessageDialog(this, opResult.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
            loadDataAsync();
        } else {
            JOptionPane.showMessageDialog(this, opResult.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to update.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pre-fill with current values
        EmployeeFormDialog.FormResult prefill = new EmployeeFormDialog.FormResult(
            str(employeeTable, selectedRow, 0),
            str(employeeTable, selectedRow, 1),
            str(employeeTable, selectedRow, 2),
            str(employeeTable, selectedRow, 3),
            str(employeeTable, selectedRow, 4),
            str(employeeTable, selectedRow, 5),
            str(employeeTable, selectedRow, 6),
            str(employeeTable, selectedRow, 7),
            str(employeeTable, selectedRow, 8),
            str(employeeTable, selectedRow, 9),
            str(employeeTable, selectedRow, 10),
            str(employeeTable, selectedRow, 11)
        );

        String csvFile = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
        EmployeeFormDialog dialog = new EmployeeFormDialog(
            this,
            "Update Employee",
            prefill,
            candidate -> {
                AdminEmployeeManagementService.OperationResult validation = adminService.validateUpdatedEmployee(
                    csvFile,
                    prefill.getEmpNumber(),
                    candidate.getLastName(), candidate.getFirstName(),
                    candidate.getSss(), candidate.getPhilhealth(), candidate.getTin(), candidate.getPagibig(), candidate.getEmail(),
                    candidate.getBasicSalary(), candidate.getRiceAllowance(),
                    candidate.getPhoneAllowance(), candidate.getClothingAllowance()
                );
                return validation.isSuccess() ? null : validation.getMessage();
            }
        );
        dialog.setVisible(true);

        EmployeeFormDialog.FormResult result = dialog.getResult();
        if (result == null) return;

        AdminEmployeeManagementService.OperationResult opResult = adminService.updateEmployee(
            csvFile,
            prefill.getEmpNumber(), result.getLastName(), result.getFirstName(),
            result.getSss(), result.getPhilhealth(), result.getTin(), result.getPagibig(), result.getEmail(),
            result.getBasicSalary(), result.getRiceAllowance(),
            result.getPhoneAllowance(), result.getClothingAllowance()
        );

        if (opResult.isSuccess()) {
            setStatus("Employee " + prefill.getEmpNumber() + " updated successfully.", BRAND_GREEN);
            JOptionPane.showMessageDialog(this, opResult.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
            loadDataAsync();
        } else {
            JOptionPane.showMessageDialog(this, opResult.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee to delete.",
                "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String empNum = str(employeeTable, selectedRow, 0);
        String name   = str(employeeTable, selectedRow, 2) + " " + str(employeeTable, selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete:\n" + empNum + " — " + name.trim() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String csvFile = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
        AdminEmployeeManagementService.OperationResult result = adminService.deleteEmployee(csvFile, empNum);

        if (result.isSuccess()) {
            setStatus("Employee " + empNum + " deleted.", BRAND_RED);
            JOptionPane.showMessageDialog(this, result.getMessage(), "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadDataAsync();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openHROps() {
        HRDashboard hrDash = new HRDashboard("admin", true);
        hrDash.setVisible(true);
    }

    private void openFinanceOps() {
        FinanceDashboard financeDash = new FinanceDashboard("admin", true);
        financeDash.setVisible(true);
    }

    private void openITOps() {
        ITDashboard itDash = new ITDashboard("admin", true);
        itDash.setVisible(true);
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

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //
    private String str(JTable table, int row, int col) {
        if (col >= table.getColumnCount()) {
            return "";
        }
        Object val = table.getValueAt(row, col);
        return val != null ? val.toString() : "";
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

    // ------------------------------------------------------------------ //
    //  Inner Classes
    // ------------------------------------------------------------------ //
    public static class CurrentUser {
        private final String role;

        public CurrentUser(String role) {
            this.role = role;
        }

        public String getRole() {
            return role;
        }
    }
}
