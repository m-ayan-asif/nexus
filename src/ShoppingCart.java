import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ShoppingCart extends JFrame {
    private JTable cartTable;
    private DefaultTableModel tableModel;
    private JLabel subtotalLabel;
    private JLabel taxLabel;
    private JLabel totalLabel;
    private JButton updateButton;
    private JButton removeButton;
    private JButton checkoutButton;
    private JButton continueShoppingButton;
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

    private static final double TAX_RATE = 0.08; // 8% tax

    public ShoppingCart(String userType, String username) {
        this.currentUserType = userType;
        this.currentUsername = username;
        this.cartManager = CartManager.getInstance();

        // For sellers, show a message and return to ProductCatalog
        if (userType.equals("Seller")) {
            JOptionPane.showMessageDialog(null,
                    "ShoppingCart is for buyers only.\n" +
                            "Sellers can manage products in the Product Catalog.\n" +
                            "Returning to Product Catalog...",
                    "Seller Mode",
                    JOptionPane.INFORMATION_MESSAGE);

            new ProductCatalog(userType, username);
            return;
        }

        setTitle("NEXUS - Shopping Cart");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setResizable(true);

        subtotalLabel = new JLabel("$0.00");
        taxLabel = new JLabel("$0.00");
        totalLabel = new JLabel("$0.00");

        initializeUI();
        updatePrices();
        setVisible(true);
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

        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);

        contentPanel.add(createCartTablePanel(), BorderLayout.CENTER);
        contentPanel.add(createSummaryPanel(), BorderLayout.EAST);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(createBottomButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Shopping Cart");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);

        JLabel userLabel = new JLabel("Buyer • " + currentUsername);
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

    private JPanel createCartTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columnNames = {"Product ID", "Product Name", "Unit Price", "Quantity", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };

        refreshCartTable();

        cartTable = new JTable(tableModel);
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(cartTable);

        cartTable.getModel().addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (e.getColumn() == 3) {
                    int row = e.getFirstRow();
                    try {
                        int newQuantity = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
                        if (newQuantity > 0) {
                            ArrayList<CartManager.CartItem> items = cartManager.getCartItems();
                            if (row < items.size()) {
                                items.get(row).setQuantity(newQuantity);
                                refreshCartTable();
                            }
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(ShoppingCart.this, "Please enter a valid quantity", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        refreshCartTable();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbarPanel.setOpaque(false);

        updateButton = createIconButton("Update Cart");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCartTable();
                JOptionPane.showMessageDialog(ShoppingCart.this, "Cart updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        toolbarPanel.add(updateButton);

        removeButton = createIconButton("Remove Item");
        removeButton.setBackground(WARNING_COLOR);
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeItem();
            }
        });
        toolbarPanel.add(removeButton);

        panel.add(toolbarPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createSummaryPanel() {
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
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setPreferredSize(new Dimension(250, 0));

        JLabel summaryTitle = new JLabel("ORDER SUMMARY");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 14));
        summaryTitle.setForeground(ACCENT_PURPLE);
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(summaryTitle);

        panel.add(Box.createVerticalStrut(15));

        JPanel subtotalPanel = createSummaryRow("Subtotal:", subtotalLabel);
        panel.add(subtotalPanel);

        JPanel taxPanel = createSummaryRow("Tax (8%):", taxLabel);
        panel.add(taxPanel);

        panel.add(Box.createVerticalStrut(10));

        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(55, 65, 81));
        panel.add(separator);

        panel.add(Box.createVerticalStrut(10));

        JPanel totalPanel = createSummaryRow("TOTAL:", totalLabel);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(SUCCESS_COLOR);
        panel.add(totalPanel);

        panel.add(Box.createVerticalStrut(20));

        JLabel itemsCountLabel = new JLabel("Items: " + cartManager.getItemCount());
        itemsCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        itemsCountLabel.setForeground(SUBTEXT_COLOR);
        panel.add(itemsCountLabel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createSummaryRow(String label, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 12));
        labelComponent.setForeground(SUBTEXT_COLOR);

        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        valueLabel.setForeground(TEXT_COLOR);

        panel.add(labelComponent, BorderLayout.WEST);
        panel.add(valueLabel, BorderLayout.EAST);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        return panel;
    }

    private JPanel createBottomButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        JButton profileButton = createIconButton("My Profile");
        profileButton.setBackground(ACCENT_PURPLE);
        profileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUserProfile();
            }
        });
        panel.add(profileButton);

        continueShoppingButton = createIconButton("Continue Shopping");
        continueShoppingButton.setBackground(new Color(55, 65, 81));
        continueShoppingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBackToProductCatalog();
            }
        });
        panel.add(continueShoppingButton);

        checkoutButton = createIconButton("Proceed to Checkout");
        checkoutButton.setBackground(SUCCESS_COLOR);
        checkoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                proceedToCheckout();
            }
        });
        panel.add(checkoutButton);

        return panel;
    }

    private void refreshCartTable() {
        tableModel.setRowCount(0);
        ArrayList<CartManager.CartItem> items = cartManager.getCartItems();
        for (CartManager.CartItem item : items) {
            tableModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getProductName(),
                    "$" + String.format("%.2f", item.getUnitPrice()),
                    item.getQuantity(),
                    "$" + String.format("%.2f", item.getTotalPrice())
            });
        }
        updatePrices();
    }

    private void updatePrices() {
        double subtotal = cartManager.getSubtotal();
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        subtotalLabel.setText("$" + String.format("%.2f", subtotal));
        taxLabel.setText("$" + String.format("%.2f", tax));
        totalLabel.setText("$" + String.format("%.2f", total));
    }

    private void removeItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to remove", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        cartManager.removeItem(selectedRow);
        refreshCartTable();
        JOptionPane.showMessageDialog(this, "Item removed from cart", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void proceedToCheckout() {
        if (cartManager.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Please add items before checkout.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = cartManager.getSubtotal() * (1 + TAX_RATE);
        int result = JOptionPane.showConfirmDialog(this,
                "Proceeding to checkout...\n\n" +
                        "Cart Summary:\n" +
                        "Items: " + cartManager.getItemCount() + "\n" +
                        "Total: $" + String.format("%.2f", total) + "\n\n" +
                        "Click OK to continue to Order Processing",
                "Checkout Confirmation",
                JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            openOrderProcessing();
        }
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

    private void openUserProfile() {
        this.dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserProfile(currentUsername, currentUserType);
            }
        });
    }

    private void openOrderProcessing() {
        this.dispose();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OrderProcessing(currentUsername, currentUserType);
            }
        });
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setGridColor(new Color(55, 65, 81));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(30, 30, 45));
        table.getTableHeader().setForeground(ACCENT_PURPLE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ShoppingCart("Buyer", "buyer1");
            }
        });
    }
}