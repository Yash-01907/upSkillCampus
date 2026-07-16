package service;

import model.Expense;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to generate analytical reports, summaries, and breakdowns of expenses.
 */
public class ReportService {

    /**
     * Holds the summarized metrics of a list of expenses.
     */
    public static class ExpenseSummary {
        private final double total;
        private final long count;
        private final double average;
        private final Expense highest;
        private final Expense lowest;
        private final String mostUsedCategory;
        private final Map<String, Double> categoryTotals;
        private final Map<String, Double> categoryPercentages;

        public ExpenseSummary(double total, long count, double average, Expense highest, Expense lowest,
                              String mostUsedCategory, Map<String, Double> categoryTotals,
                              Map<String, Double> categoryPercentages) {
            this.total = total;
            this.count = count;
            this.average = average;
            this.highest = highest;
            this.lowest = lowest;
            this.mostUsedCategory = mostUsedCategory;
            this.categoryTotals = categoryTotals;
            this.categoryPercentages = categoryPercentages;
        }

        public double getTotal() { return total; }
        public long getCount() { return count; }
        public double getAverage() { return average; }
        public Expense getHighest() { return highest; }
        public Expense getLowest() { return lowest; }
        public String getMostUsedCategory() { return mostUsedCategory; }
        public Map<String, Double> getCategoryTotals() { return categoryTotals; }
        public Map<String, Double> getCategoryPercentages() { return categoryPercentages; }
    }

    /**
     * Generates a comprehensive summary from a provided list of expenses.
     */
    public ExpenseSummary generateSummary(List<Expense> list) {
        if (list == null || list.isEmpty()) {
            return new ExpenseSummary(0, 0, 0, null, null, "N/A", new HashMap<>(), new HashMap<>());
        }

        double total = list.stream().mapToDouble(Expense::getAmount).sum();
        long count = list.size();
        double average = total / count;

        Expense highest = list.stream()
                .max(Comparator.comparingDouble(Expense::getAmount))
                .orElse(null);

        Expense lowest = list.stream()
                .min(Comparator.comparingDouble(Expense::getAmount))
                .orElse(null);

        // Find most used category (by frequency)
        Map<String, Long> categoryFrequency = list.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.counting()));

        String mostUsedCategory = categoryFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Calculate Category breakdown and percentage
        Map<String, Double> categoryTotals = list.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        Map<String, Double> categoryPercentages = new HashMap<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            double categoryTotal = entry.getValue();
            double percentage = total > 0 ? (categoryTotal / total) * 100 : 0.0;
            categoryPercentages.put(entry.getKey(), percentage);
        }

        return new ExpenseSummary(total, count, average, highest, lowest, mostUsedCategory, categoryTotals, categoryPercentages);
    }

    /**
     * Generates a monthly spending summary across all historical records.
     * Returns a sorted map of YearMonth to total double spending.
     */
    public Map<YearMonth, Double> getMonthlySpendingSummary(List<Expense> list) {
        if (list == null || list.isEmpty()) {
            return new TreeMap<>();
        }

        // Use a TreeMap to keep the keys sorted chronologically
        return list.stream()
                .collect(Collectors.groupingBy(
                        e -> YearMonth.from(e.getDate()),
                        TreeMap::new,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    /**
     * Generates a yearly spending summary.
     * Returns a sorted map of Year to total double spending.
     */
    public Map<Integer, Double> getYearlySpendingSummary(List<Expense> list) {
        if (list == null || list.isEmpty()) {
            return new TreeMap<>();
        }

        return list.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().getYear(),
                        TreeMap::new,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }
}
