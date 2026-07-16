import storage.FileStorage;
import service.CategoryService;
import service.ExpenseService;
import service.ReportService;
import util.ConsoleUI;

/**
 * Entry point for the Expense Tracker application.
 */
public class Main {
    public static void main(String[] args) {
        // Define paths for data files relative to the project root
        String expensesPath = "data/expenses.txt";
        String categoriesPath = "data/categories.txt";

        // Initialize components
        FileStorage storage = new FileStorage(expensesPath, categoriesPath);
        CategoryService categoryService = new CategoryService(storage);
        ExpenseService expenseService = new ExpenseService(storage, categoryService);
        ReportService reportService = new ReportService();

        // Boot and start Console UI
        ConsoleUI ui = new ConsoleUI(expenseService, categoryService, reportService);
        ui.start();
    }
}
