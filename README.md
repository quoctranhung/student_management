# Student Management System

## 📋 Thông tin nhóm

- **Danh sách thành viên**:
  - [2212841] - [Trần Hưng Quốc]

## 🌐 Public URL

- **Web Application**: http://localhost:8080/students
- **API Endpoint**: http://localhost:8080/api/students
- **Deployed URL** (Lab 5): https://student-management-1zfz.onrender.com/students

## 🚀 Hướng dẫn chạy dự án

### Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- PostgreSQL database (hoặc sử dụng Neon Cloud Database)

### Các bước chạy

1. **Clone repository**
   ```bash
   git clone [your-repo-url]
   cd student-management
   ```

2. **Cấu hình database** (tùy chọn)
   
   Mặc định, ứng dụng sử dụng Neon PostgreSQL Cloud. Nếu muốn sử dụng database riêng, chỉnh sửa file `src/main/resources/application-prod.properties`:
   
   ```properties
   spring.datasource.url=jdbc:postgresql://[HOST]:[PORT]/[DATABASE]?sslmode=require
   spring.datasource.username=[USERNAME]
   spring.datasource.password=[PASSWORD]
   ```

3. **Build project**
   ```bash
   ./mvnw clean package
   ```

4. **Chạy ứng dụng**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Truy cập ứng dụng**
   - Web UI: http://localhost:8080/students
   - REST API: http://localhost:8080/api/students

##  Câu trả lời các câu hỏi lý thuyết

### Lab 1 - Database & Hibernate

#### Câu 1: Dữ liệu Database
**Yêu cầu**: Hãy thử thêm ít nhất **10 sinh viên** vào.

**Trả lời**: Đã thêm 18 sinh viên vào database, bao gồm:
- 15 sinh viên từ 18-25 tuổi (ID: 1-15)
- 3 sinh viên dưới 18 tuổi (ID: 100-102) để test chức năng hiển thị có điều kiện

Data mẫu có thể xem trong commit history hoặc truy cập database production.

#### Câu 2: Ràng buộc Khóa Chính (Primary Key)
**Câu hỏi**: Có thể Insert một đối tượng với `id` trùng với một người đã có sẵn. Quan sát thông báo lỗi `UNIQUE constraint failed`. Tại sao Database lại từ chối thao tác này?

**Trả lời**: 

Database từ chối thao tác insert với ID trùng lặp vì **Primary Key có ràng buộc UNIQUE**. Cụ thể:

1. **Primary Key = UNIQUE + NOT NULL**: Khi một column được đánh dấu là Primary Key, nó tự động có 2 ràng buộc:
   - **UNIQUE**: Giá trị phải duy nhất trong toàn bộ table
   - **NOT NULL**: Không được phép NULL

2. **Mục đích**: Primary Key được sử dụng để định danh duy nhất mỗi record trong table. Nếu cho phép trùng lặp, sẽ không thể xác định chính xác một record cụ thể.

3. **Code implementation**:
   ```java
   @Entity
   @Table(name = "students")
   public class Student {
       @Id  // Primary Key annotation
       private String id;
       // ...
   }
   ```

4. **Error khi vi phạm**: 
   ```
   UNIQUE constraint failed: students.id
   ```
   

#### Câu 3: Toàn vẹn dữ liệu (Constraints)
**Câu hỏi**: 
- a) Thử Insert một sinh viên với `name` để `NULL`, kết quả trả về constraint nào?
- b) Database có bao nhiêu Constraint? Tổ chức mỗi Constraint ảnh hưởng tới column nào?

**Trả lời**:

**a) Insert với name = NULL**:
```sql
INSERT INTO students (id, name, email, age) VALUES ('999', NULL, 'test@example.com', 20);
```
**Kết quả**: Vi phạm ràng buộc `NOT NULL constraint` trên column `name`.

Error message:
```
ERROR: null value in column "name" violates not-null constraint
```

**b) Các Constraints trong Database**:

Database `students` có các constraints sau:

1. **PRIMARY KEY trên `id`**
   - Ảnh hưởng: Column `id`
   - Ràng buộc: UNIQUE + NOT NULL
   - Mục đích: Định danh duy nhất mỗi sinh viên

2. **NOT NULL trên `name`**
   - Ảnh hưởng: Column `name`
   - Ràng buộc: Giá trị bắt buộc phải có
   - Mục đích: Đảm bảo mọi sinh viên đều có tên

3. **NOT NULL trên `email`**
   - Ảnh hưởng: Column `email`
   - Ràng buộc: Giá trị bắt buộc phải có
   - Mục đích: Đảm bảo mọi sinh viên đều có email liên lạc

4. **NOT NULL trên `age`** (implicit do kiểu int)
   - Ảnh hưởng: Column `age`
   - Ràng buộc: Giá trị bắt buộc phải có
   - Mục đích: Đảm bảo mọi sinh viên đều có tuổi

**Tổng kết**: 4 constraints chính, trong đó PRIMARY KEY ảnh hưởng 1 column, NOT NULL ảnh hưởng 3 columns.

#### Câu 4: Cấu trúc Hibernate
**Câu hỏi**: Tại sao một method `save()` lại có thể dùng được cả cho Insert lẫn Update? Tại sao có các method không dùng ở đây nhưng vẫn có trong Database?

**Trả lời**:

**1. Tại sao `save()` dùng được cho cả Insert và Update?**

Method `save()` của Spring Data JPA Repository hoạt động theo cơ chế **"merge"** của Hibernate:

```java
public Student save(Student student) {
    return repository.save(student);  // Auto-detect insert vs update
}
```

**Cơ chế hoạt động**:
- **Insert**: Khi `student.getId()` không tồn tại trong database, Hibernate thực hiện INSERT
- **Update**: Khi `student.getId()` đã tồn tại, Hibernate thực hiện UPDATE

**Flow chi tiết**:
```
1. Gọi repository.save(student)
2. Hibernate kiểm tra EntityManager
3. IF entity.id == null OR entity.id not exists in DB
   ├─→ Execute INSERT SQL
4. ELSE entity.id exists in DB
   ├─→ Execute UPDATE SQL
5. Return managed entity
```

**Code example**:
```java
// Insert (id chưa tồn tại)
Student newStudent = new Student("999", "New Student", "new@example.com", 20);
service.save(newStudent);  // → INSERT INTO students...

// Update (id đã tồn tại)
Student existing = service.getById("1");
existing.setName("Updated Name");
service.save(existing);  // → UPDATE students SET name=...
```

**2. Tại sao có methods không dùng nhưng vẫn có trong Database?**

Đây là do Spring Data JPA cung cấp **nhiều methods built-in** thông qua `JpaRepository`:

```java
public interface StudentRepository extends JpaRepository<Student, String> {
    // Tự động có sẵn các methods:
    // - save(), saveAll()
    // - findById(), findAll()
    // - deleteById(), delete(), deleteAll()
    // - count(), existsById()
    // - ... và nhiều methods khác
}
```

**Lý do**:
1. **Framework Design Pattern**: Spring Data JPA implements Repository Pattern, cung cấp full CRUD operations mà không cần code
2. **Flexibility**: Developers có thể sử dụng bất kỳ method nào khi cần, không bắt buộc phải dùng hết
3. **Standard Interface**: Đảm bảo tính nhất quán across projects
4. **Future-proof**: Khi cần thêm chức năng, không cần modify interface

**Methods không dùng trong project hiện tại nhưng vẫn có sẵn**:
- `saveAll(Iterable<Student>)` - Lưu nhiều records cùng lúc
- `deleteAll()` - Xóa toàn bộ table
- `flush()` - Đẩy changes xuống DB ngay lập tức
- `getOne()`, `getReferenceById()` - Lazy loading variants

---

### Lab 2 - REST API

#### Câu hỏi: Tại sao cần xây dựng API (Lab 2) trước khi phát triển giao diện (Lab 3/4)?

**Trả lời**:

Trong thực tế phát triển phần mềm, Backend thường được thiết kế để phục vụ đa nền tảng (Web, Mobile App, 3rd Party Partners). Việc xây dựng các REST API trả về định dạng JSON (trong Lab 2) mang lại các lợi ích sau:

