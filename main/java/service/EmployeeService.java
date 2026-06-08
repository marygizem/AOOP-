
package service;

import java.util.List;

import dao.EmployeeJDBCDAO;
import dao.EmployeeDAO;
import model.Employee;

public class EmployeeService {
    private final EmployeeDAO employeeDAO;
    
    public EmployeeService() {
        this.employeeDAO = new EmployeeJDBCDAO();
    }

    public List<String[]> getEmployees(String filePath) throws Exception {
        return employeeDAO.loadAll(filePath);
    }
  
    public List<Employee> loadAllEmployees(String filePath) throws Exception {
        return employeeDAO.loadAllEmployees(filePath);
    }

    public Employee findEmployeeByNumber(String filePath, String employeeNumber) throws Exception {
        List<Employee> employees = employeeDAO.loadAllEmployees(filePath);
        for (Employee employee : employees) {
            if (employee != null
                    && employee.getEmployeeNumber() != null
                    && employee.getEmployeeNumber().trim().equals(employeeNumber.trim())) {
                return employee;
            }
        }
        return null;
    }

    public String findFullNameByEmployeeNumber(String filePath, String empNumber) throws Exception {
        List<String[]> data = employeeDAO.loadAll(filePath);

        for (int i = 1; i < data.size(); i++) { 
            String[] row = data.get(i);
            if (row[0].trim().equals(empNumber.trim())) {
                String lastName = row[1];
                String firstName = row[2];
                return firstName + " " + lastName;
            }
        }

        return null; 
    }
}