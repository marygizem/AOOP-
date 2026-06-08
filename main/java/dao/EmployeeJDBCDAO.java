package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Employee;

public class EmployeeJDBCDAO implements EmployeeDAO {

    @Override
    public List<String[]> loadAll(String filePath) throws Exception {

        List<String[]> employees = new ArrayList<>();

        String sql = "SELECT * FROM employeedata";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            while (rs.next()) {

                String[] row = {
                    String.valueOf(rs.getInt("ID")),
                    rs.getString("lastname"),
                    rs.getString("firstname"),
                    rs.getString("Birthday"),
                    rs.getString("Address"),
                    rs.getString("phonenumber"),
                    String.valueOf(rs.getInt("rate")),
                    rs.getString("Position")
                };

                employees.add(row);
            }
        }

        return employees;
    }

    @Override
    public List<Employee> loadAllEmployees(String filePath) throws Exception {

        return new ArrayList<>();
    }
}