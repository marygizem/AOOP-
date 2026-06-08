package service;

import java.util.List;

import model.Payroll;
import model.PayslipReport;

public class ReportService {

    private final PayrollService payrollService;

    public ReportService() {
        this.payrollService = new PayrollService();
    }

    public PayslipReport generatePayslip(String employeeNumber, String employeeFilePath) throws Exception {
        List<Payroll> payrollRecords = payrollService.getPayrollRecords(employeeFilePath);

        for (Payroll payroll : payrollRecords) {
            if (payroll.getEmployeeNumber().equals(employeeNumber)) {
                return new PayslipReport(
                        payroll.getEmployeeNumber(),
                        payroll.getEmployeeName(),
                        payroll.getBasicSalary(),
                        payroll.getAllowances(),
                        payroll.getDeductions(),
                        payroll.getNetPay()
                );
            }
        }

        return null;
    }

    public double generatePayrollSummary(String employeeFilePath) throws Exception {
        List<Payroll> payrollRecords = payrollService.getPayrollRecords(employeeFilePath);
        return payrollService.getTotalPayroll(payrollRecords);
    }
}