package util;

import model.Expense;

import java.util.List;

/**
 * Formats a list of expenses into a neat ASCII table.
 */
public class TableFormatter {

    /**
     * Prints the list of expenses in a structured ASCII table.
     */
    public static void printTable(List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            System.out.println("No expenses to display.");
            return;
        }

        // Header titles
        String hId = "ID";
        String hDate = "Date";
        String hAmount = "Amount";
        String hCategory = "Category";
        String hDescription = "Description";

        // Determine content widths (minimum header width)
        int wId = hId.length();
        int wDate = hDate.length();
        int wAmount = hAmount.length();
        int wCategory = hCategory.length();
        int wDesc = hDescription.length();

        for (Expense e : expenses) {
            wId = Math.max(wId, String.valueOf(e.getId()).length());
            wDate = Math.max(wDate, e.getDate().toString().length());
            wAmount = Math.max(wAmount, String.format("%.2f", e.getAmount()).length());
            wCategory = Math.max(wCategory, e.getCategory().length());
            wDesc = Math.max(wDesc, e.getDescription().length());
        }

        // Separator line: + w1+2 dashes + w2+2 dashes + ...
        String separator = "+" + "-".repeat(wId + 2) +
                "+" + "-".repeat(wDate + 2) +
                "+" + "-".repeat(wAmount + 2) +
                "+" + "-".repeat(wCategory + 2) +
                "+" + "-".repeat(wDesc + 2) + "+";

        // Row format with 1 leading and 1 trailing space for each column cell
        String rowFormat = "| %-" + wId + "s | %-" + wDate + "s | %-" + wAmount + "s | %-" + wCategory + "s | %-" + wDesc + "s |\n";

        // Print header
        System.out.println(separator);
        System.out.printf(rowFormat, hId, hDate, hAmount, hCategory, hDescription);
        System.out.println(separator);

        // Print rows
        for (Expense e : expenses) {
            System.out.printf(rowFormat,
                    String.valueOf(e.getId()),
                    e.getDate().toString(),
                    String.format("%.2f", e.getAmount()),
                    e.getCategory(),
                    e.getDescription()
            );
        }
        System.out.println(separator);
    }
}
