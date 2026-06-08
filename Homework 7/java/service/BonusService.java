package service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service for grouped bonus report data.
 */
public class BonusService {
    private final EmployeeService employeeService;

    public BonusService() {
        this.employeeService = new EmployeeService();
    }

    public BonusSummary getBonusSummary(String bonusCsvFile) throws Exception {
        List<String[]> bonusData = employeeService.getEmployees(bonusCsvFile);
        Map<String, List<BonusEntry>> groupedBonuses = new LinkedHashMap<>();
        double grandTotal = 0.0;

        for (int i = 1; i < bonusData.size(); i++) {
            String[] row = bonusData.get(i);
            if (row.length < 5) {
                continue;
            }

            String employeeName = row[1] + ", " + row[2];
            String bonusType = row[3].trim();
            double amount = parseAmount(row[4]);

            groupedBonuses.putIfAbsent(bonusType, new ArrayList<>());
            groupedBonuses.get(bonusType).add(new BonusEntry(employeeName, amount));
            grandTotal += amount;
        }

        return new BonusSummary(groupedBonuses, grandTotal);
    }

    private double parseAmount(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    public static class BonusEntry {
        private final String employeeName;
        private final double amount;

        public BonusEntry(String employeeName, double amount) {
            this.employeeName = employeeName;
            this.amount = amount;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public double getAmount() {
            return amount;
        }
    }

    public static class BonusSummary {
        private final Map<String, List<BonusEntry>> groupedBonuses;
        private final double grandTotal;

        public BonusSummary(Map<String, List<BonusEntry>> groupedBonuses, double grandTotal) {
            this.groupedBonuses = groupedBonuses;
            this.grandTotal = grandTotal;
        }

        public Map<String, List<BonusEntry>> getGroupedBonuses() {
            return groupedBonuses;
        }

        public double getGrandTotal() {
            return grandTotal;
        }
    }
}