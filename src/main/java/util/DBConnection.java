package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/meeting_room_management?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "123456";

    private DBConnection() {
    }

    public static Connection openConnection() {
        String url = getEnvOrDefault("DB_URL", DEFAULT_URL);
        String username = getEnvOrDefault("DB_USERNAME", DEFAULT_USERNAME);
        String password = getEnvOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Chua cai dat mysql driver.", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Loi SQL: Ket noi that bai.", e);
        }
    }

    public static String getConnectionSummary() {
        String url = getEnvOrDefault("DB_URL", DEFAULT_URL);
        String username = getEnvOrDefault("DB_USERNAME", DEFAULT_USERNAME);
        return "URL=" + url + ", USERNAME=" + username;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    public static void initializeDatabase() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(50) UNIQUE NOT NULL,"
                + "password VARCHAR(255) NOT NULL,"
                + "full_name VARCHAR(100),"
                + "phone VARCHAR(20),"
                + "email VARCHAR(100),"
                + "role ENUM('EMPLOYEE', 'SUPPORT', 'ADMIN') NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";

        String createRooms = "CREATE TABLE IF NOT EXISTS rooms ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "capacity INT NOT NULL,"
                + "location VARCHAR(100),"
                + "description VARCHAR(100)"
                + ")";

        String createEquipments = "CREATE TABLE IF NOT EXISTS equipments ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "total_quantity INT NOT NULL,"
                + "available_quantity INT NOT NULL,"
                + "status VARCHAR(50)"
                + ")";

        String createServices = "CREATE TABLE IF NOT EXISTS services ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "name VARCHAR(100) NOT NULL,"
                + "price DECIMAL(10,2) DEFAULT 0"
                + ")";

        String createBookings = "CREATE TABLE IF NOT EXISTS bookings ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT,"
                + "room_id INT,"
                + "start_time DATETIME NOT NULL,"
                + "end_time DATETIME NOT NULL,"
                + "status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',"
                + "support_staff_id INT,"
                + "preparation_status ENUM('PREPARING', 'READY', 'MISSING_EQUIPMENT') DEFAULT 'PREPARING',"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (user_id) REFERENCES users(id),"
                + "FOREIGN KEY (room_id) REFERENCES rooms(id),"
                + "FOREIGN KEY (support_staff_id) REFERENCES users(id)"
                + ")";

        String createBookingDetails = "CREATE TABLE IF NOT EXISTS booking_details ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "booking_id INT,"
                + "equipment_id INT,"
                + "service_id INT,"
                + "quantity INT DEFAULT 1,"
                + "FOREIGN KEY (booking_id) REFERENCES bookings(id),"
                + "FOREIGN KEY (equipment_id) REFERENCES equipments(id),"
                + "FOREIGN KEY (service_id) REFERENCES services(id)"
                + ")";

        String seedEquipments = "INSERT INTO equipments (name, total_quantity, available_quantity, status) "
            + "SELECT 'May chieu', 5, 5, 'SAN_SANG' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM equipments) "
            + "UNION ALL "
            + "SELECT 'Loa di dong', 8, 8, 'SAN_SANG' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM equipments) "
            + "UNION ALL "
            + "SELECT 'Micro khong day', 10, 10, 'SAN_SANG' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM equipments)";

        String seedRooms = "INSERT INTO rooms (name, capacity, location, description) "
            + "SELECT 'Phong A1', 12, 'Tang 1', 'Phong hop nho cho team' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM rooms) "
            + "UNION ALL "
            + "SELECT 'Phong B1', 20, 'Tang 2', 'Phong hop trung binh cho phong ban' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM rooms) "
            + "UNION ALL "
            + "SELECT 'Phong C1', 40, 'Tang 3', 'Phong hop lon cho su kien noi bo' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM rooms)";

        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.execute(createUsers);
            statement.execute(createRooms);
            statement.execute(createEquipments);
            statement.execute(createServices);
            statement.execute(createBookings);
            statement.execute(createBookingDetails);
            statement.executeUpdate(seedEquipments);
            statement.executeUpdate(seedRooms);

            String currentDb = "(khong xac dinh)";
            try (ResultSet rs = statement.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    currentDb = rs.getString(1);
                }
            }

            int tableCount = 0;
            StringBuilder tableNames = new StringBuilder();
            try (ResultSet rs = statement.executeQuery("SHOW TABLES")) {
                while (rs.next()) {
                    if (tableCount > 0) {
                        tableNames.append(", ");
                    }
                    tableNames.append(rs.getString(1));
                    tableCount++;
                }
            }

            System.out.println("Da khoi tao schema tren CSDL: " + currentDb + ".");
            System.out.println("Tong so bang hien co: " + tableCount + ". Danh sach: " + tableNames);
        } catch (SQLException e) {
            throw new IllegalStateException("Khoi tao schema that bai.", e);
        }
    }
}
