# 🛒 Online Shop - Spring Boot Learning Project

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)

A simple e-commerce web application built with Spring Boot MVC as a learning project.

## 📖 About

This is a **3-month Spring Boot learning project** for web development course. It's a basic online shop selling gaming products with essential e-commerce features.

**⚠️ Note**: This is a student project for learning purposes, not a production-ready application.

## ✨ Features

### 👤 User Management
- 📝 User registration and login
- 🔐 Google OAuth2 integration
- 🎭 Role-based access (Admin/Customer)
- 👨‍💼 User profile management

### 🛍️ Product Management
- 📋 Product listing with pagination
- 🏷️ Category-based filtering
- 💰 Price sorting (ascending/descending)
- 🔍 Product search
- 📄 Product details page

### 🛒 Shopping Cart & Orders
- ➕ Add/remove items from cart
- 📦 Order placement and tracking
- 💳 Basic checkout process

### ⚙️ Admin Panel
- 🔧 Product CRUD operations
- 📂 Category management
- 👥 User management
- 📊 Order management
- 📈 Simple dashboard

## 🛠️ Tech Stack

**Backend:**
- ☕ Java 17
- 🍃 Spring Boot 3.2.0
- 🏗️ Spring MVC
- 🔐 Spring Security
- 💾 Spring Data JPA
- 🗄️ MySQL

**Frontend:**
- 🌿 Thymeleaf
- 🎨 Bootstrap 5
- ⚡ JavaScript/jQuery

**External APIs:**
- 🔑 Google OAuth2
- 💳 VNPay (payment)
- 🤖 Gemini AI (chat support)

## 🚀 Setup & Run

### 📋 Prerequisites
- ☕ Java 17+
- 🗄️ MySQL 8.0+
- 📦 Maven

### ⚡ Quick Start

1. **📥 Clone the repository**
```bash
git clone https://github.com/your-username/fsa-online-shop.git
cd fsa-online-shop
```

2. **🗄️ Create database**
```sql
CREATE DATABASE online_shop;
```

3. **⚙️ Configure environment**
Create `.env` file:
```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/online_shop
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Email Configuration
MAIL_EMAIL=your_email@example.com
MAIL_APPLICATION_PASSWORD=your_app_password

# VNPAY Configuration
VNPAY_TMN_CODE=your_vnpay_tmn_code
VNPAY_HASH_SECRET=your_vnpay_hash_secret
VNPAY_RETURN_URL=http://localhost:8080/cart-detail/checkout-vnpay-success

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Gemini API Key
GEMINI_API_KEY=your_gemini_api_key

# Remember-Me Key for Spring Security
REMEMBER_ME_KEY=your_remember_me_key

```

4. **▶️ Run the application**
```bash
./mvnw spring-boot:run
```

5. **🌐 Access the application**
- 🏠 Homepage: http://localhost:8080
- 👨‍💼 Admin panel: http://localhost:8080/admin
- 🔑 Default admin: admin@example.com / admin123

## 📁 Project Structure

```
src/main/java/fsa/project/online_shop/
├── 🎮 controllers/     # MVC Controllers
├── 📊 models/         # JPA Entities
├── 💾 repositories/   # Data Access
├── ⚙️ services/       # Business Logic
├── 🔧 configs/        # Security & Config
└── 🛠️ utils/          # Utilities

src/main/resources/
├── 🌐 templates/      # Thymeleaf templates
├── 🎨 static/         # CSS, JS, images
└── ⚙️ application.yml # Configuration
```

## 🎯 Learning Objectives

This project demonstrates:
- 🏗️ **MVC Pattern** implementation with Spring Boot
- 🔐 **Spring Security** for authentication/authorization
- 💾 **JPA/Hibernate** for database operations
- 🌿 **Thymeleaf**utemplating
- 📱 **Responsive** web design with Bootstrap

## 📝 Note

This is a **learning project** created by students who have been studying Spring Boot for **3 months**. The code may not follow all production best practices but demonstrates core Spring Boot concepts.

---

<div align="center">

**🎓 Made with ❤️ by Spring Boot learners**

![GitHub repo size](https://img.shields.io/github/repo-size/your-username/fsa-online-shop)
![GitHub last commit](https://img.shields.io/github/last-commit/your-username/fsa-online-shop)
![GitHub issues](https://img.shields.io/github/issues/your-username/fsa-online-shop)

</div>
