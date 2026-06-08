package service;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;

import model.Deduction;

/**
 * Computes monthly employee deductions using contribution table CSV resources.
 */
public class StatutoryDeductionService {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d[\\d,]*(?:\\.\\d+)?");

    private static final double DEFAULT_PHILHEALTH_RATE = 0.03;
    private static final double DEFAULT_PHILHEALTH_MIN_SALARY = 10_000.00;
    private static final double DEFAULT_PHILHEALTH_MAX_SALARY = 60_000.00;
    private static final double DEFAULT_PHILHEALTH_MIN_PREMIUM = 300.00;
    private static final double DEFAULT_PHILHEALTH_MAX_PREMIUM = 1_800.00;

    private static final double DEFAULT_PAGIBIG_LOWER_THRESHOLD = 1_500.00;
    private static final double DEFAULT_PAGIBIG_LOWER_RATE = 0.01;
    private static final double DEFAULT_PAGIBIG_HIGHER_RATE = 0.02;
    private static final double DEFAULT_PAGIBIG_MAX_EMPLOYEE_SHARE = 100.00;

    private final List<SssBracket> sssBrackets;
    private final PhilhealthRule philhealthRule;
    private final PagibigRule pagibigRule;
    private final List<TaxBracket> taxBrackets;

    public StatutoryDeductionService() {
        this.sssBrackets = loadSssBrackets();
        this.philhealthRule = loadPhilhealthRule();
        this.pagibigRule = loadPagibigRule();
        this.taxBrackets = loadTaxBrackets();
    }

    public Deduction computeMonthlyDeduction(double monthlyBasicSalary) {
        double salary = Math.max(0.0, monthlyBasicSalary);

        if (salary <= 0.0) {
            return new Deduction(0.0, 0.0, 0.0, 0.0);
        }

        double sss = round2(computeSss(salary));
        double philhealth = round2(computePhilhealth(salary));
        double pagibig = round2(computePagibig(salary));

        double taxableIncome = Math.max(0.0, salary - sss - philhealth - pagibig);
        double withholdingTax = round2(computeWithholdingTax(taxableIncome));

        double total = sss + philhealth + pagibig + withholdingTax;
        if (total > salary && total > 0.0) {
            double scale = salary / total;
            sss = round2(sss * scale);
            philhealth = round2(philhealth * scale);
            pagibig = round2(pagibig * scale);
            withholdingTax = round2(withholdingTax * scale);
        }

        return new Deduction(sss, philhealth, pagibig, withholdingTax);
    }

    private double computeSss(double salary) {
        if (sssBrackets.isEmpty()) {
            return 0.0;
        }

        for (SssBracket bracket : sssBrackets) {
            if (bracket.matches(salary)) {
                return bracket.employeeContribution;
            }
        }

        return sssBrackets.get(sssBrackets.size() - 1).employeeContribution;
    }

    private double computePhilhealth(double salary) {
        double monthlyPremium;
        if (salary <= philhealthRule.minSalary) {
            monthlyPremium = philhealthRule.minPremium;
        } else if (salary >= philhealthRule.maxSalary) {
            monthlyPremium = philhealthRule.maxPremium;
        } else {
            monthlyPremium = salary * philhealthRule.rate;
        }

        return monthlyPremium / 2.0;
    }

    private double computePagibig(double salary) {
        double rate = salary <= pagibigRule.lowerThreshold
            ? pagibigRule.lowerRate
            : pagibigRule.higherRate;
        double employeeShare = salary * rate;
        return Math.min(pagibigRule.maxEmployeeShare, employeeShare);
    }

    private double computeWithholdingTax(double taxableIncome) {
        for (TaxBracket bracket : taxBrackets) {
            if (bracket.matches(taxableIncome)) {
                double excess = Math.max(0.0, taxableIncome - bracket.excessOver);
                return bracket.baseTax + (excess * bracket.rate);
            }
        }

        return 0.0;
    }

