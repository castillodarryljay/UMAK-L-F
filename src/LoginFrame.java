import javax.swing.*;
import java.awt.*;
import java.sql.*;

/*
 * Main login screen handling entry and registration.
 */
public class LoginFrame extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainCardPanel = new JPanel(cardLayout);
    
    private JTextField loginUser = new JTextField();
    private JPasswordField loginPass = new JPasswordField();
    
    private JTextField regName = new JTextField();
    private JTextField regID = new JTextField();
    private JTextField regEmail = new JTextField();
    private JPasswordField regPass = new JPasswordField();
    private JPasswordField regConfirmPass = new JPasswordField();
    private JComboBox<String> regDept = new JComboBox<>(new String[]{
        "Select Department",
        "CCAPS: College of Continuing, Advanced and Professional Studies",
        "IAD: Institute of Arts and Design",
        "CBFS: College of Business and Financial Science",
        "IOA: Institute of Accountancy",
        "CCIS: College of Computing and Information Sciences",
        "CCSE: College of Construction Sciences and Engineering",
        "CHK: College of Human Kinetics",
        "CGPP: College of Governance and Public Policy",
        "ION: Institute of Nursing",
        "IOP: Institute of Pharmacy",
        "IIHS: Institute of Imaging Health Sciences",
        "CITE: College of Innovative Teacher Education",
        "IOPsy: Institute of Psychology",
        "CTHM: College of Tourism and Hospitality Management",
        "IDEM: Institute for Disaster and Emergency Management",
        "ISW: Institute for Social Work",
        "CET: College of Engineering Technology (Formerly CTM)",
        "SOL: School of Law",
        "CITE-HSU: Higher School ng UMak (Senior High School level)"
    });

    private final Color PRIMARY = new Color(0, 30, 64);
    private final Color SECONDARY = new Color(0, 106, 106);
    private final String BACKGROUND_PATH = "sample image/background.jpg"; 
    private final String LOGO_PATH = "sample image/logo.png";
    private final String PROJECT_LOGO_PATH = "sample image/project_logo.png";

    public LoginFrame() {
        setTitle("UMAK Lost & Found Inventory - Login");
        setSize(1000, 600);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        mainCardPanel.add(createLoginView(), "login");
        mainCardPanel.add(new JScrollPane(createRegisterView()) {{ setBorder(null); getVerticalScrollBar().setUnitIncrement(16); }}, "register");

        add(mainCardPanel);
    }

    // Creates the initial login view
    private JPanel createLoginView() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(new LeftImagePanel(BACKGROUND_PATH));
        
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        
        ImageIcon logoIcon = new ImageIcon(new ImageIcon(LOGO_PATH).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        
        ImageIcon projectLogoIcon = new ImageIcon(new ImageIcon(PROJECT_LOGO_PATH).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
        JLabel projectLogoLabel = new JLabel(projectLogoIcon);
        
        JLabel brandText = new JLabel("<html><b style='font-size:16px;'>UNIVERSITY OF MAKATI</b><br>Lost & Found Inventory</html>");
        header.add(logoLabel);
        header.add(projectLogoLabel);
        header.add(brandText);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        rightPanel.add(header, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(30, 40, 5, 40);
        JLabel emailLabel = new JLabel("<html>Student No / Email: <font color='red'>*</font></html>");
        emailLabel.setFont(new Font("Inter", Font.BOLD, 12));
        rightPanel.add(emailLabel, gbc);
        
        gbc.gridy = 2; gbc.insets = new Insets(0, 50, 20, 50);
        loginUser.setPreferredSize(new Dimension(350, 45));
        rightPanel.add(loginUser, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(10, 50, 5, 50);
        JLabel passLabel = new JLabel("<html>Password: <font color='red'>*</font></html>");
        passLabel.setFont(new Font("Inter", Font.BOLD, 12));
        rightPanel.add(passLabel, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 50, 20, 50);
        loginPass.setPreferredSize(new Dimension(350, 45));
        addPasswordToggle(loginPass);
        rightPanel.add(loginPass, gbc);

        // Submit on Enter key
        java.awt.event.KeyAdapter loginEnter = new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) handleLogin();
            }
        };
        loginUser.addKeyListener(loginEnter);
        loginPass.addKeyListener(loginEnter);

        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 20, 40);
        JPanel linksPanel = new JPanel(new BorderLayout());
        linksPanel.setBackground(Color.WHITE);

        JButton goReg = new JButton("Create account");
        styleLinkBtn(goReg);
        goReg.addActionListener(e -> cardLayout.show(mainCardPanel, "register"));

        linksPanel.add(goReg, BorderLayout.EAST);
        rightPanel.add(linksPanel, gbc);

        JButton signInBtn = new JButton("Sign In");
        stylePrimaryBtn(signInBtn);
        signInBtn.addActionListener(e -> handleLogin());
        gbc.gridy = 6; gbc.insets = new Insets(10, 40, 10, 40);
        rightPanel.add(signInBtn, gbc);

        mainPanel.add(rightPanel);
        return mainPanel;
    }

    // Creates the account registration view
    private JPanel createRegisterView() {
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(new LeftImagePanel(BACKGROUND_PATH));

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Public Sans", Font.BOLD, 24));
        title.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 40, 30, 40);
        rightPanel.add(title, gbc);

        gbc.insets = new Insets(5, 40, 5, 40);
        gbc.gridwidth = 2;
        
        gbc.gridy = 1; rightPanel.add(new JLabel("<html>Full Name: <font color='red'>*</font></html>"), gbc);
        gbc.gridy = 2; regName.setPreferredSize(new Dimension(350, 40)); rightPanel.add(regName, gbc);
        
        gbc.gridy = 3; rightPanel.add(new JLabel("<html>Student Number: <font color='red'>*</font></html>"), gbc);
        gbc.gridy = 4; regID.setPreferredSize(new Dimension(350, 40)); rightPanel.add(regID, gbc);
        
        gbc.gridy = 5; rightPanel.add(new JLabel("<html>Department: <font color='red'>*</font></html>"), gbc);
        gbc.gridy = 6; regDept.setPreferredSize(new Dimension(350, 40)); rightPanel.add(regDept, gbc);

        gbc.gridy = 7; rightPanel.add(new JLabel("<html>Email: <font color='red'>*</font></html>"), gbc);
        gbc.gridy = 8; regEmail.setPreferredSize(new Dimension(350, 40)); rightPanel.add(regEmail, gbc);
        
        gbc.gridy = 9; rightPanel.add(new JLabel("<html>Password: <font color='red'>*</font></html>"), gbc);
        gbc.gridy = 10; 
        regPass.setPreferredSize(new Dimension(350, 40));
        addPasswordToggle(regPass);
        rightPanel.add(regPass, gbc);

        gbc.gridy = 11; rightPanel.add(new JLabel("<html>Confirm Password: <font color='red'>*</font></html>"), gbc);
        gbc.gridy = 12;
        regConfirmPass.setPreferredSize(new Dimension(350, 40));
        addPasswordToggle(regConfirmPass);
        rightPanel.add(regConfirmPass, gbc);

        // Password strength hint
        JLabel passHint = new JLabel("Must be 8+ chars with uppercase, lowercase, and a number.");
        passHint.setFont(new Font("Inter", Font.PLAIN, 11));
        passHint.setForeground(Color.GRAY);
        gbc.gridy = 13; gbc.insets = new Insets(2, 40, 5, 40);
        rightPanel.add(passHint, gbc);

        regPass.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String p = new String(regPass.getPassword());
                if (p.isEmpty()) {
                    passHint.setText("Must be 8+ chars with uppercase, lowercase, and a number.");
                    passHint.setForeground(Color.GRAY);
                } else if (p.length() < 8) {
                    passHint.setText("Too short — needs at least 8 characters.");
                    passHint.setForeground(new Color(200, 60, 60));
                } else if (!p.matches(".*[A-Z].*")) {
                    passHint.setText("Add at least one uppercase letter.");
                    passHint.setForeground(new Color(200, 60, 60));
                } else if (!p.matches(".*[a-z].*")) {
                    passHint.setText("Add at least one lowercase letter.");
                    passHint.setForeground(new Color(200, 60, 60));
                } else if (!p.matches(".*[0-9].*")) {
                    passHint.setText("Add at least one number.");
                    passHint.setForeground(new Color(200, 60, 60));
                } else {
                    passHint.setText("✓ Strong password.");
                    passHint.setForeground(new Color(34, 139, 34));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        java.awt.event.KeyAdapter regEnter = new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) handleRegister();
            }
        };
        regName.addKeyListener(regEnter);
        regID.addKeyListener(regEnter);
        regEmail.addKeyListener(regEnter);
        regPass.addKeyListener(regEnter);
        regConfirmPass.addKeyListener(regEnter);

        gbc.gridy = 14; gbc.insets = new Insets(30, 40, 10, 40);
        JButton registerBtn = new JButton("Register");
        stylePrimaryBtn(registerBtn);
        registerBtn.addActionListener(e -> handleRegister());
        rightPanel.add(registerBtn, gbc);

        gbc.gridy = 15; gbc.insets = new Insets(0, 40, 10, 40);
        JButton backBtn = new JButton("← Back to Login");
        styleLinkBtn(backBtn);
        backBtn.addActionListener(e -> cardLayout.show(mainCardPanel, "login"));
        rightPanel.add(backBtn, gbc);

        mainPanel.add(rightPanel);
        return mainPanel;
    }

    private void stylePrimaryBtn(JButton b) {
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Inter", Font.BOLD, 15));
        b.setPreferredSize(new Dimension(300, 45));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleLinkBtn(JButton b) {
        b.setForeground(SECONDARY);
        b.setFont(new Font("Inter", Font.BOLD, 13));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addPasswordToggle(JPasswordField field) {
        field.setLayout(new BorderLayout());

        JButton toggle = new JButton("👁");
        toggle.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        toggle.setContentAreaFilled(false);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            if (field.getEchoChar() == (char) 0) {
                field.setEchoChar('•');
                toggle.setText("👁");
            } else {
                field.setEchoChar((char) 0);
                toggle.setText("🙈");
            }
        });

        field.add(toggle, BorderLayout.EAST);
    }

    // Authenticates user and starts session
    private void handleLogin() {
        String user = loginUser.getText().trim();
        String pass = new String(loginPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both student number/email and password", "Login", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.connect()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed. Is XAMPP running?", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "SELECT * FROM users WHERE (student_no = ? OR email = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user);
                ps.setString(2, user);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString("password");
                        String decrypted = DBConnection.decryptPassword(stored);
                        if (!pass.equals(decrypted)) {
                            JOptionPane.showMessageDialog(this, "Invalid student number or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        int uid = rs.getInt("id");
                        // Track online status
                        try (PreparedStatement updatePs = conn.prepareStatement("UPDATE users SET is_online = 1, last_seen = CURRENT_TIMESTAMP WHERE id = ?")) {
                            updatePs.setInt(1, uid);
                            updatePs.executeUpdate();
                        }
                        
                        UserStatus.init(
                            uid,
                            rs.getString("full_name"),
                            rs.getString("role"),
                            rs.getString("student_no"),
                            rs.getString("email"),
                            rs.getString("department")
                        );

                        new UMAKSystemMain().setVisible(true);
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid student number or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Registers a new student account
    private void handleRegister() {
        String email = regEmail.getText().trim();
        if(regName.getText().isEmpty() || regID.getText().isEmpty() || email.isEmpty() || regDept.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields and select a department");
            return;
        }

        // Email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address (e.g., name@email.com)", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String password = new String(regPass.getPassword());
        String confirmPassword = new String(regConfirmPass.getPassword());

        if (confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please confirm your password.", "Confirm Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Confirm Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.matches(".*[A-Z].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one uppercase letter.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.matches(".*[a-z].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one lowercase letter.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.matches(".*[0-9].*")) {
            JOptionPane.showMessageDialog(this, "Password must contain at least one number.", "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

                try (Connection conn = DBConnection.connect();
                         PreparedStatement ps = conn.prepareStatement("INSERT INTO users (full_name, student_no, email, password, department, role) VALUES (?,?,?,?,?,'Student')")) {
                        String encrypted = DBConnection.encryptPassword(password);
            ps.setString(1, regName.getText());
            ps.setString(2, regID.getText());
            ps.setString(3, regEmail.getText());
                        ps.setString(4, encrypted);
            ps.setString(5, regDept.getSelectedItem().toString());
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Account created! You can now sign in.");
            cardLayout.show(mainCardPanel, "login");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
        }
    }

    // Custom panel for the left-side branding
    class LeftImagePanel extends JPanel {
        private Image bgImage;

        public LeftImagePanel(String path) {
            this.bgImage = new ImageIcon(path).getImage();
            setLayout(new GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                int iw = bgImage.getWidth(this);
                int ih = bgImage.getHeight(this);
                if (iw > 0 && ih > 0) {
                    double imageAspect = (double) iw / ih;
                    double canvasAspect = (double) getWidth() / getHeight();
                    
                    int x, y, w, h;
                    if (canvasAspect > imageAspect) {
                        w = getWidth();
                        h = (int) (w / imageAspect);
                        x = 0;
                        y = (getHeight() - h) / 2;
                    } else {
                        h = getHeight();
                        w = (int) (h * imageAspect);
                        x = (getWidth() - w) / 2;
                        y = 0;
                    }
                    g.drawImage(bgImage, x, y, w, h, this);
                }
            }
            g.setColor(new Color(0, 30, 64, 180)); 
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        @Override
        public void addNotify() {
            super.addNotify();
            JPanel textOverlay = new JPanel();
            textOverlay.setOpaque(false);
            textOverlay.setLayout(new BoxLayout(textOverlay, BoxLayout.Y_AXIS));

            JLabel title1 = new JLabel("Find What's Yours.");
            JLabel title2 = new JLabel("Return What's Theirs.");
            JLabel description = new JLabel("<html><div style='text-align: center; width: 300px;'>"
                + "The official UMak community hub to quickly report lost belongings and turn in found items.</div></html>");

            title1.setFont(new Font("SansSerif", Font.BOLD, 36)); 
            title1.setForeground(Color.WHITE);
            title2.setFont(new Font("SansSerif", Font.BOLD, 36)); 
            title2.setForeground(Color.WHITE);
            
            description.setFont(new Font("SansSerif", Font.PLAIN, 16));
            description.setForeground(new Color(240, 240, 240));
            description.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

            title1.setAlignmentX(Component.CENTER_ALIGNMENT);
            title2.setAlignmentX(Component.CENTER_ALIGNMENT);
            description.setAlignmentX(Component.CENTER_ALIGNMENT);

            textOverlay.add(title1);
            textOverlay.add(title2);
            textOverlay.add(description);
            add(textOverlay);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}