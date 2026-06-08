package dao;

public interface LeaveDAO {
    boolean appendLeaveRequest(String leaveCsvFile, String employeeNumber, String leaveType,
                               String startDate, String endDate, String reason) throws Exception;
}