import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private JButton loginButton;
    private JButton clearButton;
    private JLabel errorLabel;
    private JCheckBox showPasswordCheckbox;

    // Color scheme
    private static final Color BG_COLOR = new Color(10, 10, 15);
    private static final Color CARD_COLOR = new Color(20, 20, 30);
    private static final Color ACCENT_PURPLE = new Color(168, 85, 247);
    private static final Color ACCENT_PURPLE_DARK = new Color(126, 34, 206);
    private static final Color TEXT_COLOR = new Color(240, 240, 245);
    private static final Color SUBTEXT_COLOR = new Color(156, 163, 175);

    public LoginScreen() {
        setTitle("NEXUS Login - E-Commerce Platform");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark background
                g2d.setColor(BG_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Subtle purple gradient overlays
                GradientPaint gradientTop = new GradientPaint(
                        getWidth() - 150, 0,
                        new Color(168, 85, 247, 20),
                        getWidth(), 300,
                        new Color(168, 85, 247, 5)
                );
                g2d.setPaint(gradientTop);
                g2d.fillRect(0, 0, getWidth(), 300);

                GradientPaint gradientBottom = new GradientPaint(
                        0, getHeight() - 250,
                        new Color(126, 34, 206, 15),
                        getWidth(), getHeight(),
                        new Color(126, 34, 206, 5)
                );
                g2d.setPaint(gradientBottom);
                g2d.fillRect(0, getHeight() - 250, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBackground(BG_COLOR);

        // Header section
        JPanel headerPanel = createHeaderPanel();
        headerPanel.setBounds(0, 20, 500, 100);
        mainPanel.add(headerPanel);

        // Card panel
        JPanel cardPanel = createCardPanel();
        cardPanel.setBounds(30, 130, 440, 480);
        mainPanel.add(cardPanel);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                setOpaque(false);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Logo circle
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background for logo
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_PURPLE, getWidth(), getHeight(), ACCENT_PURPLE_DARK);
                g2d.setPaint(gp);
                g2d.fillOval(0, 0, 60, 60);

                // Infinity symbol
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                FontMetrics fm = g2d.getFontMetrics();
                String symbol = "\u221E"; // Infinity symbol
                g2d.drawString(symbol, (60 - fm.stringWidth(symbol)) / 2, ((60 - fm.getAscent()) / 2) + fm.getAscent());
            }
        };
        logoPanel.setPreferredSize(new Dimension(60, 60));
        logoPanel.setMaximumSize(new Dimension(60, 60));

        JLabel titleLabel = new JLabel("NEXUS");
        titleLabel.setFont(new Font("Georgia", Font.BOLD, 36));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Marketplace Platform");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(ACCENT_PURPLE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(logoPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded rectangle background
                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Border
                g2d.setColor(new Color(168, 85, 247, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 25, 25, 25));

        // User Type Section
        JLabel userTypeLabel = createLabel("ACCOUNT TYPE", 11);
        panel.add(userTypeLabel);
        panel.add(Box.createVerticalStrut(10));

        JPanel userTypePanel = new JPanel();
        userTypePanel.setOpaque(false);
        userTypePanel.setLayout(new GridLayout(1, 2, 10, 0));
        userTypePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        String[] accountTypes = {"Buyer", "Seller"};
        userTypeCombo = new JComboBox<String>(accountTypes);
        styleComboBox(userTypeCombo);
        userTypePanel.add(userTypeCombo);

        panel.add(userTypePanel);
        panel.add(Box.createVerticalStrut(20));

        // Username Section
        JLabel usernameLabel = createLabel("USERNAME", 11);
        panel.add(usernameLabel);
        panel.add(Box.createVerticalStrut(8));

        usernameField = new JTextField();
        styleTextField(usernameField, "Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(usernameField);

        JLabel usernameHintLabel = new JLabel("Demo: " + (userTypeCombo.getSelectedIndex() == 0 ? "buyer1" : "seller1"));
        usernameHintLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        usernameHintLabel.setForeground(SUBTEXT_COLOR);
        panel.add(usernameHintLabel);

        panel.add(Box.createVerticalStrut(15));

        // Password Section
        JLabel passwordLabel = createLabel("PASSWORD", 11);
        panel.add(passwordLabel);
        panel.add(Box.createVerticalStrut(8));

        passwordField = new JPasswordField();
        styleTextField(passwordField, "Enter your password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.add(passwordField);

        JLabel passwordHintLabel = new JLabel("Min. 6 characters • Demo: password123");
        passwordHintLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        passwordHintLabel.setForeground(SUBTEXT_COLOR);
        panel.add(passwordHintLabel);

        panel.add(Box.createVerticalStrut(15));

        // Show Password Checkbox
        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setOpaque(false);
        showPasswordCheckbox.setForeground(SUBTEXT_COLOR);
        showPasswordCheckbox.setFont(new Font("Arial", Font.PLAIN, 11));
        showPasswordCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckbox.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar('\u25CF'); // Bullet point
                }
            }
        });
        panel.add(showPasswordCheckbox);

        panel.add(Box.createVerticalStrut(15));

        // Error Label
        errorLabel = new JLabel("");
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        errorLabel.setForeground(new Color(239, 68, 68));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(errorLabel);

        panel.add(Box.createVerticalStrut(10));

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(1, 2, 10, 0));
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        loginButton = createButton("Sign In");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        clearButton = createButton("Clear");
        clearButton.setBackground(new Color(55, 65, 81));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        panel.add(buttonPanel);

        return panel;
    }

    private JLabel createLabel(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(SUBTEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        field.setForeground(TEXT_COLOR);
        field.setBackground(new Color(30, 30, 45));
        field.setCaretColor(ACCENT_PURPLE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_COLOR);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_PURPLE, 2),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(SUBTEXT_COLOR);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(55, 65, 81), 1),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBackground(new Color(30, 30, 45));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(55, 65, 81), 1));
        comboBox.setFocusable(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(ACCENT_PURPLE);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
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

    private void handleLogin() {
        errorLabel.setText("");
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeCombo.getSelectedItem();

        // Validation
        if (username.isEmpty() || username.equals("Enter your username")) {
            errorLabel.setText("Please enter your username");
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters");
            return;
        }

        // Demo credentials
        String validUsername = userType.equalsIgnoreCase("Buyer") ? "buyer1" : "seller1";
        String validPassword = "password123";

        if (username.equals(validUsername) && password.equals(validPassword)) {
            String message = "Welcome " + userType + "!\n\n" + username + "\n\nOpening Product Catalog...";
            JOptionPane.showMessageDialog(this,
                    message,
                    "Login Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            // Navigate to ProductCatalog
            openProductCatalog(userType, username);

        } else {
            errorLabel.setText("Invalid username or password");
        }
    }

    private void openProductCatalog(String userType, String username) {
        // Close the login screen
        this.dispose();

        // Open the ProductCatalog with user info
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ProductCatalog(userType, username);
            }
        });
    }

    private void clearFields() {
        usernameField.setText("Enter your username");
        usernameField.setForeground(SUBTEXT_COLOR);
        passwordField.setText("");
        showPasswordCheckbox.setSelected(false);
        passwordField.setEchoChar('\u25CF'); // Bullet point
        errorLabel.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginScreen();
            }
        });
    }
}