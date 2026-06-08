package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

import service.LeaveService;
import service.ResourcePathService;

public class LeaveFilingDialog extends JDialog {

    private final String employeeNumber;
    private boolean submitted = false;

    public LeaveFilingDialog(Frame parent, String employeeNumber) {
        super(parent, "File Leave", true);
        this.employeeNumber = employeeNumber;
        initComponents();
    }

    private void initComponents() {
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);

        // Main container with padding
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header Title
        JLabel titleLabel = new JLabel("File a Leave Request");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form Area
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        jLabel1 = createLabel("Leave Type:");
        jLabel2 = createLabel("Start Date:");
        jLabel3 = createLabel("End Date:");
        jLabel4 = createLabel("Reason:");

        jComboBox1 = new JComboBox<>(new String[]{"Vacation", "Sick Leave", "Personal Leave"});
        jComboBox1.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jComboBox1.setBackground(Color.WHITE);

        startDateSpinner = createDateSpinner();
        endDateSpinner = createDateSpinner();
        jTextField3 = createTextField();
        jComboBox1.addActionListener(evt -> enforceDatePolicy());
        enforceDatePolicy();

        addFormRow(formPanel, gbc, jLabel1, jComboBox1, 0);
        addFormRow(formPanel, gbc, jLabel2, startDateSpinner, 1);
        addFormRow(formPanel, gbc, jLabel3, endDateSpinner, 2);
        addFormRow(formPanel, gbc, jLabel4, jTextField3, 3);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Button Area
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);

        jButton1 = createButton("Submit", true);
        jButton2 = createButton("Cancel", false);

        jButton1.addActionListener(evt -> submitLeave());
        jButton2.addActionListener(evt -> cancel());

        buttonPanel.add(jButton2); 
        buttonPanel.add(jButton1);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(jButton1);

        add(mainPanel);
        pack();
        setLocationRelativeTo(getParent()); 
    }

    // --- UI HELPER METHODS ---

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(73, 80, 87));
        return label;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField(15);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    private JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return spinner;
    }

    private void enforceDatePolicy() {
        boolean isSickLeave = "Sick Leave".equalsIgnoreCase((String) jComboBox1.getSelectedItem());
        SpinnerDateModel startModel = (SpinnerDateModel) startDateSpinner.getModel();
        SpinnerDateModel endModel = (SpinnerDateModel) endDateSpinner.getModel();

        if (isSickLeave) {
            Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
            startModel.setEnd(today);
            endModel.setEnd(today);

            if (((Date) startDateSpinner.getValue()).after(today)) {
                startDateSpinner.setValue(today);
            }
            if (((Date) endDateSpinner.getValue()).after(today)) {
                endDateSpinner.setValue(today);
            }
        } else {
            startModel.setEnd(null);
            endModel.setEnd(null);
        }
    }

    private JButton createButton(String text, boolean isPrimary) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        if (isPrimary) {
            btn.setBackground(new Color(25, 135, 84)); // Modern Green for Submit
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(233, 236, 239)); // Light Gray for Cancel
            btn.setForeground(new Color(33, 37, 41));
        }
        return btn;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, JLabel label, JComponent field, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        panel.add(field, gbc);
    }


    private void submitLeave() {
        String leaveType = (String) jComboBox1.getSelectedItem();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        String startDate = ((Date) startDateSpinner.getValue()).toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate().format(fmt);
        String endDate = ((Date) endDateSpinner.getValue()).toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate().format(fmt);
        String reason = jTextField3.getText();

        if (reason == null || reason.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String csvFile = ResourcePathService.resourceFile("LeaveRequests.csv");

        LeaveService leaveService = new LeaveService();
        if (leaveService.submitLeaveRequest(csvFile, employeeNumber, leaveType, startDate, endDate, reason)) {
            submitted = true;
            JOptionPane.showMessageDialog(this, 
                "Leave request submitted successfully!\n\n" +
                "Type: " + leaveType + "\n" +
                "Start: " + startDate + "\n" +
                "End: " + endDate + "\n" +
                "Status: Pending approval", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Error saving leave request to file", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancel() {
        dispose();
    }

    public boolean isSubmitted() {
        return submitted;
    }

    // --- VARIABLE DECLARATIONS ---
    private JLabel jLabel1, jLabel2, jLabel3, jLabel4;
    private JComboBox<String> jComboBox1;
    private JTextField jTextField3;
    private JSpinner startDateSpinner, endDateSpinner;
    private JButton jButton1, jButton2;
}
