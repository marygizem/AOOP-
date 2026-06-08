
package dao;

import java.util.List;

import model.Employee;

public interface EmployeeDAO {
  List<String[]> loadAll(String filePath) throws Exception;
  List<Employee> loadAllEmployees(String filePath) throws Exception;
}
