import java.util.*;

/**
 * Enhanced OrderHistory manages shared order data with seller tracking
 * Stores orders created during the session with seller product information
 */
public class OrderHistory {
    private static OrderHistory instance;
    private ArrayList<OrderRecord> orders;
    private static int nextOrderId = 1001;

    OrderHistory() {
        this.orders = new ArrayList<>();
    }

    public static OrderHistory getInstance() {
        if (instance == null) {
            instance = new OrderHistory();
        }
        return instance;
    }

    // Add order to history
    public void addOrder(OrderRecord order) {
        orders.add(order);
    }

    // Get all orders for a buyer
    public ArrayList<OrderRecord> getUserOrders(String username) {
        ArrayList<OrderRecord> userOrders = new ArrayList<>();
        for (OrderRecord order : orders) {
            if (order.getBuyerUsername().equals(username)) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    // Get all orders for a seller (orders containing seller's products)
    public ArrayList<OrderRecord> getSellerOrders(String sellerUsername) {
        ArrayList<OrderRecord> sellerOrders = new ArrayList<>();
        for (OrderRecord order : orders) {
            if (order.containsSellerProduct(sellerUsername)) {
                sellerOrders.add(order);
            }
        }
        return sellerOrders;
    }

    // Get all orders
    public ArrayList<OrderRecord> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // Clear all orders (for testing/reset)
    public void clearOrders() {
        orders.clear();
        nextOrderId = 1001;
    }

    // Get next order ID
    public static int getNextOrderId() {
        return nextOrderId++;
    }

    // Order Record class with seller tracking
    public static class OrderRecord {
        private int orderId;
        private String buyerUsername;
        private String email;
        private String deliveryAddress;
        private ArrayList<OrderItem> items;  // Changed to track seller info
        private double totalAmount;
        private String status;
        private String orderDate;

        public OrderRecord(int orderId, String buyerUsername, String email, String deliveryAddress, ArrayList<OrderItem> items, double totalAmount, String status, String orderDate) {
            this.orderId = orderId;
            this.buyerUsername = buyerUsername;
            this.email = email;
            this.deliveryAddress = deliveryAddress;
            this.items = new ArrayList<>(items);
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
        }

        // Constructor for backwards compatibility with string items
        public OrderRecord(int orderId, String buyerUsername, String email, String deliveryAddress, ArrayList<String> items, double totalAmount, String status, String orderDate, int val) {
            this.orderId = orderId;
            this.buyerUsername = buyerUsername;
            this.email = email;
            this.deliveryAddress = deliveryAddress;
            this.items = new ArrayList<>();
            // Convert strings to OrderItems (seller unknown at this point)
            for (String item : items) {
                this.items.add(new OrderItem(item, "Unknown"));
            }
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
        }

        // Check if order contains products from a specific seller
        public boolean containsSellerProduct(String sellerUsername) {
            for (OrderItem item : items) {
                if (item.getSellerUsername().equals(sellerUsername)) {
                    return true;
                }
            }
            return false;
        }

        // Get seller's items in this order
        public ArrayList<OrderItem> getSellerItems(String sellerUsername) {
            ArrayList<OrderItem> sellerItems = new ArrayList<>();
            for (OrderItem item : items) {
                if (item.getSellerUsername().equals(sellerUsername)) {
                    sellerItems.add(item);
                }
            }
            return sellerItems;
        }

        // Get items as strings for display
        public ArrayList<String> getItemsAsStrings() {
            ArrayList<String> itemStrings = new ArrayList<>();
            for (OrderItem item : items) {
                itemStrings.add(item.getProductName());
            }
            return itemStrings;
        }

        // Getters
        public int getOrderId() { return orderId; }
        public String getBuyerUsername() { return buyerUsername; }
        public String getEmail() { return email; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public ArrayList<OrderItem> getItems() { return new ArrayList<>(items); }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public String getOrderDate() { return orderDate; }

        // Setters
        public void setStatus(String status) { this.status = status; }
    }

    // Order Item class to track product and seller
    public static class OrderItem {
        private String productName;
        private String sellerUsername;

        public OrderItem(String productName, String sellerUsername) {
            this.productName = productName;
            this.sellerUsername = sellerUsername;
        }

        public String getProductName() { return productName; }
        public String getSellerUsername() { return sellerUsername; }

        public void setSellerUsername(String sellerUsername) {
            this.sellerUsername = sellerUsername;
        }
    }
}