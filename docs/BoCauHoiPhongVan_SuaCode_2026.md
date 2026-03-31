# Bộ Câu Hỏi Phỏng Vấn Và Cách Sửa Code (Meeting Room Management)

## Mục tiêu tài liệu
Tài liệu này mô phỏng các câu hỏi mà nhà tuyển dụng thường hỏi khi review dự án thật và yêu cầu bạn sửa ngay tại chỗ.

Mỗi mục gồm:
1. Câu hỏi phỏng vấn có thể gặp.
2. Vị trí code cụ thể.
3. Vì sao đây là vấn đề.
4. Cách sửa đề xuất.
5. Mẫu code sửa nhanh để luyện nói và code tại chỗ.

---

## A. Kiến trúc, tổ chức code, khả năng mở rộng

## 1) Tại sao UI đang tự tạo service bằng `new`?
**Vị trí:**
- `src/main/java/presentation/AuthUI.java` (constructor)
- `src/main/java/presentation/AdminUI.java` (constructor)
- `src/main/java/presentation/SupportUI.java` (constructor)

**Có thể bị hỏi:**
- "Nếu mai cần mock service để test UI thì làm sao?"

**Vì sao là vấn đề:**
- Khó test unit UI.
- Coupling cao giữa UI và concrete service.

**Cách sửa:**
- Cho UI nhận service qua constructor (hoặc tạo `AppContext` để gom wiring).

**Mẫu sửa:**
```java
public class SupportUI {
    private final SupportService supportService;

    public SupportUI(SupportService supportService) {
        this.supportService = supportService;
    }

    public SupportUI() {
        this(new SupportService());
    }
}
```

---

## 2) Có cần một lớp `AppContext` để quản lý dependency không?
**Vị trí:** toàn bộ `presentation/*`.

**Có thể bị hỏi:**
- "Tại sao dependency đang được tạo rải rác ở nhiều nơi?"

**Cách sửa:**
- Tạo `AppContext` chứa singleton service/UI cần tái sử dụng.

**Mẫu sửa:**
```java
public final class AppContext {
    public final UserService userService = new UserService();
    public final AuthService authService = new AuthService(userService);
    public final BookingService bookingService = new BookingService();
}
```

---

## 3) Service đã có constructor injection, vậy còn điểm nào cần chuẩn hóa nữa?
**Vị trí:** `src/main/java/service/*`.

**Có thể bị hỏi:**
- "Bạn đã DI nhưng mới ở service, vậy tầng trên đã tận dụng chưa?"

**Cách sửa:**
- Truyền dependency từ composition root thay vì gọi constructor mặc định trong UI.

---

## 4) Vì sao `AdminUI` vẫn khá lớn?
**Vị trí:** `src/main/java/presentation/AdminUI.java`.

**Có thể bị hỏi:**
- "File này có thể tách tiếp thế nào để clean?"

**Cách sửa:**
- Tách thành handler theo use case: `AdminUserAccountHandler`, `AdminBookingApprovalHandler`.
- Hoặc giữ file nhưng thêm vùng comment rõ và private helper theo khối.

---

## 5) Mô hình hiện tại có áp dụng Interface cho Service không?
**Vị trí:** `src/main/java/service/*`.

**Có thể bị hỏi:**
- "Tại sao DAO dùng interface nhưng service chưa?"

**Cách sửa:**
- Tạo interface service nếu dự án lớn hoặc có nhiều implementation.

---

## B. Bảo mật và an toàn dữ liệu

## 6) Hard-code thông tin DB có rủi ro gì?
**Vị trí:** `src/main/java/util/DBConnection.java`.

**Code hiện tại:**
```java
private static final String DEFAULT_USERNAME = "root";
private static final String DEFAULT_PASSWORD = "123456";
```

**Có thể bị hỏi:**
- "Lên production thì làm sao tránh lộ tài khoản DB?"

**Cách sửa:**
- Bắt buộc lấy từ env/file cấu hình, không để default yếu.

**Mẫu sửa:**
```java
private static String requiredEnv(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
        throw new IllegalStateException("Missing env: " + key);
    }
    return value;
}
```

---

## 7) Seed tài khoản mặc định `admin/support` có nguy hiểm không?
**Vị trí:** `src/main/java/service/AuthService.java`, `seedDefaultAccounts()`.

**Có thể bị hỏi:**
- "Nếu deploy nhầm môi trường thật thì sao?"

**Cách sửa:**
- Chỉ seed khi `APP_ENV=dev`.
- Hoặc seed qua migration riêng cho môi trường local.

---

