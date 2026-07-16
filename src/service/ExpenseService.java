package service;

import model.Expense;
import storage.FileStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages expense operations including CRUD, filtering, searching, sorting,
 * and data integrity checks.
 */
public class ExpenseService {
    private final FileStorage fileStorage;
    private final CategoryService categoryService;
    private final List<Expense> expenses;

    public ExpenseService(FileStorage fileStorage, CategoryService categoryService) {
        this.fileStorage = fileStorage;
        this.categoryService = categoryService;
        this.expenses = fileStorage.loadExpenses();
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }

    /**
     * Adds a new expense after validating that the category exists.
     */
    public void addExpense(LocalDate date, double amount, String category, String description) 
            throws CategoryNotFoundException, IOException {
        
        if (!categoryService.exists(category)) {
            throw new CategoryNotFoundException("Category '" + category + "' does not exist.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        int newId = expenses.stream().mapToInt(Expense::getId).max().orElse(0) + 1;
        Expense expense = new Expense(newId, date, amount, category.trim(), description.trim());
        expenses.add(expense);
        save();
    }

    /**
     * Updates an existing expense.
     */
    public void updateExpense(int id, LocalDate date, double amount, String category, String description) 
            throws ExpenseNotFoundException, CategoryNotFoundException, IOException {
        
        Expense expense = searchById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense with ID " + id + " not found."));

        if (!categoryService.exists(category)) {
            throw new CategoryNotFoundException("Category '" + category + "' does not exist.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        expense.setDate(date);
        expense.setAmount(amount);
        expense.setCategory(category.trim());
        expense.setDescription(description.trim());
        save();
    }

    /**
     * Deletes an expense by ID.
     */
    public void deleteExpense(int id) throws ExpenseNotFoundException, IOException {
        Expense expense = searchById(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense with ID " + id + " not found."));
        expenses.remove(expense);
        save();
    }

    /**
     * Searches for an expense by ID.
     */
    public Optional<Expense> searchById(int id) {
        return expenses.stream()
                .filter(e -> e.getId() == id)
                .findFirst();
    }

    /**
     * Searches for expenses containing the description term (case-insensitive).
     */
    public List<Expense> searchByDescription(String term) {
        if (term == null || term.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String cleanTerm = term.toLowerCase().trim();
        return expenses.stream()
                .filter(e -> e.getDescription().toLowerCase().contains(cleanTerm))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses by a single date.
     */
    public List<Expense> filterByDate(LocalDate date) {
        return expenses.stream()
                .filter(e -> e.getDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses within a date range (inclusive).
     */
    public List<Expense> filterByDateRange(LocalDate start, LocalDate end) {
        return expenses.stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(end))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses by category name (case-insensitive).
     */
    public List<Expense> filterByCategory(String category) {
        return expenses.stream()
                .filter(e -> e.getCategory().equalsIgnoreCase(category.trim()))
                .collect(Collectors.toList());
    }

    /**
     * Filters expenses within an amount range (inclusive).
     */
    public List<Expense> filterByAmountRange(double min, double max) {
        return expenses.stream()
                .filter(e -> e.getAmount() >= min && e.getAmount() <= max)
                .collect(Collectors.toList());
    }

    /**
     * Sorts expenses using the specified option.
     * Options:
     * 1 - Ascending Amount
     * 2 - Descending Amount
     * 3 - Newest First
     * 4 - Oldest First
     */
    public List<Expense> getSortedExpenses(int option) {
        List<Expense> sorted = new ArrayList<>(expenses);
        switch (option) {
            case 1 -> sorted.sort(Comparator.comparingDouble(Expense::getAmount));
            case 2 -> sorted.sort(Comparator.comparingDouble(Expense::getAmount).reversed());
            case 3 -> sorted.sort(Comparator.comparing(Expense::getDate).reversed());
            case 4 -> sorted.sort(Comparator.comparing(Expense::getDate));
            default -> throw new IllegalArgumentException("Invalid sorting option.");
        }
        return sorted;
    }

    /**
     * Updates references when a category is renamed.
     */
    public void renameCategoryInExpenses(String oldName, String newName) throws IOException {
        boolean updated = false;
        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(oldName)) {
                expense.setCategory(newName);
                updated = true;
            }
        }
        if (updated) {
            save();
        }
    }

    /**
     * Checks if a category is currently referenced by any expense.
     */
    public boolean isCategoryInUse(String categoryName) {
        return expenses.stream()
                .anyMatch(e -> e.getCategory().equalsIgnoreCase(categoryName));
    }

    private void save() throws IOException {
        fileStorage.saveExpenses(expenses);
    }

    // Custom Exceptions
    public static class ExpenseNotFoundException extends Exception {
        public ExpenseNotFoundException(String message) {
            super(message);
        }
    }

    public static class CategoryNotFoundException extends Exception {
        public CategoryNotFoundException(String message) {
            super(message);
        }
    }
}
