# Tài Liệu Chi Tiết Dự Án Quản Lý Phòng Họp (Meeting Room Management)

## 1. Mục tiêu tài liệu
Tài liệu này giúp bạn hiểu dự án từ đầu đến cuối theo cách dễ theo dõi:
- Kiến trúc tổng thể và vai trò từng tầng.
- Luồng xử lý chính của từng chức năng (Đăng nhập, Đặt phòng, Duyệt booking, Cập nhật chuẩn bị...).
- Mô tả chi tiết logic kiểm tra dữ liệu (validate), nghiệp vụ, truy vấn DB.
- Trích code thực tế kèm vị trí file để bạn mở và đối chiếu trực tiếp.

---

## 2. Tổng quan dự án
Đây là ứng dụng Java chạy console để quản lý:
- Người dùng (EMPLOYEE, SUPPORT, ADMIN).
- Phòng họp.
- Thiết bị đi kèm.
- Dịch vụ đi kèm.
- Yêu cầu đặt phòng và quy trình duyệt.

Nguyên tắc kiến trúc:
- `Presentation`: nhận input/hiển thị output.
- `Service`: xử lý nghiệp vụ.
- `DAO`: truy vấn cơ sở dữ liệu.
- `Model`: biểu diễn dữ liệu.
- `Util`: tiện ích dùng chung.

---

## 3. Công nghệ và dependency
Vị trí file: `build.gradle`

```gradle
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm'
}

dependencies {
    testImplementation platform('org.junit:junit-bom:5.10.0')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    implementation("com.mysql:mysql-connector-j:9.6.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
}
```

Ý nghĩa:
- Dùng MySQL Connector để truy cập DB.
- Dùng BCrypt để hash/verify mật khẩu.
- Dùng JUnit 5 cho test.

---

## 4. Cấu trúc thư mục chính
- `src/main/java/model`: lớp dữ liệu.
- `src/main/java/model/enums`: enum nghiệp vụ.
- `src/main/java/dao`: interface DAO.
- `src/main/java/dao/impl`: triển khai SQL cụ thể.
- `src/main/java/service`: nghiệp vụ.
- `src/main/java/presentation`: màn hình console chính.
- `src/main/java/presentation/admin`: UI quản trị theo module.
- `src/main/java/presentation/employee`: UI đặt phòng (employee).
- `src/main/java/util`: tiện ích (DB, validate, console, datetime, password, table).

---

## 5. Điểm bắt đầu chương trình
Vị trí file: `src/main/java/presentation/Main.java`

```java
public class Main {
    public static void main(String[] args) {
        try {
            DBConnection.initializeDatabase();
            System.out.println("Ket noi CSDL thanh cong (" + DBConnection.getConnectionSummary() + ").");
            new AuthUI().run();
        } catch (IllegalStateException e) {
            System.out.println("Khoi dong that bai: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Da xay ra loi khong mong muon: " + e.getMessage());
        }
    }
}
```

Giải thích:
1. Khởi tạo schema DB nếu chưa có.
2. In thông tin kết nối.
3. Mở màn hình xác thực (`AuthUI`).
4. Có bắt lỗi để tránh crash thẳng ra terminal.

---

## 6. Tầng Model (dữ liệu)

### 6.1 `Booking`
Vị trí file: `src/main/java/model/Booking.java`

```java
private Integer bookingId;
private Integer userId;
private Integer roomId;
private LocalDateTime startTime;
private LocalDateTime endTime;
private BookingStatus status;
private Integer supportStaffId;
private PreparationStatus preparationStatus;
private LocalDateTime createdAt;
```

Ý nghĩa cốt lõi:
- `status`: trạng thái phê duyệt (`PENDING/APPROVED/REJECTED`).
- `supportStaffId`: support được gán (nếu có).
- `preparationStatus`: mức chuẩn bị (`PREPARING/READY/MISSING_EQUIPMENT`).

### 6.2 Các model còn lại
- `Room`: tên, sức chứa, vị trí, mô tả.
- `Equipment`: tổng số lượng, khả dụng, trạng thái.
- `Service`: tên dịch vụ, giá.
- `User`: username/password/hash, thông tin cá nhân, role.
- `BookingDetail`: chi tiết vật tư/dịch vụ theo booking.

---

