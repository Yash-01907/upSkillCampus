package util;

import model.Category;
import model.Expense;
import service.CategoryService;
import service.ExpenseService;
import service.ReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console-based user interface for interacting with the Expense Tracker.
 */
public class ConsoleUI {
    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final ReportService reportService;
    private final InputValidator validator;
    private final Scanner scanner;

    public ConsoleUI(ExpenseService expenseService, CategoryService categoryService, ReportService reportService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
        this.reportService = reportService;
        this.validator = new InputValidator(categoryService);
        this.scanner = new Scanner(System.in);
    }

    /**
     * Helper to clear the terminal screen.
     */
    private void clearScreen() {
        System.out.print("\u001b[H\u001b[2J");
        System.out.flush();
    }

    /**
     * Helper to pause and wait for Enter.
     */
    private void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Starts the main console application loop.
     */
    public void start() {
        boolean exit = false;
        while (!exit) {
            clearScreen();
            System.out.println("==================================================");
            System.out.println("             EXPENSE TRACKER SYSTEM               ");
            System.out.println("==================================================");
            System.out.println("1. Add Expense\n");
            System.out.println("2. View Expenses\n");
            System.out.println("3. Update Expense\n");
            System.out.println("4. Delete Expense\n");
            System.out.println("5. Search Expense\n");
            System.out.println("6. Manage Categories\n");
            System.out.println("7. Filter Expenses\n");
            System.out.println("8. Reports\n");
            System.out.println("9. Exit");
            System.out.println("==================================================");
            System.out.print("Enter choice (1-9): ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addExpenseFlow();
                    case "2" -> viewExpensesFlow();
                    case "3" -> updateExpenseFlow();
                    case "4" -> deleteExpenseFlow();
                    case "5" -> searchExpensesFlow();
                    case "6" -> manageCategoriesFlow();
                    case "7" -> filterExpensesFlow();
                    case "8" -> reportsFlow();
                    case "9" -> {
                        System.out.println("Thank you for using Expense Tracker! Goodbye.");
                        exit = true;
                    }
                    default -> {
                        System.out.println("Invalid choice! Please enter a number between 1 and 9.");
                        pause();
                    }
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                pause();
            }
        }
    }

    // ==========================================
    // EXPENSE FLOWS
    // ==========================================

    private void addExpenseFlow() {
        clearScreen();
        System.out.println("==================================================");
        System.out.println("                  ADD EXPENSE                     ");
        System.out.println("==================================================");

        // Date Input
        System.out.print("Enter Date (yyyy-MM-dd) [Default today]: ");
        String dateInput = scanner.nextLine().trim();
        LocalDate date = LocalDate.now();
        if (!dateInput.isEmpty()) {
            if (!validator.isValidDate(dateInput)) {
                System.out.println("Error: Invalid date format. Must be yyyy-MM-dd.");
                pause();
                return;
            }
            date = validator.parseDate(dateInput);
        }

        // Amount Input
        System.out.print("Enter Amount: ");
        String amountInput = scanner.nextLine().trim();
        if (!validator.isValidAmount(amountInput)) {
            System.out.println("Error: Amount must be a positive number.");
            pause();
            return;
        }
        double amount = validator.parseAmount(amountInput);

        // Category Input
        displayAvailableCategoriesInline();
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();
        if (!validator.isValidCategory(category)) {
            System.out.println("Error: Category does not exist. Please add it first.");
            pause();
            return;
        }

        // Description Input
        System.out.print("Enter Description: ");
        String description = scanner.nextLine().trim();
        if (!validator.isValidDescription(description)) {
            System.out.println("Error: Description cannot be empty.");
            pause();
            return;
        }

        try {
            expenseService.addExpense(date, amount, category, description);
            System.out.println("\nSUCCESS: Expense added successfully!");
        } catch (IOException | ExpenseService.CategoryNotFoundException e) {
            System.out.println("Error adding expense: " + e.getMessage());
        }
        pause();
    }

