import java.io.Serializable;
import java.util.*;

/**
 * OrderHistory manages shared order data with seller tracking and file persistence.
 */
public class OrderHistory {
    private static OrderHistory instance;
    private ArrayList<OrderRecord> orders;
    private static int nextOrderId = 1001;

    OrderHistory() {
        ArrayList<OrderRecord> saved = DatabaseManager.loadOrders();
        if (saved.isEmpty()) {
            this.orders = new ArrayList<>();
        } else {
            this.orders = saved;
            // Restore nextOrderId from saved data
            for (OrderRecord r : this.orders) {
                if (r.getOrderId() >= nextOrderId) {
                    nextOrderId = r.getOrderId() + 1;
                }
            }
        }
    }

    public static OrderHistory getInstance() {
        if (instance == null) {
            instance = new OrderHistory();
        }
        return instance;
    }

    public void addOrder(OrderRecord order) {
        orders.add(order);
        DatabaseManager.saveOrders(orders);
    }

    public ArrayList<OrderRecord> getUserOrders(String username) {
        ArrayList<OrderRecord> userOrders = new ArrayList<>();
        for (OrderRecord order : orders) {
            if (order.getBuyerUsername().equals(username)) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    public ArrayList<OrderRecord> getSellerOrders(String sellerUsername) {
        ArrayList<OrderRecord> sellerOrders = new ArrayList<>();
        for (OrderRecord order : orders) {
            if (order.containsSellerProduct(sellerUsername)) {
                sellerOrders.add(order);
            }
        }
        return sellerOrders;
    }

    public ArrayList<OrderRecord> getAllOrders() {
        return new ArrayList<>(orders);
    }

    public void clearOrders() {
        orders.clear();
        nextOrderId = 1001;
    }

    public static int getNextOrderId() {
        return nextOrderId++;
    }

    // Order Record with seller tracking and payment method
    public static class OrderRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        private int orderId;
        private String buyerUsername;
        private String email;
        private String deliveryAddress;
        private ArrayList<OrderItem> items;
        private double totalAmount;
        private String status;
        private String orderDate;
        private String paymentMethod;

        public OrderRecord(int orderId, String buyerUsername, String email, String deliveryAddress,
                           ArrayList<OrderItem> items, double totalAmount, String status, String orderDate) {
            this.orderId = orderId;
            this.buyerUsername = buyerUsername;
            this.email = email;
            this.deliveryAddress = deliveryAddress;
            this.items = new ArrayList<>(items);
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
            this.paymentMethod = "Cash on Delivery";
        }

        public OrderRecord(int orderId, String buyerUsername, String email, String deliveryAddress,
                           ArrayList<OrderItem> items, double totalAmount, String status, String orderDate,
                           String paymentMethod) {
            this(orderId, buyerUsername, email, deliveryAddress, items, totalAmount, status, orderDate);
            this.paymentMethod = paymentMethod;
        }

        // Backwards-compatibility constructor (string items list)
        public OrderRecord(int orderId, String buyerUsername, String email, String deliveryAddress,
                           ArrayList<String> items, double totalAmount, String status, String orderDate, int val) {
            this.orderId = orderId;
            this.buyerUsername = buyerUsername;
            this.email = email;
            this.deliveryAddress = deliveryAddress;
            this.items = new ArrayList<>();
            for (String item : items) this.items.add(new OrderItem(item, "Unknown"));
            this.totalAmount = totalAmount;
            this.status = status;
            this.orderDate = orderDate;
            this.paymentMethod = "Cash on Delivery";
        }

        public boolean containsSellerProduct(String sellerUsername) {
            for (OrderItem item : items) {
                if (item.getSellerUsername().equals(sellerUsername)) return true;
            }
            return false;
        }

        public ArrayList<OrderItem> getSellerItems(String sellerUsername) {
            ArrayList<OrderItem> sellerItems = new ArrayList<>();
            for (OrderItem item : items) {
                if (item.getSellerUsername().equals(sellerUsername)) sellerItems.add(item);
            }
            return sellerItems;
        }

        public ArrayList<String> getItemsAsStrings() {
            ArrayList<String> itemStrings = new ArrayList<>();
            for (OrderItem item : items) itemStrings.add(item.getProductName());
            return itemStrings;
        }

        public int getOrderId() { return orderId; }
        public String getBuyerUsername() { return buyerUsername; }
        public String getEmail() { return email; }
        public String getDeliveryAddress() { return deliveryAddress; }
        public ArrayList<OrderItem> getItems() { return new ArrayList<>(items); }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public String getOrderDate() { return orderDate; }
        public String getPaymentMethod() { return paymentMethod != null ? paymentMethod : "Cash on Delivery"; }

        public void setStatus(String status) { this.status = status; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }

    // Order Item with seller tracking
    public static class OrderItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private String productName;
        private String sellerUsername;

        public OrderItem(String productName, String sellerUsername) {
            this.productName = productName;
            this.sellerUsername = sellerUsername;
        }

        public String getProductName() { return productName; }
        public String getSellerUsername() { return sellerUsername; }
        public void setSellerUsername(String sellerUsername) { this.sellerUsername = sellerUsername; }
    }
}
