package dao;

import java.util.List;

import model.CredentialRecord;

public interface CredentialDAO {
    List<CredentialRecord> loadAllCredentials(String filePath) throws Exception;

    void saveAllCredentials(String filePath, List<CredentialRecord> credentials) throws Exception;
}