## 7. Enum nghiệp vụ
Vị trí file:
- `src/main/java/model/enums/Role.java`
- `src/main/java/model/enums/BookingStatus.java`
- `src/main/java/model/enums/PreparationStatus.java`

```java
public enum Role {
    EMPLOYEE,
    SUPPORT,
    ADMIN
}
```

```java
public enum BookingStatus {
    PENDING,
    APPROVED,
    REJECTED
}
```

```java
public enum PreparationStatus {
    PREPARING,
    READY,
    MISSING_EQUIPMENT
}
```

---

## 8. Tầng Util

### 8.1 Kết nối DB + tạo schema
Vị trí file: `src/main/java/util/DBConnection.java`

```java
private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/meeting_room_management?createDatabaseIfNotExist=true...";
private static final String DEFAULT_USERNAME = "root";
private static final String DEFAULT_PASSWORD = "123456";
```

Có thể override bằng biến môi trường:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

Tạo bảng chính trong `initializeDatabase()`:
- `users`
- `rooms`
- `equipments`
- `services`
- `bookings`
- `booking_details`

Ngoài ra còn seed dữ liệu mẫu phòng và thiết bị.

### 8.2 Validate
Vị trí file: `src/main/java/util/Validator.java`

```java
public static void validateRoomCapacityVsParticipants(int roomCapacity, int participantCount) {
    if (roomCapacity <= participantCount) {
        throw new IllegalArgumentException("Suc chua phong phai lon hon so nguoi tham gia.");
    }
}
```

Điểm quan trọng:
- Rule hiện tại là **sức chứa phải lớn hơn số người** (`>`), không phải `>=`.
- Username, email, phone, password đều có validate cụ thể.

### 8.3 ConsoleHelper
Vị trí file: `src/main/java/util/ConsoleHelper.java`

- Đóng gói các hàm nhập liệu có lặp validate (`promptWithValidation`, `promptIntInRange`, `promptPositiveInt`...).
- Giảm lặp code trong UI.

### 8.4 DateTimeUtil
Vị trí file: `src/main/java/util/DateTimeUtil.java`

- Parse format chuẩn: `yyyy-MM-dd HH:mm`.
- Format datetime để hiển thị cho user.

### 8.5 PasswordHash
Vị trí file: `src/main/java/util/PasswordHash.java`

- Hash password bằng BCrypt (`cost=12`).
- Verify password an toàn, không so sánh plain text.

### 8.6 TablePrinter
Vị trí file: `src/main/java/util/TablePrinter.java`

```java
public static void printTable(String[] headers, int[] widths, List<String[]> rows)
```

- In bảng đẹp, cột cố định.
- Có cắt chuỗi dài bằng `...` để không vỡ layout.

---

## 9. Tầng DAO: interface và triển khai

### 9.1 Interface
Vị trí file:
- `src/main/java/dao/IBookingDAO.java`
- `src/main/java/dao/IRoomDAO.java`
- `src/main/java/dao/IEquipmentDAO.java`
- `src/main/java/dao/IServiceDAO.java`
- `src/main/java/dao/IUserDAO.java`

Ý nghĩa: định nghĩa hợp đồng truy cập dữ liệu, tránh Service phụ thuộc trực tiếp SQL cụ thể.

### 9.2 BookingDAOImpl
Vị trí file: `src/main/java/dao/impl/BookingDAOImpl.java`

Ví dụ logic kiểm tra xung đột lịch:

```java
String sql = "SELECT 1 FROM bookings "
        + "WHERE room_id = ? "
        + "AND status IN ('PENDING', 'APPROVED') "
        + "AND start_time < ? "
        + "AND end_time > ? "
        + "LIMIT 1";
```

Điểm hay:
- Dùng transaction khi lưu booking + booking_details.
- Dùng rollback khi lỗi.

### 9.3 Room/Equipment/Service/User DAOImpl
- RoomDAOImpl: CRUD phòng, tìm theo từ khóa tên.
- EquipmentDAOImpl: CRUD thiết bị, update số lượng khả dụng.
- ServiceDAOImpl: CRUD dịch vụ, tìm theo tên/ID.
- UserDAOImpl: tìm theo username/email/role, tạo user mới.

Tất cả đều mở kết nối qua `DBConnection.openConnection()` và dùng `PreparedStatement` chống SQL injection cơ bản.

