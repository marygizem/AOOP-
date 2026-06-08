package model;

public class PayslipReport {
    private final String employeeNumber;
    private final String employeeName;
    private final double basicSalary;
    private final double allowances;
    private final double deductions;
    private final double netPay;

    public PayslipReport(String employeeNumber,
                         String employeeName,
                         double basicSalary,
                         double allowances,
                         double deductions,
                         double netPay) {
        this.employeeNumber = employeeNumber;
        this.employeeName = employeeName;
        this.basicSalary = basicSalary;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netPay = netPay;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getAllowances() {
        return allowances;
    }

    public double getDeductions() {
        return deductions;
    }

    public double getNetPay() {
        return netPay;
    }
}