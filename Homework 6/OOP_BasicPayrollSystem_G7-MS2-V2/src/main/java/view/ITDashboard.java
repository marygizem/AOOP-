package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import service.AuthService;
import service.ITCredentialService;
import service.ResourcePathService;

public class ITDashboard extends JFrame {

    private final String username;
    private final boolean openedFromAdmin;
    private final ITCredentialService credentialService;

    private JTable accountTable;
    private JLabel statusLabel;

    public ITDashboard(String username) {
        this(username, false);
    }

    public ITDashboard(String username, boolean openedFromAdmin) {
        this.username = username;
        this.openedFromAdmin = openedFromAdmin;
        this.credentialService = new ITCredentialService();
        initComponents();
        loadAccounts();
    }

    private void initComponents() {
        setTitle("MotorPH - IT Dashboard");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 18));
        header.setBackground(new Color(0, 102, 102));
        JLabel title = new JLabel("IT Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 20));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel note = new JLabel(
                "System tools access for user: " + (username == null ? "IT" : username),
                SwingConstants.LEFT);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        note.setForeground(new Color(33, 37, 41));
        center.add(note, BorderLayout.NORTH);

        accountTable = new JTable(new DefaultTableModel(
            new String[]{"Employee #", "Email", "Username", "Role", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        styleTable(accountTable);
        JScrollPane scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        center.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton reloadBtn = buildButton("Reload Accounts", new Color(0, 102, 102));
        reloadBtn.addActionListener(evt -> loadAccounts());

        JButton resetPwdBtn = buildButton("Reset Password", new Color(25, 135, 84));
        resetPwdBtn.addActionListener(evt -> resetPassword());

        JButton roleBtn = buildButton("Change Role", new Color(13, 110, 253));
        roleBtn.addActionListener(evt -> changeRole());

        JButton lockBtn = buildButton("Lock/Unlock", new Color(220, 53, 69));
        lockBtn.addActionListener(evt -> toggleLock());

        buttonPanel.add(reloadBtn);
        buttonPanel.add(resetPwdBtn);
        buttonPanel.add(roleBtn);
        buttonPanel.add(lockBtn);

        JPanel southPanel = new JPanel(new BorderLayout(0, 10));
        southPanel.setBackground(Color.WHITE);
        southPanel.add(buttonPanel, BorderLayout.NORTH);

        statusLabel = new JLabel(" Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        center.add(southPanel, BorderLayout.SOUTH);

        if (!openedFromAdmin) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            footer.setBackground(Color.WHITE);
            footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

            JButton logoutBtn = buildButton("Logout", new Color(220, 53, 69));
            logoutBtn.addActionListener(evt -> logout());
            footer.add(logoutBtn);
            add(footer, BorderLayout.SOUTH);
        }

        add(center, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1080, 640));
        setLocationRelativeTo(null);
    }

    private void loadAccounts() {
        String csvPath = ResourcePathService.resourceFile("MotorPHcredentialLogin.csv");

        try {
            List<ITCredentialService.AccountRow> accounts = credentialService.loadAccounts(csvPath);
            DefaultTableModel model = (DefaultTableModel) accountTable.getModel();
            model.setRowCount(0);
            for (ITCredentialService.AccountRow account : accounts) {
                model.addRow(new Object[]{
                    account.getEmployeeNumber(),
                    account.getEmail(),
                    account.getUsername(),
                    account.getRole(),
                    account.getStatus()
                });
            }
            setStatus("Loaded " + accounts.size() + " account(s).", new Color(0, 102, 102));
        } catch (Exception e) {
            setStatus("Failed loading credentials.", new Color(220, 53, 69));
            JOptionPane.showMessageDialog(this,
                "Error loading credentials: " + e.getMessage(),
                "IT Credentials", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPassword() {
        String selectedUser = getSelectedUsername();
        if (selectedUser == null) {
            return;
        }

        String newPassword = JOptionPane.showInputDialog(this,
            "Enter new password for '" + selectedUser + "':",
            "Reset Password", JOptionPane.PLAIN_MESSAGE);
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }

        try {
            credentialService.resetPassword(
                ResourcePathService.resourceFile("MotorPHcredentialLogin.csv"),
                selectedUser,
                newPassword.trim());
            setStatus("Password reset for " + selectedUser + ".", new Color(25, 135, 84));
        } catch (Exception e) {
            setStatus("Password reset failed.", new Color(220, 53, 69));
            JOptionPane.showMessageDialog(this,
                "Unable to reset password: " + e.getMessage(),
                "IT Credentials", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changeRole() {
        String selectedUser = getSelectedUsername();
        if (selectedUser == null) {
            return;
        }

        Object selected = JOptionPane.showInputDialog(
            this,
            "Select new role for '" + selectedUser + "':",
            "Change Role",
            JOptionPane.PLAIN_MESSAGE,
            null,
            AuthService.AuthRole.values(),
            AuthService.AuthRole.EMPLOYEE);

        if (!(selected instanceof AuthService.AuthRole role)) {
            return;
        }

        try {
            credentialService.updateRole(
                ResourcePathService.resourceFile("MotorPHcredentialLogin.csv"),
                selectedUser,
                role);
            setStatus("Role updated for " + selectedUser + " -> " + role.name() + ".", new Color(25, 135, 84));
            loadAccounts();
        } catch (Exception e) {
            setStatus("Role update failed.", new Color(220, 53, 69));
            JOptionPane.showMessageDialog(this,
                "Unable to change role: " + e.getMessage(),
                "IT Credentials", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleLock() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Select an account first.",
                "IT Credentials", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedUser = getSelectedUsername();
        String status = String.valueOf(accountTable.getValueAt(selectedRow, 4));
        boolean lock = !ITCredentialService.STATUS_LOCKED.equalsIgnoreCase(status);

        try {
            credentialService.setAccountLocked(
                ResourcePathService.resourceFile("MotorPHcredentialLogin.csv"),
                selectedUser,
                lock);
            setStatus((lock ? "Locked " : "Unlocked ") + selectedUser + ".", new Color(25, 135, 84));
            loadAccounts();
        } catch (Exception e) {
            setStatus("Status update failed.", new Color(220, 53, 69));
            JOptionPane.showMessageDialog(this,
                "Unable to update account status: " + e.getMessage(),
                "IT Credentials", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedUsername() {
        int selectedRow = accountTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Select an account first.",
                "IT Credentials", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return String.valueOf(accountTable.getValueAt(selectedRow, 2));
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(" " + message);
        statusLabel.setForeground(color);
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(34);
        table.setSelectionBackground(new Color(0, 102, 102));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 250));
                    setForeground(new Color(33, 37, 41));
                }
                return this;
            }
        });

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 102));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private JButton buildButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return btn;
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
