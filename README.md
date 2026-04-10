# Banking System - Full Stack Project

A full-stack banking application built with **Spring Boot** (backend) and **React.js** (frontend).

---

## Project Structure

```
mary project/
├── banking-system-backend/       # Spring Boot (Java 17, Maven)
│   ├── pom.xml
│   └── src/main/java/com/banking/
│       ├── BankingSystemApplication.java
│       ├── config/SecurityConfig.java
│       ├── controller/
│       │   ├── UserController.java
│       │   ├── AccountController.java
│       │   └── TransactionController.java
│       ├── model/
│       │   ├── User.java
│       │   ├── Account.java
│       │   └── Transaction.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── AccountRepository.java
│       │   └── TransactionRepository.java
│       └── service/
│           ├── UserService.java
│           ├── AccountService.java
│           └── TransactionService.java
│
├── banking-system-frontend/      # React.js
│   ├── package.json
│   ├── public/index.html
│   └── src/
│       ├── App.js
│       ├── App.css
│       ├── index.js
│       ├── components/Navbar.js
│       ├── pages/
│       │   ├── LoginPage.js
│       │   ├── RegisterPage.js
│       │   └── Dashboard.js
│       ├── routes/AppRoutes.js
│       └── services/api.js
│
├── .gitignore
└── README.md
```

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+** & **npm**
- **MySQL 8.0+**

---

## Backend Setup

### 1. Create the MySQL database

```sql
CREATE DATABASE IF NOT EXISTS banking_system;
```

Or run the full schema script at:  
`banking-system-backend/src/main/resources/schema.sql`

### 2. Configure database credentials

Edit `banking-system-backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/banking_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password_here
```

### 3. Run the backend

```bash
cd banking-system-backend
mvn spring-boot:run
```

Backend starts at **http://localhost:8080**

### 4. Test the connection

Visit `http://localhost:8080/api/users` — should return `[]` (empty list).

---

## Frontend Setup

```bash
cd banking-system-frontend
npm install
npm start
```

Frontend starts at **http://localhost:3000**

---

## API Endpoints

| Method | Endpoint                          | Description              |
|--------|-----------------------------------|--------------------------|
| POST   | `/api/users/register`             | Register a new user      |
| GET    | `/api/users/{id}`                 | Get user by ID           |
| GET    | `/api/users`                      | Get all users            |
| POST   | `/api/accounts/create/{userId}`   | Create account for user  |
| GET    | `/api/accounts/{id}`              | Get account by ID        |
| GET    | `/api/accounts/user/{userId}`     | Get accounts by user     |
| POST   | `/api/accounts/{id}/deposit`      | Deposit into account     |
| POST   | `/api/transactions/transfer`      | Transfer between accounts|
| GET    | `/api/transactions/account/{id}`  | Get transactions by acct |

---

## Git Initialization Guide

```bash
# Navigate to the project root
cd "mary project"

# Initialize Git
git init

# Add all files
git add .

# First commit
git commit -m "Initial commit: Full-stack banking system with Spring Boot and React"

# (Optional) Connect to GitHub
git remote add origin https://github.com/YOUR_USERNAME/banking-system.git
git branch -M main
git push -u origin main
```

---

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, MySQL
- **Frontend:** React 18, React Router 6, Axios
- **Database:** MySQL 8
