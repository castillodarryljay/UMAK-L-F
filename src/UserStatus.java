/*
 * Global session to store the logged-in user's details.
 */
public class UserStatus {
    public static int userId;
    public static String fullName;
    public static String role;
    public static String studentNo;
    public static String email;
    public static String department;

    // Sets session data after successful login
    public static void init(int id, String name, String r, String sno, String mail, String dept) {
        userId = id;
        fullName = name;
        role = r;
        studentNo = sno;
        email = mail;
        department = dept;
    }

    // Clears data when logging out
    public static void clear() {
        userId = 0;
        fullName = null;
        role = null;
        studentNo = null;
        email = null;
        department = null;
    }
}

