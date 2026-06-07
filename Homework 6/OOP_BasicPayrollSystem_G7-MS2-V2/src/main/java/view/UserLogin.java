package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import service.AuthService;
import service.ResourcePathService;

public class UserLogin extends JFrame {

    // --- Left panel (logo) ---
    private JPanel logoPanel;
    private JLabel logoLabel;

    // --- Right panel (form) ---
    private JPanel formPanel;
    private JLabel titleLabel;
    private JLabel userLabel;
    private JLabel passwordLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JLabel registerLink;
    private JLabel forgotPasswordLink;

    private static final Color BRAND_TEAL   = new Color(0, 102, 102);
    private static final Color BRAND_DARK   = new Color(0, 51, 51);
    private static final Color TEXT_DARK    = new Color(33, 37, 41);
    private static final Color BORDER_GRAY  = new Color(206, 212, 218);
    private static final Color BG_FIELD     = new Color(255, 255, 255);

    public UserLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("LOGIN");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        add(buildLogoPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(loginBtn);
        pack();
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ //
    //  LEFT — Logo Panel
    // ------------------------------------------------------------------ //
    private JPanel buildLogoPanel() {
        logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BRAND_TEAL);
        logoPanel.setPreferredSize(new Dimension(480, 500));

        logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        loadLogoImage();
        logoPanel.add(logoLabel, BorderLayout.CENTER);

        return logoPanel;
    }

    // ------------------------------------------------------------------ //
    //  RIGHT — Form Panel
    // ------------------------------------------------------------------ //
    private JPanel buildFormPanel() {
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setPreferredSize(new Dimension(400, 500));
        formPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Title
        titleLabel = new JLabel("LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(BRAND_DARK);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(titleLabel, gbc);

        // User label + info icon with tooltip
        userLabel = createFieldLabel("User:");
        JLabel infoIcon = new JLabel("ⓘ", JLabel.CENTER);
        infoIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 14));
        infoIcon.setForeground(BRAND_TEAL);
        infoIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        infoIcon.setPreferredSize(new Dimension(16, 16));
        infoIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                infoIcon.setForeground(new Color(0, 82, 82));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                infoIcon.setForeground(BRAND_TEAL);
            }
        });
        infoIcon.setToolTipText(
            "<html><div style='width: 220px; padding: 4px 2px;'>"
            + "<b>Login Guide</b><br>"
            + "If Employee: input your Employee # as username.<br><br>"
            + "If Core role: input role as your username "
            + "(<b>admin</b>, <b>hr</b>, <b>finance</b>, <b>it</b>)."
            + "</div></html>");

        JPanel userHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        userHeaderPanel.setBackground(Color.WHITE);
        userHeaderPanel.add(userLabel);
        userHeaderPanel.add(infoIcon);

        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 2, 0);
        formPanel.add(userHeaderPanel, gbc);

        // Username field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setBackground(BG_FIELD);
        usernameField.setForeground(TEXT_DARK);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        usernameField.setPreferredSize(new Dimension(0, 44));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(usernameField, gbc);

        // Password label
        passwordLabel = createFieldLabel("Password:");
        gbc.gridy = 3;
        gbc.insets = new Insets(6, 0, 2, 0);
        formPanel.add(passwordLabel, gbc);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setBackground(BG_FIELD);
        passwordField.setForeground(TEXT_DARK);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_GRAY, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        passwordField.setPreferredSize(new Dimension(0, 44));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 24, 0);
        formPanel.add(passwordField, gbc);

        // Login button
        loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginBtn.setBackground(BRAND_TEAL);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        loginBtn.setPreferredSize(new Dimension(0, 49));
        loginBtn.addActionListener(evt -> handleLogin());
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 30, 0);
        formPanel.add(loginBtn, gbc);

        // Register & Forgot Password links
        JPanel linksPanel = new JPanel(new BorderLayout());
        linksPanel.setBackground(Color.WHITE);

        registerLink = createLink("Register");
        registerLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Registration reg = new Registration();
                reg.setVisible(true);
                dispose();
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerLink.setFont(registerLink.getFont().deriveFont(Font.BOLD));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerLink.setFont(registerLink.getFont().deriveFont(Font.PLAIN));
            }
        });

        forgotPasswordLink = createLink("Forgot Password");
        forgotPasswordLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                forgotPasswordLink.setFont(forgotPasswordLink.getFont().deriveFont(Font.BOLD));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                forgotPasswordLink.setFont(forgotPasswordLink.getFont().deriveFont(Font.PLAIN));
            }
        });

        linksPanel.add(registerLink, BorderLayout.WEST);
        linksPanel.add(forgotPasswordLink, BorderLayout.EAST);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(linksPanel, gbc);

        return formPanel;
    }

    // ------------------------------------------------------------------ //
    //  UI Helpers
    // ------------------------------------------------------------------ //
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private JLabel createLink(String text) {
        JLabel link = new JLabel(text);
        link.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        link.setForeground(BRAND_TEAL);
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return link;
    }

    private void loadLogoImage() {
        try {
            String logoPath = ResourcePathService.imageFile("motorph_logo.png");
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(logoPath);
                Image scaled = icon.getImage().getScaledInstance(480, 500, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
                return;
            }
        } catch (Exception e) {
            // Fall through to text fallback
        }
        logoLabel.setText("MotorPH");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
    }

    // ------------------------------------------------------------------ //
    //  Login Logic
    // ------------------------------------------------------------------ //
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String credentialCsv = ResourcePathService.resourceFile("MotorPHcredentialLogin.csv");
        AuthService authService = new AuthService();

        try {
            AuthService.AuthResult result = authService.authenticate(username, password, credentialCsv);
            if (result != null) {
                openDashboard(result);
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Login service is unavailable: " + e.getMessage(),
                "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            "Invalid username or password. Please try again.",
            "Login Failed", JOptionPane.ERROR_MESSAGE);

        passwordField.setText("");
        passwordField.requestFocus();
    }

    private void openDashboard(AuthService.AuthResult result) {
        switch (result.getRole()) {
            case ADMIN:
                AdminDashboard adminUI = new AdminDashboard(
                    new AdminDashboard.CurrentUser("ADMIN"));
                adminUI.setVisible(true);
                dispose();
                break;
            case HR:
                openHR();
                break;
            case FINANCE:
                openFinance(result.getUsername());
                break;
            case IT:
                openIT(result.getUsername());
                break;
            case EMPLOYEE:
            default:
                String employeeCsv = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
                AuthService authService = new AuthService();
                if (authService.isExistingEmployeeNumber(result.getEmployeeNumber(), employeeCsv)) {
                    EmployeeDashboard empDash = new EmployeeDashboard(result.getEmployeeNumber());
                    empDash.setVisible(true);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Employee number not found in records.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                break;
        }
    }

    private void openHR() {
        try {
            AdminDashboard hrUI = new AdminDashboard(
                new AdminDashboard.CurrentUser("HR"));
            hrUI.setVisible(true);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading HR dashboard: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFinance(String username) {
        try {
            FinanceDashboard payrollDash = new FinanceDashboard(username);
            payrollDash.setVisible(true);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading finance dashboard: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openIT(String username) {
        try {
            ITDashboard itDash = new ITDashboard(username);
            itDash.setVisible(true);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error loading IT dashboard: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
