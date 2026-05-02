# 🧪 Member Profile Update & BMI Calculation - Test Guide

## 🎯 Mục Tiêu Test

✅ **Test 1**: Member có thể xem hồ sơ của chính mình  
✅ **Test 2**: Member có thể cập nhật thông tin cá nhân  
✅ **Test 3**: **BMI tự động tính toán** từ height và weight  
✅ **Test 4**: BMI được lưu vào database  

---

## 🚀 Quy Trình Test Toàn Bộ

### Step 1: Đăng nhập để lấy JWT Token
```http
POST /auth/login
Content-Type: application/json

{
  "username": "user@fitlife.local",
  "password": "123456"
}

Response:
{
  "code": 200,
  "data": {
    "token": "eyJhbGc...",
    ...
  }
}
```

**Lưu JWT token vào biến `{{auth_token}}` trong member.http**

---

### Step 2: Test Case 3 - Xem Hồ Sơ Hiện Tại
```http
GET /members/me
Authorization: Bearer {{auth_token}}
```

**Kỳ vọng:**
```json
{
  "code": 200,
  "message": "Lấy hồ sơ cá nhân thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "fullName": "...",
    "phone": "...",
    "email": "...",
    "status": "ACTIVE",
    "avatarUrl": null,
    "height": null,      // Ban đầu có thể null
    "weight": null,      // Ban đầu có thể null
    "bmi": null,         // Ban đầu có thể null
    "fitnessGoal": null  // Ban đầu có thể null
  }
}
```

---

### Step 3: Test Case 4 - Cập Nhật Hồ Sơ & Test BMI Auto-Calculation

```http
PUT /members/me
Content-Type: application/json
Authorization: Bearer {{auth_token}}

{
  "fullName": "Chiến Binh Mùa Đông",
  "phone": "0987654321",
  "weight": 75.0,
  "height": 175.0,
  "fitnessGoal": "Gain Muscle"
}
```

**📊 BMI Calculation (Auto-calculated by Backend):**
```
Input:
  - weight = 75.0 kg
  - height = 175.0 cm

Calculation:
  1. Convert height: 175 cm ÷ 100 = 1.75 m
  2. BMI formula: weight / (height_m)²
  3. BMI = 75 / (1.75 × 1.75)
  4. BMI = 75 / 3.0625
  5. BMI = 24.489795...
  6. Round to 2 decimals: 24.49 ✅

Expected Response:
```json
{
  "code": 200,
  "message": "Cập nhật hồ sơ thành công",
  "data": {
    "id": 1,
    "userId": 1,
    "fullName": "Chiến Binh Mùa Đông",      // ✅ Updated
    "phone": "0987654321",                  // ✅ Updated
    "email": "user@fitlife.local",
    "status": "ACTIVE",
    "avatarUrl": null,
    "height": 175.0,                        // ✅ Updated
    "weight": 75.0,                         // ✅ Updated
    "bmi": 24.49,                           // ✅ Auto-calculated!
    "fitnessGoal": "Gain Muscle"            // ✅ Updated
  }
}
```

**✅ Assertions (Tự động kiểm tra):**
```
✅ Status 200 OK
✅ fullName cập nhật: "Chiến Binh Mùa Đông"
✅ phone cập nhật: "0987654321"
✅ fitnessGoal cập nhật: "Gain Muscle"
✅ height = 175.0 cm
✅ weight = 75.0 kg
✅ bmi = 24.49 (Auto-calculated)
```

---

### Step 4: Test Case 5 - Kiểm Tra BMI Lưu Trong Database

```http
GET /members/me
Authorization: Bearer {{auth_token}}
```

**Mục đích:** Xác nhận BMI được lưu vào database (không phải tính lại mỗi lần)

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "fullName": "Chiến Binh Mùa Đông",
    "weight": 75.0,
    "height": 175.0,
    "bmi": 24.49,                  // ✅ Lưu trong DB, không phải tính lại
    "fitnessGoal": "Gain Muscle"
  }
}
```

---

## 🧮 BMI Classification (Tham Khảo)

| BMI | Classification | Status |
|-----|-----------------|--------|
| < 18.5 | Underweight | 🔵 Underweight |
| 18.5 - 24.9 | Normal Weight | 🟢 **Healthy** |
| 25 - 29.9 | Overweight | 🟡 Overweight |
| ≥ 30 | Obese | 🔴 Obese |

**Ví dụ test case:**
- BMI = 24.49 → **Normal Weight (Healthy)** ✅

---

## 🔍 Kiểm Tra Chi Tiết

### Kiểm Tra 1: Height, Weight Fields
```bash
# Sau khi cập nhật PUT /members/me
curl -X GET http://localhost:8080/members/me \
  -H "Authorization: Bearer {JWT_TOKEN}" | jq '.data | {height, weight, bmi}'

Output:
{
  "height": 175.0,
  "weight": 75.0,
  "bmi": 24.49
}
```