    private void viewExpensesFlow() {
        clearScreen();
        System.out.println("==================================================");
        System.out.println("                 VIEW EXPENSES                    ");
        System.out.println("==================================================");

        List<Expense> list = expenseService.getExpenses();
        if (list.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            pause();
            return;
        }

        System.out.println("Sort options:");
        System.out.println("1. Default (Chronological - original)");
        System.out.println("2. Amount (Low to High)");
        System.out.println("3. Amount (High to Low)");
        System.out.println("4. Date (Newest First)");
        System.out.println("5. Date (Oldest First)");
        System.out.print("Enter sorting choice [1]: ");
        String sortChoice = scanner.nextLine().trim();

        List<Expense> sorted = list;
        try {
            if (!sortChoice.isEmpty() && !sortChoice.equals("1")) {
                int option = Integer.parseInt(sortChoice);
                // Map choices 2,3,4,5 to sort choices 1,2,3,4 in ExpenseService
                if (option >= 2 && option <= 5) {
                    sorted = expenseService.getSortedExpenses(option - 1);
                } else {
                    System.out.println("Invalid sorting choice. Displaying default view.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid format. Displaying default view.");
        }

        clearScreen();
        System.out.println("==================================================");
        System.out.println("                 EXPENSE LIST                     ");
        System.out.println("==================================================");
        TableFormatter.printTable(sorted);
        pause();
    }

    private void updateExpenseFlow() {
        clearScreen();
        System.out.println("==================================================");
        System.out.println("                UPDATE EXPENSE                    ");
        System.out.println("==================================================");

        System.out.print("Enter Expense ID to update: ");
        String idStr = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID format.");
            pause();
            return;
        }

        Optional<Expense> opt = expenseService.searchById(id);
        if (opt.isEmpty()) {
            System.out.println("Error: Expense not found with ID " + id);
            pause();
            return;
        }

        Expense expense = opt.get();
        System.out.println("\nCurrent Details:");
        System.out.println("Date: " + expense.getDate());
        System.out.println("Amount: " + expense.getAmount());
        System.out.println("Category: " + expense.getCategory());
        System.out.println("Description: " + expense.getDescription());
        System.out.println("----------------------------------");
        System.out.println("(Press Enter to keep current values)");

        // Update Date
        System.out.print("Enter New Date (yyyy-MM-dd) [" + expense.getDate() + "]: ");
        String dateInput = scanner.nextLine().trim();
        LocalDate date = expense.getDate();
        if (!dateInput.isEmpty()) {
            if (!validator.isValidDate(dateInput)) {
                System.out.println("Error: Invalid date format.");
                pause();
                return;
            }
            date = validator.parseDate(dateInput);
        }

        // Update Amount
        System.out.print("Enter New Amount [" + expense.getAmount() + "]: ");
        String amountInput = scanner.nextLine().trim();
        double amount = expense.getAmount();
        if (!amountInput.isEmpty()) {
            if (!validator.isValidAmount(amountInput)) {
                System.out.println("Error: Amount must be positive.");
                pause();
                return;
            }
            amount = validator.parseAmount(amountInput);
        }

        // Update Category
        displayAvailableCategoriesInline();
        System.out.print("Enter New Category [" + expense.getCategory() + "]: ");
        String categoryInput = scanner.nextLine().trim();
        String category = expense.getCategory();
        if (!categoryInput.isEmpty()) {
            if (!validator.isValidCategory(categoryInput)) {
                System.out.println("Error: Category does not exist.");
                pause();
                return;
            }
            category = categoryInput;
        }

        // Update Description
        System.out.print("Enter New Description [" + expense.getDescription() + "]: ");
        String descriptionInput = scanner.nextLine().trim();
        String description = expense.getDescription();
        if (!descriptionInput.isEmpty()) {
            if (!validator.isValidDescription(descriptionInput)) {
                System.out.println("Error: Description cannot be empty.");
                pause();
                return;
            }
            description = descriptionInput;
        }

        try {
            expenseService.updateExpense(id, date, amount, category, description);
            System.out.println("\nSUCCESS: Expense updated successfully!");
        } catch (IOException | ExpenseService.ExpenseNotFoundException | ExpenseService.CategoryNotFoundException e) {
            System.out.println("Error updating expense: " + e.getMessage());
        }
        pause();
    }

    private void deleteExpenseFlow() {
        clearScreen();
        System.out.println("==================================================");
        System.out.println("                DELETE EXPENSE                    ");
        System.out.println("==================================================");

        System.out.print("Enter Expense ID to delete: ");
        String idStr = scanner.nextLine().trim();
        int id;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID format.");
            pause();
            return;
        }

        Optional<Expense> opt = expenseService.searchById(id);
        if (opt.isEmpty()) {
            System.out.println("Error: Expense not found.");
            pause();
            return;
        }

        Expense expense = opt.get();
        System.out.println("\nExpense to delete:");
        System.out.printf("ID: %d | Date: %s | Amount: ₹%,.2f | Category: %s | Description: %s\n",
                expense.getId(), expense.getDate(), expense.getAmount(), expense.getCategory(), expense.getDescription());
        System.out.print("\nAre you sure you want to delete this expense? (y/N): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("y") || confirm.equals("yes")) {
            try {
                expenseService.deleteExpense(id);
                System.out.println("\nSUCCESS: Expense deleted successfully!");
            } catch (IOException | ExpenseService.ExpenseNotFoundException e) {
                System.out.println("Error deleting expense: " + e.getMessage());
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
        pause();
    }

    // ==========================================
    // SEARCH FLOWS
    // ==========================================

    private void searchExpensesFlow() {
        boolean back = false;
        while (!back) {
            clearScreen();
            System.out.println("==================================================");
            System.out.println("                SEARCH EXPENSE                    ");
            System.out.println("==================================================");
            System.out.println("1. Search by ID");
            System.out.println("2. Search by Description");
            System.out.println("3. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter choice (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter ID: ");
                    String idStr = scanner.nextLine().trim();
                    try {
                        int id = Integer.parseInt(idStr);
                        Optional<Expense> opt = expenseService.searchById(id);
                        if (opt.isPresent()) {
                            TableFormatter.printTable(List.of(opt.get()));
                        } else {
                            System.out.println("No expense found with ID " + id);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    pause();
                }
                case "2" -> {
                    System.out.print("Enter Search Term (Description): ");
                    String term = scanner.nextLine().trim();
                    List<Expense> list = expenseService.searchByDescription(term);
                    if (!list.isEmpty()) {
                        TableFormatter.printTable(list);
                    } else {
                        System.out.println("No expenses found matching description '" + term + "'");
                    }
                    pause();
                }
                case "3" -> back = true;
                default -> {
                    System.out.println("Invalid choice!");
                    pause();
                }
            }
        }
    }

    // ==========================================
    // FILTER FLOWS
    // ==========================================

    private void filterExpensesFlow() {
        boolean back = false;
        while (!back) {
            clearScreen();
            System.out.println("==================================================");
            System.out.println("                FILTER EXPENSES                   ");
            System.out.println("==================================================");
            System.out.println("1. Filter by Date");
            System.out.println("2. Filter by Date Range");
            System.out.println("3. Filter by Category");
            System.out.println("4. Filter by Amount Range");
            System.out.println("5. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter choice (1-5): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter Date (yyyy-MM-dd): ");
                    String dateInput = scanner.nextLine().trim();
                    if (!validator.isValidDate(dateInput)) {
                        System.out.println("Invalid date format.");
                        pause();
                        break;
                    }
                    LocalDate date = validator.parseDate(dateInput);
                    TableFormatter.printTable(expenseService.filterByDate(date));
                    pause();
                }
                case "2" -> {
                    System.out.print("Enter Start Date (yyyy-MM-dd): ");
                    String startStr = scanner.nextLine().trim();
                    System.out.print("Enter End Date (yyyy-MM-dd): ");
                    String endStr = scanner.nextLine().trim();
                    if (!validator.isValidDate(startStr) || !validator.isValidDate(endStr)) {
                        System.out.println("Invalid date format.");
                        pause();
                        break;
                    }
                    LocalDate start = validator.parseDate(startStr);
                    LocalDate end = validator.parseDate(endStr);
                    if (start.isAfter(end)) {
                        System.out.println("Start date cannot be after end date.");
                        pause();
                        break;
                    }
                    TableFormatter.printTable(expenseService.filterByDateRange(start, end));
                    pause();
                }
                case "3" -> {
                    displayAvailableCategoriesInline();
                    System.out.print("Enter Category: ");
                    String cat = scanner.nextLine().trim();
                    TableFormatter.printTable(expenseService.filterByCategory(cat));
                    pause();
                }
                case "4" -> {
                    System.out.print("Enter Minimum Amount: ");
                    String minInput = scanner.nextLine().trim();
                    System.out.print("Enter Maximum Amount: ");
                    String maxInput = scanner.nextLine().trim();
                    if (!validator.isValidAmount(minInput) || !validator.isValidAmount(maxInput)) {
                        System.out.println("Amounts must be positive numbers.");
                        pause();
                        break;
                    }
                    double min = validator.parseAmount(minInput);
                    double max = validator.parseAmount(maxInput);
                    if (min > max) {
                        System.out.println("Minimum amount cannot exceed maximum amount.");
                        pause();
                        break;
                    }
                    TableFormatter.printTable(expenseService.filterByAmountRange(min, max));
                    pause();
                }
                case "5" -> back = true;
                default -> {
                    System.out.println("Invalid choice!");
                    pause();
                }
            }
        }
    }

