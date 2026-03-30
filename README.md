# Meeting Room Management - Huong Dan Nhanh

## 1. Chay ung dung

Yeu cau:
- Java (theo toolchain cua Gradle)
- MySQL dang chay
- Co the cau hinh bien moi truong: DB_URL, DB_USERNAME, DB_PASSWORD

Lenh chay:

```bash
./gradlew.bat run
```

Neu project khong co task run san, co the dung:

```bash
./gradlew.bat compileJava
```

sau do chay lop Main trong package presentation.

## 2. Tai khoan mac dinh

- Admin:
  - username: admin
  - password: Admin@123
- Support:
  - username: support
  - password: Support@123

## 3. Workflow Day 3-4

- Employee:
  - Dat phong theo khoang thoi gian.
  - Kiem tra xung dot lich.
  - Muon them thiet bi (booking_details).
  - Xem lich va trang thai san sang cua cuoc hop.

- Admin:
  - Duyet/Tu choi booking PENDING.
  - Khi duyet co phan cong nhan vien Support.

- Support:
  - Xem cong viec duoc phan cong.
  - Cap nhat preparation status: PREPARING, READY, MISSING_EQUIPMENT.

## 4. Validation Day 5

Da bo sung:
- Thoi gian bat dau/ket thuc khong duoc o qua khu.
- So nguoi tham gia phai > 0.
- Suc chua phong phai lon hon so nguoi tham gia.
- Bat loi nhap sai kieu so qua ConsoleHelper de tranh crash.
- Bat loi khoi dong he thong trong Main (DB/Runtime).

## 5. Kiem thu

Chay test:

```bash
./gradlew.bat test
```

Da co test mau:
- BookingServiceTest: test logic xung dot thoi gian.
- ValidatorTest: test validation ngay gio va suc chua.

## 6. Luu y demo

- Neu menu dat phong bao khong co phong, vao Admin de tao phong truoc.
- Neu Admin duyet booking, can chon Support de phan cong.
- Employee chi thay "Da san sang" khi booking da APPROVED va preparation_status = READY.