## 8) Mật khẩu nhập bằng terminal có thể bị lộ không?
**Vị trí:** `src/main/java/util/ConsoleHelper.java`, `promptPassword()`.

**Có thể bị hỏi:**
- "Vì sao vẫn có trường hợp đọc password bằng `prompt()`?"

**Vì sao:**
- `System.console()` có thể `null` trong IDE.

**Cách sửa:**
- Cảnh báo rõ cho user khi fallback.
- Ưu tiên chạy qua terminal thực.

---

## 9) Thông báo lỗi đăng ký có thể lộ thông tin user tồn tại?
**Vị trí:** `AuthUI.handleRegister()`, `AdminUI.handleSupportAccountCreation()`.

**Câu hỏi có thể gặp:**
- "Bạn có đang vô tình cho attacker biết username/email nào tồn tại không?"

**Cách sửa:**
- Dùng thông báo chung khi cần bảo mật cao.

---

## 10) Thiếu audit log cho hành động admin quan trọng
**Vị trí:** `AdminService.approveBooking/rejectBooking`.

**Có thể bị hỏi:**
- "Ai duyệt booking lúc nào? Có lịch sử không?"

**Cách sửa:**
- Thêm bảng audit hoặc logger sự kiện admin.

---

## C. Validation, business rule, edge case

## 11) Rule sức chứa đang là `roomCapacity > participantCount`, có đúng kỳ vọng không?
**Vị trí:** `src/main/java/util/Validator.java`, `validateRoomCapacityVsParticipants()`.

**Code hiện tại:**
```java
if (roomCapacity <= participantCount) {
    throw new IllegalArgumentException("Suc chua phong phai lon hon so nguoi tham gia.");
}
```

**Có thể bị hỏi:**
- "Phòng 10 chỗ có cho đúng 10 người không?"

**Cách sửa (nếu muốn cho bằng):**
```java
if (roomCapacity < participantCount) {
    throw new IllegalArgumentException("Suc chua phong phai lon hon hoac bang so nguoi tham gia.");
}
```

---

## 12) `validateNotPastDateTime` dùng `LocalDateTime.now()` có thể gây flaky test
**Vị trí:** `Validator.validateNotPastDateTime()`.

**Có thể bị hỏi:**
- "Làm sao test ổn định theo thời gian?"

**Cách sửa:**
- Inject `Clock` vào service/validator khi cần test thời gian.

---

## 13) Chưa kiểm tra trùng tên phòng khi tạo/cập nhật
**Vị trí:** `RoomService.createRoom/updateRoom`.

**Có thể bị hỏi:**
- "Có cho tạo 2 phòng cùng tên không?"

**Cách sửa:**
- Thêm DAO `findByName` + check unique logic.

---

## 14) Chưa chặn quantity dịch vụ quá lớn
**Vị trí:** `BookingService.buildServiceDetails()`.

**Có thể bị hỏi:**
- "Nếu nhập số lượng 999999 thì sao?"

**Cách sửa:**
- Đặt upper-bound theo rule nghiệp vụ.

---

## 15) Trạng thái thiết bị đang là String dễ typo
**Vị trí:** `Equipment.status`, `EquipmentService`.

**Có thể bị hỏi:**
- "Tại sao không dùng enum cho status?"

**Cách sửa:**
- Tạo enum `EquipmentStatus` thay vì String.

---

## 16) Validate email ở service nhưng DB chưa có unique constraint
**Vị trí:**
- Service: `AuthService.registerEmployee`, `AdminService.createSupportAccount`
- DB schema: `DBConnection.initializeDatabase()`

**Có thể bị hỏi:**
- "Hai request song song cùng email thì sao?"

**Cách sửa:**
- Thêm `UNIQUE(email)` ở DB.
- Bắt lỗi duplicate key ở DAO/service.

**Mẫu sửa SQL:**
```sql
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
```

---

## 17) Định dạng email regex hiện tại còn đơn giản
**Vị trí:** `Validator.EMAIL_PATTERN`.

**Câu hỏi:**
- "Regex này có cover hết email hợp lệ RFC không?"

**Cách sửa:**
- Hoặc dùng regex chặt hơn, hoặc xác thực theo business đủ dùng + email verify thực tế.

---

## 18) `BookingService.findRoomById()` đang load all rồi loop
**Vị trí:** `BookingService.findRoomById()`.

**Có thể bị hỏi:**
- "Tại sao không query by id trực tiếp?"

**Cách sửa:**
- Thêm method `RoomService.findById()` dựa trên `IRoomDAO.findById`.

---

