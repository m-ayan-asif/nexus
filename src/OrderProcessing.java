import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderProcessing extends JFrame {
    private JTable orderHistoryTable;
    private DefaultTableModel tableModel;
    private JLabel totalOrdersLabel;
    private JLabel totalSpentLabel;
    private JLabel averageOrderLabel;
    private String currentUsername;
    private String currentUserType;
    private CartManager cartManager;

    // Color scheme
    private static final Color BG_COLOR = new Color(10, 10, 15);
    private static final Color CARD_COLOR = new Color(20, 20, 30);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_PURPLE_DARK = new Color(126, 34, 206);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final Color SUBTEXT_COLOR = new Color(156, 163, 175);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(239, 68, 68);

    private ArrayList<Order> orderDatabase;

    public OrderProcessing(String username, String userType) {
        this.currentUsername = username;
        this.currentUserType = userType;
        this.cartManager = CartManager.getInstance();

        initializeDatabase();

        setTitle("NEXUS - Order Processing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeUI();
        setVisible(true);
    }

    private void initializeDatabase() {
        orderDatabase = new ArrayList<>();

        // Show cart items as a preview order (order ID will be assigned on confirm)
        ArrayList<CartManager.CartItem> cartItems = cartManager.getCartItems();
        if (!cartItems.isEmpty()) {
            ArrayList<String> items = new ArrayList<>();
            double totalAmount = 0;
            for (CartManager.CartItem item : cartItems) {
                items.add(item.getProductName());
                totalAmount += item.getTotalPrice();
            }

            // Add 8% tax
            double tax = totalAmount * 0.08;
            double finalTotal = totalAmount + tax;

            Order previewOrder = new Order(
                    0, // Temporary ID, will be replaced on confirm
                    currentUsername,
                    currentUsername + "@email.com",
                    "123 Main St, Apt 4, New York, NY 10001",
                    items,
                    finalTotal,
                    "Processing",
                    getCurrentDate()
            );
            orderDatabase.add(previewOrder);
        }
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(BG_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                GradientPaint gradient = new GradientPaint(
                        getWidth() - 200, 0,
                        new Color(168, 85, 247, 15),
                        getWidth(), 400,
                        new Color(168, 85, 247, 5)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), 400);
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(createStatsPanel(), BorderLayout.NORTH);
        mainPanel.add(createOrderTablePanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Order Processing & History");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);

        JLabel userLabel = new JLabel(currentUserType + " • " + currentUsername);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(SUBTEXT_COLOR);

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(userLabel);

        panel.add(leftPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 15, 0));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 100));

        panel.add(createStatCard("Total Orders", "", totalOrdersLabel = new JLabel("0")));
        panel.add(createStatCard("Total Spent", "", totalSpentLabel = new JLabel("$0.00")));
        panel.add(createStatCard("Average Order", "", averageOrderLabel = new JLabel("$0.00")));

        updateStatistics();

        return panel;
    }

    private JPanel createStatCard(String title, String subtitle, JLabel valueLabel) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.setColor(new Color(168, 85, 247, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(ACCENT_PURPLE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
    }

    private JPanel createOrderTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columnNames = {"Order ID", "Date", "Items", "Total", "Status"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshOrderTable();

        JTable orderTable = new JTable(tableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(orderTable);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbarPanel.setOpaque(false);

        JButton viewDetailsButton = createIconButton("View Details");
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewOrderDetails(orderTable.getSelectedRow());
            }
        });
        toolbarPanel.add(viewDetailsButton);

        JButton refreshButton = createIconButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshOrderTable();
            }
        });
        toolbarPanel.add(refreshButton);

        panel.add(toolbarPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        JButton viewProfileButton = createIconButton("View Profile");
        viewProfileButton.setBackground(ACCENT_PURPLE);
        viewProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUserProfile();
            }
        });
        panel.add(viewProfileButton);

        JButton backToCartButton = createIconButton("Back to Cart");
        backToCartButton.setBackground(new Color(55, 65, 81));
        backToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToShoppingCart();
            }
        });
        panel.add(backToCartButton);

        JButton confirmOrderButton = createIconButton("Confirm Order");
        confirmOrderButton.setBackground(SUCCESS_COLOR);
        confirmOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmOrder();
            }
        });
        panel.add(confirmOrderButton);

        return panel;
    }

    private void refreshOrderTable() {
        tableModel.setRowCount(0);

        for (Order order : orderDatabase) {
            tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getOrderDate(),
                    joinItems(order.getItems()),
                    "$" + String.format("%.2f", order.getTotalAmount()),
                    order.getStatus()
            });
        }

        updateStatistics();
    }

    private void updateStatistics() {
        totalOrdersLabel.setText(String.valueOf(orderDatabase.size()));

        double totalSpent = 0;
        for (Order order : orderDatabase) {
            totalSpent += order.getTotalAmount();
        }
        totalSpentLabel.setText("$" + String.format("%.2f", totalSpent));

        double average = orderDatabase.isEmpty() ? 0 : totalSpent / orderDatabase.size();
        averageOrderLabel.setText("$" + String.format("%.2f", average));
    }

    private void viewOrderDetails(int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to view", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Order order = orderDatabase.get(selectedRow);

        JDialog dialog = new JDialog(this, "Order Details - #" + order.getOrderId(), true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_COLOR);

        addDetailField(panel, "Order ID:", String.valueOf(order.getOrderId()));
        addDetailField(panel, "Date:", order.getOrderDate());
        addDetailField(panel, "Status:", order.getStatus());
        addDetailField(panel, "Delivery Address:", order.getDeliveryAddress());
        addDetailField(panel, "Items:", joinItems(order.getItems()));
        addDetailField(panel, "Total Amount:", "$" + String.format("%.2f", order.getTotalAmount()));

        JButton closeButton = createIconButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_COLOR);
        buttonPanel.add(closeButton);

        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonPanel);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addDetailField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 11));
        labelComponent.setForeground(ACCENT_PURPLE);

        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        valueComponent.setForeground(TEXT_COLOR);

        panel.add(labelComponent);
        panel.add(Box.createVerticalStrut(3));
        panel.add(valueComponent);
        panel.add(Box.createVerticalStrut(10));
    }

    private void goBackToShoppingCart() {
        this.dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ShoppingCart(currentUserType, currentUsername);
            }
        });
    }

    private void openUserProfile() {
        this.dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserProfile(currentUsername, currentUserType);
            }
        });
    }

    private void confirmOrder() {
        ArrayList<CartManager.CartItem> cartItems = cartManager.getCartItems();

        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Please add items before checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double subtotal = cartManager.getSubtotal();
        double tax = subtotal * 0.08;
        double total = subtotal + tax;

        int result = JOptionPane.showConfirmDialog(this,
                "Confirm order placement?\n\n" +
                        "Cart Summary:\n" +
                        "Items: " + cartItems.size() + "\n" +
                        "Subtotal: $" + String.format("%.2f", subtotal) + "\n" +
                        "Tax (8%): $" + String.format("%.2f", tax) + "\n" +
                        "Total: $" + String.format("%.2f", total) + "\n\n" +
                        "Your order will be processed and sent for delivery.",
                "Confirm Order",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Create order from cart items
            ArrayList<String> items = new ArrayList<>();
            for (CartManager.CartItem item : cartItems) {
                items.add(item.getProductName());
            }

            // Get next order ID
            int orderId = 1001;

            // Create order record
            OrderHistory.OrderRecord orderRecord = new OrderHistory.OrderRecord(
                    orderId,
                    currentUsername,
                    currentUsername + "@email.com",
                    "123 Main St, Apt 4, New York, NY 10001",
                    items,
                    total,
                    "Processing",
                    getCurrentDate()
            );

            // Add to global order history
            OrderHistory.getInstance().addOrder(orderRecord);

            // Replace the preview order in local database with confirmed order
            Order confirmedOrder = new Order(
                    orderId,
                    currentUsername,
                    currentUsername + "@email.com",
                    "123 Main St, Apt 4, New York, NY 10001",
                    items,
                    total,
                    "Processing",
                    getCurrentDate()
            );

            // Clear preview orders and add confirmed order
            orderDatabase.clear();
            orderDatabase.add(confirmedOrder);

            JOptionPane.showMessageDialog(this,
                    "Order confirmed successfully!\n\n" +
                            "Order ID: #" + orderId + "\n" +
                            "Total: $" + String.format("%.2f", total) + "\n\n" +
                            "Your order has been placed and will be processed shortly.\n" +
                            "Thank you for shopping with NEXUS!",
                    "Order Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refresh table to show confirmed order
            refreshOrderTable();

            // Clear the cart after order confirmation
            cartManager.clearCart();
        }
    }

    private String joinItems(ArrayList<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setGridColor(new Color(55, 65, 81));
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setRowHeight(26);
        table.getTableHeader().setBackground(new Color(30, 30, 45));
        table.getTableHeader().setForeground(ACCENT_PURPLE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
    }

    private JButton createIconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT_PURPLE);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getBackground().equals(new Color(55, 65, 81)) && !button.getBackground().equals(SUCCESS_COLOR)) {
                    button.setBackground(ACCENT_PURPLE_DARK);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Keep original color
            }
        });

        return button;
    }

    private String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OrderProcessing("buyer1", "Buyer");
            }
        });
    }

    // Order model
    static class Order {
        private int orderId;
        private String buyerUsername;
        private String email;
        private String deliveryAddress;
        private ArrayList<String> items;
        private double totalAmount;
        private String status;
        private String orderDate;

        public Order(int orderId, String buyerUsername, String email, String deliveryAddress, ArrayList<String> items, double totalAmount, String status, String orderDate) {
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
        public ArrayList<String> getItems() { return items; }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public String getOrderDate() { return orderDate; }

        public void setStatus(String status) { this.status = status; }
    }
}