---

## 10. Tầng Service: nghiệp vụ trung tâm

## 10.1 Service wiring mới (constructor injection)
Các service hiện đã có 2 constructor:
1. Constructor mặc định (auto wire dependency mặc định).
2. Constructor nhận dependency để dễ test/mocking.

Ví dụ ở `BookingService` (file: `src/main/java/service/BookingService.java`):

```java
public BookingService() {
    this(new BookingDAOImpl(), new RoomService(), new EquipmentService(), new ServiceService());
}

public BookingService(IBookingDAO bookingDAO, RoomService roomService, EquipmentService equipmentService, ServiceService serviceService) {
    if (bookingDAO == null || roomService == null || equipmentService == null || serviceService == null) {
        throw new IllegalArgumentException("BookingService dependencies khong duoc null.");
    }
    ...
}
```

### 10.2 AuthService
Vị trí file: `src/main/java/service/AuthService.java`

Nhiệm vụ:
- Đăng ký nhân viên.
- Đăng nhập.
- Seed sẵn 2 tài khoản mặc định `admin` và `support`.

Đoạn seed quan trọng:

```java
createDefaultUserIfMissing("admin", "Admin@123", ... , Role.ADMIN);
createDefaultUserIfMissing("support", "Support@123", ... , Role.SUPPORT);
```

### 10.3 AdminService
Vị trí file: `src/main/java/service/AdminService.java`

Nhiệm vụ:
- Điều phối RoomService, EquipmentService, ServiceService, UserService, BookingService.
- Tạo tài khoản support.
- Duyệt/từ chối booking.

### 10.4 BookingService
Vị trí file: `src/main/java/service/BookingService.java`

Đây là service quan trọng nhất vì chứa nhiều rule nghiệp vụ.

Luật thời gian:

```java
private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    Validator.validateNotPastDateTime(startTime, "Thoi gian bat dau");
    Validator.validateNotPastDateTime(endTime, "Thoi gian ket thuc");
    if (!startTime.isBefore(endTime)) {
        throw new IllegalArgumentException("Thoi gian bat dau phai nho hon thoi gian ket thuc.");
    }
}
```

Luật sức chứa:

```java
Validator.validateRoomCapacityVsParticipants(room.getCapacity(), validatedParticipants);
```

Luật xung đột:
- Có conflict khi `newStart < existingEnd && newEnd > existingStart`.

Luật tạo booking:
1. Validate user/time/room/participant.
2. Kiểm tra trùng lịch.
3. Validate chi tiết thiết bị/dịch vụ.
4. Tạo booking trạng thái `PENDING`, `PREPARING`.
5. Lưu transaction qua DAO.

### 10.5 RoomService / EquipmentService / ServiceService
Vị trí file:
- `src/main/java/service/RoomService.java`
- `src/main/java/service/EquipmentService.java`
- `src/main/java/service/ServiceService.java`

Mỗi service chịu trách nhiệm rule nghiệp vụ riêng theo entity (CRUD + validate domain).

### 10.6 SupportService / UserService
Vị trí file:
- `src/main/java/service/SupportService.java`
- `src/main/java/service/UserService.java`

- `SupportService`: lấy booking được phân công + cập nhật preparation.
- `UserService`: truy vấn/tạo user, làm nền cho Auth/Admin.

---

## 11. Tầng Presentation (UI console)

## 11.1 AuthUI
Vị trí file: `src/main/java/presentation/AuthUI.java`

Menu xác thực:
- Đăng ký nhân viên.
- Đăng nhập.
- Điều hướng theo vai trò.

Điều hướng theo role:

```java
switch (role) {
    case ADMIN:
        adminUI.run(user);
        break;
    case SUPPORT:
        supportUI.run(user);
        break;
    case EMPLOYEE:
        employeeUI.run(user);
        break;
}
```

## 11.2 AdminUI + module admin
Vị trí file:
- `src/main/java/presentation/AdminUI.java`
- `src/main/java/presentation/admin/AdminRoomManagementUI.java`
- `src/main/java/presentation/admin/AdminEquipmentManagementUI.java`
- `src/main/java/presentation/admin/AdminServiceManagementUI.java`

