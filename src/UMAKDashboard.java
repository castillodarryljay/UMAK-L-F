import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.io.File;

/*
 * Dashboard panel that shows all lost and found items.
 * Includes search and category filters.
 */
public class UMAKDashboard extends JPanel {
    private JPanel grid;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private Color surfaceColor;
    private Color cardColor;

    public UMAKDashboard(Color surfaceColor, Color cardColor) {
        this.surfaceColor = surfaceColor;
        this.cardColor = cardColor;

        setBackground(surfaceColor);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel pageTitle = new JLabel("Browse Lost & Found Items");
        pageTitle.setFont(new Font("Inter", Font.BOLD, 32));

        JPanel topBar = new JPanel(new GridBagLayout());
        topBar.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 10);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(400, 40));
        searchField.addActionListener(e -> refreshData());
        gbc.weightx = 1.0; gbc.gridx = 0; topBar.add(searchField, gbc);

        categoryCombo = new JComboBox<>(new String[]{"All Categories", "ID Card", "Electronics", "Wallet", "Documents", "Keys", "Bags", "Other"});
        categoryCombo.setPreferredSize(new Dimension(160, 40));
        categoryCombo.addActionListener(e -> refreshData());
        gbc.weightx = 0; gbc.gridx = 1; topBar.add(categoryCombo, gbc);

        JButton filterBtn = new JButton("Refresh");
        filterBtn.setPreferredSize(new Dimension(100, 40));
        filterBtn.addActionListener(e -> refreshData());
        gbc.gridx = 2; topBar.add(filterBtn, gbc);

        JPanel headerContainer = new JPanel(new BorderLayout(0, 20));
        headerContainer.setOpaque(false);
        headerContainer.add(pageTitle, BorderLayout.NORTH);
        headerContainer.add(topBar, BorderLayout.CENTER);
        add(headerContainer, BorderLayout.NORTH);

        grid = new JPanel(new GridLayout(0, 3, 25, 25));
        grid.setOpaque(false);

        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setOpaque(false);
        gridContainer.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(gridContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    // Fetches items from the database based on filters
    public void refreshData() {
        grid.removeAll();
        String search = searchField.getText();
        String cat = categoryCombo.getSelectedItem().toString();

        String sql = "SELECT * FROM items WHERE is_archived = 0 AND status NOT IN ('Returned', 'Denied')";
        if (!search.isEmpty()) sql += " AND (item_name LIKE ? OR description LIKE ?)";
        if (!cat.equals("All Categories")) sql += " AND category = ?";
        sql += " ORDER BY date_added DESC";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int idx = 1;
            if (!search.isEmpty()) {
                ps.setString(idx++, "%" + search + "%");
                ps.setString(idx++, "%" + search + "%");
            }
            if (!cat.equals("All Categories")) ps.setString(idx++, cat);

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasItems = false;
                while (rs.next()) {
                    grid.add(createItemCard(rs));
                    hasItems = true;
                }
                if (!hasItems) {
                    JLabel emptyLabel = new JLabel("No items found. Try different filters.", SwingConstants.CENTER);
                    emptyLabel.setFont(new Font("Inter", Font.ITALIC, 16));
                    emptyLabel.setForeground(Color.GRAY);
                    grid.add(emptyLabel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        grid.revalidate();
        grid.repaint();
    }

    // Creates a UI card for a single item
    private JPanel createItemCard(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("item_name");
        String time = rs.getString("date_added");
        String loc = rs.getString("location");
        String path = rs.getString("image_path");
        String category = rs.getString("category");
        String description = rs.getString("description");
        String status = rs.getString("status");
        String itemType = rs.getString("item_type");
        int reporterId = rs.getInt("reporter_id");
        int editCount = rs.getInt("edit_count");

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(cardColor);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel picLabel = new JLabel("", SwingConstants.CENTER);
        picLabel.setPreferredSize(new Dimension(200, 180));
        picLabel.setOpaque(true);
        picLabel.setBackground(new Color(248, 250, 252));

        File imgFile = DBConnection.resolveImagePath(path);
        if (imgFile != null && imgFile.exists()) {
            ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
            Image img = icon.getImage();
            if (img.getWidth(null) > 0) {
                double ratio = Math.min(220.0 / img.getWidth(null), 200.0 / img.getHeight(null));
                picLabel.setIcon(new ImageIcon(img.getScaledInstance((int)(img.getWidth(null)*ratio), (int)(img.getHeight(null)*ratio), Image.SCALE_SMOOTH)));
            }
        } else {
            picLabel.setText("No Image");
            picLabel.setForeground(Color.LIGHT_GRAY);
        }

        JLabel typeTag = new JLabel(itemType.toUpperCase(), SwingConstants.CENTER);
        typeTag.setOpaque(true);
        typeTag.setFont(new Font("Inter", Font.BOLD, 11));
        typeTag.setForeground(Color.WHITE);
        typeTag.setBackground(itemType.equalsIgnoreCase("Found") ? new Color(16, 185, 129) : new Color(249, 115, 22));
        typeTag.setPreferredSize(new Dimension(75, 26));
        typeTag.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JPanel overlay = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        overlay.setOpaque(false);
        overlay.add(typeTag);
        picLabel.setLayout(new BorderLayout());
        picLabel.add(overlay, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Inter", Font.BOLD, 16));

        JLabel metaLbl = new JLabel(category + " • " + loc);
        metaLbl.setFont(new Font("Inter", Font.PLAIN, 12));
        metaLbl.setForeground(Color.GRAY);

        JLabel statusLbl = new JLabel("Status: " + status);
        statusLbl.setFont(new Font("Inter", Font.BOLD, 12));
        statusLbl.setForeground(new Color(59, 130, 246));

        info.add(titleLbl);
        info.add(Box.createVerticalStrut(5));
        info.add(metaLbl);
        info.add(Box.createVerticalStrut(5));
        info.add(statusLbl);

        card.add(picLabel, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    new ItemDetailModal((JFrame) SwingUtilities.getWindowAncestor(card), id, title, category, time, loc, description, path, reporterId, status, itemType, surfaceColor, cardColor, UMAKDashboard.this, editCount).setVisible(true);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(59, 130, 246), 1),
                    BorderFactory.createEmptyBorder(2, 2, 4, 4)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(2, 2, 4, 4)
                ));
            }
        });

        return card;
    }
}

