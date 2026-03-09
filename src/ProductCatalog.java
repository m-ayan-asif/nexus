import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ProductCatalog extends JFrame {
    private JTable productsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addProductButton;
    private JButton editProductButton;
    private JButton deleteProductButton;
    private JButton viewDetailsButton;
    private JButton addToCartButton;
    private JButton viewCartButton;
    private String currentUserType;
    private String currentUsername;
    private CartManager cartManager;
    private ProductManager productManager;

    // Color scheme
    private static final Color BG_COLOR = new Color(10, 10, 15);
    private static final Color CARD_COLOR = new Color(20, 20, 30);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_PURPLE_DARK = new Color(126, 34, 206);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final Color SUBTEXT_COLOR = new Color(156, 163, 175);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(239, 68, 68);

    private ArrayList<Product> productDatabase;

    public ProductCatalog(String userType, String username) {
        this.currentUserType = userType;
        this.currentUsername = username;
        this.cartManager = CartManager.getInstance();
        this.cartManager.setUsername(username);
        this.cartManager.setUserType(userType);
        this.productManager = ProductManager.getInstance();

        setTitle("NEXUS - Product Catalog");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        initializeUI();
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
        mainPanel.add(createToolbarPanel(), BorderLayout.SOUTH);
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Product Catalog");
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

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        searchField = new JTextField(20);
        styleTextField(searchField, "Search products...");
        panel.add(searchField);

        viewDetailsButton = createIconButton("View Details");
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewProductDetails();
            }
        });
        panel.add(viewDetailsButton);

        // Buyer buttons
        if (currentUserType.equals("Buyer")) {
            addToCartButton = createIconButton("+ Add to Cart");
            addToCartButton.setBackground(SUCCESS_COLOR);
            addToCartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addToCart();
                }
            });
            panel.add(addToCartButton);

            viewCartButton = createIconButton("View Cart (" + cartManager.getItemCount() + ")");
            viewCartButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openShoppingCart();
                }
            });
            panel.add(viewCartButton);

            JButton profileButton = createIconButton("My Profile");
            profileButton.setBackground(ACCENT_PURPLE);
            profileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openUserProfile();
                }
            });
            panel.add(profileButton);
        }

        // Seller buttons
        if (currentUserType.equals("Seller")) {
            addProductButton = createIconButton("+ Add Product");
            addProductButton.setBackground(SUCCESS_COLOR);
            addProductButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openAddProductDialog();
                }
            });
            panel.add(addProductButton);

            editProductButton = createIconButton("Edit");
            editProductButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openEditProductDialog();
                }
            });
            panel.add(editProductButton);

            deleteProductButton = createIconButton("Delete");
            deleteProductButton.setBackground(WARNING_COLOR);
            deleteProductButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteProduct();
                }
            });
            panel.add(deleteProductButton);

            JButton statsButton = createIconButton("Stats");
            statsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    viewSellerStats();
                }
            });
            panel.add(statsButton);

            JButton profileButton = createIconButton("My Profile");
            profileButton.setBackground(ACCENT_PURPLE);
            profileButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openUserProfile();
                }
            });
            panel.add(profileButton);
        }

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String[] columnNames;
        if (currentUserType.equals("Seller")) {
            columnNames = new String[]{"ID", "Product Name", "Price", "Stock", "Category", "Status"};
        } else {
            columnNames = new String[]{"ID", "Product Name", "Seller", "Price", "Stock", "Category"};
        }

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        refreshTable();

        productsTable = new JTable(tableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(productsTable);

        JScrollPane scrollPane = new JScrollPane(productsTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ArrayList<ProductManager.Product> allProducts = productManager.getAllProducts();

        if (currentUserType.equals("Seller")) {
            // Show only seller's products
            for (ProductManager.Product product : allProducts) {
                if (product.getSeller().equals(currentUsername)) {
                    tableModel.addRow(new Object[]{
                            product.getId(),
                            product.getName(),
                            "$" + String.format("%.2f", product.getPrice()),
                            product.getStock(),
                            product.getCategory(),
                            "Active"
                    });
                }
            }
        } else {
            // Show all products for buyers
            for (ProductManager.Product product : allProducts) {
                tableModel.addRow(new Object[]{
                        product.getId(),
                        product.getName(),
                        product.getSeller(),
                        "$" + String.format("%.2f", product.getPrice()),
                        product.getStock(),
                        product.getCategory()
                });
            }
        }
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

    private void styleTextField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(SUBTEXT_COLOR);
        field.setBackground(new Color(30, 30, 45));
        field.setCaretColor(ACCENT_PURPLE);
        field.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(SUBTEXT_COLOR);
                }
            }
        });
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
                button.setBackground(ACCENT_PURPLE_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT_PURPLE);
            }
        });

        return button;
    }

    private void addToCart() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to cart", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<ProductManager.Product> allProducts = productManager.getAllProducts();
        if (selectedRow >= allProducts.size()) {
            return;
        }

        ProductManager.Product selectedProduct = allProducts.get(selectedRow);

        JPanel quantityPanel = new JPanel();
        quantityPanel.setLayout(new BoxLayout(quantityPanel, BoxLayout.Y_AXIS));
        quantityPanel.setBackground(CARD_COLOR);

        JLabel label = new JLabel("Enter quantity for: " + selectedProduct.getName());
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(TEXT_COLOR);

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, selectedProduct.getStock(), 1));
        quantitySpinner.setPreferredSize(new Dimension(100, 30));

        quantityPanel.add(label);
        quantityPanel.add(Box.createVerticalStrut(10));
        quantityPanel.add(quantitySpinner);

        int result = JOptionPane.showConfirmDialog(this, quantityPanel, "Add to Cart", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            int quantity = (Integer) quantitySpinner.getValue();
            CartManager.CartItem cartItem = new CartManager.CartItem(
                    selectedProduct.getId(),
                    selectedProduct.getName(),
                    selectedProduct.getPrice(),
                    quantity
            );
            cartManager.addItem(cartItem);

            String message = "Added " + quantity + " x " + selectedProduct.getName() + " to cart!\n\n" +
                    "Price: $" + String.format("%.2f", selectedProduct.getPrice()) + "\n" +
                    "Subtotal: $" + String.format("%.2f", selectedProduct.getPrice() * quantity);
            JOptionPane.showMessageDialog(this, message, "Added to Cart", JOptionPane.INFORMATION_MESSAGE);

            viewCartButton.setText("View Cart (" + cartManager.getItemCount() + ")");
        }
    }

    private void openShoppingCart() {
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

    private void viewProductDetails() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to view", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product product = productDatabase.get(selectedRow);

        JDialog dialog = new JDialog(this, "Product Details", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(CARD_COLOR);

        addDetailField(panel, "Product ID:", String.valueOf(product.getId()));
        addDetailField(panel, "Name:", product.getName());
        addDetailField(panel, "Seller:", product.getSeller());
        addDetailField(panel, "Price:", "$" + String.format("%.2f", product.getPrice()));
        addDetailField(panel, "Stock:", String.valueOf(product.getStock()));
        addDetailField(panel, "Category:", product.getCategory());
        addDetailField(panel, "Description:", product.getDescription());

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

    private void openAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(CARD_COLOR);

        // Product name
        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(nameLabel);

        JTextField nameField = new JTextField();
        styleField(nameField);
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(10));

        // Price
        JLabel priceLabel = new JLabel("Price ($):");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 11));
        priceLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(priceLabel);

        JTextField priceField = new JTextField();
        styleField(priceField);
        formPanel.add(priceField);
        formPanel.add(Box.createVerticalStrut(10));

        // Stock
        JLabel stockLabel = new JLabel("Stock Quantity:");
        stockLabel.setFont(new Font("Arial", Font.BOLD, 11));
        stockLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(stockLabel);

        JTextField stockField = new JTextField();
        styleField(stockField);
        formPanel.add(stockField);
        formPanel.add(Box.createVerticalStrut(10));

        // Category
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 11));
        categoryLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(categoryLabel);

        String[] categories = {"Electronics", "Accessories", "Office", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setBackground(new Color(30, 30, 45));
        categoryCombo.setForeground(TEXT_COLOR);
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(categoryCombo);
        formPanel.add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 11));
        descLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(descLabel);

        JTextArea descArea = new JTextArea(4, 30);
        descArea.setBackground(new Color(30, 30, 45));
        descArea.setForeground(TEXT_COLOR);
        descArea.setFont(new Font("Arial", Font.PLAIN, 11));
        descArea.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        JScrollPane scrollPane = new JScrollPane(descArea);
        formPanel.add(scrollPane);
        formPanel.add(Box.createVerticalStrut(15));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton saveButton = createIconButton("Save");
        JButton cancelButton = createIconButton("Cancel");
        cancelButton.setBackground(new Color(55, 65, 81));

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = nameField.getText();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    String category = (String) categoryCombo.getSelectedItem();
                    String description = descArea.getText();

                    if (name.isEmpty() || price <= 0 || stock < 0) {
                        JOptionPane.showMessageDialog(dialog, "Please fill in all fields correctly", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int newId = productManager.getNextProductId();
                    ProductManager.Product newProduct = new ProductManager.Product(newId, name, currentUsername, price, description, stock, category);
                    productManager.addProduct(newProduct);
                    refreshTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(ProductCatalog.this, "Product added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter valid price and stock", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
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

        JScrollPane mainScroll = new JScrollPane(formPanel);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setBackground(BG_COLOR);

        dialog.add(mainScroll);
        dialog.setVisible(true);
    }

    private void openEditProductDialog() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<ProductManager.Product> sellerProducts = productManager.getSellerProducts(currentUsername);
        if (selectedRow >= sellerProducts.size()) {
            return;
        }

        ProductManager.Product selectedProduct = sellerProducts.get(selectedRow);

        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        formPanel.setBackground(CARD_COLOR);

        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(nameLabel);

        JTextField nameField = new JTextField(selectedProduct.getName());
        styleField(nameField);
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel priceLabel = new JLabel("Price ($):");
        priceLabel.setFont(new Font("Arial", Font.BOLD, 11));
        priceLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(priceLabel);

        JTextField priceField = new JTextField(String.valueOf(selectedProduct.getPrice()));
        styleField(priceField);
        formPanel.add(priceField);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel stockLabel = new JLabel("Stock Quantity:");
        stockLabel.setFont(new Font("Arial", Font.BOLD, 11));
        stockLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(stockLabel);

        JTextField stockField = new JTextField(String.valueOf(selectedProduct.getStock()));
        styleField(stockField);
        formPanel.add(stockField);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 11));
        categoryLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(categoryLabel);

        String[] categories = {"Electronics", "Accessories", "Office", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        categoryCombo.setSelectedItem(selectedProduct.getCategory());
        categoryCombo.setBackground(new Color(30, 30, 45));
        categoryCombo.setForeground(TEXT_COLOR);
        categoryCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(categoryCombo);
        formPanel.add(Box.createVerticalStrut(10));

        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 11));
        descLabel.setForeground(ACCENT_PURPLE);
        formPanel.add(descLabel);

        JTextArea descArea = new JTextArea(selectedProduct.getDescription(), 4, 30);
        descArea.setBackground(new Color(30, 30, 45));
        descArea.setForeground(TEXT_COLOR);
        descArea.setFont(new Font("Arial", Font.PLAIN, 11));
        descArea.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        JScrollPane scrollPane = new JScrollPane(descArea);
        formPanel.add(scrollPane);
        formPanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(CARD_COLOR);

        JButton saveButton = createIconButton("Save Changes");
        JButton cancelButton = createIconButton("Cancel");
        cancelButton.setBackground(new Color(55, 65, 81));

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    selectedProduct.setName(nameField.getText());
                    selectedProduct.setPrice(Double.parseDouble(priceField.getText()));
                    selectedProduct.setStock(Integer.parseInt(stockField.getText()));
                    selectedProduct.setCategory((String) categoryCombo.getSelectedItem());
                    selectedProduct.setDescription(descArea.getText());

                    productManager.updateProduct(selectedProduct);
                    refreshTable();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(ProductCatalog.this, "Product updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Please enter valid price and stock", "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
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

        JScrollPane mainScroll = new JScrollPane(formPanel);
        mainScroll.setOpaque(false);
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setBackground(BG_COLOR);

        dialog.add(mainScroll);
        dialog.setVisible(true);
    }

    private void deleteProduct() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this product?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Get product ID from table
            int productId = (Integer) tableModel.getValueAt(selectedRow, 0);
            productManager.deleteProduct(productId);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Product deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void viewSellerStats() {
        int totalProducts = 0;
        int totalStock = 0;
        double totalRevenue = 0;
        int productsSold = 0;

        for (Product p : productDatabase) {
            if (p.getSeller().equals(currentUsername)) {
                totalProducts++;
                totalStock += p.getStock();
                totalRevenue += p.getPrice() * (50 - p.getStock()); // Estimate based on stock reduction
                productsSold += (50 - p.getStock());
            }
        }

        String stats = "SELLER STATISTICS\n\n" +
                "Total Products: " + totalProducts + "\n" +
                "Total Stock Available: " + totalStock + "\n" +
                "Estimated Revenue: $" + String.format("%.2f", totalRevenue) + "\n" +
                "Estimated Products Sold: " + productsSold + "\n" +
                "Average Product Price: $" + String.format("%.2f",
                totalProducts > 0 ? productDatabase.stream()
                        .filter(p -> p.getSeller().equals(currentUsername))
                        .mapToDouble(Product::getPrice)
                        .average()
                        .orElse(0) : 0);

        JOptionPane.showMessageDialog(this, stats, "Seller Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(30, 30, 45));
        field.setForeground(TEXT_COLOR);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ProductCatalog("Buyer", "buyer1");
            }
        });
    }

    // Product model
    static class Product {
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