Chức năng admin:
1. Quản lý phòng.
2. Quản lý thiết bị.
3. Tạo support user.
4. Duyệt/từ chối booking.
5. Quản lý dịch vụ.
6. Xem danh sách users.

## 11.3 EmployeeUI + EmployeeBookingCreationUI
Vị trí file:
- `src/main/java/presentation/EmployeeUI.java`
- `src/main/java/presentation/employee/EmployeeBookingCreationUI.java`

Menu employee:
1. Đặt phòng.
2. Xem booking của tôi.
3. Hủy booking PENDING.

Điểm đáng chú ý trong đặt phòng:
- Validate ngay khi nhập sai (định dạng thời gian, thời gian kết thúc > bắt đầu, số người < sức chứa, y/n hợp lệ...).
- Cho phép thêm thiết bị/dịch vụ tùy chọn.

## 11.4 SupportUI
Vị trí file: `src/main/java/presentation/SupportUI.java`

Chức năng:
1. Xem việc được phân công.
2. Cập nhật trạng thái chuẩn bị (`PREPARING/READY/MISSING_EQUIPMENT`).

---

## 12. Luồng xử lý end-to-end quan trọng

## 12.1 Luồng đăng ký nhân viên
1. User nhập thông tin trong `AuthUI.handleRegister()`.
2. Validate tức thời qua `ConsoleHelper.promptWithValidation` + `Validator`.
3. Gọi `AuthService.registerEmployee()`.
4. `AuthService` kiểm tra trùng username/email qua `UserService`.
5. Hash password bằng `PasswordHash.hashPassword()`.
6. Lưu DB qua `UserDAO.save()`.

## 12.2 Luồng đăng nhập
1. `AuthUI.handleLogin()` nhận identifier + password.
2. `AuthService.login()` tìm user theo username/email.
3. Verify bcrypt.
4. Trả về user + role.
5. `AuthUI.routeByRole()` chuyển sang màn hình role tương ứng.

## 12.3 Luồng đặt phòng (Employee)
1. Hiển thị tất cả phòng.
2. Nhập start/end time.
3. Service lọc phòng trống theo time conflict.
4. User chọn phòng trống.
5. Validate số người theo sức chứa.
6. Option thêm thiết bị/dịch vụ.
7. Tạo booking trạng thái PENDING.
8. Lưu transaction booking + booking_details.

## 12.4 Luồng duyệt booking (Admin)
1. Admin xem danh sách booking.
2. Chỉ xử lý booking PENDING.
3. Chọn duyệt/từ chối.
4. Nếu duyệt: chọn support để phân công.
5. Cập nhật status qua `BookingService.approveBooking()` hoặc `rejectBooking()`.

## 12.5 Luồng hỗ trợ chuẩn bị (Support)
1. Support xem booking được gán.
2. Chọn booking và trạng thái chuẩn bị.
3. `SupportService.updatePreparationStatus()` gọi `BookingService`.
4. DAO update có điều kiện đúng support + status APPROVED.

---

## 13. Cơ chế validate hiện tại

### 13.1 Validate tại UI (ngay lúc nhập)
- Dùng `ConsoleHelper.promptWithValidation` / `promptIntInRange` / `promptPositiveInt`.
- Với đặt phòng: có vòng lặp nhập lại khi sai (`y/n`, thời gian, số lượng...)

### 13.2 Validate tại Service (bảo vệ nghiệp vụ)
- Dù UI đã validate, service vẫn kiểm tra lại để tránh lỗi khi gọi từ nơi khác.
- Đây là lớp bảo vệ quan trọng nhất của business rule.

---

## 14. Xử lý lỗi
Pattern chung:
- DAO ném `IllegalStateException` khi lỗi SQL/kết nối.
- Service ném `IllegalArgumentException` khi dữ liệu đầu vào sai hoặc vi phạm rule.
- UI bắt exception và in thông báo người dùng hiểu được.

Ví dụ trong `BookingService`:

```java
if (bookingDAO.existsTimeConflict(roomId, startTime, endTime)) {
    throw new IllegalArgumentException("Phong da co lich trung trong khoang thoi gian nay.");
}
```

---

## 15. Dữ liệu ban đầu và tài khoản mặc định

### 15.1 Seed DB
Trong `DBConnection.initializeDatabase()` có seed:
- 3 thiết bị mẫu.
- 3 phòng mẫu.

