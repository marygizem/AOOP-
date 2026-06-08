package service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class AttendanceService {
    private static final String[] MOTORPH_HEADER = {
        "Employee #", "Last Name", "First Name", "Date", "Log In", "Log Out"
    };

    private static final DateTimeFormatter TIME_FMT_24 = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter TIME_FMT_24_SHORT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_FMT_12 = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_FMT_12_SHORT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FMT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FMT_MOTORPH = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    public static class AttendanceSummary {
        private final int totalAbsences;
        private final double averageAttendance;

        public AttendanceSummary(int totalAbsences, double averageAttendance) {
            this.totalAbsences = totalAbsences;
            this.averageAttendance = averageAttendance;
        }

        public int getTotalAbsences() {
            return totalAbsences;
        }

        public double getAverageAttendance() {
            return averageAttendance;
        }
    }

    public static class AttendanceLog {
        private final String date;
        private final String timeIn;
        private final String timeOut;

        public AttendanceLog(String date, String timeIn, String timeOut) {
            this.date = date;
            this.timeIn = timeIn;
            this.timeOut = timeOut;
        }

        public String getDate() {
            return date;
        }

        public String getTimeIn() {
            return timeIn;
        }

        public String getTimeOut() {
            return timeOut;
        }
    }

    public boolean timeIn(String attendanceCsv, String employeeNumber) {
        return upsertTodayLog(attendanceCsv, employeeNumber, true);
    }

    public boolean timeOut(String attendanceCsv, String employeeNumber) {
        return upsertTodayLog(attendanceCsv, employeeNumber, false);
    }

    public List<AttendanceLog> getEmployeeLogs(String attendanceCsv, String employeeNumber) {
        List<AttendanceLog> logs = new ArrayList<>();

        try {
            ensureAttendanceFile(attendanceCsv);
            try (CSVReader reader = new CSVReader(new FileReader(attendanceCsv))) {
                List<String[]> rows = reader.readAll();
                boolean motorPhFormat = isMotorPhFormat(rows);

                for (int i = 1; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (motorPhFormat) {
                        if (row.length >= 6 && row[0].trim().equals(employeeNumber.trim())) {
                            logs.add(new AttendanceLog(row[3], row[4], row[5]));
                        }
                    } else {
                        if (row.length >= 4 && row[0].trim().equals(employeeNumber.trim())) {
                            logs.add(new AttendanceLog(row[1], row[2], row[3]));
                        }
                    }
                }
            }
        } catch (Exception e) {
            return logs;
        }

        return logs;
    }

    public List<String[]> buildTeamAttendanceRows(String employeeCsvFile, String attendanceCsv) {
        List<String[]> rows = new ArrayList<>();
        Map<String, TeamAttendance> teamMap = new LinkedHashMap<>();

        try {
            try (CSVReader employeeReader = new CSVReader(new FileReader(employeeCsvFile))) {
                List<String[]> employeeRows = employeeReader.readAll();
                for (int i = 1; i < employeeRows.size(); i++) {
                    String[] row = employeeRows.get(i);
                    if (row.length < 3) {
                        continue;
                    }

                    String empNum = row[0].trim();
                    if (empNum.isEmpty()) {
                        continue;
                    }

                    teamMap.put(empNum, new TeamAttendance(empNum, row[1].trim(), row[2].trim()));
                }
            }

            ensureAttendanceFile(attendanceCsv);
            try (CSVReader logReader = new CSVReader(new FileReader(attendanceCsv))) {
                List<String[]> logRows = logReader.readAll();
                boolean motorPhFormat = isMotorPhFormat(logRows);

                for (int i = 1; i < logRows.size(); i++) {
                    String[] row = logRows.get(i);
                    if (motorPhFormat) {
                        if (row.length < 6) {
                            continue;
                        }

                        String empNum = row[0].trim();
                        TeamAttendance att = teamMap.get(empNum);
                        if (att == null) {
                            continue;
                        }

                        String date = row[3] == null ? "" : row[3].trim();
                        String timeIn = row[4] == null ? "" : row[4].trim();
                        String timeOut = row[5] == null ? "" : row[5].trim();
                        applyLog(att, date, timeIn, timeOut);
                    } else {
                        if (row.length < 4) {
                            continue;
                        }

                        String empNum = row[0].trim();
                        TeamAttendance att = teamMap.get(empNum);
                        if (att == null) {
                            continue;
                        }

                        String date = row[1] == null ? "" : row[1].trim();
                        String timeIn = row[2] == null ? "" : row[2].trim();
                        String timeOut = row[3] == null ? "" : row[3].trim();
                        applyLog(att, date, timeIn, timeOut);
                    }
                }
            }

            final int expectedWorkingDays = 22;
            for (TeamAttendance att : teamMap.values()) {
                int workedDays = att.workedDates.size();
                int absences = Math.max(0, expectedWorkingDays - workedDays);
                double attendancePercent = (workedDays * 100.0) / expectedWorkingDays;

                rows.add(new String[]{
                    att.employeeNumber,
                    att.lastName,
                    att.firstName,
                    String.format("%.1f", att.totalHours),
                    String.valueOf(absences),
                    String.format("%.1f%%", attendancePercent)
                });
            }
        } catch (Exception e) {
            return rows;
        }

        return rows;
    }

    public AttendanceSummary summarize(List<String[]> attendanceRows) {
        if (attendanceRows == null || attendanceRows.isEmpty()) {
            return new AttendanceSummary(0, 0.0);
        }

        int totalAbsences = 0;
        double sumAttendancePercent = 0.0;
        int validRows = 0;

        for (String[] row : attendanceRows) {
            if (row == null || row.length < 6) {
                continue;
            }

            try {
                // Team row format: [emp#, last, first, hours, absences, attendance%]
                totalAbsences += Integer.parseInt(row[4].trim());
                sumAttendancePercent += Double.parseDouble(row[5].replace("%", "").trim());
                validRows++;
            } catch (NumberFormatException e) {
            }
        }

        double averageAttendance = validRows > 0 ? sumAttendancePercent / validRows : 0.0;
        return new AttendanceSummary(totalAbsences, averageAttendance);
    }

    private boolean upsertTodayLog(String attendanceCsv, String employeeNumber, boolean isTimeIn) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            return false;
        }

        try {
            ensureAttendanceFile(attendanceCsv);

            List<String[]> rows;
            try (CSVReader reader = new CSVReader(new FileReader(attendanceCsv))) {
                rows = reader.readAll();
            }

            boolean motorPhFormat = isMotorPhFormat(rows);
            String todayMotorPh = LocalDate.now().format(DATE_FMT_MOTORPH);
            String todayIso = LocalDate.now().format(DATE_FMT_ISO);
            String now12h = LocalTime.now().format(TIME_FMT_12);
            String now24h = LocalTime.now().format(TIME_FMT_24);
            boolean found = false;
            boolean updated = false;

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (motorPhFormat) {
                    if (row.length >= 6
                            && row[0].trim().equals(employeeNumber.trim())
                            && row[3].trim().equals(todayMotorPh)) {
                        if (isTimeIn) {
                            row[4] = now12h;
                            updated = true;
                        } else {
                            if (row[4] == null || row[4].trim().isEmpty()) {
                                // Validation: cannot time out without timing in first.
                                return false;
                            }
                            row[5] = now12h;
                            updated = true;
                        }
                        found = true;
                        break;
                    }
                } else {
                    if (row.length >= 4
                            && row[0].trim().equals(employeeNumber.trim())
                            && row[1].trim().equals(todayIso)) {
                        if (isTimeIn) {
                            row[2] = now24h;
                            updated = true;
                        } else {
                            if (row[2] == null || row[2].trim().isEmpty()) {
                                // Validation: cannot time out without timing in first.
                                return false;
                            }
                            row[3] = now24h;
                            updated = true;
                        }
                        found = true;
                        break;
                    }
                 }
             }

             if (!found) {
                 // Validation: cannot time out without any existing same-day log.
                 if (!isTimeIn) {
                     return false;
                 }

                 if (motorPhFormat) {
                     String[] name = resolveEmployeeName(employeeNumber);
                     String timeIn = isTimeIn ? now12h : "";
                     String timeOut = isTimeIn ? "" : now12h;
                     rows.add(new String[]{employeeNumber.trim(), name[0], name[1], todayMotorPh, timeIn, timeOut});
                     updated = true;
                 } else {
                     String timeIn = isTimeIn ? now24h : "";
                     String timeOut = isTimeIn ? "" : now24h;
                     rows.add(new String[]{employeeNumber.trim(), todayIso, timeIn, timeOut});
                     updated = true;
                 }
             }

             if (!updated) {
                 return false;
             }

             try (CSVWriter writer = new CSVWriter(new FileWriter(attendanceCsv, false))) {
                 writer.writeAll(rows);
             }

             return true;
         } catch (Exception e) {
             return false;
         }
     }

    private void applyLog(TeamAttendance att, String date, String timeIn, String timeOut) {
        if (!timeIn.isEmpty() && !date.isEmpty()) {
            att.workedDates.add(date);
        }

        if (!timeIn.isEmpty() && !timeOut.isEmpty()) {
            LocalTime in = parseTime(timeIn);
            LocalTime out = parseTime(timeOut);
            if (in != null && out != null && !out.isBefore(in)) {
                att.totalHours += Duration.between(in, out).toMinutes() / 60.0;
            }
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value, TIME_FMT_12);
        } catch (Exception e) {
            try {
                return LocalTime.parse(value, TIME_FMT_12_SHORT);
            } catch (Exception e2) {
                try {
                    return LocalTime.parse(value, TIME_FMT_24);
                } catch (Exception e3) {
                    try {
                        return LocalTime.parse(value, TIME_FMT_24_SHORT);
                    } catch (Exception ignored) {
                        return null;
                    }
                }
            }
        }
    }

    private String[] resolveEmployeeName(String employeeNumber) {
        String[] name = {"", ""};
        try {
            String employeeCsv = ResourcePathService.resourceFile("EmployeeDetails_AdminView.csv");
            try (CSVReader reader = new CSVReader(new FileReader(employeeCsv))) {
                List<String[]> rows = reader.readAll();
                for (int i = 1; i < rows.size(); i++) {
                    String[] row = rows.get(i);
                    if (row.length >= 3 && row[0].trim().equals(employeeNumber.trim())) {
                        name[0] = row[1].trim();
                        name[1] = row[2].trim();
                        return name;
                    }
                }
            }
        } catch (java.io.IOException | CsvException e) {
            return name;
        }

        return name;
    }

    private boolean isMotorPhFormat(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return true;
        }

        String[] header = rows.get(0);
        if (header == null || header.length == 0) {
            return true;
        }

        if (header.length >= 6) {
            return true;
        }

        String joined = String.join(",", header).toLowerCase();
        return joined.contains("log in") || joined.contains("log out");
    }

    private void ensureAttendanceFile(String attendanceCsv) throws Exception {
        File file = new File(attendanceCsv);
        if (file.exists() && file.length() > 0) {
            return;
        }

        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            file.createNewFile();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter(file, false))) {
            writer.writeNext(MOTORPH_HEADER);
        }
    }

    private static class TeamAttendance {
        private final String employeeNumber;
        private final String lastName;
        private final String firstName;
        private double totalHours;
        private final Set<String> workedDates;

        private TeamAttendance(String employeeNumber, String lastName, String firstName) {
            this.employeeNumber = employeeNumber;
            this.lastName = lastName;
            this.firstName = firstName;
            this.totalHours = 0.0;
            this.workedDates = new HashSet<>();
        }
    }
}
