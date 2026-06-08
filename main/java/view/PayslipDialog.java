package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

public class PayslipDialog extends JDialog {

    public PayslipDialog(JFrame parent, String payslipHtml) {
        super(parent, "Payslip", true);
        initComponents(payslipHtml);
    }

    private void initComponents(String payslipHtml) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(Color.WHITE);

        JEditorPane payslipPane = new JEditorPane("text/html", payslipHtml);
        payslipPane.setEditable(false);
        payslipPane.setBackground(Color.WHITE);
        payslipPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(payslipPane);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(560, 680));
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = createButton("Close", new Color(0, 102, 102), Color.WHITE);
        closeBtn.addActionListener(evt -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(0, 10, 8, 10));
        btnPanel.add(closeBtn);
        contentPanel.add(btnPanel, BorderLayout.SOUTH);

        add(contentPanel);
        pack();
        setMinimumSize(getSize());
        setResizable(false);
        setLocationRelativeTo(getParent());
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setBorder(new EmptyBorder(10, 28, 10, 28));
        return btn;
    }

    // ─── HTML Generator ────────────────────────────────────────────────────────
    public static String generateHtml(
            String payslipNo,     String periodStart,    String periodEnd,
            String employeeId,    String employeeName,   String positionDept,
            double monthlySalary, double dailyRate,      int daysWorked,    double overtime,
            double riceSubsidy,   double phoneAllowance, double clothingAllowance,
            double sss,           double philhealth,
            double pagibig,       double withholdingTax) {

        // Load logo from project root 
        File logoFile = new File("images/motorpic.png");
        String logoTag = logoFile.exists()
                ? "<img src='" + logoFile.toURI().toString() + "' width='72' height='72'/>"
                : "";

        double grossIncome     = dailyRate * daysWorked + overtime;
        double totalBenefits   = riceSubsidy + phoneAllowance + clothingAllowance;
        double totalDeductions = sss + philhealth + pagibig + withholdingTax;
        double takeHomePay     = grossIncome + totalBenefits - totalDeductions;

        String css =
            "body{font-family:Arial,sans-serif;font-size:11px;color:#111;margin:14px;}" +
            "table{width:100%;border-collapse:collapse;}" +

            // Company header
            ".logo{width:80px;vertical-align:middle;}" +
            ".co-info{padding-left:12px;vertical-align:middle;}" +
            ".co-name{font-size:24px;font-weight:bold;font-family:'Arial Black',Arial;}" +
            ".co-detail{font-size:9px;color:#444;line-height:1.6;margin-top:2px;}" +

            // Page title
            ".page-title{text-align:center;font-size:13px;font-weight:bold;" +
            "text-decoration:underline;letter-spacing:2px;margin:10px 0;}" +

            // Employee info grid
            ".emp-grid td{border:1px solid #aaa;padding:5px 8px;font-size:10px;}" +
            ".emp-lbl{font-weight:bold;background:#f5f5f5;width:18%;}" +
            ".emp-val{width:25%;}" +

            // Section headers
            ".sec{background:#1c1c1c;color:#fff;padding:5px 8px;" +
            "font-size:11px;font-weight:bold;letter-spacing:1px;margin-top:8px;}" +

            // Line item rows
            ".row td{padding:4px 8px;border-bottom:1px solid #ececec;}" +

            // Sub-total rows
            ".sub td{padding:5px 8px;font-weight:bold;background:#d6d6d6;" +
            "border-top:1px solid #999;}" +

            // Summary rows
            ".sum td{padding:4px 8px;border-bottom:1px solid #ddd;}" +

            // Take home pay row
            ".takehome td{padding:6px 8px;font-weight:bold;font-size:12px;" +
            "background:#b0b0b0;border-top:2px solid #888;}" +

            ".amt{text-align:right;}";

        return "<html><head><style>" + css + "</style></head><body>" +

            // ── Company Header ───────────────────────────────────────────
            "<table style='margin-bottom:4px;'><tr>" +
            "<td class='logo'>" + logoTag + "</td>" +
            "<td class='co-info'>" +
            "<div class='co-name'>MotorPH</div>" +
            "<div class='co-detail'>" +
            "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City<br/>" +
            "Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073<br/>" +
            "Email: corporate@motorph.com" +
            "</div></td></tr></table>" +

            // ── Page Title ──────────────────────────────────────────────
            "<p class='page-title'>EMPLOYEE PAYSLIP</p>" +

            // ── Employee Info Grid ───────────────────────────────────────
            "<table class='emp-grid'>" +
            "<tr>" +
            "<td class='emp-lbl'>PAYSLIP NO</td>" +
            "<td class='emp-val'>" + payslipNo + "</td>" +
            "<td class='emp-lbl'>PERIOD START DATE</td>" +
            "<td class='emp-val'>" + periodStart + "</td>" +
            "</tr><tr>" +
            "<td class='emp-lbl'>EMPLOYEE ID</td>" +
            "<td class='emp-val'>" + employeeId + "</td>" +
            "<td class='emp-lbl'>PERIOD END DATE</td>" +
            "<td class='emp-val'>" + periodEnd + "</td>" +
            "</tr><tr>" +
            "<td class='emp-lbl'>EMPLOYEE NAME</td>" +
            "<td class='emp-val'>" + employeeName + "</td>" +
            "<td class='emp-lbl'>EMPLOYEE POSITION/DEPARTMENT</td>" +
            "<td class='emp-val'>" + positionDept + "</td>" +
            "</tr></table>" +

            // ── Earnings ─────────────────────────────────────────────────
            "<div class='sec'>EARNINGS</div>" +
            "<table>" +
            row("Monthly Salary", fmt(monthlySalary),        "row") +
            row("Daily Rate",     fmt(dailyRate),             "row") +
            row("Days Worked",    String.valueOf(daysWorked), "row") +
            row("Overtime",       fmt(overtime),              "row") +
            row("GROSS INCOME",   fmt(grossIncome),           "sub") +
            "</table>" +

            // ── Benefits ─────────────────────────────────────────────────
            "<div class='sec'>BENEFITS</div>" +
            "<table>" +
            row("Rice Subsidy",       fmt(riceSubsidy),       "row") +
            row("Phone Allowance",    fmt(phoneAllowance),    "row") +
            row("Clothing Allowance", fmt(clothingAllowance), "row") +
            row("TOTAL",              fmt(totalBenefits),     "sub") +
            "</table>" +

            // ── Deductions ───────────────────────────────────────────────
            "<div class='sec'>DEDUCTIONS</div>" +
            "<table>" +
            row("Social Security System", fmt(sss),            "row") +
            row("Philhealth",             fmt(philhealth),     "row") +
            row("Pag-ibig",               fmt(pagibig),        "row") +
            row("Withholding Tax",        fmt(withholdingTax), "row") +
            row("TOTAL DEDUCTIONS",       fmt(totalDeductions),"sub") +
            "</table>" +

            // ── Summary ──────────────────────────────────────────────────
            "<div class='sec'>SUMMARY</div>" +
            "<table>" +
            row("Gross Income", fmt(grossIncome),     "sum") +
            row("Benefits",     fmt(totalBenefits),   "sum") +
            row("Deductions",   fmt(totalDeductions), "sum") +
            row("TAKE HOME PAY",fmt(takeHomePay),     "takehome") +
            "</table>" +

            "</body></html>";
    }

    private static String fmt(double v) {
        return String.format("&#8369;%,.2f", v);
    }

    private static String row(String label, String value, String cssClass) {
        return "<tr class='" + cssClass + "'>" +
               "<td>" + label + "</td>" +
               "<td class='amt'>" + value + "</td>" +
               "</tr>";
    }
}
