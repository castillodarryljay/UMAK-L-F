import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
 * Database manager for UMAK Lost & Found.
 * Handles MySQL connection and table setup.
 */
public class DBConnection {
    private static boolean migrated = false;
    private static final String PASSWORD_PREFIX = "ENC:";
    private static final String SECRET_SEED = "UMAK-LF-SIMPLE-KEY";

    // Establishes connection to the local database
    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/UMAK_LostFound", "root", "");
            
            // Setup tables if first time connecting
            if (!migrated) {
                ensureSchema(conn);
                migratePlaintextPasswords(conn);
                autoArchiveOldItems(conn);
                migrated = true;
            }
            
            return conn;
        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
            return null;
        }
    }

    // Encrypts a plaintext password for storage
    public static String encryptPassword(String plain) {
        if (plain == null || plain.isEmpty()) return plain;
        try {
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return PASSWORD_PREFIX
                + Base64.getEncoder().encodeToString(iv)
                + ":"
                + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            System.out.println("Password Encrypt Error: " + e.getMessage());
            return plain;
        }
    }

    // Decrypts a stored password for local verification
    public static String decryptPassword(String stored) {
        if (stored == null || !stored.startsWith(PASSWORD_PREFIX)) return stored;
        try {
            String payload = stored.substring(PASSWORD_PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) return stored;
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Password Decrypt Error: " + e.getMessage());
            return stored;
        }
    }

    private static SecretKeySpec getSecretKey() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(SECRET_SEED.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(Arrays.copyOf(keyBytes, 16), "AES");
    }

    private static void migratePlaintextPasswords(Connection conn) {
        String selectSql = "SELECT id, password FROM users WHERE password IS NOT NULL AND password NOT LIKE 'ENC:%'";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement selectPs = conn.prepareStatement(selectSql);
             ResultSet rs = selectPs.executeQuery();
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String plain = rs.getString("password");
                String encrypted = encryptPassword(plain);
                updatePs.setString(1, encrypted);
                updatePs.setInt(2, id);
                updatePs.addBatch();
            }
            updatePs.executeBatch();
        } catch (Exception e) {
            System.out.println("Password Migration Note: " + e.getMessage());
        }
    }

    // Automatically archives processed items older than 30 days
    private static void autoArchiveOldItems(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            int rows = stmt.executeUpdate("UPDATE items SET is_archived = 1 " +
                                       "WHERE date_added < DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                                       "AND is_archived = 0 " +
                                       "AND status != 'Pending'");
            if (rows > 0) {
                System.out.println("Auto-archived " + rows + " processed items older than 30 days.");
            }
        } catch (Exception e) {
            System.out.println("Auto-archive Note: " + e.getMessage());
        }
    }

    // Creates or updates tables to match current requirements
    private static void ensureSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            DatabaseMetaData meta = conn.getMetaData();

            // Add new columns to users table if they don't exist
            if (!meta.getColumns(null, null, "users", "department").next()) {
                stmt.execute("ALTER TABLE users ADD COLUMN department VARCHAR(255) AFTER password");
            }
            if (!meta.getColumns(null, null, "users", "is_online").next()) {
                stmt.execute("ALTER TABLE users ADD COLUMN is_online BOOLEAN DEFAULT 0");
            }
            if (!meta.getColumns(null, null, "users", "last_seen").next()) {
                stmt.execute("ALTER TABLE users ADD COLUMN last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
            }

            // Setup and update items table columns
            if (!meta.getColumns(null, null, "items", "item_type").next()) {
                stmt.execute("ALTER TABLE items ADD COLUMN item_type ENUM('Lost', 'Found') AFTER reporter_id");
                stmt.execute("UPDATE items SET item_type = 'Lost' WHERE status = 'Lost'");
                stmt.execute("UPDATE items SET item_type = 'Found' WHERE status = 'Found'");
                stmt.execute("UPDATE items SET item_type = 'Found' WHERE status = 'Claimed'"); 
            }

            stmt.execute("ALTER TABLE items MODIFY COLUMN status VARCHAR(50) DEFAULT 'Pending'");
            stmt.execute("UPDATE items SET status = 'Pending' WHERE status IN ('Lost', 'Found')");
            
            if (!meta.getColumns(null, null, "items", "date_lost").next()) {
                stmt.execute("ALTER TABLE items ADD COLUMN date_lost DATE AFTER reporter_id");
            }
            if (!meta.getColumns(null, null, "items", "time_lost").next()) {
                stmt.execute("ALTER TABLE items ADD COLUMN time_lost TIME AFTER date_lost");
            }
            if (!meta.getColumns(null, null, "items", "is_archived").next()) {
                stmt.execute("ALTER TABLE items ADD COLUMN is_archived TINYINT(1) DEFAULT 0 AFTER time_lost");
            }
            if (!meta.getColumns(null, null, "items", "edit_count").next()) {
                stmt.execute("ALTER TABLE items ADD COLUMN edit_count INT DEFAULT 0 AFTER is_archived");
            }
            
            // Create claims table if missing
            if (!meta.getTables(null, null, "claims", null).next()) {
                stmt.execute("CREATE TABLE claims (" +
                            "claim_id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "item_id INT, " +
                            "user_id INT, " +
                            "student_name VARCHAR(100), " +
                            "student_email VARCHAR(100), " +
                            "claim_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "status VARCHAR(20) DEFAULT 'Pending', " +
                            "justification TEXT, " +
                            "FOREIGN KEY (item_id) REFERENCES items(id), " +
                            "FOREIGN KEY (user_id) REFERENCES users(id))");
            } else {
                // Update claims table structure if needed
                if (!meta.getColumns(null, null, "claims", "user_id").next()) {
                    if (meta.getColumns(null, null, "claims", "claimant_id").next()) {
                        stmt.execute("ALTER TABLE claims CHANGE COLUMN claimant_id user_id INT");
                    } else {
                        stmt.execute("ALTER TABLE claims ADD COLUMN user_id INT AFTER item_id");
                    }
                }
                if (!meta.getColumns(null, null, "claims", "student_name").next()) {
                    stmt.execute("ALTER TABLE claims ADD COLUMN student_name VARCHAR(100) AFTER user_id");
                }
                if (!meta.getColumns(null, null, "claims", "student_email").next()) {
                    stmt.execute("ALTER TABLE claims ADD COLUMN student_email VARCHAR(100) AFTER student_name");
                }
                if (!meta.getColumns(null, null, "claims", "justification").next()) {
                    stmt.execute("ALTER TABLE claims ADD COLUMN justification TEXT AFTER status");
                }
                if (!meta.getColumns(null, null, "claims", "image_proof").next()) {
                    stmt.execute("ALTER TABLE claims ADD COLUMN image_proof VARCHAR(255) AFTER justification");
                }
                if (!meta.getColumns(null, null, "claims", "claim_date").next()) {
                    stmt.execute("ALTER TABLE claims ADD COLUMN claim_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER student_email");
                }
                if (!meta.getColumns(null, null, "claims", "claim_id").next()) {
                    if (meta.getColumns(null, null, "claims", "id").next()) {
                        stmt.execute("ALTER TABLE claims CHANGE COLUMN id claim_id INT AUTO_INCREMENT");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Schema Note: " + e.getMessage());
        }
    }

    // Locates item images in common directories
    public static java.io.File resolveImagePath(String path) {
        if (path == null || path.isEmpty()) return null;
        java.io.File f = new java.io.File(path);
        if (f.exists()) return f;
        java.io.File[] paths = { 
            new java.io.File("website/" + path), 
            new java.io.File("C:/xampp/htdocs/umak/" + path), 
            new java.io.File("uploads/" + f.getName()) 
        };
        for (java.io.File p : paths) if (p.exists()) return p;
        return null;
    }
}

