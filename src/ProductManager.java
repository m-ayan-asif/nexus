import java.util.*;

/**
 * ProductManager manages shared product data across all modules
 * Ensures sellers' changes are visible to buyers in real-time
 */
public class ProductManager {
    private static ProductManager instance;
    private ArrayList<Product> productDatabase;

    private ProductManager() {
        this.productDatabase = new ArrayList<>();
        initializeDefaultProducts();
    }

    public static ProductManager getInstance() {
        if (instance == null) {
            instance = new ProductManager();
        }
        return instance;
    }

    // Add product
    public void addProduct(Product product) {
        productDatabase.add(product);
    }

    // Update product
    public void updateProduct(Product product) {
        for (int i = 0; i < productDatabase.size(); i++) {
            if (productDatabase.get(i).getId() == product.getId()) {
                productDatabase.set(i, product);
                return;
            }
        }
    }

    // Delete product
    public void deleteProduct(int productId) {
        for (int i = 0; i < productDatabase.size(); i++) {
            if (productDatabase.get(i).getId() == productId) {
                productDatabase.remove(i);
                return;
            }
        }
    }

    // Get single product
    public Product getProduct(int productId) {
        for (Product p : productDatabase) {
            if (p.getId() == productId) {
                return p;
            }
        }
        return null;
    }

    // Get all products
    public ArrayList<Product> getAllProducts() {
        return new ArrayList<>(productDatabase);
    }

    // Get seller's products
    public ArrayList<Product> getSellerProducts(String sellerUsername) {
        ArrayList<Product> sellerProducts = new ArrayList<>();
        for (Product p : productDatabase) {
            if (p.getSeller().equals(sellerUsername)) {
                sellerProducts.add(p);
            }
        }
        return sellerProducts;
    }

    // Get next product ID
    public int getNextProductId() {
        int maxId = 0;
        for (Product p : productDatabase) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }
        return maxId + 1;
    }

    // Initialize default products
    private void initializeDefaultProducts() {
        productDatabase.add(new Product(1, "Wireless Headphones", "seller1", 79.99, "High-quality wireless headphones with noise cancellation", 45, "Electronics"));
        productDatabase.add(new Product(2, "USB-C Cable", "seller1", 12.99, "Durable 2-meter USB-C charging cable", 150, "Accessories"));
        productDatabase.add(new Product(3, "Laptop Stand", "seller2", 34.99, "Adjustable aluminum laptop stand for better ergonomics", 28, "Office"));
        productDatabase.add(new Product(4, "Mechanical Keyboard", "seller1", 89.99, "RGB mechanical keyboard with custom switches", 62, "Electronics"));
        productDatabase.add(new Product(5, "Mouse Pad", "seller2", 19.99, "Large extended mouse pad with non-slip base", 85, "Accessories"));
        productDatabase.add(new Product(6, "Phone Case", "seller3", 14.99, "Protective TPU phone case with shock absorption", 200, "Accessories"));
    }

    // Product model
    public static class Product {
        private int id;
        private String name;
        private String seller;
        private double price;
        private String description;
        private int stock;
        private String category;

        public Product(int id, String name, String seller, double price, String description, int stock, String category) {
            this.id = id;
            this.name = name;
            this.seller = seller;
            this.price = price;
            this.description = description;
            this.stock = stock;
            this.category = category;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSeller() { return seller; }
        public double getPrice() { return price; }
        public String getDescription() { return description; }
        public int getStock() { return stock; }
        public String getCategory() { return category; }

        public void setName(String name) { this.name = name; }
        public void setPrice(double price) { this.price = price; }
        public void setDescription(String description) { this.description = description; }
        public void setStock(int stock) { this.stock = stock; }
        public void setCategory(String category) { this.category = category; }
    }
}