### Kiểm Tra 2: BMI Persistence (Lưu trong DB)
```sql
-- Kiểm tra trong database MySQL
SELECT id, height, weight, bmi, fitness_goal FROM members WHERE id = 1;

Output:
+----+--------+--------+-------+---------------+
| id | height | weight | bmi   | fitness_goal  |
+----+--------+--------+-------+---------------+
|  1 | 175.0  | 75.0   | 24.49 | Gain Muscle   |
+----+--------+--------+-------+---------------+
```

### Kiểm Tra 3: Cập Nhật Lại & BMI Recalculation
```http
PUT /members/me
{
  "weight": 80.0,     // Thay đổi weight
  "height": 175.0
}

Expected: BMI = 80 / (1.75²) = 26.12 ✅
```

---

## ⚠️ Edge Cases Test

### Test Case A: Null Values
```http
PUT /members/me
{
  "fullName": "New Name"
  // weight, height: null
}

Expected:
- fullName cập nhật ✅
- weight, height, bmi: không thay đổi ✅
```

### Test Case B: Chỉ Cập Nhật Height (Không Cập Nhật Weight)
```http
PUT /members/me
{
  "height": 180.0
  // weight: null
}

Expected:
- height cập nhật
- weight không thay đổi
- **BMI không tính lại** (vì weight null) ✅
```

### Test Case C: Giá Trị Biên
```http
PUT /members/me
{
  "weight": 30.0,    // Min weight
  "height": 100.0    // Min height
}

Expected: BMI = 30 / (1.0²) = 30.0 ✅
```

---

## 📝 Test Results Checklist

- [ ] ✅ GET /members/me - Lấy profile thành công
- [ ] ✅ PUT /members/me - Cập nhật profile thành công
- [ ] ✅ Height được lưu đúng: 175.0
- [ ] ✅ Weight được lưu đúng: 75.0
- [ ] ✅ BMI tự động tính: 24.49
- [ ] ✅ BMI được lưu trong database
- [ ] ✅ Cập nhật lại & BMI recalculate
- [ ] ✅ fitnessGoal được lưu: "Gain Muscle"
- [ ] ✅ GET /members/me lấy lại BMI từ DB
- [ ] ✅ Edge case: Null values
- [ ] ✅ Edge case: Chỉ cập nhật một field
- [ ] ✅ Error: fitnessGoal với validation errors (nếu có)

---

## 🚀 Chạy Test Toàn Bộ

### Cách 1: Dùng HTTP Client trong IDE
1. Mở file: `fitlife-backend/http-requests/member.http`
2. Đặt `{{auth_token}}` từ login
3. Chạy test case 3, 4, 5 lần lượt
4. Kiểm tra assertions tự động

### Cách 2: Dùng cURL
```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user@fitlife.local","password":"123456"}' | jq -r '.data.token')

# Test GET /members/me
curl -X GET http://localhost:8080/members/me \
  -H "Authorization: Bearer $TOKEN" | jq

# Test PUT /members/me
curl -X PUT http://localhost:8080/members/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Chiến Binh Mùa Đông",
    "phone": "0987654321",
    "weight": 75.0,
    "height": 175.0,
    "fitnessGoal": "Gain Muscle"
  }' | jq '.data | {height, weight, bmi}'

# Test GET /members/me again
curl -X GET http://localhost:8080/members/me \
  -H "Authorization: Bearer $TOKEN" | jq '.data | {height, weight, bmi}'
```

---

## 🎯 Expected Output (Success)

```
✅ Member có thể xem hồ sơ của mình
✅ Member có thể cập nhật thông tin cá nhân
✅ BMI tự động tính toán: 75 / (1.75)² = 24.49
✅ BMI được lưu vào database
✅ Lấy profile lại xác nhận BMI vẫn là 24.49
✅ fitnessGoal được cập nhật và lưu
```

---

## ⚡ Troubleshooting

### Issue 1: "Cannot resolve column 'bmi'"
- **Nguyên nhân**: Migration chưa chạy
- **Giải pháp**: `mvn flyway:migrate`

### Issue 2: BMI = null sau cập nhật
- **Nguyên nhân**: Chỉ cập nhật một trong height/weight
- **Giải pháp**: Cập nhật cả height lẫn weight

### Issue 3: 401 Unauthorized
- **Nguyên nhân**: JWT token hết hạn hoặc sai
- **Giải pháp**: Login lại để lấy token mới

### Issue 4: 400 Bad Request
- **Nguyên nhân**: Validation error (weight < 30 hoặc height < 100)
- **Giải pháp**: Kiểm tra MemberUpdateRequest validation rules

---

**Status**: ✅ Ready to Test
**Date**: May 2, 2026