**1. Kiểm thử độc lập (Independent Testing)**

Đảm bảo logic nghiệp vụ (Service/Repository) hoạt động chính xác mà không phụ thuộc vào giao diện người dùng.

```java
// Test API endpoint trực tiếp
GET http://localhost:8080/api/students
Response: [
  {"id": "1", "name": "Nguyen Van A", "email": "nva@example.com", "age": 20},
  ...
]
```

**Lợi ích**:
- Phát hiện bugs sớm ở tầng Backend
- Test automation dễ dàng hơn (Postman, JUnit)
- Không cần UI để verify business logic

**2. Khả năng mở rộng đa nền tảng (Better Extensibility)**

Tạo điều kiện thuận lợi cho việc phát triển các ứng dụng đa nền tảng (như Mobile App với React Native/Flutter) thông qua việc tái sử dụng API.

```
         ┌──────────────────┐
         │   REST API       │
         │ (JSON Response)  │
         └────────┬─────────┘
                  │
    ┌─────────────┼─────────────┬──────────────┐
    │             │             │              │
┌───▼────┐  ┌────▼─────┐  ┌────▼──────┐  ┌───▼──────┐
│Web App │  │Mobile App│  │Desktop App│  │3rd Party │
│(Thyme- │  │(React    │  │(Electron) │  │Partners  │
│ leaf)  │  │ Native)  │  │           │  │          │
└────────┘  └──────────┘  └───────────┘  └──────────┘
```

**Lợi ích**:
- **Code Reusability**: 1 API phục vụ nhiều clients
- **Consistency**: Đảm bảo data và business logic nhất quán
- **Parallel Development**: Frontend và Backend teams làm việc độc lập
- **Versioning**: Dễ dàng version API cho các clients khác nhau

**3. Separation of Concerns**

Tách biệt rõ ràng giữa:
- **Backend (Lab 2)**: Business logic, data access, validation
- **Frontend (Lab 3/4)**: Presentation, user interaction, UI/UX

**4. API-First Development Approach**

```
Lab 1: Database & Entity Layer
   ↓
Lab 2: REST API (Core/Heart của hệ thống)
   ↓
Lab 3/4: Web UI (Consumer của API)
   ↓
Lab 5: Deployment
```

Đây là quy trình phát triển chuẩn trong công nghiệp phần mềm hiện đại.

**Kết luận**: Do đó, Lab 2 tập trung vào việc xây dựng và kiểm chứng tầng xử lý dữ liệu REST API ("trái tim" của hệ thống). Trong các bài thực hành tiếp theo (Lab 3 & 4), sinh viên sẽ phát triển giao diện người dùng để tương tác với hệ thống này.

---

## 📸 Screenshots

### 1. Trang danh sách sinh viên
![Students List](screenshots/students-list.png)
- Hiển thị danh sách 18 sinh viên
- Sinh viên dưới 18 tuổi (ID: 100, 101, 102) hiển thị màu đỏ
- Search box và nút "Thêm sinh viên mới"

### 2. Tìm kiếm sinh viên
![Search Function](screenshots/search.png)
- Tìm kiếm với keyword "Nguyen"
- Kết quả filtered hiển thị các sinh viên có tên chứa "Nguyen"

### 3. Trang chi tiết sinh viên
![Student Detail](screenshots/student-detail.png)
- Hiển thị đầy đủ thông tin: ID, Tên, Email, Tuổi
- Badge hiển thị trạng thái tuổi
- Nút Sửa và Xóa

### 4. Form thêm/sửa sinh viên
![Student Form](screenshots/student-form.png)
- Form validation (HTML5 + JavaScript)
- Kiểm tra độ tuổi hợp lệ (10-100)
- Confirmation dialog trước khi submit

### 5. Modal xác nhận xóa
![Delete Confirmation](screenshots/delete-modal.png)
- Modal popup yêu cầu xác nhận trước khi xóa
- Hiển thị thông tin sinh viên sẽ bị xóa