## 19) Rule kiểm tra xung đột lịch đã đúng boundary chưa?
**Vị trí:**
- `BookingService.isTimeConflict()`
- `BookingDAOImpl.existsTimeConflict()`

**Giải thích khi được hỏi:**
- Nếu cuộc họp mới bắt đầu đúng bằng thời điểm cuộc cũ kết thúc thì **không conflict**.

---

## 20) Hủy booking chỉ cho `PENDING`, có đúng không?
**Vị trí:** `BookingService.cancelPendingBooking()`.

**Có thể bị hỏi:**
- "Đã APPROVED có cho hủy không?"

**Cách sửa (nếu cần nghiệp vụ mới):**
- Cho phép thêm trạng thái `CANCELLED` và soft-delete thay vì hard delete.

---

## D. Transaction, concurrency, tính nhất quán

## 21) Race condition khi đăng ký cùng username/email
**Vị trí:** `AuthService.registerEmployee`, `AdminService.createSupportAccount`.

**Vấn đề:**
- `check exists` rồi `insert` có thể bị race trong concurrent request.

**Cách sửa:**
1. DB unique index bắt buộc.
2. Bắt lỗi duplicate key để trả message thân thiện.

---

## 22) Race condition khi tạo booking cùng khung giờ
**Vị trí:** `BookingService.createBookingRequest` + `BookingDAOImpl.existsTimeConflict` + `save`.

**Vấn đề:**
- Hai request song song có thể cùng pass check rồi cùng insert.

**Cách sửa:**
- Dùng transaction isolation phù hợp.
- Lock theo `room_id + time range` (hoặc dùng giải pháp lock ứng dụng).

---

## 23) Approve/reject booking chưa khóa theo trạng thái tại câu UPDATE
**Vị trí:** `BookingDAOImpl.updateApproval()`.

**Code hiện tại:**
```java
UPDATE bookings SET status = ?, support_staff_id = ?, preparation_status = ? WHERE id = ?
```

**Có thể bị hỏi:**
- "Nếu booking đã đổi trạng thái trước đó thì sao?"

**Cách sửa:**
```sql
UPDATE bookings
SET status=?, support_staff_id=?, preparation_status=?
WHERE id=? AND status='PENDING'
```

---

## 24) `deletePendingByUser` có thể tối ưu bằng cascade
**Vị trí:** `BookingDAOImpl.deletePendingByUser()`.

**Hiện tại:**
- Xóa `booking_details` trước, rồi xóa `bookings` trong transaction.

**Cách sửa:**
- Đặt FK `booking_details.booking_id` với `ON DELETE CASCADE` để đơn giản code.

---

## 25) Chưa có cơ chế giữ/chốt tồn thiết bị theo booking
**Vị trí:** `BookingService.createBookingRequest()`.

**Có thể bị hỏi:**
- "Nhiều booking cùng mượn thiết bị thì xử lý tồn thế nào?"

**Cách sửa:**
- Trừ số lượng khả dụng khi booking được APPROVED (hoặc khi PENDING tùy business).
- Hoàn trả khi booking bị reject/cancel.

---

## E. SQL và schema

## 26) Thiếu index cho truy vấn thường dùng
**Vị trí:** schema trong `DBConnection.initializeDatabase()`.

**Có thể bị hỏi:**
- "Bảng lớn lên thì truy vấn conflict/bookings có chậm không?"

**Cách sửa:**
```sql
CREATE INDEX idx_bookings_room_time ON bookings(room_id, start_time, end_time);
CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_support ON bookings(support_staff_id);
```

---

## 27) Thiếu `NOT NULL` ở vài cột quan trọng
**Vị trí:** `DBConnection.initializeDatabase()`.

**Có thể bị hỏi:**
- "Email có bắt buộc không? Full name có bắt buộc không?"

**Cách sửa:**
- Siết `NOT NULL` theo rule nghiệp vụ.

---

## 28) `booking_details` cho phép cả equipment_id và service_id cùng null
**Vị trí:** schema `booking_details`.

**Có thể bị hỏi:**
- "Bản ghi detail rỗng có hợp lệ không?"

**Cách sửa:**
- Thêm `CHECK` constraint logic XOR giữa `equipment_id` và `service_id`.

---

## 29) `rooms.description VARCHAR(100)` có thể quá ngắn
**Vị trí:** schema bảng `rooms`.

**Câu hỏi:**
- "Mô tả dài hơn 100 ký tự thì sao?"

**Cách sửa:**
- Tăng kích thước cột hoặc dùng TEXT.

---

## 30) Dùng `ENUM` DB cho status có thể khó migrate
**Vị trí:** bảng `bookings`, `users`.

