package service;

import model.Category;
import storage.FileStorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages category operations including duplicate checks, additions,
 * updates, and deletions.
 */
public class CategoryService {
    private final FileStorage fileStorage;
    private final List<Category> categories;

    public CategoryService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
        this.categories = fileStorage.loadCategories();
        if (this.categories.isEmpty()) {
            initializeDefaults();
        }
    }

    private void initializeDefaults() {
        String[] defaults = {"Food", "Travel", "Shopping", "Medical", "Education", "Bills", "Entertainment"};
        for (int i = 0; i < defaults.length; i++) {
            categories.add(new Category(i + 1, defaults[i]));
        }
        save();
    }

    public List<Category> getCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Adds a new category if it doesn't already exist.
     */
    public void addCategory(String name) throws DuplicateCategoryException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String cleanName = name.trim();
        if (exists(cleanName)) {
            throw new DuplicateCategoryException("Category already exists: " + cleanName);
        }
        int newId = categories.stream().mapToInt(Category::getId).max().orElse(0) + 1;
        categories.add(new Category(newId, cleanName));
        save();
    }

    /**
     * Renames an existing category.
     */
    public void editCategory(String oldName, String newName) throws CategoryNotFoundException, DuplicateCategoryException {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New category name cannot be empty.");
        }
        String cleanNewName = newName.trim();
        Category category = findByName(oldName)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + oldName));

        if (!oldName.equalsIgnoreCase(cleanNewName) && exists(cleanNewName)) {
            throw new DuplicateCategoryException("Category already exists: " + cleanNewName);
        }

        category.setName(cleanNewName);
        save();
    }

    /**
     * Deletes a category.
     */
    public void deleteCategory(String name) throws CategoryNotFoundException {
        Category category = findByName(name)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + name));

        categories.remove(category);
        // Re-index IDs to remain sequential
        for (int i = 0; i < categories.size(); i++) {
            categories.get(i).setId(i + 1);
        }
        save();
    }

    public boolean exists(String name) {
        if (name == null) return false;
        return categories.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name.trim()));
    }

    public Optional<Category> findByName(String name) {
        if (name == null) return Optional.empty();
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name.trim()))
                .findFirst();
    }

    private void save() {
        try {
            fileStorage.saveCategories(categories);
        } catch (IOException e) {
            System.err.println("Failed to auto-save categories: " + e.getMessage());
        }
    }

    // Custom exceptions defined inside service layer
    public static class DuplicateCategoryException extends Exception {
        public DuplicateCategoryException(String message) {
            super(message);
        }
    }

    public static class CategoryNotFoundException extends Exception {
        public CategoryNotFoundException(String message) {
            super(message);
        }
    }
}
