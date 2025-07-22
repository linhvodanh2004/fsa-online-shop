# PC Shop Setup Guide

## Tổng quan
Tôi đã chỉnh sửa thành công cả `shop.html` và `shop-single.html` để phù hợp với shop bán PC và sử dụng dữ liệu từ database thay vì hardcode.

## Những thay đổi đã thực hiện

### 1. **shop.html**
- ✅ Thêm Thymeleaf namespace
- ✅ Thay đổi branding từ "Zay" thành "PC World"
- ✅ Cập nhật navigation links sử dụng Thymeleaf
- ✅ Thay đổi categories sidebar để hiển thị PC categories từ database
- ✅ Thay thế product grid hardcode bằng Thymeleaf loop
- ✅ Hiển thị thông tin sản phẩm từ database: tên, giá, category, quantity, sold
- ✅ Thêm link chuyển hướng đến `/shop-single/{id}`
- ✅ Cập nhật footer phù hợp với PC shop

### 2. **shop-single.html**
- ✅ Thêm Thymeleaf namespace
- ✅ Cập nhật navigation links
- ✅ Hiển thị thông tin sản phẩm chi tiết từ database:
  - Tên sản phẩm
  - Giá
  - Hình ảnh (với fallback)
  - Mô tả
  - Category
  - Số lượng trong kho (quantity)
  - Số lượng đã bán (sold)
- ✅ Related products hiển thị sản phẩm cùng category
- ✅ Giao diện đồng bộ với shop.html

### 3. **Database Sample**
- ✅ Tạo file `database_sample.sql` với dữ liệu mẫu phù hợp
- ✅ 8 categories PC: Gaming PCs, Office PCs, Workstation PCs, Gaming Laptops, Business Laptops, All-in-One PCs, Mini PCs, Custom Build PCs
- ✅ 20 sản phẩm PC với thông tin chi tiết
- ✅ Users và roles mẫu
- ✅ Carts cho users

## Cách sử dụng

### Bước 1: Import Database
```sql
-- Tạo database
CREATE DATABASE online_shop;
USE online_shop;

-- Import file database_sample.sql
SOURCE database_sample.sql;
```

### Bước 2: Cấu hình Application
Đảm bảo file `application.properties` có cấu hình đúng:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/online_shop
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Bước 3: Chạy ứng dụng
```bash
mvn spring-boot:run
```

### Bước 4: Test
- Truy cập `http://localhost:8080/shop` để xem danh sách sản phẩm
- Click vào bất kỳ sản phẩm nào để xem chi tiết
- URL sẽ có dạng: `http://localhost:8080/shop-single/1`

## Thông tin đăng nhập
- **Admin**: username: `admin`, password: `123456`
- **User**: username: `john_doe`, password: `123456`

## Tính năng đã hoàn thành
1. ✅ Hiển thị danh sách sản phẩm từ database
2. ✅ Chuyển hướng từ shop.html đến shop-single.html với product ID
3. ✅ Hiển thị chi tiết sản phẩm với đầy đủ thông tin
4. ✅ Related products cùng category
5. ✅ Giao diện responsive và đồng bộ
6. ✅ Fallback image khi không có hình ảnh
7. ✅ Format giá tiền đúng định dạng
8. ✅ Hiển thị stock và sold information

## Cấu trúc Database
```
roles (id, name)
categories (id, name, image)
users (id, username, password, fullname, email, phone, status, provider, role_id)
products (id, name, price, image, description, category_id, status, quantity, sold)
carts (id, user_id, sum)
cart_items (id, cart_id, product_id, quantity, price)
orders (id, receiver_name, receiver_phone, receiver_email, receiver_address, status, sum, creation_time, transit_time, delivery_time, cancellation_time, note, user_id)
order_items (id, order_id, product_id, quantity, price)
```

## Lưu ý
- Hình ảnh sản phẩm được lưu trong thư mục `/productImg/`
- Nếu không có hình ảnh, sẽ hiển thị `/user/img/default-pc.jpg`
- Related products giới hạn tối đa 4 sản phẩm cùng category
- Chỉ hiển thị sản phẩm có status = true và quantity > 0

## Troubleshooting
1. **Lỗi 404 khi click sản phẩm**: Kiểm tra controller mapping `/shop-single/{id}`
2. **Không hiển thị sản phẩm**: Kiểm tra database connection và dữ liệu
3. **Lỗi hình ảnh**: Đảm bảo thư mục `/productImg/` tồn tại và có quyền đọc
