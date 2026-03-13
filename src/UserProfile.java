import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class UserProfile extends JFrame {
    private JTabbedPane tabbedPane;
    private JLabel usernameLabel;
    private JLabel emailLabel;
    private JLabel memberSinceLabel;
    private JLabel accountTypeLabel;
    private JTable orderHistoryTable;
    private DefaultTableModel orderTableModel;
    private JTable addressBookTable;
    private DefaultTableModel addressTableModel;
    private String currentUsername;
    private String currentUserType;
    private CartManager cartManager;
    // Seller order management
    private JTable sellerOrdersTable;
    private DefaultTableModel sellerOrderTableModel;

    // Color scheme
    private static final Color BG_COLOR = new Color(10, 10, 15);
    private static final Color CARD_COLOR = new Color(20, 20, 30);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_PURPLE_DARK = new Color(126, 34, 206);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final Color SUBTEXT_COLOR = new Color(156, 163, 175);
    private static final Color WARNING_COLOR_ACCENT = new Color(244, 82, 82);
    private static final Color WARNING_COLOR = new Color(239, 68, 68);
    private static final Color DEFAULT_COLOR_ACCENT = new Color(40, 46, 58);
    private static final Color DEFAULT_COLOR = new Color(55, 65, 81);

    private ArrayList<Address> addressBook;
    private ArrayList<OrderHistory> orderHistory;

    public UserProfile(String username, String userType) {
        this.currentUsername = username;
        this.currentUserType = userType;
        this.cartManager = CartManager.getInstance();

        initializeData();

        setTitle("NEXUS - User Profile & Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeUI();
        setVisible(true);
    }

    private void initializeData() {
        // Initialize address book
        addressBook = new ArrayList<>();
        addressBook.add(new Address("Home", "123 Main St, Apt 4, New York, NY 10001", "John Doe", "555-0123"));
        addressBook.add(new Address("Work", "456 Corporate Blvd, Suite 500, New York, NY 10002", "John Doe", "555-0124"));

        // Initialize order history
        orderHistory = new ArrayList<>();
    }


    private JPanel createSellerOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel infoLabel = new JLabel("Manage orders containing your products");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(SUBTEXT_COLOR);
        topPanel.add(infoLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton updateStatusButton = createIconButton("Update Status");
        updateStatusButton.addActionListener(e -> updateOrderStatus());
        buttonPanel.add(updateStatusButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"Order ID", "Buyer", "Your Items", "Total", "Status"};
        sellerOrderTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshSellerOrderTable();

        sellerOrdersTable = new JTable(sellerOrderTableModel);
        sellerOrdersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(sellerOrdersTable);

        JScrollPane scrollPane = new JScrollPane(sellerOrdersTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshSellerOrderTable() {
        sellerOrderTableModel.setRowCount(0);
        ArrayList<OrderHistory.OrderRecord> sellerOrders = OrderHistory.getInstance()
                .getSellerOrders(currentUsername);

        for (OrderHistory.OrderRecord order : sellerOrders) {
            // Get only items sold by this seller
            ArrayList<OrderHistory.OrderItem> sellerItems = order.getSellerItems(currentUsername);
            StringBuilder itemsStr = new StringBuilder();
            for (int i = 0; i < sellerItems.size(); i++) {
                itemsStr.append(sellerItems.get(i).getProductName());
                if (i < sellerItems.size() - 1) itemsStr.append(", ");
            }

            Object[] row = {
                    order.getOrderId(),
                    order.getBuyerUsername(),
                    itemsStr.toString(),
                    String.format("$%.2f", order.getTotalAmount()),
                    order.getStatus()
            };
            sellerOrderTableModel.addRow(row);
        }
    }

    private void updateOrderStatus() {
        int selectedRow = sellerOrdersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to update",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) sellerOrderTableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) sellerOrderTableModel.getValueAt(selectedRow, 4);

        // Find the order
        OrderHistory.OrderRecord order = null;
        for (OrderHistory.OrderRecord o : OrderHistory.getInstance().getAllOrders()) {
            if (o.getOrderId() == orderId) {
                order = o;
                break;
            }
        }

        if (order == null) return;

        // Show status options
        String[] statusOptions = {"Processing", "Shipped", "Delivered", "Cancelled"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
                "Update order status:\n\nCurrent: " + currentStatus,
                "Order #" + orderId + " - Update Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                statusOptions,
                currentStatus);

        if (newStatus != null && !newStatus.equals(currentStatus)) {
            order.setStatus(newStatus);
            refreshSellerOrderTable();
            JOptionPane.showMessageDialog(this,
                    "Order status updated to: " + newStatus,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

// ============ SALES DASHBOARD PANEL ============

    private JPanel createSalesDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel infoLabel = new JLabel("Your sales analytics and metrics");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(SUBTEXT_COLOR);
        panel.add(infoLabel, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);

        ArrayList<OrderHistory.OrderRecord> sellerOrders =
                OrderHistory.getInstance().getSellerOrders(currentUsername);
        double totalRevenue = 0;
        int totalOrders = sellerOrders.size();

        for (OrderHistory.OrderRecord order : sellerOrders) {
            if (order.containsSellerProduct(currentUsername)) {
                totalRevenue += order.getTotalAmount();
            }
        }

        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        statsPanel.add(createStatCard("Total Orders", "", String.valueOf(totalOrders)));
        statsPanel.add(createStatCard("Total Revenue", "", String.format("$%.2f", totalRevenue)));
        statsPanel.add(createStatCard("Avg Order Value", "", String.format("$%.2f", averageOrderValue)));

        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String subtitle, String value) {
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

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 24));
        valueLabel.setForeground(TEXT_COLOR);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        return card;
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

        // Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // Profile info section
        mainPanel.add(createProfileInfoPanel(), BorderLayout.NORTH);

        // Tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(CARD_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 12));

        // Add tabs based on user type
        if (currentUserType.equals("Buyer")) {
            tabbedPane.addTab("Order History", createOrderHistoryPanel());
            tabbedPane.addTab("Address Book", createAddressBookPanel());
            tabbedPane.addTab("Account Settings", createAccountSettingsPanel());
        } else if (currentUserType.equals("Seller")) {
            tabbedPane.addTab("Order Management", createSellerOrdersPanel());
            tabbedPane.addTab("Sales Dashboard", createSalesDashboardPanel());
            tabbedPane.addTab("Account Settings", createAccountSettingsPanel());
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom navigation panel
        mainPanel.add(createBottomNavigationPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);

        JLabel subtitleLabel = new JLabel("Manage your account, view orders, and update settings");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(SUBTEXT_COLOR);

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(3));
        leftPanel.add(subtitleLabel);

        panel.add(leftPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createProfileInfoPanel() {
        JPanel panel = new JPanel() {
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
        panel.setOpaque(false);
        panel.setLayout(new GridLayout(1, 4, 15, 0));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(0, 100));

        panel.add(createProfileField("Username:", currentUsername, usernameLabel = new JLabel()));
        panel.add(createProfileField("Email:", currentUsername + "@email.com", emailLabel = new JLabel()));
        panel.add(createProfileField("Account Type:", currentUserType, accountTypeLabel = new JLabel()));
        panel.add(createProfileField("Member Since:", "January 2024", memberSinceLabel = new JLabel()));

        return panel;
    }

    private JPanel createProfileField(String label, String value, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 11));
        labelComponent.setForeground(SUBTEXT_COLOR);

        valueLabel.setText(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 14));
        valueLabel.setForeground(ACCENT_PURPLE);

        panel.add(labelComponent);
        panel.add(Box.createVerticalStrut(5));
        panel.add(valueLabel);

        return panel;
    }

    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel infoLabel = new JLabel("Your recent orders and tracking information");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(SUBTEXT_COLOR);
        panel.add(infoLabel, BorderLayout.NORTH);

        String[] columnNames = {"Order ID", "Date", "Items", "Total", "Status"};
        orderTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshOrderTable();

        orderHistoryTable = new JTable(orderTableModel);
        orderHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(orderHistoryTable);

        JScrollPane scrollPane = new JScrollPane(orderHistoryTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbarPanel.setOpaque(false);

        JButton viewButton = createIconButton("View Details");
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewOrderDetails();
            }
        });
        toolbarPanel.add(viewButton);

        JButton trackButton = createIconButton("Track Order");
        trackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trackOrder();
            }
        });
        toolbarPanel.add(trackButton);

        panel.add(toolbarPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddressBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JLabel infoLabel = new JLabel("Manage your saved delivery addresses");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(SUBTEXT_COLOR);
        panel.add(infoLabel, BorderLayout.NORTH);

        String[] columnNames = {"Address Type", "Address", "Contact Name", "Phone"};
        addressTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshAddressTable();

        addressBookTable = new JTable(addressTableModel);
        addressBookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(addressBookTable);

        JScrollPane scrollPane = new JScrollPane(addressBookTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        // Toolbar
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbarPanel.setOpaque(false);

        JButton addButton = createIconButton("+ Add Address");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAddAddressDialog();
            }
        });
        toolbarPanel.add(addButton);

        JButton editButton = createIconButton("Edit");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editAddress();
            }
        });
        toolbarPanel.add(editButton);

        JButton deleteButton = createIconButton("Delete");
        deleteButton.setBackground(WARNING_COLOR);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAddress();
            }
        });
        toolbarPanel.add(deleteButton);

        panel.add(toolbarPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAccountSettingsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 0, 15, 0));

        // Password section
        JPanel passwordSection = createSettingSection("Change Password");

        JLabel currentPassLabel = new JLabel("Current Password:");
        currentPassLabel.setFont(new Font("Arial", Font.BOLD, 11));
        currentPassLabel.setForeground(ACCENT_PURPLE);
        passwordSection.add(currentPassLabel);

        JPasswordField currentPassField = new JPasswordField();
        styleSettingField(currentPassField);
        passwordSection.add(currentPassField);
        passwordSection.add(Box.createVerticalStrut(10));

        JLabel newPassLabel = new JLabel("New Password:");
        newPassLabel.setFont(new Font("Arial", Font.BOLD, 11));
        newPassLabel.setForeground(ACCENT_PURPLE);
        passwordSection.add(newPassLabel);

        JPasswordField newPassField = new JPasswordField();
        styleSettingField(newPassField);
        passwordSection.add(newPassField);
        passwordSection.add(Box.createVerticalStrut(10));

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPassLabel.setFont(new Font("Arial", Font.BOLD, 11));
        confirmPassLabel.setForeground(ACCENT_PURPLE);
        passwordSection.add(confirmPassLabel);

        JPasswordField confirmPassField = new JPasswordField();
        styleSettingField(confirmPassField);
        passwordSection.add(confirmPassField);
        passwordSection.add(Box.createVerticalStrut(15));

        JButton changePassButton = createIconButton("Update Password");
        changePassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePassword(currentPassField, newPassField, confirmPassField);
            }
        });
        passwordSection.add(changePassButton);

        panel.add(passwordSection);
        panel.add(Box.createVerticalStrut(20));

        // Email notification preferences
        JPanel notificationSection = createSettingSection("Notifications");

        JCheckBox emailOrderCheckbox = new JCheckBox("Email notifications for order updates");
        emailOrderCheckbox.setSelected(true);
        emailOrderCheckbox.setOpaque(false);
        emailOrderCheckbox.setForeground(TEXT_COLOR);
        emailOrderCheckbox.setFont(new Font("Arial", Font.PLAIN, 11));
        notificationSection.add(emailOrderCheckbox);

        notificationSection.add(Box.createVerticalStrut(10));

        JCheckBox emailPromoCheckbox = new JCheckBox("Email notifications for promotions and deals");
        emailPromoCheckbox.setSelected(false);
        emailPromoCheckbox.setOpaque(false);
        emailPromoCheckbox.setForeground(TEXT_COLOR);
        emailPromoCheckbox.setFont(new Font("Arial", Font.PLAIN, 11));
        notificationSection.add(emailPromoCheckbox);

        notificationSection.add(Box.createVerticalStrut(15));

        JButton saveNotificationsButton = createIconButton("Save Preferences");
        saveNotificationsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(UserProfile.this, "Notification preferences updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        notificationSection.add(saveNotificationsButton);

        panel.add(notificationSection);
        panel.add(Box.createVerticalStrut(20));

        // Danger zone
        JPanel dangerSection = createSettingSection("Account Management");

        JButton deactivateButton = createIconButton("Deactivate Account");
        deactivateButton.setBackground(WARNING_COLOR);
        deactivateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(UserProfile.this,
                        "Are you sure you want to deactivate your account? This action cannot be undone.",
                        "Confirm Deactivation",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(UserProfile.this, "Account deactivated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        dangerSection.add(deactivateButton);

        panel.add(dangerSection);
        panel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(BG_COLOR);

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setOpaque(false);
        wrapperPanel.add(scrollPane, BorderLayout.CENTER);

        return wrapperPanel;
    }

    private JPanel createSettingSection(String title) {
        JPanel panel = new JPanel() {
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
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setForeground(ACCENT_PURPLE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        return panel;
    }

    private void styleSettingField(JComponent field) {
        if (field instanceof JPasswordField) {
            ((JPasswordField) field).setBackground(new Color(30, 30, 45));
            ((JPasswordField) field).setForeground(TEXT_COLOR);
            ((JPasswordField) field).setFont(new Font("Arial", Font.PLAIN, 12));
            ((JPasswordField) field).setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        } else if (field instanceof JTextField) {
            ((JTextField) field).setBackground(new Color(30, 30, 45));
            ((JTextField) field).setForeground(TEXT_COLOR);
            ((JTextField) field).setFont(new Font("Arial", Font.PLAIN, 12));
            ((JTextField) field).setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        }
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    }

    private JPanel createBottomNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        JButton backButton = createIconButton("Back to Shopping");
        backButton.setBackground(DEFAULT_COLOR);
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToProductCatalog();
            }
        });
        panel.add(backButton);

        JButton logoutButton = createIconButton("Logout");
        logoutButton.setBackground(WARNING_COLOR);
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        panel.add(logoutButton);

        return panel;
    }

    private void refreshOrderTable() {
        orderTableModel.setRowCount(0);

        if (currentUserType.equals("Buyer")) {
            // Get orders from global OrderHistory
            ArrayList<OrderHistory.OrderRecord> userOrders = OrderHistory.getInstance().getUserOrders(currentUsername);
            for (OrderHistory.OrderRecord order : userOrders) {
                StringBuilder itemsStr = new StringBuilder();
                ArrayList<String> items = order.getItemsAsStrings();
                for (int i = 0; i < items.size(); i++) {
                    itemsStr.append(items.get(i));
                    if (i < items.size() - 1) {
                        itemsStr.append(", ");
                    }
                }

                orderTableModel.addRow(new Object[]{
                        order.getOrderId(),
                        order.getOrderDate(),
                        itemsStr.toString(),
                        "$" + String.format("%.2f", order.getTotalAmount()),
                        order.getStatus()
                });
            }
        }
    }

    private void refreshAddressTable() {
        addressTableModel.setRowCount(0);
        for (Address address : addressBook) {
            addressTableModel.addRow(new Object[]{
                    address.getAddressType(),
                    address.getAddressLine(),
                    address.getContactName(),
                    address.getPhone()
            });
        }
    }

    private void viewOrderDetails() {
        int selectedRow = orderHistoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<OrderHistory.OrderRecord> userOrders = OrderHistory.getInstance().getUserOrders(currentUsername);
        if (selectedRow >= userOrders.size()) {
            return;
        }

        OrderHistory.OrderRecord order = userOrders.get(selectedRow);

        StringBuilder itemsStr = new StringBuilder();
        for (String item : order.getItemsAsStrings()) {
            itemsStr.append(item).append("\n");
        }

        String details = "Order #" + order.getOrderId() + "\n\n" +
                "Date: " + order.getOrderDate() + "\n" +
                "Items:\n" + itemsStr.toString() + "\n" +
                "Total: $" + String.format("%.2f", order.getTotalAmount()) + "\n" +
                "Status: " + order.getStatus();

        JOptionPane.showMessageDialog(this, details, "Order Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void trackOrder() {
        int selectedRow = orderHistoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<OrderHistory.OrderRecord> userOrders = OrderHistory.getInstance().getUserOrders(currentUsername);
        if (selectedRow >= userOrders.size()) {
            return;
        }

        OrderHistory.OrderRecord order = userOrders.get(selectedRow);
        String tracking = "Order #" + order.getOrderId() + "\n\n" +
                "Current Status: " + order.getStatus() + "\n\n" +
                "Estimated Delivery: 2-3 business days\n" +
                "Tracking Number: NEXUS" + order.getOrderId();

        JOptionPane.showMessageDialog(this, tracking, "Track Order", JOptionPane.INFORMATION_MESSAGE);
    }

    private void openAddAddressDialog() {
        JDialog dialog = new JDialog(this, "Add New Address", true);
        dialog.setSize(500, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(CARD_COLOR);

        JLabel typeLabel = new JLabel("Address Type:");
        typeLabel.setFont(new Font("Arial", Font.BOLD, 11));
        typeLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(typeLabel);

        JTextField typeField = new JTextField("Home");
        styleSettingField(typeField);
        formPanel.add(typeField);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel addressLabel = new JLabel("Address:");
        addressLabel.setFont(new Font("Arial", Font.BOLD, 11));
        addressLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(addressLabel);

        JTextField addressField = new JTextField();
        styleSettingField(addressField);
        formPanel.add(addressField);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel nameLabel = new JLabel("Contact Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(nameLabel);

        JTextField nameField = new JTextField();
        styleSettingField(nameField);
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Arial", Font.BOLD, 11));
        phoneLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(phoneLabel);

        JTextField phoneField = new JTextField();
        styleSettingField(phoneField);
        formPanel.add(phoneField);
        formPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton saveButton = createIconButton("Save");
        JButton cancelButton = createIconButton("Cancel");
        cancelButton.setBackground(new Color(55, 65, 81));

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressBook.add(new Address(
                        typeField.getText(),
                        addressField.getText(),
                        nameField.getText(),
                        phoneField.getText()
                ));
                refreshAddressTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(UserProfile.this, "Address added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        formPanel.add(buttonPanel);

        dialog.add(formPanel);
        dialog.setVisible(true);
    }

    private void editAddress() {
        JOptionPane.showMessageDialog(this, "Edit functionality - Select an address and modify details", "Edit Address", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteAddress() {
        int selectedRow = addressBookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an address to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        addressBook.remove(selectedRow);
        refreshAddressTable();
        JOptionPane.showMessageDialog(this, "Address deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updatePassword(JPasswordField currentPass, JPasswordField newPass, JPasswordField confirmPass) {
        String current = new String(currentPass.getPassword());
        String newPassword = new String(newPass.getPassword());
        String confirm = new String(confirmPass.getPassword());

        if (current.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all password fields", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match", "Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters", "Invalid Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        currentPass.setText("");
        newPass.setText("");
        confirmPass.setText("");
    }

    private void goBackToProductCatalog() {
        this.dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ProductCatalog(currentUserType, currentUsername);
            }
        });
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new LoginScreen();
                }
            });
        }
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

        if (text.equals("Logout") || text.equals("Deactivate Account") || text.equals("Delete")) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(WARNING_COLOR_ACCENT);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(WARNING_COLOR);
                }
            });
        }
        else if (text.equals("View Details") || text.equals("Update Password") || text.equals("Save Preferences") || text.equals("Track Order") || text.equals("+ Add Address") || text.equals("Edit") || text.equals("Update Status")) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(ACCENT_PURPLE_DARK);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(ACCENT_PURPLE);
                }
            });
        }
        else {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(DEFAULT_COLOR_ACCENT);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(DEFAULT_COLOR);
                }
            });
        }

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserProfile("buyer1", "Buyer");
            }
        });
    }

    // Address model
    static class Address {
        private String addressType;
        private String addressLine;
        private String contactName;
        private String phone;

        public Address(String addressType, String addressLine, String contactName, String phone) {
            this.addressType = addressType;
            this.addressLine = addressLine;
            this.contactName = contactName;
            this.phone = phone;
        }

        public String getAddressType() { return addressType; }
        public String getAddressLine() { return addressLine; }
        public String getContactName() { return contactName; }
        public String getPhone() { return phone; }
    }
}