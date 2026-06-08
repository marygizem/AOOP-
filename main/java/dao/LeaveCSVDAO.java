package dao;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import com.opencsv.CSVWriter;

public class LeaveCSVDAO implements LeaveDAO {

    @Override
    public boolean appendLeaveRequest(String leaveCsvFile, String employeeNumber, String leaveType,
                                      String startDate, String endDate, String reason) throws Exception {
        File file = new File(leaveCsvFile);
        boolean needsHeader = true;

        if (file.exists()) {
            EmployeeDAO employeeDAO = new EmployeeCSVDAO();
            List<String[]> rows = employeeDAO.loadAll(leaveCsvFile);
            needsHeader = rows == null || rows.isEmpty();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(leaveCsvFile, true))) {
            if (needsHeader) {
                writer.writeNext(new String[]{"Employee #", "Leave Type", "Start Date", "End Date", "Status", "Reason"});
            }

            writer.writeNext(new String[]{
                    employeeNumber,
                    leaveType,
                    startDate,
                    endDate,
                    "Pending",
                    reason
            });
        }

        return true;
    }
}