    private List<SssBracket> loadSssBrackets() {
        List<SssBracket> brackets = new ArrayList<>();
        String path = ResourcePathService.resourceFile("SSS Contribution.csv");

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rows = reader.readAll();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (row.length < 4) {
                    continue;
                }

                String left = safeTrim(row[0]);
                String right = safeTrim(row[2]);
                String contributionText = safeTrim(row[3]);
                if (left.isEmpty() || contributionText.isEmpty()) {
                    continue;
                }

                double contribution = parseAmount(contributionText);
                if (left.toLowerCase().startsWith("below")) {
                    double upperBound = parseAmount(left);
                    brackets.add(new SssBracket(null, upperBound, contribution));
                    continue;
                }

                if (right.equalsIgnoreCase("over")) {
                    double lowerBound = parseAmount(left);
                    brackets.add(new SssBracket(lowerBound, null, contribution));
                    continue;
                }

                double lowerBound = parseAmount(left);
                double upperBound = parseAmount(right);
                brackets.add(new SssBracket(lowerBound, upperBound, contribution));
            }
        } catch (Exception e) {
            brackets.clear();
            brackets.add(new SssBracket(null, 3_250.00, 135.00));
            brackets.add(new SssBracket(24_750.00, null, 1_125.00));
        }

        return brackets;
    }

    private PhilhealthRule loadPhilhealthRule() {
        String path = ResourcePathService.resourceFile("Philhealth Contribution.csv");

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rows = reader.readAll();
            double minSalary = Double.MAX_VALUE;
            double maxSalary = 0.0;
            double minPremium = Double.MAX_VALUE;
            double maxPremium = 0.0;
            double rate = DEFAULT_PHILHEALTH_RATE;
            boolean found = false;

            for (String[] row : rows) {
                String salaryRange = row.length > 0 ? safeTrim(row[0]) : "";
                String rateText = row.length > 1 ? safeTrim(row[1]) : "";
                String premiumText = row.length > 2 ? safeTrim(row[2]) : "";

                if (salaryRange.isEmpty() && rateText.isEmpty() && premiumText.isEmpty()) {
                    continue;
                }

                if (!rateText.isEmpty() && rateText.contains("%")) {
                    rate = parsePercent(rateText);
                }

                List<Double> salaryNumbers = extractNumbers(salaryRange);
                for (double value : salaryNumbers) {
                    found = true;
                    minSalary = Math.min(minSalary, value);
                    maxSalary = Math.max(maxSalary, value);
                }

                List<Double> premiumNumbers = extractNumbers(premiumText);
                for (double value : premiumNumbers) {
                    found = true;
                    minPremium = Math.min(minPremium, value);
                    maxPremium = Math.max(maxPremium, value);
                }
            }

            if (!found || minSalary == Double.MAX_VALUE || minPremium == Double.MAX_VALUE) {
                return defaultPhilhealthRule();
            }

            return new PhilhealthRule(minSalary, maxSalary, minPremium, maxPremium, rate);
        } catch (Exception e) {
            return defaultPhilhealthRule();
        }
    }

    private PagibigRule loadPagibigRule() {
        String path = ResourcePathService.resourceFile("Pag-ibig Contribution.csv");

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rows = reader.readAll();

            double lowerThreshold = DEFAULT_PAGIBIG_LOWER_THRESHOLD;
            double lowerRate = DEFAULT_PAGIBIG_LOWER_RATE;
            double higherRate = DEFAULT_PAGIBIG_HIGHER_RATE;
            double maxEmployeeShare = DEFAULT_PAGIBIG_MAX_EMPLOYEE_SHARE;

            for (String[] row : rows) {
                String rangeText = row.length > 0 ? safeTrim(row[0]).toLowerCase() : "";
                String employeeRateText = row.length > 1 ? safeTrim(row[1]) : "";
                String combinedRowText = String.join(" ", row).toLowerCase();

                if (combinedRowText.contains("maximum contribution amount")) {
                    List<Double> values = extractNumbers(combinedRowText);
                    if (!values.isEmpty()) {
                        maxEmployeeShare = values.get(values.size() - 1);
                    }
                    continue;
                }

                if (rangeText.contains("least") || rangeText.contains("to")) {
                    List<Double> values = extractNumbers(rangeText);
                    if (!values.isEmpty()) {
                        lowerThreshold = values.get(values.size() - 1);
                    }
                    if (employeeRateText.contains("%")) {
                        lowerRate = parsePercent(employeeRateText);
                    }
                    continue;
                }

                if (rangeText.contains("over")) {
                    if (employeeRateText.contains("%")) {
                        higherRate = parsePercent(employeeRateText);
                    }
                }
            }

            return new PagibigRule(lowerThreshold, lowerRate, higherRate, maxEmployeeShare);
        } catch (Exception e) {
            return defaultPagibigRule();
        }
    }

    private List<TaxBracket> loadTaxBrackets() {
        String path = ResourcePathService.resourceFile("Witholding Tax.csv");
        List<TaxBracket> brackets = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                if (row.length < 2) {
                    continue;
                }

                String rangeText = safeTrim(row[0]).toLowerCase();
                String formulaText = safeTrim(row[1]).toLowerCase();
                if (rangeText.isEmpty() || formulaText.isEmpty()) {
                    continue;
                }

                if (!rangeText.contains("below") && !rangeText.contains("above") && !rangeText.contains("to")) {
                    continue;
                }

                if (formulaText.contains("no withholding")) {
                    double max = firstOrZero(extractNumbers(rangeText));
                    brackets.add(new TaxBracket(null, max + 0.00001, 0.0, 0.0, 0.0));
                    continue;
                }

                double rate = parsePercent(formulaText);
                List<Double> formulaNumbers = extractNumbers(formulaText);
                if (formulaNumbers.isEmpty()) {
                    continue;
                }

                double baseTax = formulaText.contains("plus") && formulaNumbers.size() >= 2
                    ? firstOrZero(formulaNumbers)
                    : 0.0;
                double excessOver = formulaNumbers.get(formulaNumbers.size() - 1);

                Double minInclusive = excessOver;
                Double maxExclusive = null;

                if (rangeText.contains("to below")) {
                    List<Double> rangeNumbers = extractNumbers(rangeText);
                    if (rangeNumbers.size() >= 2) {
                        minInclusive = rangeNumbers.get(0);
                        maxExclusive = rangeNumbers.get(1);
                    }
                } else if (rangeText.contains("and above")) {
                    minInclusive = firstOrZero(extractNumbers(rangeText));
                }

                brackets.add(new TaxBracket(minInclusive, maxExclusive, baseTax, rate, excessOver));
            }
        } catch (Exception e) {
            return defaultTaxBrackets();
        }

        if (brackets.isEmpty()) {
            return defaultTaxBrackets();
        }

        return brackets;
    }

    private PhilhealthRule defaultPhilhealthRule() {
        return new PhilhealthRule(
            DEFAULT_PHILHEALTH_MIN_SALARY,
            DEFAULT_PHILHEALTH_MAX_SALARY,
            DEFAULT_PHILHEALTH_MIN_PREMIUM,
            DEFAULT_PHILHEALTH_MAX_PREMIUM,
            DEFAULT_PHILHEALTH_RATE
        );
    }

    private PagibigRule defaultPagibigRule() {
        return new PagibigRule(
            DEFAULT_PAGIBIG_LOWER_THRESHOLD,
            DEFAULT_PAGIBIG_LOWER_RATE,
            DEFAULT_PAGIBIG_HIGHER_RATE,
            DEFAULT_PAGIBIG_MAX_EMPLOYEE_SHARE
        );
    }

    private List<TaxBracket> defaultTaxBrackets() {
        List<TaxBracket> defaults = new ArrayList<>();
        defaults.add(new TaxBracket(null, 20_832.00001, 0.0, 0.0, 0.0));
        defaults.add(new TaxBracket(20_833.0, 33_333.0, 0.0, 0.20, 20_833.0));
        defaults.add(new TaxBracket(33_333.0, 66_667.0, 2_500.0, 0.25, 33_333.0));
        defaults.add(new TaxBracket(66_667.0, 166_667.0, 10_833.0, 0.30, 66_667.0));
        defaults.add(new TaxBracket(166_667.0, 666_667.0, 40_833.33, 0.32, 166_667.0));
        defaults.add(new TaxBracket(666_667.0, null, 200_833.33, 0.35, 666_667.0));
        return defaults;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private double parseAmount(String rawText) {
        List<Double> values = extractNumbers(rawText);
        return lastOrZero(values);
    }

    private double parsePercent(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return 0.0;
        }

        Matcher percentMatcher = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%").matcher(rawText);
        if (percentMatcher.find()) {
            try {
                return Double.parseDouble(percentMatcher.group(1)) / 100.0;
            } catch (NumberFormatException ignored) {
            }
        }

        List<Double> values = extractNumbers(rawText);
        return lastOrZero(values) / 100.0;
    }

    private List<Double> extractNumbers(String text) {
        List<Double> values = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return values;
        }

        Matcher matcher = NUMBER_PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                values.add(Double.valueOf(matcher.group().replace(",", "")));
            } catch (NumberFormatException ignored) {
            }
        }

        return values;
    }

    private double firstOrZero(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        Double first = values.get(0);
        return first == null ? 0.0 : first;
    }

    private double lastOrZero(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }

        Double last = values.get(values.size() - 1);
        return last == null ? 0.0 : last;
    }

    private double round2(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    private static final class SssBracket {
        private final Double minInclusive;
        private final Double maxExclusive;
        private final double employeeContribution;

        private SssBracket(Double minInclusive, Double maxExclusive, double employeeContribution) {
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
            this.employeeContribution = employeeContribution;
        }

        private boolean matches(double salary) {
            boolean meetsMin = minInclusive == null || salary >= minInclusive;
            boolean meetsMax = maxExclusive == null || salary < maxExclusive;
            return meetsMin && meetsMax;
        }
    }

    private static final class PhilhealthRule {
        private final double minSalary;
        private final double maxSalary;
        private final double minPremium;
        private final double maxPremium;
        private final double rate;

        private PhilhealthRule(double minSalary, double maxSalary, double minPremium, double maxPremium, double rate) {
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
            this.minPremium = minPremium;
            this.maxPremium = maxPremium;
            this.rate = rate;
        }
    }

    private static final class PagibigRule {
        private final double lowerThreshold;
        private final double lowerRate;
        private final double higherRate;
        private final double maxEmployeeShare;

        private PagibigRule(double lowerThreshold, double lowerRate, double higherRate, double maxEmployeeShare) {
            this.lowerThreshold = lowerThreshold;
            this.lowerRate = lowerRate;
            this.higherRate = higherRate;
            this.maxEmployeeShare = maxEmployeeShare;
        }
    }

    private static final class TaxBracket {
        private final Double minInclusive;
        private final Double maxExclusive;
        private final double baseTax;
        private final double rate;
        private final double excessOver;

        private TaxBracket(Double minInclusive, Double maxExclusive, double baseTax, double rate, double excessOver) {
            this.minInclusive = minInclusive;
            this.maxExclusive = maxExclusive;
            this.baseTax = baseTax;
            this.rate = rate;
            this.excessOver = excessOver;
        }

        private boolean matches(double taxableIncome) {
            boolean meetsMin = minInclusive == null || taxableIncome >= minInclusive;
            boolean meetsMax = maxExclusive == null || taxableIncome < maxExclusive;
            return meetsMin && meetsMax;
        }
    }
}
