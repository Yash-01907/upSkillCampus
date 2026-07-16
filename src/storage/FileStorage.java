package storage;

import model.Category;
import model.Expense;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file I/O operations for categories and expenses.
 */
public class FileStorage {
    private final String expensesPath;
    private final String categoriesPath;

    public FileStorage(String expensesPath, String categoriesPath) {
        this.expensesPath = expensesPath;
        this.categoriesPath = categoriesPath;
        ensureDirectoryExists(expensesPath);
        ensureDirectoryExists(categoriesPath);
    }

    private void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    /**
     * Loads categories from the text file. If the file is missing or empty,
     * it initializes the list with default categories.
     */
    public List<Category> loadCategories() {
        List<Category> categories = new ArrayList<>();
        File file = new File(categoriesPath);

        if (!file.exists()) {
            // File does not exist, return empty list (caller will initialize defaults)
            return categories;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentId = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    categories.add(new Category(currentId++, line));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading categories file: " + e.getMessage());
        }

        return categories;
    }

    /**
     * Saves the list of categories back to the file.
     */
    public void saveCategories(List<Category> categories) throws IOException {
        File file = new File(categoriesPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Category category : categories) {
                writer.write(category.getName());
                writer.newLine();
            }
        }
    }

    /**
     * Loads expenses from the text file. Handles corrupted lines gracefully.
     */
    public List<Expense> loadExpenses() {
        List<Expense> expenses = new ArrayList<>();
        File file = new File(expensesPath);

        if (!file.exists()) {
            return expenses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                try {
                    // Split into at most 5 parts: ID, Date, Amount, Category, Description (which may contain commas)
                    String[] parts = line.split(",", 5);
                    if (parts.length < 5) {
                        System.err.println("Skipping corrupted expense line (insufficient columns): " + line);
                        continue;
                    }
                    
                    int id = Integer.parseInt(parts[0].trim());
                    LocalDate date = LocalDate.parse(parts[1].trim());
                    double amount = Double.parseDouble(parts[2].trim());
                    String category = parts[3].trim();
                    String description = parts[4].trim();
                    
                    expenses.add(new Expense(id, date, amount, category, description));
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Skipping corrupted expense line (invalid data types): " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading expenses file: " + e.getMessage());
        }

        return expenses;
    }

    /**
     * Saves the list of expenses back to the file.
     */
    public void saveExpenses(List<Expense> expenses) throws IOException {
        File file = new File(expensesPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Expense expense : expenses) {
                String line = String.format("%d,%s,%.2f,%s,%s",
                        expense.getId(),
                        expense.getDate().toString(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDescription()
                );
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
