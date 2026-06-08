package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CredentialRecord;

public class CredentialJDBCDAO implements CredentialDAO {

    @Override
    public List<CredentialRecord> loadAllCredentials(String filePath) throws Exception {
        List<CredentialRecord> credentials = new ArrayList<>();

        String sql = "SELECT * FROM credentials";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                CredentialRecord record = new CredentialRecord(
                    rs.getString("employee_number"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );

                credentials.add(record);
            }
        }

        return credentials;
    }

    @Override
    public void saveAllCredentials(String filePath, List<CredentialRecord> credentials) throws Exception {
        String deleteSql = "DELETE FROM credentials";
        String insertSql = "INSERT INTO credentials (employee_number, username, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.prepareStatement(deleteSql).executeUpdate();

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                for (CredentialRecord record : credentials) {
                   stmt.setString(1, record.getEmployeeNumber());
                   stmt.setString(2, record.getUserName());
                   stmt.setString(3, record.getPassword());
                   stmt.setString(4, record.getRole());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }
        }
    }
}