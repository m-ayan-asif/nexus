import java.io.*;
import java.util.*;

/**
 * DatabaseManager provides file-based persistence for all application data.
 * Data is serialized to binary files in the nexus_data/ directory.
 */
public class DatabaseManager {

    private static final String DATA_DIR = "nexus_data" + File.separator;
    private static final String PRODUCTS_FILE = DATA_DIR + "products.dat";
    private static final String ORDERS_FILE = DATA_DIR + "orders.dat";
    private static final String REVIEWS_FILE = DATA_DIR + "reviews.dat";

    public static void initialize() {
        new File(DATA_DIR).mkdirs();
    }

    @SuppressWarnings("unchecked")
    private static <T> ArrayList<T> loadData(String filename) {
        File file = new File(filename);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (ArrayList<T>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Warning: Could not load " + filename + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static <T> void saveData(String filename, ArrayList<T> data) {
        new File(DATA_DIR).mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error saving " + filename + ": " + e.getMessage());
        }
    }

    // Product persistence
    public static ArrayList<ProductManager.Product> loadProducts() {
        return loadData(PRODUCTS_FILE);
    }

    public static void saveProducts(ArrayList<ProductManager.Product> products) {
        saveData(PRODUCTS_FILE, products);
    }

    // Order persistence
    public static ArrayList<OrderHistory.OrderRecord> loadOrders() {
        return loadData(ORDERS_FILE);
    }

    public static void saveOrders(ArrayList<OrderHistory.OrderRecord> orders) {
        saveData(ORDERS_FILE, orders);
    }

    // Review persistence
    public static ArrayList<ReviewManager.Review> loadReviews() {
        return loadData(REVIEWS_FILE);
    }

    public static void saveReviews(ArrayList<ReviewManager.Review> reviews) {
        saveData(REVIEWS_FILE, reviews);
    }
}
