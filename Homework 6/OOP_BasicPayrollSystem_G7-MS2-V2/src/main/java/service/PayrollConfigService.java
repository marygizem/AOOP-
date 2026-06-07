package service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Persists configurable payroll settings used by the finance module.
 */
public class PayrollConfigService {
    private static final String CONFIG_FILE = "PayrollSettings.csv";
    private static final String[] HEADER = {"Key", "Value"};

    private static final String KEY_RICE_ALLOWANCE = "RICE_ALLOWANCE";
    private static final String KEY_PHONE_ALLOWANCE = "PHONE_ALLOWANCE";
    private static final String KEY_CLOTHING_ALLOWANCE = "CLOTHING_ALLOWANCE";
    private static final String KEY_ALLOWANCE_OVERRIDE = "ALLOWANCE_OVERRIDE_ENABLED";
    private static final String KEY_STRICT_ATTENDANCE = "STRICT_ATTENDANCE_MODE";

    public static class PayrollConfig {
        private final double riceAllowance;
        private final double phoneAllowance;
        private final double clothingAllowance;
        private final boolean allowanceOverrideEnabled;
        private final boolean strictAttendanceMode;

        public PayrollConfig(
                double riceAllowance,
                double phoneAllowance,
                double clothingAllowance,
                boolean allowanceOverrideEnabled,
                boolean strictAttendanceMode) {
            this.riceAllowance = riceAllowance;
            this.phoneAllowance = phoneAllowance;
            this.clothingAllowance = clothingAllowance;
            this.allowanceOverrideEnabled = allowanceOverrideEnabled;
            this.strictAttendanceMode = strictAttendanceMode;
        }

        public double getRiceAllowance() {
            return riceAllowance;
        }

        public double getPhoneAllowance() {
            return phoneAllowance;
        }

        public double getClothingAllowance() {
            return clothingAllowance;
        }

        public boolean isAllowanceOverrideEnabled() {
            return allowanceOverrideEnabled;
        }

        public boolean isStrictAttendanceMode() {
            return strictAttendanceMode;
        }

        public double getTotalAllowance() {
            return riceAllowance + phoneAllowance + clothingAllowance;
        }
    }

    public PayrollConfig loadConfig() throws Exception {
        ensureConfigFile();
        Map<String, String> values = loadKeyValueMap();

        return new PayrollConfig(
            parseDouble(values.get(KEY_RICE_ALLOWANCE), 1500.0),
            parseDouble(values.get(KEY_PHONE_ALLOWANCE), 1000.0),
            parseDouble(values.get(KEY_CLOTHING_ALLOWANCE), 1000.0),
            parseBoolean(values.get(KEY_ALLOWANCE_OVERRIDE), false),
            parseBoolean(values.get(KEY_STRICT_ATTENDANCE), false)
        );
    }

    public void saveDeductionSettings(boolean strictAttendanceMode) throws Exception {
        ensureConfigFile();
        Map<String, String> values = loadKeyValueMap();
        values.put(KEY_STRICT_ATTENDANCE, String.valueOf(strictAttendanceMode));
        writeKeyValueMap(values);
    }

    public void saveAllowanceSettings(double riceAllowance, double phoneAllowance, double clothingAllowance)
            throws Exception {
        ensureConfigFile();
        Map<String, String> values = loadKeyValueMap();
        values.put(KEY_RICE_ALLOWANCE, String.valueOf(riceAllowance));
        values.put(KEY_PHONE_ALLOWANCE, String.valueOf(phoneAllowance));
        values.put(KEY_CLOTHING_ALLOWANCE, String.valueOf(clothingAllowance));
        values.put(KEY_ALLOWANCE_OVERRIDE, "true");
        writeKeyValueMap(values);
    }

    private void ensureConfigFile() throws Exception {
        String path = ResourcePathService.resourceFile(CONFIG_FILE);
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
            return;
        }

        Map<String, String> defaults = new LinkedHashMap<>();
        defaults.put(KEY_RICE_ALLOWANCE, "1500.0");
        defaults.put(KEY_PHONE_ALLOWANCE, "1000.0");
        defaults.put(KEY_CLOTHING_ALLOWANCE, "1000.0");
        defaults.put(KEY_ALLOWANCE_OVERRIDE, "false");
        defaults.put(KEY_STRICT_ATTENDANCE, "false");
        writeKeyValueMap(defaults);
    }

    private Map<String, String> loadKeyValueMap() throws Exception {
        Map<String, String> values = new LinkedHashMap<>();
        String path = ResourcePathService.resourceFile(CONFIG_FILE);

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rows = reader.readAll();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length >= 2) {
                    values.put(row[0].trim(), row[1].trim());
                }
            }
        }

        return values;
    }

    private void writeKeyValueMap(Map<String, String> values) throws Exception {
        String path = ResourcePathService.resourceFile(CONFIG_FILE);

        Map<String, String> sanitized = new LinkedHashMap<>();
        sanitized.put(KEY_RICE_ALLOWANCE, values.getOrDefault(KEY_RICE_ALLOWANCE, "1500.0"));
        sanitized.put(KEY_PHONE_ALLOWANCE, values.getOrDefault(KEY_PHONE_ALLOWANCE, "1000.0"));
        sanitized.put(KEY_CLOTHING_ALLOWANCE, values.getOrDefault(KEY_CLOTHING_ALLOWANCE, "1000.0"));
        sanitized.put(KEY_ALLOWANCE_OVERRIDE, values.getOrDefault(KEY_ALLOWANCE_OVERRIDE, "false"));
        sanitized.put(KEY_STRICT_ATTENDANCE, values.getOrDefault(KEY_STRICT_ATTENDANCE, "false"));

        try (CSVWriter writer = new CSVWriter(new FileWriter(path, false))) {
            writer.writeNext(HEADER);
            for (Map.Entry<String, String> entry : sanitized.entrySet()) {
                writer.writeNext(new String[]{entry.getKey(), entry.getValue()});
            }
        }
    }

    private double parseDouble(String value, double fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private boolean parseBoolean(String value, boolean fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
