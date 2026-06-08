package dao;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import model.CredentialRecord;

public class CredentialCSVDAO implements CredentialDAO {

    @Override
    public List<CredentialRecord> loadAllCredentials(String filePath) throws Exception {
        List<CredentialRecord> credentials = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 4) {
                    continue;
                }

                credentials.add(new CredentialRecord(
                        safeTrim(row[0]),
                        safeTrim(row[1]),
                        safeTrim(row[2]),
                        safeTrim(row[3]),
                        row.length > 4 ? safeTrim(row[4]) : "",
                        row.length > 5 ? safeTrim(row[5]) : "ACTIVE"
                ));
            }
        }

        return credentials;
    }

    @Override
    public void saveAllCredentials(String filePath, List<CredentialRecord> credentials) throws Exception {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, false))) {
            writer.writeNext(new String[]{"Employee #", "EmailAddress", "UserName", "PasswordHash", "Role", "Status"});
            for (CredentialRecord record : credentials) {
                writer.writeNext(new String[]{
                    safeTrim(record.getEmployeeNumber()),
                    safeTrim(record.getEmailAddress()),
                    safeTrim(record.getUserName()),
                    safeTrim(record.getPassword()),
                    safeTrim(record.getRole()),
                    safeTrim(record.getStatus())
                });
            }
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
