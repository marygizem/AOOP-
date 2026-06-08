package service;

import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

import dao.LeaveCSVDAO;
import dao.LeaveDAO;

/**
 * Application service for leave request retrieval and updates.
 */
public class LeaveService {
    private final EmployeeService employeeService;
    private final LeaveDAO leaveDAO;

    public LeaveService() {
        this.employeeService = new EmployeeService();
        this.leaveDAO = new LeaveCSVDAO();
    }

    public boolean submitLeaveRequest(String leaveCsvFile, String employeeNumber, String leaveType,
                                      String startDate, String endDate, String reason) {
        if (!isValidLeaveInput(employeeNumber, leaveType, startDate, endDate, reason)) {
            return false;
        }

        try {
            return leaveDAO.appendLeaveRequest(leaveCsvFile, employeeNumber.trim(), leaveType.trim(),
                    startDate.trim(), endDate.trim(), reason.trim());
        } catch (Exception e) {
            return false;
        }
    }

    public List<String[]> getPendingLeaveRows(String leaveCsvFile, String employeeCsvFile) throws Exception {
        return getLeaveRowsByStatus(leaveCsvFile, employeeCsvFile, true);
    }

    public List<String[]> getLeaveHistoryRows(String leaveCsvFile, String employeeCsvFile) throws Exception {
        return getLeaveRowsByStatus(leaveCsvFile, employeeCsvFile, false);
    }

    public List<String[]> getEmployeeLeaveHistoryRows(String leaveCsvFile, String employeeNumber) throws Exception {
        List<String[]> leaveData = employeeService.getEmployees(leaveCsvFile);
        List<String[]> rows = new ArrayList<>();

        for (int i = 1; i < leaveData.size(); i++) {
            String[] row = leaveData.get(i);
            if (row.length >= 5 && row[0].trim().equals(employeeNumber.trim())) {
                rows.add(new String[]{row[1], row[2], row[3], row[4]});
            }
        }

        return rows;
    }

    public boolean updateLeaveStatus(String leaveCsvFile, String empNum, String leaveType,
                                     String startDate, String endDate, String newStatus) {
        try {
            List<String[]> leaveData = employeeService.getEmployees(leaveCsvFile);
            boolean found = false;

            for (int i = 1; i < leaveData.size(); i++) {
                String[] row = leaveData.get(i);
                if (row.length >= 5
                        && row[0].trim().equals(empNum)
                        && row[1].trim().equals(leaveType)
                        && row[2].trim().equals(startDate)
                        && row[3].trim().equals(endDate)) {
                    row[4] = newStatus;
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }

            try (CSVWriter writer = new CSVWriter(new FileWriter(leaveCsvFile, false))) {
                writer.writeAll(leaveData);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String[]> getLeaveRowsByStatus(String leaveCsvFile, String employeeCsvFile, boolean pendingOnly) throws Exception {
        List<String[]> leaveData = employeeService.getEmployees(leaveCsvFile);
        Map<String, String> employeeNames = loadEmployeeNames(employeeCsvFile);
        List<String[]> rows = new ArrayList<>();

        for (int i = 1; i < leaveData.size(); i++) {
            String[] row = leaveData.get(i);
            if (row.length < 5 || row[0].trim().isEmpty()) {
                continue;
            }

            String status = row[4].trim();
            if (pendingOnly && !"Pending".equalsIgnoreCase(status)) {
                continue;
            }

            rows.add(new String[]{
                    row[0],
                    employeeNames.getOrDefault(row[0].trim(), "Unknown"),
                    row[1],
                    row[2],
                    row[3],
                    row[4]
            });
        }

        return rows;
    }

    private Map<String, String> loadEmployeeNames(String employeeCsvFile) throws Exception {
        List<String[]> employeeData = employeeService.getEmployees(employeeCsvFile);
        Map<String, String> names = new HashMap<>();

        for (int i = 1; i < employeeData.size(); i++) {
            String[] row = employeeData.get(i);
            if (row.length >= 3) {
                names.put(row[0].trim(), row[1] + ", " + row[2]);
            }
        }

        return names;
    }

    private boolean isValidLeaveInput(String employeeNumber, String leaveType, String startDate,
                                      String endDate, String reason) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            return false;
        }
        if (leaveType == null || leaveType.trim().isEmpty()) {
            return false;
        }
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate start = LocalDate.parse(startDate.trim());
            LocalDate end = LocalDate.parse(endDate.trim());
            if (end.isBefore(start)) {
                return false;
            }

            // Policy: sick leave can only be filed for today or past dates.
            if ("sick leave".equalsIgnoreCase(leaveType.trim())) {
                LocalDate today = LocalDate.now();
                if (start.isAfter(today) || end.isAfter(today)) {
                    return false;
                }
            }

            return true;
        } catch (DateTimeParseException | NullPointerException e) {
            return false;
        }
    }
}