

package dao;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

import model.AdminEmployee;
import model.Allowance;
import model.Deduction;
import model.Employee;
import model.GovernmentDetails;
import model.ManagerEmployee;
import model.PayrollOfficerEmployee;
import model.RegularEmployee;
import service.StatutoryDeductionService;

public class EmployeeCSVDAO implements EmployeeDAO {
    private final StatutoryDeductionService deductionService = new StatutoryDeductionService();

    @Override
    public List<String[]> loadAll(String filePath) throws Exception {
        List<String[]> data;
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            data = reader.readAll();
        }
        return data;
    }
    
    @Override
    public List<Employee> loadAllEmployees(String filePath) throws Exception {
        List<Employee> employees = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();
            
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                try {
                    Employee emp = parseEmployeeRow(row);
                    if (emp != null) {
                        employees.add(emp);
                    }
                } catch (Exception e) {
                    // Skip invalid rows
                }
            }
        }
        
        return employees;
    }
    
    private Employee parseEmployeeRow(String[] row) throws Exception {
        if (row.length < 17) {
            return null;
        }
        
        int empID = Integer.parseInt(row[0].trim());
        String lastName = row[1].trim();
        String firstName = row[2].trim();
        String sssNumber = row[6].trim();
        String philhealthNumber = row[7].trim();
        String tinNumber = row[8].trim();
        String pagibigNumber = row[9].trim();
        String position = row.length > 11 ? row[11].trim() : "";
        
        double basicSalary = parseDouble(row[13]);
        double riceSubsidy = parseDouble(row[14]);
        double phoneAllowance = parseDouble(row[15]);
        double clothingAllowance = parseDouble(row[16]);
        
        // Create GovernmentDetails 
        GovernmentDetails govDetails = new GovernmentDetails(
            sssNumber.isEmpty() ? "00-0000000-0" : sssNumber,
            philhealthNumber.isEmpty() ? "00-000000000-0" : philhealthNumber,
            tinNumber.isEmpty() ? "000-000-000" : tinNumber,
            pagibigNumber.isEmpty() ? "0000-0000-0000" : pagibigNumber
        );
        
        // Create Allowance
        Allowance allowance = new Allowance(riceSubsidy, phoneAllowance, clothingAllowance);
        
        // Compute statutory monthly deductions from the uploaded contribution tables.
        Deduction deduction = deductionService.computeMonthlyDeduction(basicSalary);
        
        Employee employee = createEmployeeByPosition(
            position,
            basicSalary,
            empID,
            lastName,
            firstName,
            govDetails,
            allowance,
            deduction
        );
        
        // Set position if available
        if (!position.isEmpty()) {
            employee.setPosition(position);
        }
        
        return employee;
    }

    private Employee createEmployeeByPosition(String position, double basicSalary, int empID,
                                              String lastName, String firstName,
                                              GovernmentDetails govDetails, Allowance allowance,
                                              Deduction deduction) {
        String normalizedPosition = position == null ? "" : position.trim().toLowerCase();

        if (normalizedPosition.contains("manager")) {
            return new ManagerEmployee(
                basicSalary, empID, lastName, firstName,
                null, "", "", "", govDetails, allowance, deduction
            );
        }

        if (normalizedPosition.contains("payroll")) {
            return new PayrollOfficerEmployee(
                basicSalary, empID, lastName, firstName,
                null, "", "", "", govDetails, allowance, deduction
            );
        }

        if (normalizedPosition.contains("admin") || normalizedPosition.contains("hr")) {
            return new AdminEmployee(
                basicSalary, empID, lastName, firstName,
                null, "", "", "", govDetails, allowance, deduction
            );
        }

        return new RegularEmployee(
            basicSalary, empID, lastName, firstName,
            null, "", "", "", govDetails, allowance, deduction
        );
    }
    
    private double parseDouble(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }
            
            String cleaned = value.trim().replaceAll("[^0-9.]", "");
            
            if (cleaned.isEmpty()) {
                return 0.0;
            }
            
            return Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }
}