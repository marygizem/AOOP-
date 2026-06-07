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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import service.LeaveService;
import service.ResourcePathService;

public class LeaveHistoryDialog extends JDialog {

    private final String employeeNumber;

    public LeaveHistoryDialog(JFrame parent, String employeeNumber) {
        super(parent, "My Leave History", true);
        this.employeeNumber = employeeNumber;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TITLE ---
        JLabel titleLabel = new JLabel("Leave Request History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(33, 37, 41));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // --- TABLE ---
        DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Leave Type", "Start Date", "End Date", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // read-only table
            }
        };

        loadLeaveData(tableModel);

        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);

        // Header styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(0, 102, 102));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 250));
                    setForeground(new Color(33, 37, 41));
                }
                return this;
            }
        });

        // Status column color renderer 
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, selected, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(245, 250, 250));
                    String status = val != null ? val.toString().toLowerCase() : "";
                    switch (status) {
                        case "approved"  -> setForeground(new Color(25, 135, 84));
                        case "rejected"  -> setForeground(new Color(220, 53, 69));
                        default          -> setForeground(new Color(204, 122, 0)); // pending
                    }
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return this;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218), 1));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // --- CLOSE BUTTON ---
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setBackground(new Color(0, 102, 102));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        closeBtn.addActionListener(evt -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(closeBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        add(contentPanel);
        pack();
        setMinimumSize(new Dimension(620, 420));
        setLocationRelativeTo(getParent());
    }

    private void loadLeaveData(DefaultTableModel tableModel) {
        String leaveCsvFile = ResourcePathService.resourceFile("LeaveRequests.csv");

        try {
            LeaveService leaveService = new LeaveService();
            List<String[]> rows = leaveService.getEmployeeLeaveHistoryRows(
                leaveCsvFile, employeeNumber);

            if (rows.isEmpty()) {
                tableModel.addRow(new String[]{"No leave records found.", "", "", ""});
            } else {
                for (String[] row : rows) {
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading leave history: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