/*
 * Dialog showing detailed information about a specific item.
 */
class ItemDetailModal extends JDialog {
    private UMAKDashboard dashboard;

    public ItemDetailModal(JFrame parent, int itemId, String title, String category, String time, String loc, String description, String path, int reporterId, String status, String itemType, Color surfaceColor, Color cardColor, UMAKDashboard dashboard, int editCount) {
        super(parent, "Item Details", true);
        this.dashboard = dashboard;
        setSize(500, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(15, 15));
        container.setBackground(cardColor);
        container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel typeTag = new JLabel(itemType, SwingConstants.CENTER);
        typeTag.setOpaque(true);
        typeTag.setBackground(itemType.equalsIgnoreCase("Found") ? new Color(0, 150, 136) : new Color(240, 128, 128));
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
        bigPic.setBackground(surfaceColor);
        bigPic.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        File imgFile = DBConnection.resolveImagePath(path);
        if (imgFile != null && imgFile.exists()) {
            bigPic.setIcon(new ImageIcon(new ImageIcon(imgFile.getAbsolutePath()).getImage().getScaledInstance(450, 250, Image.SCALE_SMOOTH)));
        } else {
            bigPic.setText("PIC");
            bigPic.setForeground(Color.GRAY);
            bigPic.setFont(new Font("SansSerif", Font.BOLD, 80));
        }
        container.add(bigPic, BorderLayout.CENTER);

        JPanel details = new JPanel(new GridLayout(5, 2, 5, 5));
        details.setOpaque(false);

        String reporterName = "Unknown";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT full_name FROM users WHERE id = ?")) {
            ps.setInt(1, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) reporterName = rs.getString("full_name");
            }
        } catch (Exception e) {}

        details.add(new JLabel("Category") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(category));
        details.add(new JLabel("Posted By") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(reporterName));
        details.add(new JLabel("Date Added") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(time));
        details.add(new JLabel("Location") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(loc));
        details.add(new JLabel("Status") {{ setFont(new Font("SansSerif", Font.BOLD, 12)); }});
        details.add(new JLabel(status));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);

        JLabel descHeader = new JLabel("Description");
        descHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel descBody = new JLabel("<html>" + description + "</html>");

        JPanel claimBox = new JPanel(new BorderLayout(10, 10));
        claimBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            itemType.equalsIgnoreCase("Lost") ? "Found this Item?" : "Claim this Item?"));
        claimBox.setOpaque(false);

        JLabel claimInstruction = new JLabel(itemType.equalsIgnoreCase("Lost") ? 
            "<html><small>If you found this item, file a report to notify the owner.</small></html>" : 
            "<html><small>If this belongs to you, file a claim for verification.</small></html>");
        JButton claimBtn = new JButton(itemType.equalsIgnoreCase("Lost") ? "File a report" : "File a claim");
        claimBtn.setBackground(new Color(16, 185, 129));
        claimBtn.setForeground(Color.WHITE);

        // Check if already claimed
