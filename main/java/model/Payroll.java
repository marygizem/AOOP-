package model;

public class Payroll {
	private String employeeNumber;
	private String employeeName;
	private double basicSalary;
	private double allowances;
	private double deductions;
	private double netPay;
	private double totalHoursWorked;

	public Payroll() {
	}

	public Payroll(String employeeNumber, String employeeName, double basicSalary,
				   double allowances, double deductions, double netPay) {
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

	public double getTotalHoursWorked() {
		return totalHoursWorked;
	}

	public void setTotalHoursWorked(double totalHoursWorked) {
		if (totalHoursWorked < 0) {
			throw new IllegalArgumentException("Hours worked must be >= 0");
		}
		this.totalHoursWorked = totalHoursWorked;
	}
}