**Cách trả lời phỏng vấn:**
- ENUM giúp rõ giá trị hợp lệ, nhưng migrate thêm trạng thái mới sẽ tốn công hơn.
- Nếu cần linh hoạt, cân nhắc VARCHAR + constraint/app-level validation.

---

## F. Chất lượng code và maintainability

## 31) Dùng `System.out.println` thay logger
**Vị trí:** toàn bộ UI/service/util.

**Có thể bị hỏi:**
- "Khi production log nhiều luồng thì theo dõi thế nào?"

**Cách sửa:**
- Dùng `slf4j + logback` (hoặc java.util.logging tối thiểu).

---

## 32) Một số method UI khá dài
**Vị trí:** `EmployeeBookingCreationUI.handleCreateBooking`.

**Cách sửa:**
- Tách thành các bước: `collectTime`, `collectRoom`, `collectRequests`, `submitBooking`.

---

## 33) `toString()` có thể lộ dữ liệu nhạy cảm?
**Vị trí:** `model/User.java`.

**Điểm hiện tại:**
- Không in password => tốt.

**Có thể bị hỏi:**
- "Nếu ai đó thêm password vào toString thì sao?"

**Cách sửa:**
- Quy ước rõ: không log dữ liệu nhạy cảm.

---

## 34) Tên field `bookingId` nhưng getter `getId()`
**Vị trí:** `model/Booking.java`.

**Câu hỏi:**
- "Sao không đồng nhất tên field với API getter/setter?"

**Cách sửa:**
- Chuẩn hóa nhất quán (`id`) hoặc giữ hiện tại nhưng giải thích vì tương thích code cũ.

---

## 35) `TablePrinter.fit` cắt chuỗi theo length ký tự
**Vị trí:** `util/TablePrinter.java`.

**Có thể bị hỏi:**
- "Ký tự Unicode rộng khác nhau thì cột có lệch không?"

**Cách sửa:**
- Chấp nhận mức console cơ bản, hoặc dùng thư viện render bảng tốt hơn.

---

## G. Test và chất lượng phát hành

## 36) Test hiện tại còn ít, chủ yếu utility + overlap
**Vị trí:** `test/java/service/BookingServiceTest.java`, `test/java/util/ValidatorTest.java`.

**Có thể bị hỏi:**
- "Coverage hiện bao nhiêu phần trăm?"

**Cách sửa:**
- Bổ sung test cho AuthService/AdminService/DAO layer.

---

## 37) Chưa có integration test với DB thật
**Có thể bị hỏi:**
- "Bạn đảm bảo SQL chạy đúng trên môi trường CI thế nào?"

**Cách sửa:**
- Dùng Testcontainers MySQL cho integration test.

---

## 38) Chưa test concurrency cho booking conflict
**Có thể bị hỏi:**
- "Bạn đã test 2 request đặt cùng lúc chưa?"

**Cách sửa:**
- Viết test đa luồng để tái hiện race condition.

---

## 39) Chưa test behavior rollback transaction
**Vị trí:** `BookingDAOImpl.save`, `deletePendingByUser`.

**Cách sửa:**
- Viết test ép lỗi ở bước lưu detail để xác nhận booking header không bị commit dở dang.

---

## 40) Chưa có test cho `ConsoleHelper` input loop
**Có thể bị hỏi:**
- "Bạn test nhập liệu loop như thế nào?"

**Cách sửa:**
- Tách parser khỏi I/O để test logic parser độc lập.

---

## H. Bộ câu hỏi “sửa ngay tại chỗ” (thực chiến)

## 41) Bài sửa: đổi rule sức chứa từ `>` sang `>=`
**Vị trí:** `Validator.validateRoomCapacityVsParticipants`.

**Yêu cầu sửa:**
- Cho phép số người bằng sức chứa.

**Patch mẫu:**
```java
if (roomCapacity < participantCount) {
    throw new IllegalArgumentException("Suc chua phong phai lon hon hoac bang so nguoi tham gia.");
}
```

---

## 42) Bài sửa: cập nhật SQL approve có điều kiện PENDING
**Vị trí:** `BookingDAOImpl.updateApproval`.

**Patch mẫu SQL:**
```java
String sql = "UPDATE bookings SET status = ?, support_staff_id = ?, preparation_status = ? "
    + "WHERE id = ? AND status = 'PENDING'";
```

---

## 43) Bài sửa: thêm unique email ở schema
**Vị trí:** `DBConnection.initializeDatabase()`.

**Patch mẫu:**
```sql
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
```

---

## 44) Bài sửa: thêm index cho truy vấn booking
**Vị trí:** migration/schema.

