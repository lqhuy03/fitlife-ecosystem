# 🚀 Quick Reference - FitLife Member Profile Update

## Deploy Steps (5 phút)

### 1️⃣ Build Backend
```bash
cd fitlife-backend
mvn clean package
```

### 2️⃣ Run Migration
```bash
mvn flyway:migrate
```

### 3️⃣ Start Application
```bash
mvn spring-boot:run
```

### 4️⃣ Test API
```bash
# Get profile
curl -X GET http://localhost:8080/members/me \
  -H "Authorization: Bearer {JWT_TOKEN}"

# Update profile (BMI auto-calculated)
curl -X PUT http://localhost:8080/members/me \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName":"Name",
    "phone":"0987654321",
    "weight":70.5,
    "height":175,
    "fitnessGoal":"Gain Muscle"
  }'
```

---

## 📊 API Endpoints

### Get Personal Profile
```
GET /members/me
Authorization: Bearer {JWT_TOKEN}
```

### Update Personal Profile  
```
PUT /members/me
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

Body:
{
  "fullName": "string",
  "phone": "string",
  "weight": 30-500 (kg),
  "height": 100-300 (cm),
  "fitnessGoal": "string"
}
```

---

## 🧮 BMI Calculation

**Formula**: BMI = weight / (height_m)²
**Height**: Converted from cm to m (÷100)
**Rounding**: 2 decimal places

**Example**:
- Height: 175 cm = 1.75 m
- Weight: 70 kg
- BMI: 70 / (1.75²) = **22.86**

---

## 📂 Files Modified

| File | Changes |
|------|---------|
| Member.java | +bmi, +fitnessGoal fields |
| MemberProfileResponse.java | +height, +weight, +bmi, +fitnessGoal |
| MemberMapper.java | +toProfileResponse() method |
| MemberServiceImpl.java | +getMyProfile(), +updateMyProfile() |
| ErrorCode.java | +3 error codes |
| MemberController.java | Endpoints already exist at /me |
| V3__add_bmi_to_members.sql | Database migration |

---

## ✅ Compilation Status
- ✅ 0 Compilation Errors
- ✅ All features tested
- ✅ Ready for production

---

## 🔗 Related Files

- **CHANGELOG.md** - Detailed changelog
- **member.http** - API test cases
- **FINAL_STATUS.md** - Complete status report

---

**Author**: FitLife Dev Team
**Status**: Production Ready ✅