    // ==========================================
    // CATEGORY MANAGEMENT
    // ==========================================

    private void manageCategoriesFlow() {
        boolean back = false;
        while (!back) {
            clearScreen();
            System.out.println("==================================================");
            System.out.println("               MANAGE CATEGORIES                  ");
            System.out.println("==================================================");
            System.out.println("1. Add Category");
            System.out.println("2. View Categories");
            System.out.println("3. Edit Category");
            System.out.println("4. Delete Category");
            System.out.println("5. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter choice (1-5): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    System.out.print("Enter new category name: ");
                    String name = scanner.nextLine().trim();
                    try {
                        categoryService.addCategory(name);
                        System.out.println("SUCCESS: Category '" + name + "' added!");
                    } catch (CategoryService.DuplicateCategoryException | IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause();
                }
                case "2" -> {
                    System.out.println("\nActive Categories:");
                    List<Category> list = categoryService.getCategories();
                    for (Category cat : list) {
                        System.out.printf("  %d. %s\n", cat.getId(), cat.getName());
                    }
                    pause();
                }
                case "3" -> {
                    displayAvailableCategoriesInline();
                    System.out.print("Enter the name of category to edit: ");
                    String oldName = scanner.nextLine().trim();
                    System.out.print("Enter new name: ");
                    String newName = scanner.nextLine().trim();
                    try {
                        categoryService.editCategory(oldName, newName);
                        // Update existing expenses using this category
                        expenseService.renameCategoryInExpenses(oldName, newName);
                        System.out.println("SUCCESS: Category renamed to '" + newName + "'. Expenses updated!");
                    } catch (CategoryService.DuplicateCategoryException | CategoryService.CategoryNotFoundException |
                             IllegalArgumentException | IOException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    pause();
                }
                case "4" -> {
                    displayAvailableCategoriesInline();
                    System.out.print("Enter name of category to delete: ");
                    String name = scanner.nextLine().trim();
                    if (expenseService.isCategoryInUse(name)) {
                        System.out.println("Error: Cannot delete category '" + name + "' because it is referenced by existing expenses.");
                        System.out.println("Please re-categorize or delete those expenses first before deleting this category.");
                    } else {
                        try {
                            categoryService.deleteCategory(name);
                            System.out.println("SUCCESS: Category '" + name + "' deleted!");
                        } catch (CategoryService.CategoryNotFoundException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                    pause();
                }
                case "5" -> back = true;
                default -> {
                    System.out.println("Invalid choice!");
                    pause();
                }
            }
        }
    }

    private void displayAvailableCategoriesInline() {
        List<Category> categories = categoryService.getCategories();
        System.out.print("Available Categories: [");
        for (int i = 0; i < categories.size(); i++) {
            System.out.print(categories.get(i).getName());
            if (i < categories.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    // ==========================================
    // REPORTS FLOWS
    // ==========================================

    private void reportsFlow() {
        boolean back = false;
        while (!back) {
            clearScreen();
            System.out.println("==================================================");
            System.out.println("                    REPORTS                       ");
            System.out.println("==================================================");
            System.out.println("1. Monthly Report");
            System.out.println("2. Yearly Report");
            System.out.println("3. Category Report");
            System.out.println("4. Overall Summary (All Time)");
            System.out.println("5. Monthly Spending Summary (All Time)");
            System.out.println("6. Back to Main Menu");
            System.out.println("==================================================");
            System.out.print("Enter choice (1-6): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> monthlyReportFlow();
                case "2" -> yearlyReportFlow();
                case "3" -> categoryReportFlow();
                case "4" -> overallSummaryFlow();
                case "5" -> monthlySpendingSummaryFlow();
                case "6" -> back = true;
                default -> {
                    System.out.println("Invalid choice!");
                    pause();
                }
            }
        }
    }

    private void monthlyReportFlow() {
        System.out.print("Enter Year (e.g. 2026): ");
        String yrStr = scanner.nextLine().trim();
        System.out.print("Enter Month (1-12): ");
        String moStr = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(yrStr);
            int month = Integer.parseInt(moStr);
            if (month < 1 || month > 12) {
                System.out.println("Invalid month. Must be between 1 and 12.");
                pause();
                return;
            }

            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<Expense> list = expenseService.filterByDateRange(start, end);
            ReportService.ExpenseSummary summary = reportService.generateSummary(list);

            String monthName = ym.getMonth().name();
            // Capitalize month name nicely (e.g. June)
            monthName = monthName.substring(0, 1) + monthName.substring(1).toLowerCase();

            clearScreen();
            System.out.println("========== MONTHLY REPORT =========");
            System.out.println("Month");
            System.out.println(monthName + " " + year);
            System.out.println("Total Expenses");
            System.out.printf("₹%,.2f\n", summary.getTotal());
            System.out.println("Highest Expense");
            System.out.println(summary.getHighest() != null ? String.format("₹%,.2f (%s)", summary.getHighest().getAmount(), summary.getHighest().getDescription()) : "₹0.00");
            System.out.println("Lowest Expense");
            System.out.println(summary.getLowest() != null ? String.format("₹%,.2f (%s)", summary.getLowest().getAmount(), summary.getLowest().getDescription()) : "₹0.00");
            System.out.println("Average Expense");
            System.out.printf("₹%,.2f\n", summary.getAverage());
            System.out.println("Most Used Category");
            System.out.println(summary.getMostUsedCategory());
            System.out.println("Expense Count");
            System.out.println(summary.getCount());
            System.out.println("===================================");

        } catch (NumberFormatException e) {
            System.out.println("Error: Year and Month must be numbers.");
        }
        pause();
    }

    private void yearlyReportFlow() {
        System.out.print("Enter Year (e.g. 2026): ");
        String yrStr = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(yrStr);
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);

            List<Expense> list = expenseService.filterByDateRange(start, end);
            ReportService.ExpenseSummary summary = reportService.generateSummary(list);

            clearScreen();
            System.out.println("========== YEARLY REPORT =========");
            System.out.println("Year");
            System.out.println(year);
            System.out.println("Total Expenses");
            System.out.printf("₹%,.2f\n", summary.getTotal());
            System.out.println("Highest Expense");
            System.out.println(summary.getHighest() != null ? String.format("₹%,.2f (%s)", summary.getHighest().getAmount(), summary.getHighest().getDescription()) : "₹0.00");
            System.out.println("Lowest Expense");
            System.out.println(summary.getLowest() != null ? String.format("₹%,.2f (%s)", summary.getLowest().getAmount(), summary.getLowest().getDescription()) : "₹0.00");
            System.out.println("Average Expense");
            System.out.printf("₹%,.2f\n", summary.getAverage());
            System.out.println("Most Used Category");
            System.out.println(summary.getMostUsedCategory());
            System.out.println("Expense Count");
            System.out.println(summary.getCount());
            System.out.println("==================================");

        } catch (NumberFormatException e) {
            System.out.println("Error: Year must be a number.");
        }
        pause();
    }

    private void categoryReportFlow() {
        displayAvailableCategoriesInline();
        System.out.print("Enter Category: ");
        String category = scanner.nextLine().trim();

        if (!validator.isValidCategory(category)) {
            System.out.println("Error: Category does not exist.");
            pause();
            return;
        }

        List<Expense> list = expenseService.filterByCategory(category);
        ReportService.ExpenseSummary summary = reportService.generateSummary(list);

        clearScreen();
        System.out.println("========== CATEGORY REPORT =========");
        System.out.println("Category");
        System.out.println(category);
        System.out.println("Total Expenses");
        System.out.printf("₹%,.2f\n", summary.getTotal());
        System.out.println("Highest Expense");
        System.out.println(summary.getHighest() != null ? String.format("₹%,.2f (%s)", summary.getHighest().getAmount(), summary.getHighest().getDescription()) : "₹0.00");
        System.out.println("Lowest Expense");
        System.out.println(summary.getLowest() != null ? String.format("₹%,.2f (%s)", summary.getLowest().getAmount(), summary.getLowest().getDescription()) : "₹0.00");
        System.out.println("Average Expense");
        System.out.printf("₹%,.2f\n", summary.getAverage());
        System.out.println("Expense Count");
        System.out.println(summary.getCount());
        System.out.println("====================================");
        pause();
    }

    private void overallSummaryFlow() {
        List<Expense> list = expenseService.getExpenses();
        ReportService.ExpenseSummary summary = reportService.generateSummary(list);

        clearScreen();
        System.out.println("========== OVERALL SUMMARY =========");
        System.out.println("Total Expenses");
        System.out.printf("₹%,.2f\n", summary.getTotal());
        System.out.println("Highest Expense");
        System.out.println(summary.getHighest() != null ? String.format("₹%,.2f (%s)", summary.getHighest().getAmount(), summary.getHighest().getDescription()) : "₹0.00");
        System.out.println("Lowest Expense");
        System.out.println(summary.getLowest() != null ? String.format("₹%,.2f (%s)", summary.getLowest().getAmount(), summary.getLowest().getDescription()) : "₹0.00");
        System.out.println("Average Expense");
        System.out.printf("₹%,.2f\n", summary.getAverage());
        System.out.println("Most Used Category");
        System.out.println(summary.getMostUsedCategory());
        System.out.println("Expense Count");
        System.out.println(summary.getCount());
        System.out.println("\n--- Category-wise Percentage ---");
        
        Map<String, Double> totals = summary.getCategoryTotals();
        Map<String, Double> percentages = summary.getCategoryPercentages();
        
        for (String cat : totals.keySet()) {
            System.out.printf("  %-15s: ₹%-12,.2f (%.1f%%)\n", cat, totals.get(cat), percentages.get(cat));
        }
        System.out.println("====================================");
        pause();
    }

    private void monthlySpendingSummaryFlow() {
        List<Expense> list = expenseService.getExpenses();
        Map<YearMonth, Double> summary = reportService.getMonthlySpendingSummary(list);

        clearScreen();
        System.out.println("========== MONTHLY SPENDING SUMMARY =========");
        if (summary.isEmpty()) {
            System.out.println("No expense data recorded.");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            for (Map.Entry<YearMonth, Double> entry : summary.entrySet()) {
                String monthStr = entry.getKey().format(formatter);
                System.out.printf("  %-18s: ₹%,.2f\n", monthStr, entry.getValue());
            }
        }
        System.out.println("=============================================");
        pause();
    }
}
