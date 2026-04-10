# Banking System — Deployment & Testing Guide

---

## Table of Contents

1. [Running Tests](#1-running-tests)
2. [Load Testing with Apache JMeter](#2-load-testing-with-apache-jmeter)
3. [Deploying the Spring Boot Backend](#3-deploying-the-spring-boot-backend)
4. [Deploying the React Frontend](#4-deploying-the-react-frontend)

---

## 1. Running Tests

### Backend Unit & Integration Tests

```bash
cd banking-system-backend

# Run all tests
mvn test

# Run only unit tests (service layer)
mvn test -Dtest="AuthServiceTest,AccountServiceTest,TransferServiceTest"

# Run only integration tests
mvn test -Dtest="AuthControllerIntegrationTest,TransferControllerIntegrationTest"

# Run with verbose output
mvn test -X
```

**Test files:**
- `src/test/java/com/banking/service/AuthServiceTest.java` — Register & login with Mockito
- `src/test/java/com/banking/service/AccountServiceTest.java` — Account creation, lookup, deposit
- `src/test/java/com/banking/service/TransferServiceTest.java` — Transfer success, failures, edge cases
- `src/test/java/com/banking/controller/AuthControllerIntegrationTest.java` — Full HTTP tests with MockMvc
- `src/test/java/com/banking/controller/TransferControllerIntegrationTest.java` — Transfer with JWT auth

### Frontend React Tests

```bash
cd banking-system-frontend

# Install dependencies first
npm install

# Run all tests
npm test

# Run tests with coverage
npm test -- --coverage --watchAll=false
```

**Test files:**
- `src/pages/LoginPage.test.js` — Form rendering, submission, success/error states
- `src/pages/RegisterPage.test.js` — Form rendering, submission, success/error states

---

## 2. Load Testing with Apache JMeter

### Step 1: Install JMeter
1. Download from: https://jmeter.apache.org/download_jmeter.cgi
2. Extract the ZIP file
3. Run `bin/jmeter.bat` (Windows) or `bin/jmeter.sh` (Mac/Linux)

### Step 2: Create a Test Plan for Login Load Test

1. **Open JMeter** → You'll see a blank "Test Plan"
2. **Right-click Test Plan** → Add → Threads (Users) → **Thread Group**
   - **Number of Threads (users):** `100`
   - **Ramp-Up Period (seconds):** `10` (starts 10 users per second)
   - **Loop Count:** `1`

3. **Right-click Thread Group** → Add → Sampler → **HTTP Request**
   - **Name:** Login Request
   - **Protocol:** `http`
   - **Server Name:** `localhost`
   - **Port Number:** `8080`
   - **Method:** `POST`
   - **Path:** `/api/auth/login`

4. **Right-click HTTP Request** → Add → Config Element → **HTTP Header Manager**
   - Add header: `Content-Type` = `application/json`

5. **In the HTTP Request Body Data tab**, enter:
   ```json
   {
     "email": "mary@example.com",
     "password": "123456"
   }
   ```

6. **Add Listeners** (to view results):
   - Right-click Thread Group → Add → Listener → **View Results Tree**
   - Right-click Thread Group → Add → Listener → **Summary Report**
   - Right-click Thread Group → Add → Listener → **Aggregate Report**

7. **Click the green Play button** to run the test

### Step 3: Create Transfer Load Test

1. **Add another Thread Group** (right-click Test Plan → Add → Thread Group)
   - **Number of Threads:** `50`
   - **Ramp-Up:** `5`

2. **First: Login to get JWT token**
   - Add HTTP Request for `/api/auth/login` (same as above)
   - Add **JSON Extractor** (right-click the login request → Add → Post Processors → JSON Extractor):
     - Variable Name: `token`
     - JSON Path: `$.token`

3. **Second: Transfer request**
   - Add another HTTP Request:
     - Method: `POST`
     - Path: `/api/transfer`
   - Add HTTP Header Manager:
     - `Content-Type` = `application/json`
     - `Authorization` = `Bearer ${token}`
   - Body Data:
     ```json
     {
       "senderAccountId": 1,
       "receiverAccountId": 2,
       "amount": 10
     }
     ```

4. **Run and analyze** the Summary Report for:
   - **Throughput** (requests/second)
   - **Average response time** (ms)
   - **Error %** (should be 0% for valid requests)

### What to Look For
| Metric | Good Value |
|--------|-----------|
| Average Response Time | < 200ms |
| Error Rate | 0% |
| Throughput | > 50 req/sec |
| 90th Percentile | < 500ms |

---

## 3. Deploying the Spring Boot Backend

### Option A: Build and Run JAR Locally

#### Step 1: Build the JAR
```bash
cd banking-system-backend
mvn clean package -DskipTests
```
This creates `target/banking-system-backend-0.0.1-SNAPSHOT.jar`

#### Step 2: Run the JAR
```bash
java -jar target/banking-system-backend-0.0.1-SNAPSHOT.jar
```
The app starts on http://localhost:8080

#### Step 3: Run with custom properties
```bash
java -jar target/banking-system-backend-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://your-db-host:3306/banking_system \
  --spring.datasource.username=your_username \
  --spring.datasource.password=your_password
```

### Option B: Deploy to Railway

1. **Create account** at https://railway.app
2. **Create a New Project** → "Deploy from GitHub Repo"
3. **Connect your GitHub repository**
4. **Add MySQL plugin:**
   - Click "New" → "Database" → "MySQL"
   - Railway auto-provisions a MySQL database
5. **Set environment variables** (in Railway dashboard → Variables):
   ```
   SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}
   SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
   SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   JWT_SECRET=YourProductionSecretKeyHere256BitsLongMinimum!!!
   ```
6. **Set build command:** `mvn clean package -DskipTests`
7. **Set start command:** `java -jar target/banking-system-backend-0.0.1-SNAPSHOT.jar`
8. **Deploy** — Railway builds and deploys automatically
9. **Copy the public URL** (e.g., `https://your-app.up.railway.app`)

### Option C: Deploy to Render

1. **Create account** at https://render.com
2. **New** → **Web Service** → Connect GitHub repo
3. **Settings:**
   - **Runtime:** Java
   - **Build Command:** `mvn clean package -DskipTests`
   - **Start Command:** `java -jar target/banking-system-backend-0.0.1-SNAPSHOT.jar`
4. **Add environment variables** (same as Railway)
5. **Create a MySQL database** on Render or use a free tier from:
   - https://planetscale.com (free tier)
   - https://www.freemysqlhosting.net
6. **Deploy**

---

## 4. Deploying the React Frontend

### Step 1: Update API Base URL

Before deploying, update `src/services/api.js` to point to your deployed backend:

```javascript
// Change from:
const API_BASE_URL = 'http://localhost:8080/api';

// To your deployed backend URL:
const API_BASE_URL = 'https://your-backend.up.railway.app/api';
```

**Better approach — use environment variable:**

Create a `.env` file in `banking-system-frontend/`:
```
REACT_APP_API_URL=https://your-backend.up.railway.app/api
```

Then in `api.js`:
```javascript
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

### Step 2: Build the Project

```bash
cd banking-system-frontend
npm install
npm run build
```
This creates a `build/` folder with static files.

### Option A: Deploy to Netlify

1. **Create account** at https://www.netlify.com
2. **Method 1 — Drag & Drop:**
   - Go to Netlify dashboard
   - Drag the `build/` folder onto the page
   - Done! You get a URL like `https://random-name.netlify.app`

3. **Method 2 — GitHub Deploy:**
   - Click "New Site from Git" → Connect GitHub
   - **Build Command:** `npm run build`
   - **Publish Directory:** `build`
   - **Environment Variable:** `REACT_APP_API_URL` = your backend URL
   - Click **Deploy**

4. **Important:** Create `banking-system-frontend/public/_redirects`:
   ```
   /*    /index.html   200
   ```
   This fixes React Router on Netlify (prevents 404 on page refresh).

### Option B: Deploy to Vercel

1. **Create account** at https://vercel.com
2. **Import Project** → Connect GitHub repo
3. **Settings:**
   - **Framework Preset:** Create React App
   - **Root Directory:** `banking-system-frontend`
   - **Build Command:** `npm run build`
   - **Output Directory:** `build`
4. **Environment Variables:**
   - `REACT_APP_API_URL` = your backend URL
5. **Click Deploy**
6. You get a URL like `https://your-app.vercel.app`

### Step 3: Update Backend CORS

After deploying the frontend, update `SecurityConfig.java` to allow your deployed frontend domain:

```java
configuration.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "https://your-app.netlify.app"  // Add your deployed frontend URL
));
```

---

## Quick Reference: Full Deployment Checklist

### Backend
- [ ] Update `application.properties` with production database credentials
- [ ] Change `jwt.secret` to a strong production key
- [ ] Update CORS to allow frontend production URL
- [ ] Build JAR: `mvn clean package -DskipTests`
- [ ] Deploy to Railway/Render
- [ ] Test: `GET https://your-backend-url/api/test/connection`

### Frontend
- [ ] Set `REACT_APP_API_URL` environment variable
- [ ] Build: `npm run build`
- [ ] Add `_redirects` file (for Netlify)
- [ ] Deploy to Netlify/Vercel
- [ ] Test: Open deployed URL → Register → Login → Dashboard → Transfer

### Database
- [ ] Create production MySQL database
- [ ] Run `schema.sql` or let Hibernate auto-create tables
- [ ] Verify DataLoader seeds sample data on first run