### 15.2 Tài khoản mặc định
Trong `AuthService.seedDefaultAccounts()`:
- `admin / Admin@123`
- `support / Support@123`

Lưu ý: password được hash khi lưu DB.

---

## 16. Test hiện có
Vị trí file:
- `test/java/service/BookingServiceTest.java`
- `test/java/util/ValidatorTest.java`

Đang test các phần:
- Logic `isTimeConflict()`.
- Validate thời gian quá khứ.
- Validate sức chứa phòng > số người.

---

## 17. Cách đọc code cho người mới
Bạn nên đọc theo thứ tự này:
1. `presentation/Main.java` (entry point).
2. `presentation/AuthUI.java` (flow đăng nhập/điều hướng role).
3. Mỗi role UI:
   - `presentation/AdminUI.java`
   - `presentation/EmployeeUI.java`
   - `presentation/SupportUI.java`
4. Service liên quan:
   - `service/BookingService.java` (ưu tiên số 1).
   - `service/AuthService.java`, `service/AdminService.java`.
5. DAO impl để hiểu SQL thật chạy như nào.
6. `util/DBConnection.java` để hiểu schema.

---

## 18. Điểm mạnh hiện tại
- Kiến trúc phân tầng rõ ràng.
- Validate nhiều lớp (UI + Service).
- Có rollback transaction ở các thao tác quan trọng.
- Console UI dễ chạy, dễ demo.
- Đã chuẩn hóa package enum sang `model.enums`.
- Service đã hỗ trợ constructor injection để test/mocking tốt hơn.

---

## 19. Hạn chế và đề xuất cải tiến

### 19.1 Hạn chế hiện tại
- Chưa có test tích hợp DAO với DB thật.
- Chưa có logging framework (đang dùng `System.out.println`).
- Chưa có lớp cấu hình dependency tập trung (wiring thủ công bằng `new`).
- Rule sức chứa hiện là `roomCapacity > participants` (không cho bằng), cần xác nhận đúng nghiệp vụ mong muốn.

### 19.2 Đề xuất
1. Thêm test integration cho DAO.
2. Tạo lớp `AppContext` để wire dependency tập trung.
3. Thêm migration tool (Flyway/Liquibase) thay cho tạo schema thủ công.
4. Bổ sung phân trang/lọc nâng cao ở các danh sách lớn.

---

## 20. Tóm tắt ngắn gọn để nhớ
- `Main` khởi tạo DB và mở `AuthUI`.
- `AuthUI` xác thực rồi điều hướng role.
- Mỗi role có UI riêng.
- UI gọi Service xử lý nghiệp vụ.
- Service gọi DAO để thao tác DB.
- Model là dữ liệu trung tâm, Util là hạ tầng hỗ trợ.
- Booking là nghiệp vụ phức tạp nhất: thời gian + xung đột + sức chứa + thiết bị/dịch vụ + duyệt + chuẩn bị.

---

## 21. Danh sách file quan trọng (để mở nhanh)
- `src/main/java/presentation/Main.java`
- `src/main/java/presentation/AuthUI.java`
- `src/main/java/presentation/AdminUI.java`
- `src/main/java/presentation/EmployeeUI.java`
- `src/main/java/presentation/SupportUI.java`
- `src/main/java/presentation/employee/EmployeeBookingCreationUI.java`
- `src/main/java/service/BookingService.java`
- `src/main/java/service/AuthService.java`
- `src/main/java/service/AdminService.java`
- `src/main/java/dao/impl/BookingDAOImpl.java`
- `src/main/java/util/DBConnection.java`
- `src/main/java/util/Validator.java`
- `src/main/java/model/Booking.java`

---

## 22. Kết luận
Nếu bạn chưa hiểu dự án, hãy xem theo luồng người dùng thật:
- Đăng nhập -> đi vào menu role -> gọi service -> chạm DAO -> cập nhật DB.

Bạn chỉ cần nắm chắc 3 file đầu tiên là đã hiểu hơn 60% hệ thống:
1. `src/main/java/presentation/AuthUI.java`
2. `src/main/java/service/BookingService.java`
3. `src/main/java/dao/impl/BookingDAOImpl.java`

Sau đó mở rộng sang Admin/Support và các service còn lại.