// Check user's claim status and details
        boolean hasUserClaim = false;
        int userClaimId = -1;
        String userClaimStatus = "";
        String userJustification = "";
        String userImage = "";

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT claim_id, status, justification, image_proof FROM claims WHERE item_id = ? AND user_id = ?")) {
            ps.setInt(1, itemId);
            ps.setInt(2, UserStatus.userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    hasUserClaim = true;
                    userClaimId = rs.getInt("claim_id");
                    userClaimStatus = rs.getString("status");
                    userJustification = rs.getString("justification");
                    userImage = rs.getString("image_proof");
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }

        if (hasUserClaim) {
            if ("Pending".equalsIgnoreCase(userClaimStatus)) {
                // Dilaw na pallete para sa Pending at clickable para ma-edit/cancel
                claimBtn.setText("Pending (Edit / Cancel)");
                claimBtn.setBackground(new Color(234, 179, 8)); // Yellow color
                claimBtn.setForeground(Color.BLACK);
                
                final int cid = userClaimId;
                final String cJust = userJustification;
                final String cImg = userImage;
                
                claimBtn.addActionListener(e -> {
                    new EditClaimModal(parent, cid, itemId, itemType, cJust, cImg, dashboard).setVisible(true);
                    dispose();
                });
            } else {
                claimBtn.setEnabled(false);
                claimBtn.setText("Status: " + userClaimStatus);
                claimBtn.setBackground(Color.GRAY);
            }
        } else if ("Claimed".equalsIgnoreCase(status) || "Returned".equalsIgnoreCase(status)) {
            claimBtn.setEnabled(false);
            claimBtn.setText("Already Claimed");
            claimBtn.setBackground(Color.GRAY);
        } else {
            // Default na paggawa ng bagong claim
            claimBtn.addActionListener(e -> {
                new ClaimModal(parent, itemId, itemType, dashboard).setVisible(true);
                dispose();
            });
        }

        claimBox.add(claimInstruction, BorderLayout.CENTER);
        claimBox.add(claimBtn, BorderLayout.SOUTH);

        bottom.add(descHeader); bottom.add(descBody);
        bottom.add(Box.createVerticalStrut(20));

        if ("Admin".equalsIgnoreCase(UserStatus.role) || UserStatus.userId == reporterId) {
            JPanel actionPanel = new JPanel(new GridLayout(1, 0, 10, 0));
            actionPanel.setOpaque(false);

            if ("Admin".equalsIgnoreCase(UserStatus.role)) {
                JButton manageBtn = new JButton("MANAGE");
                manageBtn.setBackground(new Color(13, 27, 42));
                manageBtn.setForeground(Color.WHITE);
                manageBtn.addActionListener(e -> {
                    dispose();
                    if (parent instanceof UMAKSystemMain) {
                        ((UMAKSystemMain) parent).navigateToClaims(itemId);
                    }
                });
                actionPanel.add(manageBtn);
            }

            JButton editBtn = new JButton("Edit Item");
            editBtn.setBackground(new Color(59, 130, 246));
            editBtn.setForeground(Color.WHITE);
            
            if (!"Admin".equalsIgnoreCase(UserStatus.role) && editCount >= 1) {
                editBtn.setEnabled(false);
                editBtn.setText("Edit (Used)");
                editBtn.setBackground(Color.GRAY);
            }

            editBtn.addActionListener(e -> {
                dispose();
                new EditItemModal(parent, itemId, title, category, loc, description, status, itemType, surfaceColor, cardColor, dashboard).setVisible(true);
            });

            JButton archiveBtn = new JButton("Archive Item");
            archiveBtn.setBackground(new Color(239, 68, 68));
            archiveBtn.setForeground(Color.WHITE);
            archiveBtn.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(this, "Hide this item from the public list?", "Archive", 0) == 0) {
                    try (Connection conn = DBConnection.connect();
                         PreparedStatement ps = conn.prepareStatement("UPDATE items SET is_archived = 1 WHERE id = ?")) {
                        ps.setInt(1, itemId);
                        ps.executeUpdate();
                        dashboard.refreshData(); dispose();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });

            actionPanel.add(editBtn);
            if ("Admin".equalsIgnoreCase(UserStatus.role)) {
                actionPanel.add(archiveBtn);
            }
            bottom.add(actionPanel);
        } else {
            bottom.add(claimBox);
        }

        JPanel infoWrapper = new JPanel(new BorderLayout());
        infoWrapper.setOpaque(false);
        infoWrapper.add(details, BorderLayout.NORTH);
        infoWrapper.add(bottom, BorderLayout.CENTER);

        container.add(infoWrapper, BorderLayout.SOUTH);
        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll);
    }
}

