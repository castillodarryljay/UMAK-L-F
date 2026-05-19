import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;

/*
 * Main application window for the Lost & Found system.
 * Handles navigation, content display, and admin features.
 */
public class UMAKSystemMain extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContent = new JPanel(cardLayout);
    private File selectedImageFile = null;
    private String currentCard = "dash";
    private java.util.Map<String, JButton> navButtons = new java.util.HashMap<>();
    
    private final Color SIDEBAR_BG = new Color(13, 27, 42); 
    private Color PRIMARY = new Color(0, 30, 64);
    private Color SECONDARY = new Color(0, 106, 106);
    private Color SURFACE = new Color(247, 250, 252);
    private Color OUTLINE = new Color(115, 119, 128);
    private Color TEXT_MAIN = Color.BLACK;
    private Color CARD_BG = Color.WHITE;
    
    private final String BACKGROUND_PATH = "sample image/background.jpg"; 
    private final String LOGO_PATH = "sample image/logo.png";
    private final String PROJECT_LOGO_PATH = "sample image/project_logo.png";

    private UMAKDashboard dashboardPanel;
    private JPanel profilePanelContainer;

    public UMAKSystemMain() {
        setTitle("UMAK Lost & Found Inventory - Portal");
        setMinimumSize(new Dimension(1000, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        dashboardPanel = new UMAKDashboard(SURFACE, CARD_BG);
        mainContent.add(dashboardPanel, "dash");
        mainContent.add(createReportForm(), "report");
        mainContent.add(createProfilePanel(), "profile");
        mainContent.add(createMyActivityPanel(), "my_activity");
        
        if ("Admin".equalsIgnoreCase(UserStatus.role)) {
            mainContent.add(createAdminStatusPanel(), "admin_status");
            mainContent.add(createAdminStatsPanel(), "admin_stats");
            mainContent.add(createAdminInventoryPanel(), "admin_inventory");
            mainContent.add(createAdminArchivePanel(), "admin_archive");
            mainContent.add(createAdminSessionsPanel(), "admin_sessions");
        }

        add(createSidebar(), BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
        
        // Update status when closing
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                setOfflineStatus();
            }
        });

        refreshDashboard();
    }

    // Sets user status to offline in database
    private void setOfflineStatus() {
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET is_online = 0 WHERE id = ?")) {
            ps.setInt(1, UserStatus.userId);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Rebuilds the UI components
    private void refreshFullUI() {
        mainContent.removeAll();
        dashboardPanel = new UMAKDashboard(SURFACE, CARD_BG);
        mainContent.add(dashboardPanel, "dash");
        mainContent.add(createReportForm(), "report");
        mainContent.add(createProfilePanel(), "profile");
        mainContent.add(createMyActivityPanel(), "my_activity");
        if ("Admin".equalsIgnoreCase(UserStatus.role)) {
            mainContent.add(createAdminStatusPanel(), "admin_status");
            mainContent.add(createAdminStatsPanel(), "admin_stats");
            mainContent.add(createAdminInventoryPanel(), "admin_inventory");
            mainContent.add(createAdminArchivePanel(), "admin_archive");
            mainContent.add(createAdminSessionsPanel(), "admin_sessions");
        }
        
        getContentPane().removeAll();
        add(createSidebar(), BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Navigation sidebar on the left
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG); 
        sidebar.setPreferredSize(new Dimension(280, 900));

        JPanel navPanel = new JPanel();
        navPanel.setOpaque(false);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(new EmptyBorder(30, 25, 30, 25));

        // Brand section centered
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        brandPanel.setOpaque(false);
        brandPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        ImageIcon logoIcon = new ImageIcon(new ImageIcon(LOGO_PATH).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        ImageIcon pLogoIcon = new ImageIcon(new ImageIcon(PROJECT_LOGO_PATH).getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH));
        JLabel pLogoLabel = new JLabel(pLogoIcon);
        
        JLabel brandLabel = new JLabel("<html><center><b style='color:white; font-size:17px;'>Lost & Found</b><br><font color='#006a6a'>UMAK Inventory</font></center></html>");
        brandLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        brandPanel.add(logoLabel);
        brandPanel.add(pLogoLabel);
        brandPanel.add(Box.createRigidArea(new Dimension(12, 0)));
        brandPanel.add(brandLabel);
        
        navPanel.add(brandPanel);
        navPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // Nav buttons centered
        navPanel.add(createNavBtn("🏠   Dashboard", "dash"));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createNavBtn("➕   Report Item", "report"));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createNavBtn("📋   My Activity", "my_activity"));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createNavBtn("👤   Profile", "profile"));
        
        if ("Admin".equalsIgnoreCase(UserStatus.role)) {
            navPanel.add(Box.createRigidArea(new Dimension(0, 35)));
            JLabel adminLbl = new JLabel("ADMINISTRATION");
            adminLbl.setForeground(new Color(100, 116, 139));
            adminLbl.setFont(new Font("Inter", Font.BOLD, 11));
            adminLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            navPanel.add(adminLbl);
            navPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            
            navPanel.add(createNavBtn("📊   Statistics", "admin_stats"));
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("⚖️   Manage Claims & Reports", "admin_status"));
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("📦   Inventory", "admin_inventory"));            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("🗄️   Archived Items", "admin_archive"));
            navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            navPanel.add(createNavBtn("👥   User Status", "admin_sessions"));
        }

        navPanel.add(Box.createVerticalGlue());
        sidebar.add(navPanel, BorderLayout.CENTER);

       JPanel profileCard = new JPanel(new BorderLayout());
        profileCard.setBackground(new Color(23, 37, 52));
        profileCard.setBorder(new EmptyBorder(15, 25, 15, 25));

        JPanel userInfo = new JPanel(new GridLayout(2, 1));
        userInfo.setOpaque(false);
        JLabel nameLbl = new JLabel(UserStatus.fullName != null ? UserStatus.fullName : "User", SwingConstants.CENTER);
        nameLbl.setFont(new Font("Inter", Font.BOLD, 14));
        nameLbl.setForeground(Color.WHITE);
        JLabel emailLbl = new JLabel(UserStatus.email != null ? UserStatus.email : "", SwingConstants.CENTER);
        emailLbl.setFont(new Font("Inter", Font.PLAIN, 11));
        emailLbl.setForeground(new Color(160, 160, 160));
        userInfo.add(nameLbl);
        userInfo.add(emailLbl);
        profileCard.add(userInfo, BorderLayout.CENTER);
        
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 10));
        bottomContainer.setOpaque(false);
        bottomContainer.add(profileCard, BorderLayout.CENTER);

        sidebar.add(bottomContainer, BorderLayout.SOUTH);
        return sidebar;
    }

    // Creates stylized navigation buttons
    private JButton createNavBtn(String text, String card) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(new Color(220, 220, 220));
        btn.setFont(new Font("Inter", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 20, 0, 0));

        navButtons.put(card, btn);

        btn.addActionListener(e -> {
            setActiveNavButton(card);
            refreshCurrentPanel(card);
            cardLayout.show(mainContent, card);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!card.equals(currentCard)) btn.setBackground(new Color(30, 41, 59));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!card.equals(currentCard)) btn.setBackground(SIDEBAR_BG);
            }
        });
        return btn;
    }

    // Highlights the current active navigation item
    private void setActiveNavButton(String card) {
        JButton prevBtn = navButtons.get(currentCard);
        if (prevBtn != null) prevBtn.setBackground(SIDEBAR_BG);
        
        JButton curBtn = navButtons.get(card);
        if (curBtn != null) curBtn.setBackground(new Color(30, 41, 59));
        
        currentCard = card;
    }

    // Refreshes data when switching panels
    private void refreshCurrentPanel(String card) {
        if(card.equals("dash")) refreshDashboard();
        else if(card.equals("admin_stats")) refreshAdminStats();
        else if(card.equals("admin_status")) refreshAdminClaimsTable();
        else if(card.equals("admin_inventory")) refreshAdminInventory();
        else if(card.equals("admin_archive")) refreshAdminArchive();
        else if(card.equals("admin_sessions")) refreshAdminSessions();
        else if(card.equals("my_activity")) refreshMyActivity();
        else if(card.equals("profile")) refreshProfile();
    }

    // Jumps to claims management for a specific item
    public void navigateToClaims(int itemId) {
        cardLayout.show(mainContent, "admin_status");
        setActiveNavButton("admin_status");
        refreshAdminClaimsTable();
        
        // Find and highlight rows matching the itemId
        for (int i = 0; i < claimsTable.getRowCount(); i++) {
            int rowItemId = (int) claimsModel.getValueAt(i, 6);
            if (rowItemId == itemId) {
                claimsTable.addRowSelectionInterval(i, i);
                // Scroll to the first match
                claimsTable.scrollRectToVisible(claimsTable.getCellRect(i, 0, true));
            }
        }
    }

    private DefaultTableModel sessionsModel;
    private JTable sessionsTable;
    private JPanel createAdminSessionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("User Sessions");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        String[] columns = {"Account", "Full Name", "Student ID", "Last Seen", "Status"};
        sessionsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        sessionsTable = new JTable(sessionsModel);
        sessionsTable.setRowHeight(50);
        sessionsTable.setShowGrid(false);
        sessionsTable.setFont(new Font("SansSerif", Font.PLAIN, 15));

        sessionsTable.getColumnModel().getColumn(4).setCellRenderer(new SessionStatusRenderer());

        JScrollPane scroll = new JScrollPane(sessionsTable);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scroll, BorderLayout.CENTER);
        panel.add(tableBox, BorderLayout.CENTER);

        return panel;
    }

    // Fetches all user sessions
    private void refreshAdminSessions() {
        if (sessionsModel == null) return;
        sessionsModel.setRowCount(0);
        try (Connection conn = DBConnection.connect(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT email, full_name, student_no, last_seen, is_online FROM users ORDER BY is_online DESC, last_seen DESC")) {
            while (rs.next()) {
                sessionsModel.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getBoolean(5) ? "Online" : "Offline"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    class SessionStatusRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String status = (String) value;
            JLabel label = new JLabel();
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            
            if ("Online".equalsIgnoreCase(status)) {
                label.setForeground(new Color(40, 167, 69));
                label.setText("● Online");
            } else {
                label.setForeground(new Color(220, 53, 69));
                label.setText("○ Offline");
            }
            
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            wrapper.add(label);
            return wrapper;
        }
    }

    private void refreshDashboard() {
        if (dashboardPanel != null) dashboardPanel.refreshData();
    }

    // Form for reporting lost/found items
    private JPanel createReportForm() {
        JPanel wrapper = new JPanel(new GridBagLayout()); 
        wrapper.setBackground(SURFACE);
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG); 
        card.setBorder(BorderFactory.createEmptyBorder(50, 60, 50, 60));
        card.setPreferredSize(new Dimension(650, 950));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Report Item");
        title.setFont(new Font("Inter", Font.BOLD, 36));
        title.setForeground(PRIMARY);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 35, 0);
        card.add(title, gbc);

        gbc.gridy = 1;
        JPanel statusPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        statusPanel.setOpaque(false);
        JRadioButton lostRadio = new JRadioButton("REPORT LOST", true);
        JRadioButton foundRadio = new JRadioButton("REPORT FOUND");
        lostRadio.setFont(new Font("Inter", Font.BOLD, 12));
        foundRadio.setFont(new Font("Inter", Font.BOLD, 12));
        ButtonGroup statusGroup = new ButtonGroup();
        statusGroup.add(lostRadio); statusGroup.add(foundRadio);
        statusPanel.add(lostRadio); statusPanel.add(foundRadio);
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(statusPanel, gbc);

        JTextField nameField = createTextField("Item Name");
        addLabelAndField(card, "<html>Item Name <font color='red'>*</font></html>", nameField, 2, 0, gbc, true);

        JComboBox<String> catCombo = createComboBox(new String[]{"Select Category", "ID Card", "Electronics", "Wallet", "Documents", "Keys", "Bags", "Other"});
        addLabelAndField(card, "<html>Category <font color='red'>*</font></html>", catCombo, 3, 0, gbc, false);
        
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setValue(new java.util.Date());
        dateSpinner.setBorder(BorderFactory.createLineBorder(OUTLINE));
        addLabelAndField(card, "<html>Date <font color='red'>*</font></html>", dateSpinner, 3, 1, gbc, false);

        JTextField timeField = createTextField("hh:mm (Optional)");
        addLabelAndField(card, "Time (Optional)", timeField, 4, 0, gbc, false);
        
        JTextField locField = createTextField("Location");
        addLabelAndField(card, "<html>Location <font color='red'>*</font></html>", locField, 4, 1, gbc, false);

        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 8, 0);
        JLabel descLabel = new JLabel("Description");
        descLabel.setForeground(new Color(71, 85, 105));
        descLabel.setFont(new Font("Inter", Font.BOLD, 13));
        card.add(descLabel, gbc);

        JTextArea descArea = new JTextArea(6, 20);
        addPlaceholder(descArea, "Provide more details about the item.");
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("Inter", Font.PLAIN, 14));
        descArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        gbc.gridy = 10; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.2;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(new JScrollPane(descArea), gbc);

        gbc.gridy = 11; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        gbc.insets = new Insets(10, 0, 8, 0);
        JLabel uploadHeader = new JLabel("Upload Photo");
        uploadHeader.setForeground(new Color(71, 85, 105));
        uploadHeader.setFont(new Font("Inter", Font.BOLD, 13));
        card.add(uploadHeader, gbc);

        JLabel uploadLabel = new JLabel("Click to Upload Photo");
        uploadLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        JPanel uploadPanel = createUploadPanel(uploadLabel);
        gbc.gridy = 12;
        gbc.insets = new Insets(0, 0, 30, 0);
        card.add(uploadPanel, gbc);
        
        JButton submitBtn = new JButton("Submit Report");
        submitBtn.setBackground(PRIMARY);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Inter", Font.BOLD, 18));
        submitBtn.setPreferredSize(new Dimension(250, 55));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        submitBtn.addActionListener(e -> {
            String category = catCombo.getSelectedItem().toString();
            if (category.equals("Select Category")) {
                JOptionPane.showMessageDialog(this, "Select a category."); return;
            }

            String itemName = nameField.getText().trim();
            if (itemName.equals("Item Name") || itemName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter item name."); return;
            }

            String timeText = timeField.getText().trim();
            Time validatedTime = null;
            if (!timeText.isEmpty() && !timeText.equals("hh:mm (Optional)")) {
                if (!timeText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                    JOptionPane.showMessageDialog(this, "Enter time as HH:mm."); return;
                }
                try { validatedTime = Time.valueOf(timeText + ":00"); } catch (Exception ex) { return; }
            }

            try (Connection c = DBConnection.connect()) {
                if (c == null) return;
                String sql = "INSERT INTO items (item_name, category, description, location, item_type, status, image_path, reporter_id, date_lost, time_lost) VALUES (?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, itemName);
                    ps.setString(2, category);
                    ps.setString(3, descArea.getText().trim().equals("Provide more details about the item.") ? "" : descArea.getText().trim());
                    ps.setString(4, locField.getText().trim().equals("Location") ? "" : locField.getText().trim());
                    ps.setString(5, lostRadio.isSelected() ? "Lost" : "Found");
                    ps.setString(6, "Pending");
                    ps.setString(7, (selectedImageFile != null ? selectedImageFile.getAbsolutePath() : ""));
                    ps.setInt(8, UserStatus.userId);
                    ps.setString(9, new java.text.SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) dateSpinner.getValue()));
                    if (validatedTime != null) ps.setTime(10, validatedTime); else ps.setNull(10, Types.TIME);
                    ps.executeUpdate();

                    selectedImageFile = null;
                    JOptionPane.showMessageDialog(this, "Submitted!");

                    // Reset all fields
                    catCombo.setSelectedIndex(0);
                    nameField.setText("Item Name");
                    nameField.setForeground(Color.GRAY);
                    timeField.setText("hh:mm (Optional)");
                    timeField.setForeground(Color.GRAY);
                    locField.setText("Location");
                    locField.setForeground(Color.GRAY);
                    descArea.setText("Provide more details about the item.");
                    descArea.setForeground(Color.GRAY);
                    dateSpinner.setValue(new java.util.Date());
                    lostRadio.setSelected(true);
                    uploadLabel.setText("Click to Upload Photo");

                    cardLayout.show(mainContent, "dash"); refreshDashboard(); refreshAdminStats(); refreshAdminInventory(); refreshMyActivity();
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        gbc.gridy = 13; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 0, 50, 0);
        card.add(submitBtn, gbc);

        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.insets = new Insets(50, 50, 50, 50);
        wrapper.add(card, wrapperGbc); 

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel finalPanel = new JPanel(new BorderLayout());
        finalPanel.add(scroll, BorderLayout.CENTER);
        return finalPanel;
    }

    private void addLabelAndField(JPanel panel, String labelText, JComponent field, int row, int col, GridBagConstraints gbc, boolean full) {
        if (full) { gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.gridx = 0; } 
        else { gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.gridx = col; }

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = (row * 2) - 1;
        gbc.insets = new Insets(12, (col == 1 && !full) ? 20 : 0, 5, 0);
        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(71, 85, 105));
        label.setFont(new Font("Inter", Font.BOLD, 13));
        panel.add(label, gbc);

        gbc.gridy = row * 2;
        gbc.insets = new Insets(0, (col == 1 && !full) ? 20 : 0, 15, 0);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 45));
        panel.add(field, gbc);
    }
    private void addPlaceholder(javax.swing.text.JTextComponent comp, String hint) {
        comp.setText(hint);
        comp.setForeground(Color.GRAY);
        comp.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (comp.getText().equals(hint)) { comp.setText(""); comp.setForeground(TEXT_MAIN); }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (comp.getText().isEmpty()) { comp.setText(hint); comp.setForeground(Color.GRAY); }
            }
        });
    }

    private JTextField createTextField(String hint) {
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(OUTLINE),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        tf.setFont(new Font("Inter", Font.PLAIN, 14));
        if (hint != null) addPlaceholder(tf, hint);
        return tf;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setBackground(Color.WHITE);
        cb.setFont(new Font("Inter", Font.PLAIN, 14));
        cb.setBorder(BorderFactory.createLineBorder(OUTLINE));
        return cb;
    }

    private JPanel createUploadPanel(JLabel text) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createDashedBorder(OUTLINE, 3, 2), BorderFactory.createEmptyBorder(30, 0, 30, 0)));
        p.add(text);
        p.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedImageFile = chooser.getSelectedFile();
                    text.setText(selectedImageFile.getName());
                }
            }
        });
        return p;
    }

    private DefaultTableModel actModel;
    private JTable actTable;
    private JPanel createMyActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Activity History");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        String[] columns = {"Type", "Item", "Status", "Date"};
        actModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        actTable = new JTable(actModel);
        styleTable(actTable, 2);

        JScrollPane scroll = new JScrollPane(actTable);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scroll, BorderLayout.CENTER);
        panel.add(tableBox, BorderLayout.CENTER);

        return panel;
    }

    // Fetches user-specific reports and claims
    private void refreshMyActivity() {
        actModel.setRowCount(0);
        try (Connection c = DBConnection.connect()) {
            if (c == null) return;
            try (PreparedStatement ps1 = c.prepareStatement("SELECT 'Reported', item_name, status, date_added FROM items WHERE reporter_id = ? AND is_archived = 0")) {
                ps1.setInt(1, UserStatus.userId); 
                try (ResultSet rs1 = ps1.executeQuery()) {
                    while(rs1.next()) actModel.addRow(new Object[]{rs1.getString(1), rs1.getString(2), rs1.getString(3), rs1.getTimestamp(4)});
                }
            }
            try (PreparedStatement ps2 = c.prepareStatement("SELECT 'Claimed', i.item_name, c.status, c.claim_date FROM claims c JOIN items i ON c.item_id = i.id WHERE c.user_id = ? AND i.is_archived = 0")) {
                ps2.setInt(1, UserStatus.userId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    while(rs2.next()) actModel.addRow(new Object[]{rs2.getString(1), rs2.getString(2), rs2.getString(3), rs2.getTimestamp(4)});
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createProfilePanel() {
        profilePanelContainer = new JPanel(new BorderLayout());
        profilePanelContainer.setBackground(SURFACE);
        return profilePanelContainer;
    }

    // Displays the user profile and stats
    private void refreshProfile() {
        profilePanelContainer.removeAll();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH; gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("Profile");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weighty = 0;
        panel.add(title, gbc);

        gbc.gridy = 1; gbc.weighty = 0.3;
        panel.add(createUserInfoCard(), gbc);

        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5; gbc.weighty = 0.5;
        panel.add(createActivitySummary(), gbc);

        gbc.gridx = 1;
        panel.add(createPrivacySecurity(), gbc);

        profilePanelContainer.add(new JScrollPane(panel) {{ setBorder(null); }}, BorderLayout.CENTER);
        profilePanelContainer.revalidate(); profilePanelContainer.repaint();
    }

    private JPanel createUserInfoCard() {
        JPanel card = new JPanel(new BorderLayout(30, 0));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1), new EmptyBorder(30, 30, 30, 30)));

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SECONDARY);
                g2.fillOval(0, 0, 160, 160);
                g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif", Font.BOLD, 60));
                String initials = "";
                if (UserStatus.fullName != null && !UserStatus.fullName.isEmpty()) {
                    String[] pts = UserStatus.fullName.split(" ");
                    if (pts.length > 0) initials += pts[0].charAt(0);
                    if (pts.length > 1) initials += pts[pts.length - 1].charAt(0);
                }
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials, (160 - fm.stringWidth(initials)) / 2, (160 + fm.getAscent() / 2) / 2);
            }
        };
        avatar.setPreferredSize(new Dimension(160, 160)); avatar.setOpaque(false);
        card.add(avatar, BorderLayout.WEST);

        JPanel details = new JPanel(new GridLayout(4, 1, 0, 5));
        details.setOpaque(false);
        details.add(createLabelRow("Name:", UserStatus.fullName));
        details.add(createLabelRow("Student No.:", UserStatus.studentNo));
        details.add(createLabelRow("UMak Email:", UserStatus.email));
        details.add(createLabelRow("Department:", UserStatus.department));
        card.add(details, BorderLayout.CENTER);

        return card;
    }

    private JPanel createActivitySummary() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1), new EmptyBorder(25, 30, 25, 30)));

        JLabel title = new JLabel("Activity Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JPanel list = new JPanel(new GridLayout(4, 1, 0, 10));
        list.setOpaque(false); list.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        int lost = 0, returned = 0, claimed = 0;
        try (Connection c = DBConnection.connect()) {
            if (c != null) {
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM items WHERE reporter_id = ? AND item_type = 'Lost' AND status NOT IN ('Returned', 'Denied', 'Claimed')")) {
                    ps.setInt(1, UserStatus.userId); ResultSet rs = ps.executeQuery(); if(rs.next()) lost = rs.getInt(1);
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM items WHERE reporter_id = ? AND status = 'Returned'")) {
                    ps.setInt(1, UserStatus.userId); ResultSet rs = ps.executeQuery(); if(rs.next()) returned = rs.getInt(1);
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM claims WHERE user_id = ? AND status = 'Approved'")) {
                    ps.setInt(1, UserStatus.userId); ResultSet rs = ps.executeQuery(); if(rs.next()) claimed = rs.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        list.add(createStatusRow("Active Lost Reports:", String.valueOf(lost)));
        list.add(createStatusRow("Returned Items:", String.valueOf(returned)));
        list.add(createStatusRow("Claimed Items:", String.valueOf(claimed)));

        JButton btn = createStyledButton("View Activity Log");
        btn.addActionListener(e -> { cardLayout.show(mainContent, "my_activity"); refreshMyActivity(); });

        card.add(title, BorderLayout.NORTH); card.add(list, BorderLayout.CENTER); card.add(btn, BorderLayout.SOUTH);
        return card;
    }


 /* * Creates the Privacy & Security panel for the Profile tab.
 * Includes a password verification loop for changing passwords
 * and a secure session sign-out mechanism. */

private JPanel createPrivacySecurity() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200), 1), new EmptyBorder(25, 30, 25, 30)));

        JLabel title = new JLabel("Privacy & Security", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0,0,20,0));

        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JButton cpBtn = createStyledButton("Change Password");
        cpBtn.setPreferredSize(new Dimension(200, 45));
        cpBtn.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            JPasswordField oldPass = new JPasswordField();
            oldPass.setLayout(new BorderLayout());
            JButton showOldBtn = new JButton("👁");
            showOldBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            showOldBtn.setContentAreaFilled(false);
            showOldBtn.setBorderPainted(false);
            showOldBtn.setFocusPainted(false);
            showOldBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            showOldBtn.addActionListener(ev -> {
                if (oldPass.getEchoChar() == (char) 0) {
                    oldPass.setEchoChar('•');
                    showOldBtn.setText("👁");
                } else {
                    oldPass.setEchoChar((char) 0);
                    showOldBtn.setText("🙈");
                }
            });
            oldPass.add(showOldBtn, BorderLayout.EAST);

            JPasswordField newPass = new JPasswordField();
            newPass.setLayout(new BorderLayout());
            JButton showNewBtn = new JButton("👁");
            showNewBtn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            showNewBtn.setContentAreaFilled(false);
            showNewBtn.setBorderPainted(false);
            showNewBtn.setFocusPainted(false);
            showNewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            showNewBtn.addActionListener(ev -> {
                if (newPass.getEchoChar() == (char) 0) {
                    newPass.setEchoChar('•');
                    showNewBtn.setText("👁");
                } else {
                    newPass.setEchoChar((char) 0);
                    showNewBtn.setText("🙈");
                }
            });
            newPass.add(showNewBtn, BorderLayout.EAST);

            panel.add(new JLabel("Current Password:"));
            panel.add(oldPass);
            panel.add(new JLabel("New Password:"));
            panel.add(newPass);

            boolean keepOpen = true;
            
            while (keepOpen) {
                int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                
                if (result == JOptionPane.OK_OPTION) {
                    String oldP = new String(oldPass.getPassword());
                    String newP = new String(newPass.getPassword());

                    if (oldP.isEmpty() || newP.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Both password fields are required.", "Error", JOptionPane.WARNING_MESSAGE);
                        continue;
                    }

                    try (Connection c = DBConnection.connect()) {
                        boolean isCorrect = false;
                        
                        try (PreparedStatement ps = c.prepareStatement("SELECT password FROM users WHERE id = ?")) {
                            ps.setInt(1, UserStatus.userId);
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    String currentDbPass = rs.getString("password");
                                    if (currentDbPass.equals(oldP)) {
                                        isCorrect = true;
                                    }
                                }
                            }
                        }
                        
                        if (!isCorrect) {
                            JOptionPane.showMessageDialog(this, "Incorrect current password.", "Error", JOptionPane.ERROR_MESSAGE);
                            oldPass.setText("");
                            continue;
                        }

                        try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) { 
                            ps.setString(1, newP); 
                            ps.setInt(2, UserStatus.userId); 
                            ps.executeUpdate(); 
                            JOptionPane.showMessageDialog(this, "Password updated successfully!"); 
                            keepOpen = false;
                        }
                    } catch(Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        keepOpen = false;
                    } 
                } else {
                    keepOpen = false;
                }
            }
        });

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 15, 0); // May 15px na space sa ilalim para lumayo ang susunod na button
        btnPanel.add(cpBtn, gbc);

        JButton profileLogoutBtn = new JButton("Logout Account");
        profileLogoutBtn.setPreferredSize(new Dimension(200, 45));
        profileLogoutBtn.setBackground(new Color(186, 26, 26)); // Pulang palette
        profileLogoutBtn.setForeground(Color.WHITE);
        profileLogoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        profileLogoutBtn.setFocusPainted(false);
        profileLogoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileLogoutBtn.setBorder(BorderFactory.createLineBorder(new Color(153, 21, 21)));

        profileLogoutBtn.addActionListener(e -> { 
            profileLogoutBtn.setEnabled(false);
            setOfflineStatus(); 
            UserStatus.clear();
            new LoginFrame().setVisible(true);
            this.dispose(); 
        });

        profileLogoutBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { profileLogoutBtn.setBackground(new Color(153, 21, 21)); }
            public void mouseExited(java.awt.event.MouseEvent e) { profileLogoutBtn.setBackground(new Color(186, 26, 26)); }
        });        

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Wala nang space sa ilalim nito dahil huli na siya
        btnPanel.add(profileLogoutBtn, gbc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(btnPanel);

        card.add(title, BorderLayout.NORTH); 
        card.add(wrapper, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLabelRow(String k, String v) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); p.setOpaque(false);
        JLabel kl = new JLabel(k); kl.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel vl = new JLabel(v != null ? v : "N/A"); vl.setFont(new Font("SansSerif", Font.PLAIN, 18));
        p.add(kl); p.add(vl); return p;
    }

    private JPanel createStatusRow(String k, String v) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        p.add(new JLabel(k), BorderLayout.WEST);
        JLabel vl = new JLabel(v); vl.setFont(new Font("SansSerif", Font.BOLD, 16)); vl.setForeground(SECONDARY);
        p.add(vl, BorderLayout.EAST); return p;
    }

    private JButton createStyledButton(String t) {
        JButton b = new JButton(t); b.setFocusPainted(false); b.setBackground(new Color(240, 242, 245));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200,200,200)), new EmptyBorder(10, 20, 10, 20)));
        b.setFont(new Font("SansSerif", Font.BOLD, 14)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel statsCardsPanel;
    private JPanel createAdminStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("System Statistics");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        statsCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        statsCardsPanel.setOpaque(false);
        panel.add(statsCardsPanel, BorderLayout.CENTER);

        return panel;
    }

    // Fetches system-wide stats
    private void refreshAdminStats() {
        if (statsCardsPanel == null) return;
        statsCardsPanel.removeAll();
        int found = 0, lost = 0, resolved = 0, pending = 0, total = 0;
        try (Connection conn = DBConnection.connect()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM items WHERE item_type = 'Found' AND status = 'Pending' AND is_archived = 0");
                if (rs.next()) found = rs.getInt(1);
                rs = st.executeQuery("SELECT COUNT(*) FROM items WHERE item_type = 'Lost' AND status = 'Pending' AND is_archived = 0");
                if (rs.next()) lost = rs.getInt(1);
                rs = st.executeQuery("SELECT COUNT(*) FROM items WHERE status IN ('Claimed', 'Returned', 'Approved') AND is_archived = 0");
                if (rs.next()) resolved = rs.getInt(1);
                rs = st.executeQuery("SELECT COUNT(*) FROM claims WHERE status = 'Pending'");
                if (rs.next()) pending = rs.getInt(1);
                rs = st.executeQuery("SELECT COUNT(*) FROM items WHERE is_archived = 0");
                if (rs.next()) total = rs.getInt(1);
            }
            statsCardsPanel.add(createStatCard("FOUND ITEMS", found, new Color(16, 185, 129))); 
            statsCardsPanel.add(createStatCard("LOST REPORTS", lost, new Color(249, 115, 22))); 
            statsCardsPanel.add(createStatCard("CLAIMED", resolved, new Color(34, 197, 94)));
            statsCardsPanel.add(createStatCard("PENDING CLAIMS", pending, new Color(59, 130, 246)));
            statsCardsPanel.add(createStatCard("TOTAL RECORDS", total, PRIMARY));
        } catch (Exception e) { e.printStackTrace(); }
        statsCardsPanel.revalidate(); statsCardsPanel.repaint();
    }

    private JPanel createStatCard(String label, int val, Color c) {
        JPanel cd = new JPanel(new BorderLayout()); cd.setPreferredSize(new Dimension(280, 160));
        cd.setBackground(CARD_BG); 
        cd.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), new EmptyBorder(25, 30, 25, 30)));
        JLabel l = new JLabel(label); l.setFont(new Font("SansSerif", Font.BOLD, 12)); l.setForeground(OUTLINE);
        cd.add(l, BorderLayout.NORTH);
        JLabel v = new JLabel(String.valueOf(val)); v.setFont(new Font("SansSerif", Font.BOLD, 48)); v.setForeground(c);
        cd.add(v, BorderLayout.CENTER);
        return cd;
    }

    private DefaultTableModel claimsModel;
    private JTable claimsTable;
    private JPanel createAdminStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Manage Claims & Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        String[] columns = {"ID", "Item", "Claimant", "Justification", "Status", "Proof", "item_id", "actual_proof_path"};
        claimsModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        claimsTable = new JTable(claimsModel);
        styleTable(claimsTable, 4);
        claimsTable.getColumnModel().getColumn(0).setMinWidth(0);
        claimsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        claimsTable.getColumnModel().getColumn(6).setMinWidth(0);
        claimsTable.getColumnModel().getColumn(6).setMaxWidth(0);
        claimsTable.getColumnModel().getColumn(7).setMinWidth(0);
        claimsTable.getColumnModel().getColumn(7).setMaxWidth(0);

        // Add action for Proof column
        claimsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = claimsTable.getSelectedRow();
                int col = claimsTable.getSelectedColumn();
                if (col == 5) { // Proof column
                    String proofPath = (String) claimsModel.getValueAt(row, 7); // Use hidden actual_proof_path column
                    String justification = (String) claimsModel.getValueAt(row, 3);
                    String claimant = (String) claimsModel.getValueAt(row, 2);
                    String item = (String) claimsModel.getValueAt(row, 1);
                    String status = (String) claimsModel.getValueAt(row, 4);
                    showClaimDetails(proofPath, justification, claimant, item, status);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(claimsTable);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scroll, BorderLayout.CENTER);
        panel.add(tableBox, BorderLayout.CENTER);

        JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); 
        bPanel.setOpaque(false); bPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton approve = new JButton("APPROVE"); approve.setBackground(SECONDARY); approve.setForeground(Color.WHITE);
        approve.setPreferredSize(new Dimension(150, 40));
        approve.addActionListener(e -> handleClaimStatus("Approved"));
        
        JButton deny = new JButton("REJECT"); deny.setBackground(new Color(186, 26, 26)); deny.setForeground(Color.WHITE);
        deny.setPreferredSize(new Dimension(150, 40));
        deny.addActionListener(e -> handleClaimStatus("Rejected"));
        
        bPanel.add(deny); bPanel.add(approve); 
        panel.add(bPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void refreshAdminClaimsTable() {
        if (claimsModel == null) return;
        claimsModel.setRowCount(0);
        String sql = "SELECT CONCAT('C:', c.claim_id), i.item_name, c.student_name, c.justification, c.status, c.image_proof, c.item_id, i.item_type " +
                     "FROM claims c JOIN items i ON c.item_id = i.id " +
                     "WHERE c.status = 'Pending' " +
                     "ORDER BY c.claim_date DESC";

        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String itemName = rs.getString(2);
                String itemType = rs.getString(8);
                String indicator = itemType.equalsIgnoreCase("Lost") ? "[REPORT] " : "[CLAIM] ";
                String proof = rs.getString(6);
                
                claimsModel.addRow(new Object[]{
                    rs.getString(1), 
                    indicator + itemName, 
                    rs.getString(3), 
                    rs.getString(4), 
                    rs.getString(5),
                    (proof == null || proof.isEmpty()) ? "No Proof" : "Show Proof",
                    rs.getInt(7),
                    proof // hidden actual_proof_path column
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Updates claim and item status
    private void handleClaimStatus(String status) {
        int row = claimsTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a claim."); return; }
        String rawId = (String) claimsModel.getValueAt(row, 0);
        if (!rawId.startsWith("C:")) return;
        int claimId = Integer.parseInt(rawId.substring(2));
        
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE claims SET status = ? WHERE claim_id = ?")) {
                    ps.setString(1, status); ps.setInt(2, claimId); ps.executeUpdate();
                }
                if (status.equalsIgnoreCase("Approved")) {
                    try (PreparedStatement ps2 = conn.prepareStatement("UPDATE items SET status = 'Claimed' WHERE id = (SELECT item_id FROM claims WHERE claim_id = ?)")) {
                        ps2.setInt(1, claimId); ps2.executeUpdate();
                    }
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Claim " + status);
            } catch (Exception ex) { conn.rollback(); throw ex; }
            finally { conn.setAutoCommit(true); }
            refreshAdminClaimsTable(); refreshAdminInventory(); refreshDashboard(); refreshAdminStats();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // Helper to show claim details (justification + image) matching dashboard style
    private void showClaimDetails(String path, String justification, String claimant, String item, String status) {
        JDialog viewer = new JDialog(this, "Report Details", true);
        viewer.setSize(500, 750);
        viewer.setLocationRelativeTo(this);
        viewer.setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(CARD_BG);
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel typeTag = new JLabel("REPORT", SwingConstants.CENTER);
        typeTag.setOpaque(true);
        typeTag.setBackground(new Color(59, 130, 246));
        typeTag.setForeground(Color.WHITE);
        typeTag.setPreferredSize(new Dimension(80, 25));
        typeTag.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        tagPanel.setOpaque(false);
        tagPanel.add(typeTag);
        container.add(tagPanel, BorderLayout.NORTH);

        JLabel bigPic = new JLabel("", SwingConstants.CENTER);
        bigPic.setPreferredSize(new Dimension(400, 250));
        bigPic.setOpaque(true);
        bigPic.setBackground(SURFACE);
        bigPic.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        if (path != null && !path.isEmpty() && !path.equals("No Proof")) {
            new SwingWorker<ImageIcon, Void>() {
                @Override protected ImageIcon doInBackground() {
                    try {
                        ImageIcon icon = new ImageIcon(path);
                        Image img = icon.getImage();
                        return new ImageIcon(img.getScaledInstance(450, 250, Image.SCALE_SMOOTH));
                    } catch (Exception e) { return null; }
                }
                @Override protected void done() {
                    try { if (get() != null) { bigPic.setText(""); bigPic.setIcon(get()); } else { bigPic.setText("IMAGE ERROR"); }
                    } catch (Exception e) { bigPic.setText("NO IMAGE"); }
                }
            }.execute();
        } else {
            bigPic.setText("PIC");
            bigPic.setForeground(Color.GRAY);
            bigPic.setFont(new Font("SansSerif", Font.BOLD, 80));
        }
        container.add(bigPic, BorderLayout.CENTER);

        JPanel details = new JPanel(new GridLayout(5, 2, 5, 5));
        details.setOpaque(false);

        details.add(new JLabel("User") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(claimant));
        details.add(new JLabel("Item") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(item));
        details.add(new JLabel("Status") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(status));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JLabel descHeader = new JLabel("Ownership Proof");
        descHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel descBody = new JLabel("<html>" + justification + "</html>");

        bottom.add(descHeader); 
        bottom.add(descBody);
        bottom.add(Box.createVerticalStrut(20));

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.add(details, BorderLayout.NORTH);
        infoWrapper.add(bottom, BorderLayout.CENTER);

        container.add(infoWrapper, BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        viewer.add(scroll);
        viewer.setVisible(true);
    }

    private DefaultTableModel invModel;
    private JTable invTable;
    private DefaultTableModel archiveModel;
    private JTable archiveTable;
    private JPanel createAdminInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Inventory");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));
        
        String[] columns = {"ID", "Items", "Type", "Date", "Status"};
        invModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        invTable = new JTable(invModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer ren, int r, int c) {
                Component comp = super.prepareRenderer(ren, r, c);
                if (!isRowSelected(r)) {
                    if ("Pending".equalsIgnoreCase((String) getValueAt(r, 4))) comp.setBackground(new Color(255, 255, 200));
                    else comp.setBackground(getBackground());
                }
                return comp;
            }
        };
        styleTable(invTable, 4); 
        invTable.getColumnModel().getColumn(0).setMinWidth(0); invTable.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(invTable);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scroll, BorderLayout.CENTER);
        panel.add(tableBox, BorderLayout.CENTER);

        JButton delBtn = new JButton("Archive Item");
        delBtn.setBackground(new Color(186, 26, 26)); delBtn.setForeground(Color.WHITE);
        delBtn.addActionListener(e -> {
            int row = invTable.getSelectedRow(); if (row == -1) return;
            int id = Integer.parseInt(invModel.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Archive this item?", "Confirm", 0) == 0) {
                try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement("UPDATE items SET is_archived = 1 WHERE id = ?")) {
                    ps.setInt(1, id); ps.executeUpdate();
                    refreshAdminInventory(); refreshAdminStats(); refreshDashboard();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT)); south.setOpaque(false); south.add(delBtn);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    private void styleTable(JTable table, int statusCol) {
        table.setRowHeight(60); table.setShowGrid(false);
        table.setFont(new Font("SansSerif", Font.PLAIN, 16));
        table.setBackground(CARD_BG); table.setForeground(TEXT_MAIN);

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setBackground(CARD_BG); header.setFont(new Font("SansSerif", Font.BOLD, 18));
        ((javax.swing.table.DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        if (statusCol != -1) table.getColumnModel().getColumn(statusCol).setCellRenderer(new StatusBadgeRenderer());
    }

    // Fetches active inventory
    private void refreshAdminInventory() {
        if (invModel == null) return;
        invModel.setRowCount(0);
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, item_name, item_type, date_added, status FROM items WHERE is_archived = 0")) {
            while (rs.next()) invModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private JPanel createAdminArchivePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        JLabel title = new JLabel("Archived Items");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel tableBox = new JPanel(new BorderLayout());
        tableBox.setBackground(CARD_BG);
        tableBox.setBorder(new javax.swing.border.LineBorder(OUTLINE, 1));

        String[] columns = {"ID", "Items", "Type", "Date Archived", "Status"};
        archiveModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        archiveTable = new JTable(archiveModel);
        styleTable(archiveTable, 4);
        archiveTable.getColumnModel().getColumn(0).setMinWidth(0); archiveTable.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane scroll = new JScrollPane(archiveTable);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        tableBox.add(scroll, BorderLayout.CENTER);
        panel.add(tableBox, BorderLayout.CENTER);

        JButton restoreBtn = new JButton("Restore Item");
        restoreBtn.setBackground(new Color(16, 185, 129)); restoreBtn.setForeground(Color.WHITE);
        restoreBtn.addActionListener(e -> {
            int row = archiveTable.getSelectedRow(); if (row == -1) return;
            int id = Integer.parseInt(archiveModel.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Restore to active inventory?", "Confirm", 0) == 0) {
                try (Connection conn = DBConnection.connect(); PreparedStatement ps = conn.prepareStatement("UPDATE items SET is_archived = 0 WHERE id = ?")) {
                    ps.setInt(1, id); ps.executeUpdate();
                    refreshAdminArchive(); refreshAdminInventory(); refreshAdminStats(); refreshDashboard();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        JButton permDelBtn = new JButton("Permanently Delete");
        permDelBtn.setBackground(new Color(186, 26, 26)); permDelBtn.setForeground(Color.WHITE);
        permDelBtn.addActionListener(e -> {
            int row = archiveTable.getSelectedRow(); if (row == -1) return;
            int id = Integer.parseInt(archiveModel.getValueAt(row, 0).toString());
            if (JOptionPane.showConfirmDialog(this, "Delete permanently?", "WARNING", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.connect()) {
                    conn.setAutoCommit(false);
                    try {
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM claims WHERE item_id = ?")) { ps.setInt(1, id); ps.executeUpdate(); }
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM items WHERE id = ?")) { ps.setInt(1, id); ps.executeUpdate(); }
                        conn.commit();
                        refreshAdminArchive(); refreshAdminStats(); refreshDashboard();
                    } catch (Exception ex) { conn.rollback(); throw ex; }
                    finally { conn.setAutoCommit(true); }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); south.setOpaque(false);
        south.add(restoreBtn); south.add(permDelBtn);
        panel.add(south, BorderLayout.SOUTH);

        return panel;
    }

    // Fetches archived items
    private void refreshAdminArchive() {
        if (archiveModel == null) return;
        archiveModel.setRowCount(0);
        try (Connection conn = DBConnection.connect(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, item_name, item_type, date_added, status FROM items WHERE is_archived = 1")) {
            while (rs.next()) archiveModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    class StatusBadgeRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String status = (value != null) ? value.toString() : "";
            JLabel label = new JLabel(status, SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(new Font("SansSerif", Font.BOLD, 13));
            label.setPreferredSize(new Dimension(110, 32));
            
            if ("Pending".equalsIgnoreCase(status)) {
                label.setBackground(new Color(255, 193, 7)); label.setForeground(Color.BLACK);
            } else if ("Approved".equalsIgnoreCase(status) || "Claimed".equalsIgnoreCase(status) || "Returned".equalsIgnoreCase(status) || "Found".equalsIgnoreCase(status)) {
                label.setBackground(new Color(40, 167, 69)); label.setForeground(Color.WHITE);
            } else if ("Rejected".equalsIgnoreCase(status) || "Denied".equalsIgnoreCase(status) || "Lost".equalsIgnoreCase(status)) {
                label.setBackground(new Color(220, 53, 69)); label.setForeground(Color.WHITE);
            } else {
                label.setBackground(UIManager.getColor("Table.background")); label.setForeground(UIManager.getColor("Label.disabledForeground"));
            }
            
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 14));
            wrapper.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            if (!isSelected && "Pending".equalsIgnoreCase(status)) wrapper.setBackground(new Color(255, 255, 225));
            wrapper.add(label);
            return wrapper;
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new UMAKSystemMain().setVisible(true)); }
}