**Patch mẫu:**
```sql
CREATE INDEX idx_bookings_room_time ON bookings(room_id, start_time, end_time);
```

---

## 45) Bài sửa: thêm method tìm room theo id trực tiếp
**Vị trí:** `RoomService`, `BookingService`.

**Patch mẫu:**
```java
public Room findRoomById(int id) {
    return roomDAO.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Khong tim thay phong hop voi ID " + id + "."));
}
```

---

## 46) Bài sửa: không seed default account ở production
**Vị trí:** `AuthService.seedDefaultAccounts`.

**Patch mẫu:**
```java
if (!"dev".equalsIgnoreCase(System.getenv("APP_ENV"))) {
    return;
}
```

---

## 47) Bài sửa: chuẩn hóa status thiết bị thành enum
**Vị trí:** `model/Equipment.java`, `EquipmentService`, `EquipmentDAOImpl`.

**Patch mẫu (ý tưởng):**
```java
public enum EquipmentStatus { SAN_SANG, HET_HANG }
```

---

## 48) Bài sửa: thêm logging thay cho println
**Vị trí:** toàn bộ service/dao.

**Patch mẫu:**
```java
private static final Logger log = LoggerFactory.getLogger(BookingService.class);
log.info("Create booking request for userId={}", userId);
```

---

## 49) Bài sửa: gom dependency vào AppContext
**Vị trí:** `presentation/Main.java` và các UI constructor.

**Mục tiêu:**
- Không còn new service rải rác.

---

## 50) Bài sửa: viết test cho approve/reject booking
**Vị trí:** `test/java/service`.

**Mẫu test ý tưởng:**
- Case 1: approve booking PENDING thành công.
- Case 2: approve booking REJECTED ném lỗi.
- Case 3: reject booking không tồn tại ném lỗi.

---

## I. Câu trả lời mẫu ngắn khi phỏng vấn hỏi sâu

## 51) “Vì sao chọn kiến trúc Model-DAO-Service-Presentation?”
**Trả lời gợi ý:**
- Tách rõ trách nhiệm, dễ test, dễ thay đổi DB/UI độc lập.

## 52) “Điểm yếu lớn nhất hiện tại của dự án là gì?”
**Trả lời gợi ý:**
- Chưa có integration test và cơ chế chống race condition booking đủ mạnh ở mức DB.

## 53) “Bạn đã làm gì để code dễ test hơn?”
**Trả lời gợi ý:**
- Đã refactor service sang constructor injection, có thể truyền mock DAO/service khi test.

## 54) “Nếu cần scale lên web API thì sao?”
**Trả lời gợi ý:**
- Giữ nguyên service/dao, thay presentation console bằng REST controller.

## 55) “Bạn ưu tiên fix gì đầu tiên nếu lên production?”
**Trả lời gợi ý:**
1. Bảo mật cấu hình DB + seed account.
2. Unique/index/migration DB.
3. Concurrency booking.
4. Logging + monitoring.
5. Test integration.

---

## J. Checklist luyện trước phỏng vấn (đề xuất)

1. Chạy thử toàn bộ menu theo 3 role và ghi lại luồng thực tế.
2. Tự code lại 5 patch quan trọng (#41 -> #45).
3. Viết thêm 3 test mới cho BookingService.
4. Giải thích thành lời 3 luồng end-to-end:
   - Login -> route role.
   - Employee đặt phòng.
   - Admin duyệt + Support cập nhật chuẩn bị.
5. Chuẩn bị 2 phút nói về điểm mạnh/yếu kiến trúc.

---

## K. Danh sách vị trí code hay bị hỏi nhất

- `src/main/java/service/BookingService.java`
- `src/main/java/dao/impl/BookingDAOImpl.java`
- `src/main/java/util/Validator.java`
- `src/main/java/util/DBConnection.java`
- `src/main/java/presentation/AuthUI.java`
- `src/main/java/presentation/employee/EmployeeBookingCreationUI.java`
- `src/main/java/presentation/AdminUI.java`
- `src/main/java/service/AuthService.java`
- `src/main/java/service/AdminService.java`

---

## Lời kết
Nếu bạn luyện hết tài liệu này, bạn sẽ có 2 lợi thế lớn khi phỏng vấn:
1. Trả lời được câu hỏi “vì sao code như vậy”.
2. Sửa được các lỗi/chỗ yếu ngay tại chỗ với patch rõ ràng.

Mẹo học nhanh:
- Chọn 10 mục đầu tiên trong phần B/C/D để luyện trước.
- Sau đó quay lại phần H (bài sửa thực chiến) để tự tay sửa thật trong code.
