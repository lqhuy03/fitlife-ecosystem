# FitLife Member Management - Changelog

## Phiên bản: v1.0.1 (2025-05-02)

### 🆕 Tính Năng Mới

#### 1. **BMI Tracking & Management**
- ✅ Thêm `bmi` field vào Member entity
- ✅ Tự động tính BMI khi member cập nhật chiều cao/cân nặng
- ✅ BMI được tính theo công thức chuẩn: `BMI = weight / (height_in_meter)²`
- ✅ BMI được làm tròn 2 chữ số thập phân

#### 2. **Fitness Goal Tracking**
- ✅ Thêm `fitnessGoal` field (ví dụ: "Giảm cân", "Tăng cơ", "Duy trì sức khỏe")
- ✅ Member có thể cập nhật mục tiêu fitness personal

#### 3. **Personal Profile API**
- ✅ Endpoint `GET /members/me` - Lấy hồ sơ cá nhân
- ✅ Endpoint `PUT /members/me` - Cập nhật hồ sơ cá nhân
  - Cập nhật: fullName, phone, weight, height, fitnessGoal
  - BMI được tự động tính toán

### 🔧 Cải Thiện & Sửa Lỗi

#### Exception Handling
- ✅ Thay thế tất cả `RuntimeException` bằng `AppException` + `ErrorCode`
- ✅ Thêm 3 error codes mới:
  - `PHONE_ALREADY_EXISTS` (400)
  - `EMAIL_ALREADY_EXISTS` (400)
  - `MEMBER_NO_ACCOUNT` (400)

#### Logging
- ✅ Thay thế `System.err.println()` bằng `log.warn()`
- ✅ Thêm `@Slf4j` logger annotation

#### Code Quality
- ✅ Thêm constants cho status strings (ACTIVE_STATUS, BANNED_STATUS, INACTIVE_STATUS)
- ✅ Thêm `@Serial` annotation cho `serialVersionUID`
- ✅ Thêm `transient` modifier cho List<Subscription> (Serializable compliance)

### 📋 Tập Tin Đã Thay Đổi

#### Backend Files
```
fitlife-backend/
├── src/main/java/com/fitlife/
│   ├── member/
│   │   ├── entity/Member.java                          [MODIFIED]
│   │   ├── dto/MemberProfileResponse.java              [MODIFIED]
│   │   ├── dto/MemberUpdateRequest.java                [NO CHANGE]
│   │   ├── mapper/MemberMapper.java                    [MODIFIED]
│   │   ├── controller/MemberController.java            [MODIFIED]
│   │   └── service/impl/MemberServiceImpl.java          [MODIFIED]
│   └── core/exception/ErrorCode.java                   [MODIFIED]
└── src/main/resources/
    └── db/migration/V3__add_bmi_to_members.sql         [NEW]
```

#### HTTP Test Files
```
fitlife-backend/http-requests/
└── member.http                                          [MODIFIED - Thêm 2 test cases]
```

### 💾 Database Changes

#### Migration: `V3__add_bmi_to_members.sql`
```sql
ALTER TABLE members ADD COLUMN bmi DOUBLE NULL DEFAULT NULL;
ALTER TABLE members ADD COLUMN fitness_goal VARCHAR(255) NULL DEFAULT NULL;
```

### 🔐 API Endpoints

#### Lấy Hồ Sơ Cá Nhân
```http
GET /members/me
Authorization: Bearer {JWT_TOKEN}

Response:
{
  "code": 200,
  "message": "Lấy hồ sơ cá nhân thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "fullName": "Nguyen Van A",
    "phone": "0912345678",
    "email": "user@fitlife.local",
    "status": "ACTIVE",
    "avatarUrl": "https://...",
    "height": 175.0,
    "weight": 70.5,
    "bmi": 23.02,
    "fitnessGoal": "Gain Muscle"
  }
}
```

#### Cập Nhật Hồ Sơ Cá Nhân
```http
PUT /members/me
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

Request:
{
  "fullName": "Nguyen Van A Updated",
  "phone": "0987654321",
  "weight": 70.5,
  "height": 175,
  "fitnessGoal": "Gain Muscle"
}

Response: (Same as GET /members/me)
```

### 📝 DTO Changes

#### MemberUpdateRequest.java
```java
@Getter
@Setter
public class MemberUpdateRequest {
    private String fullName;
    private String phone;
    
    @Min(value = 30, message = "Cân nặng phải lớn hơn 30kg")
    private Double weight;
    
    @Min(value = 100, message = "Chiều cao phải lớn hơn 100cm")
    private Double height;
    
    private String fitnessGoal;
}
```

#### MemberProfileResponse.java
```java
@Builder
@Getter
@Setter
public class MemberProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String email;
    private String status;
    private String avatarUrl;
    private Double height;          // NEW
    private Double weight;          // NEW
    private Double bmi;             // NEW
    private String fitnessGoal;     // NEW
}
```

### 🧪 Testing

#### New Test Cases Added (member.http)
- `GET /members/me` - Test get personal profile
- `PUT /members/me` - Test update personal profile with BMI calculation

#### Manual Testing
```bash
# 1. Đăng nhập để lấy JWT token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@fitlife.local","password":"123456"}'

# 2. Lấy hồ sơ cá nhân
curl -X GET http://localhost:8080/members/me \
  -H "Authorization: Bearer {JWT_TOKEN}"

# 3. Cập nhật hồ sơ (BMI sẽ tự động tính)
curl -X PUT http://localhost:8080/members/me \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Nguyen Van B",
    "phone":"0987654321",
    "weight":70.5,
    "height":175,
    "fitnessGoal":"Gain Muscle"
  }'
```

### ⚠️ Important Notes

1. **Migration Required**: Chạy migration trước khi start application
   ```bash
   mvn flyway:migrate
   ```

2. **BMI Calculation Logic**: 
   - Công thức: `BMI = weight_kg / (height_m)²`
   - Height được convert từ cm sang m (chia cho 100)
   - Kết quả được làm tròn 2 chữ số thập phân

3. **Security**:
   - `/members/me` và `PUT /members/me` sử dụng `Principal.getName()` để đảm bảo không xảy ra IDOR
   - Admin endpoints vẫn cần `@PreAuthorize("hasRole('ADMIN')")`

4. **Backward Compatibility**:
   - Tất cả endpoints cũ vẫn hoạt động bình thường
   - Chỉ thêm fields mới, không xóa fields cũ

### 🚀 Deployment Steps

1. **Build Backend**
   ```bash
   cd fitlife-backend
   mvn clean package
   ```

2. **Run Migration**
   ```bash
   mvn flyway:migrate
   ```

3. **Start Application**
   ```bash
   mvn spring-boot:run
   # hoặc
   java -jar target/fitlife-0.0.1-SNAPSHOT.jar
   ```

4. **Verify** (Swagger UI)
   ```
   http://localhost:8080/swagger-ui.html
   ```

### 📌 Version Info
- **Release Date**: 2025-05-02
- **Database Version**: V3 (V3__add_bmi_to_members.sql)
- **Java Version**: 17+
- **Spring Boot**: 3.x
- **JPA**: Hibernate

### 🔗 Related Issues
- Member profile management
- BMI tracking
- Fitness goal tracking
- Personal profile API

---

**Author**: FitLife Dev Team
**Status**: Released ✅