/*
 * Dialog to update item information.
 */
class EditItemModal extends JDialog {
    public EditItemModal(JFrame parent, int itemId, String title, String category, String loc, String description, String status, String itemType, Color surfaceColor, Color cardColor, UMAKDashboard dashboard) {
        super(parent, "Edit Item", true);
        setSize(500, 750);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(cardColor);
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(0, 0, 20, 0);

        JTextField titleF = new JTextField(title);
        titleF.setPreferredSize(new Dimension(0, 45));
        titleF.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        
        JComboBox<String> catC = new JComboBox<>(new String[]{"ID Card", "Electronics", "Wallet", "Documents", "Keys", "Bags", "Other"});
        catC.setSelectedItem(category);
        catC.setPreferredSize(new Dimension(0, 45));
        catC.setBackground(Color.WHITE);

        JTextField locF = new JTextField(loc);
        locF.setPreferredSize(new Dimension(0, 45));
        locF.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JComboBox<String> statC = new JComboBox<>(new String[]{"Pending", "Approved", "Claimed", "Returned", "Denied"});
        statC.setSelectedItem(status);
        statC.setPreferredSize(new Dimension(0, 45));
        statC.setBackground(Color.WHITE);

        JTextArea descA = new JTextArea(description, 5, 20);
        descA.setLineWrap(true); descA.setWrapStyleWord(true);
        descA.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int row = 0;
        addEditField(p, "Item Title", titleF, c, row++);
        addEditField(p, "Category", catC, c, row++);
        addEditField(p, "Location", locF, c, row++);
        addEditField(p, "Status", statC, c, row++);
        
        JScrollPane descScroll = new JScrollPane(descA);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        addEditField(p, "Description", descScroll, c, row++);

        JButton save = new JButton("Save Changes");
        save.setBackground(new Color(59, 130, 246));
        save.setForeground(Color.WHITE);
        save.setFont(new Font("Inter", Font.BOLD, 15));
        save.setPreferredSize(new Dimension(0, 50));
        save.setCursor(new Cursor(Cursor.HAND_CURSOR));

        save.addActionListener(e -> {
            if (titleF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title is required."); return;
            }
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("UPDATE items SET item_name=?, category=?, location=?, description=?, status=?, edit_count=edit_count+1 WHERE id=?")) {
                ps.setString(1, titleF.getText()); ps.setString(2, catC.getSelectedItem().toString());
                ps.setString(3, locF.getText()); ps.setString(4, descA.getText());
                ps.setString(5, statC.getSelectedItem().toString()); ps.setInt(6, itemId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Item updated!");
                dashboard.refreshData(); dispose();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        c.gridy = row * 2; c.insets = new Insets(20, 0, 0, 0); p.add(save, c);
        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll);
    }

    private void addEditField(JPanel p, String label, JComponent f, GridBagConstraints c, int row) {
        c.gridy = row * 2;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Inter", Font.BOLD, 13));
        l.setForeground(new Color(71, 85, 105));
        c.insets = new Insets(0, 0, 5, 0);
        p.add(l, c);

        c.gridy = row * 2 + 1;
        c.insets = new Insets(0, 0, 20, 0);
        p.add(f, c);
    }
}


//Dialog to file a claim with justification and optional image.
class ClaimModal extends JDialog {
    private File selectedImage = null;

    public ClaimModal(JFrame parent, int itemId, String itemType, UMAKDashboard dashboard) {
        super(parent, itemType.equalsIgnoreCase("Lost") ? "File a Report" : "File a Claim", true);
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(0, 0, 15, 0);

        JTextArea justA = new JTextArea(6, 20);
        justA.setLineWrap(true); justA.setWrapStyleWord(true);
        justA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        justA.setFont(new Font("Inter", Font.PLAIN, 14));

        int row = 0;
        addLabel(p, itemType.equalsIgnoreCase("Lost") ? "How/Where did you find this item?" : "Why does this belong to you? (Proof)", c, row++);
        c.gridy = row++ * 2 - 1; 
        c.insets = new Insets(0, 0, 25, 0);
        p.add(new JScrollPane(justA), c);

        addLabel(p, "Optional: Upload Image Proof", c, row++);
        JLabel uploadLabel = new JLabel("Click to select image", SwingConstants.CENTER);
        uploadLabel.setBorder(BorderFactory.createDashedBorder(new Color(148, 163, 184), 3, 2));
        uploadLabel.setPreferredSize(new Dimension(0, 100));
        uploadLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        uploadLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedImage = chooser.getSelectedFile();
                    uploadLabel.setText(selectedImage.getName());
                    uploadLabel.setBorder(BorderFactory.createLineBorder(new Color(16, 185, 129), 2));
                }
            }
        });
        c.gridy = row++ * 2 - 1; 
        c.insets = new Insets(0, 0, 30, 0);
        p.add(uploadLabel, c);

        JButton submit = new JButton(itemType.equalsIgnoreCase("Lost") ? "Submit Report" : "Submit Claim");
        submit.setBackground(new Color(16, 185, 129));
        submit.setForeground(Color.WHITE);
        submit.setFont(new Font("Inter", Font.BOLD, 16));
        submit.setPreferredSize(new Dimension(300, 50));
        submit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        submit.addActionListener(e -> {
            String justification = justA.getText().trim();
            if (justification.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please provide a justification."); 
                return;
            }
            
            try (Connection conn = DBConnection.connect()) {
                if (conn == null) {
                    JOptionPane.showMessageDialog(this, "Database connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String sql = "INSERT INTO claims (item_id, user_id, student_name, student_email, justification, image_proof, status, claim_date) VALUES (?,?,?,?,?,?,?,NOW())";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, itemId);
                    ps.setInt(2, UserStatus.userId);
                    ps.setString(3, UserStatus.fullName);
                    ps.setString(4, UserStatus.email);
                    ps.setString(5, justification);
                    ps.setString(6, selectedImage != null ? selectedImage.getAbsolutePath() : "");
                    ps.setString(7, "Pending");
                    ps.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Report filed successfully!");
                    if (dashboard != null) dashboard.refreshData();
                    dispose();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        c.gridy = 10; 
        c.insets = new Insets(10, 0, 0, 0);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        p.add(submit, c);
        
        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private void addLabel(JPanel p, String text, GridBagConstraints c, int row) {
        c.gridy = row * 2;
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.BOLD, 13));
        l.setForeground(new Color(71, 85, 105));
        c.insets = new Insets(0, 0, 8, 0);
        p.add(l, c);
    }
}


// Dialog to edit or cancel an existing user claim.
class EditClaimModal extends JDialog {
    private File selectedImage = null;

    public EditClaimModal(JFrame parent, int claimId, int itemId, String itemType, String justification, String imagePath, UMAKDashboard dashboard) {
        super(parent, itemType.equalsIgnoreCase("Lost") ? "Edit or Cancel Report" : "Edit or Cancel Claim", true);
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(0, 0, 15, 0);

        JTextArea justA = new JTextArea(justification, 6, 20);
        justA.setLineWrap(true); justA.setWrapStyleWord(true);
        justA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        justA.setFont(new Font("Inter", Font.PLAIN, 14));

        int row = 0;
        addLabel(p, itemType.equalsIgnoreCase("Lost") ? "Update your report details:" : "Update your justification:", c, row++);
        c.gridy = row++ * 2 - 1; 
        c.insets = new Insets(0, 0, 25, 0);
        p.add(new JScrollPane(justA), c);

        addLabel(p, "Optional: Update Image Proof", c, row++);
        JLabel uploadLabel = new JLabel("Click to select new image", SwingConstants.CENTER);
        if (imagePath != null && !imagePath.isEmpty()) {
            uploadLabel.setText("Existing image saved. Click to change.");
        }
        uploadLabel.setBorder(BorderFactory.createDashedBorder(new Color(148, 163, 184), 3, 2));
        uploadLabel.setPreferredSize(new Dimension(0, 100));
        uploadLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        uploadLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    selectedImage = chooser.getSelectedFile();
                    uploadLabel.setText(selectedImage.getName());
                    uploadLabel.setBorder(BorderFactory.createLineBorder(new Color(16, 185, 129), 2));
                }
            }
        });
        c.gridy = row++ * 2 - 1; 
        c.insets = new Insets(0, 0, 30, 0);
        p.add(uploadLabel, c);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);

        JButton updateBtn = new JButton(itemType.equalsIgnoreCase("Lost") ? "Update Report" : "Update Claim");
        updateBtn.setBackground(new Color(59, 130, 246)); // Blue
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Inter", Font.BOLD, 15));
        updateBtn.setPreferredSize(new Dimension(0, 50));
        updateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton cancelBtn = new JButton(itemType.equalsIgnoreCase("Lost") ? "Cancel" : "Cancel");
        cancelBtn.setBackground(new Color(239, 68, 68)); // Red
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(new Font("Inter", Font.BOLD, 15));
        cancelBtn.setPreferredSize(new Dimension(0, 50));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        updateBtn.addActionListener(e -> {
            String newJustification = justA.getText().trim();
            if (newJustification.isEmpty()) {
                JOptionPane.showMessageDialog(this, itemType.equalsIgnoreCase("Lost") ? "Please provide report details." : "Please provide a justification."); 
                return;
            }
            try (Connection conn = DBConnection.connect()) {
                String sql = selectedImage != null ? 
                    "UPDATE claims SET justification=?, image_proof=? WHERE claim_id=?" : 
                    "UPDATE claims SET justification=? WHERE claim_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, newJustification);
                    if (selectedImage != null) {
                        ps.setString(2, selectedImage.getAbsolutePath());
                        ps.setInt(3, claimId);
                    } else {
                        ps.setInt(2, claimId);
                    }
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, itemType.equalsIgnoreCase("Lost") ? "Report updated successfully!" : "Claim updated successfully!");
                    if (dashboard != null) dashboard.refreshData();
                    dispose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, itemType.equalsIgnoreCase("Lost") ? "Are you sure you want to cancel and delete this report?" : "Are you sure you want to cancel and delete this claim?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DBConnection.connect();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM claims WHERE claim_id=?")) {
                    ps.setInt(1, claimId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, itemType.equalsIgnoreCase("Lost") ? "Report cancelled successfully." : "Claim cancelled successfully.");
                    if (dashboard != null) dashboard.refreshData();
                    dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        buttonPanel.add(updateBtn);
        buttonPanel.add(cancelBtn);

        c.gridy = 10; 
        c.insets = new Insets(10, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(buttonPanel, c);
        
        JScrollPane scroll = new JScrollPane(p);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private void addLabel(JPanel p, String text, GridBagConstraints c, int row) {
        c.gridy = row * 2;
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.BOLD, 13));
        l.setForeground(new Color(71, 85, 105));
        c.insets = new Insets(0, 0, 8, 0);
        p.add(l, c);
    }
}
