import java.util.*;

/**
 * OrderHistory manages shared order data across modules
 * Stores orders created during the session
 */
public class OrderHistory {
    private static OrderHistory instance;
    private ArrayList<OrderRecord> orders;

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

    // Get all orders for a user
    public ArrayList<OrderRecord> getUserOrders(String username) {
        ArrayList<OrderRecord> userOrders = new ArrayList<>();
        for (OrderRecord order : orders) {
            if (order.getBuyerUsername().equals(username)) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    // Get all orders
    public ArrayList<OrderRecord> getAllOrders() {
        return new ArrayList<>(orders);
    }

    // Clear all orders (for testing/reset)
    public void clearOrders() {
        orders.clear();
    }

    // Order Record class
    public static class OrderRecord {
        private int orderId;
        private String buyerUsername;
        private String email;
        private String deliveryAddress;
        private ArrayList<String> items;
        private double totalAmount;
        private String status;
        private String orderDate;

        public OrderRecord(int orderId, String buyerUsername, String email, String deliveryAddress, ArrayList<String> items, double totalAmount, String status, String orderDate) {
            this.orderId = orderId;
            this.buyerUsername = buyerUsername;
            this.email = email;
            this.deliveryAddress = deliveryAddress;
            this.items = new ArrayList<>(items);
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
        }

        public int getOrderId() { return orderId; }
        public String getBuyerUsername() { return buyerUsername; }
        public String getEmail() { return email; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public ArrayList<String> getItems() { return new ArrayList<>(items); }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public String getOrderDate() { return orderDate; }

        public void setStatus(String status) { this.status = status; }
    }
}