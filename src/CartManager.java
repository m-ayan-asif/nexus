import java.util.*;

/**
 * CartManager handles shopping cart data persistence and sharing
 * across ProductCatalog, ShoppingCart, and OrderProcessing modules
 */
public class CartManager {
    private static CartManager instance;
    private ArrayList<CartItem> cartItems;
    private String currentUsername;
    private String currentUserType;

    // Prevent instantiation
    private CartManager() {
        this.cartItems = new ArrayList<>();
    }

    // Singleton pattern
    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Cart Item class
    public static class CartItem {
        private int productId;
        private String productName;
        private double unitPrice;
        private int quantity;

        public CartItem(int productId, String productName, double unitPrice, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getTotalPrice() { return unitPrice * quantity; }
    }

    // Cart management methods
    public void addItem(CartItem item) {
        // Check if item already exists
        for (CartItem existingItem : cartItems) {
            if (existingItem.getProductId() == item.getProductId()) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        cartItems.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
        }
    }

    public void clearCart() {
        cartItems.clear();
    }

    public ArrayList<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    public void setUsername(String username) {
        this.currentUsername = username;
    }

    public void setUserType(String userType) {
        this.currentUserType = userType;
    }

    public String getUsername() {
        return currentUsername;
    }

    public String getUserType() {
        return currentUserType;
    }
}