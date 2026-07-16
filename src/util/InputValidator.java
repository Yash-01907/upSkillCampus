package util;

import service.CategoryService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validates inputs for the Expense Tracker application.
 */
public class InputValidator {
    private final CategoryService categoryService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InputValidator(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Validates and parses a date string in yyyy-MM-dd format.
     */
    public boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        try {
            LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Parses a date string. Assumes prior validation with isValidDate.
     */
    public LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
    }

    /**
     * Validates that the amount is positive.
     */
    public boolean isValidAmount(String amountStr) {
        if (amountStr == null) return false;
        try {
            double amount = Double.parseDouble(amountStr.trim());
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Parses an amount. Assumes prior validation with isValidAmount.
     */
    public double parseAmount(String amountStr) {
        return Double.parseDouble(amountStr.trim());
    }

    /**
     * Validates that the category exists.
     */
    public boolean isValidCategory(String categoryName) {
        if (categoryName == null) return false;
        return categoryService.exists(categoryName.trim());
    }

    /**
     * Validates that a description is not empty or null.
     */
    public boolean isValidDescription(String description) {
        return description != null && !description.trim().isEmpty();
    }
}
