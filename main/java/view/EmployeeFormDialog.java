package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class EmployeeFormDialog extends JDialog {

    @FunctionalInterface
    public interface SaveValidator {
        String validate(FormResult candidate);
    }

    private static final Color BRAND_TEAL  = new Color(0, 102, 102);
    private static final Color TEXT_DARK   = new Color(33, 37, 41);
    private static final Color BORDER_GRAY = new Color(206, 212, 218);

    private JTextField empNumberField;
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField sssField;
    private JTextField philhealthField;
    private JTextField tinField;
    private JTextField pagibigField;
    private JTextField emailField;
    private JTextField basicSalaryField;
    private JTextField riceAllowanceField;
    private JTextField phoneAllowanceField;
    private JTextField clothingAllowanceField;

    private FormResult result = null;
    private final boolean isUpdate;
    private final SaveValidator saveValidator;

    public EmployeeFormDialog(JFrame parent, String title, FormResult prefill) {
        this(parent, title, prefill, null);
    }

    public EmployeeFormDialog(JFrame parent, String title, FormResult prefill, SaveValidator saveValidator) {
        super(parent, title, true);
        this.isUpdate = prefill != null;
        this.saveValidator = saveValidator;
        initComponents(prefill);
    }

    private void initComponents(FormResult prefill) {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 30, 10, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 8, 7, 8);

        empNumberField   = addRow(content, gbc, "Employee Number:", 0, prefill != null ? prefill.getEmpNumber()   : "");
        lastNameField    = addRow(content, gbc, "Last Name:",        1, prefill != null ? prefill.getLastName()    : "");
        firstNameField   = addRow(content, gbc, "First Name:",       2, prefill != null ? prefill.getFirstName()   : "");
        sssField         = addRow(content, gbc, "SSS Number:",       3, prefill != null ? prefill.getSss()         : "");
        philhealthField  = addRow(content, gbc, "Philhealth #:",     4, prefill != null ? prefill.getPhilhealth()  : "");
        tinField         = addRow(content, gbc, "TIN #:",            5, prefill != null ? prefill.getTin()         : "");
        pagibigField     = addRow(content, gbc, "Pag-ibig #:",       6, prefill != null ? prefill.getPagibig()     : "");
        emailField       = addRow(content, gbc, "Email Address:",    7, prefill != null ? prefill.getEmail()       : "");
        basicSalaryField = addRow(content, gbc, "Basic Salary:",     8, prefill != null ? prefill.getBasicSalary() : "");
        riceAllowanceField = addRow(content, gbc, "Rice Allowance:", 9, prefill != null ? prefill.getRiceAllowance() : "");
        phoneAllowanceField = addRow(content, gbc, "Phone Allowance:", 10, prefill != null ? prefill.getPhoneAllowance() : "");
        clothingAllowanceField = addRow(content, gbc, "Clothing Allowance:", 11,
            prefill != null ? prefill.getClothingAllowance() : "");

        // Employee number is read-only on update
        if (isUpdate) {
            empNumberField.setEditable(false);
            empNumberField.setBackground(new Color(233, 236, 239));
        }

        add(content, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(getParent());
    }

    private JTextField addRow(JPanel panel, GridBagConstraints gbc,
                               String labelText, int row, String value) {
        gbc.gridy = row;

        gbc.gridx = 0; gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_DARK);
        panel.add(label, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        JTextField field = new JTextField(value, 18);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GRAY, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(field, gbc);

        return field;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setForeground(TEXT_DARK);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GRAY, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        cancelBtn.addActionListener(evt -> dispose());

        JButton saveBtn = new JButton(isUpdate ? "Save Changes" : "Add Employee");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBackground(BRAND_TEAL);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        saveBtn.addActionListener(evt -> handleSave());

        getRootPane().setDefaultButton(saveBtn);

        panel.add(cancelBtn);
        panel.add(saveBtn);

        return panel;
    }

    private void handleSave() {
        String empNum   = empNumberField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String basicSalary = basicSalaryField.getText().trim();
        String riceAllowance = riceAllowanceField.getText().trim();
        String phoneAllowance = phoneAllowanceField.getText().trim();
        String clothingAllowance = clothingAllowanceField.getText().trim();
        String sss = sssField.getText().trim();
        String philhealth = philhealthField.getText().trim();
        String tin = tinField.getText().trim();
        String pagibig = pagibigField.getText().trim();
        String email = emailField.getText().trim();

        if (empNum.isEmpty() || lastName.isEmpty() || firstName.isEmpty()
                || basicSalary.isEmpty() || riceAllowance.isEmpty() || phoneAllowance.isEmpty()
                || clothingAllowance.isEmpty() || sss.isEmpty() || philhealth.isEmpty()
                || tin.isEmpty() || pagibig.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "All fields are required.",
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FormResult candidate = new FormResult(
            empNum, lastName, firstName,
            sss,
            philhealth,
            tin,
            pagibig,
            email,
            basicSalary,
            riceAllowance,
            phoneAllowance,
            clothingAllowance
        );

        if (saveValidator != null) {
            String validationError = saveValidator.validate(candidate);
            if (validationError != null && !validationError.isBlank()) {
                JOptionPane.showMessageDialog(this,
                    validationError,
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        result = candidate;
        dispose();
    }

    public FormResult getResult() {
        return result;
    }

    // Immutable data container for form values
    public static class FormResult {
        private final String empNumber;
        private final String lastName;
        private final String firstName;
        private final String sss;
        private final String philhealth;
        private final String tin;
        private final String pagibig;
        private final String email;
        private final String basicSalary;
        private final String riceAllowance;
        private final String phoneAllowance;
        private final String clothingAllowance;

        public FormResult(String empNumber, String lastName, String firstName,
                          String sss, String philhealth, String tin, String pagibig,
                          String email, String basicSalary, String riceAllowance,
                          String phoneAllowance, String clothingAllowance) {
            this.empNumber  = empNumber;
            this.lastName   = lastName;
            this.firstName  = firstName;
            this.sss        = sss;
            this.philhealth = philhealth;
            this.tin        = tin;
            this.pagibig    = pagibig;
            this.email      = email;
            this.basicSalary = basicSalary;
            this.riceAllowance = riceAllowance;
            this.phoneAllowance = phoneAllowance;
            this.clothingAllowance = clothingAllowance;
        }

        public String getEmpNumber() {
            return empNumber;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getSss() {
            return sss;
        }

        public String getPhilhealth() {
            return philhealth;
        }

        public String getTin() {
            return tin;
        }

        public String getPagibig() {
            return pagibig;
        }

        public String getEmail() {
            return email;
        }

        public String getBasicSalary() {
            return basicSalary;
        }

        public String getRiceAllowance() {
            return riceAllowance;
        }

        public String getPhoneAllowance() {
            return phoneAllowance;
        }

        public String getClothingAllowance() {
            return clothingAllowance;
        }
    }
}
