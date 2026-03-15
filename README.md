# Smart Canteen Pre-Ordering & Digital Queue Management System
### DBMS Project — Eclipse Dynamic Web Project + Apache Tomcat + MySQL

---

## Folder Structure

```
SmartCanteenSystem/
│
├── database/
│   └── smart_canteen.sql           ← Run this first in MySQL
│
├── src/
│   └── com/
│       └── smartcanteen/
│           ├── db/
│           │   └── DBConnection.java
│           └── servlet/
│               ├── RegisterServlet.java
│               ├── LoginServlet.java
│               ├── MenuServlet.java
│               ├── CartServlet.java
│               ├── OrderServlet.java
│               ├── AdminMenuServlet.java
│               └── OrderStatusServlet.java
│
└── WebContent/
    ├── css/
    │   └── style.css
    ├── pages/
    │   ├── index.html              ← Login
    │   ├── register.html
    │   ├── menu.html
    │   ├── cart.html
    │   ├── order_success.html
    │   └── admin_dashboard.html
    └── WEB-INF/
        └── web.xml
```

---

## Step-by-Step Setup

### 1. Set up MySQL Database
1. Open MySQL Workbench (or any MySQL client)
2. Run the file: `database/smart_canteen.sql`
3. This creates the `smart_canteen` database, all 5 tables, and sample data

### 2. Create Eclipse Dynamic Web Project
1. Open **Eclipse IDE for Enterprise Java Developers**
2. Go to `File → New → Dynamic Web Project`
3. Set Project Name: `SmartCanteenSystem`
4. Target Runtime: **Apache Tomcat v9.0** (or v10)
5. Click **Finish**

### 3. Add Source Files
- Copy the `src/com/` folder contents into the Eclipse project's `src/` folder
- Copy the `WebContent/` folder contents into the Eclipse project's `WebContent/` folder

### 4. Add MySQL JDBC Driver
1. Download **mysql-connector-java-8.x.x.jar** from:
   https://dev.mysql.com/downloads/connector/j/
2. Place the JAR in: `WebContent/WEB-INF/lib/`
3. Right-click project → `Build Path → Configure Build Path → Libraries → Add JARs`
4. Add the connector JAR

### 5. Configure Database Credentials
Open `src/com/smartcanteen/db/DBConnection.java` and update:
```java
private static final String DB_URL  = "jdbc:mysql://localhost:3306/smart_canteen?useSSL=false&serverTimezone=UTC";
private static final String DB_USER = "root";    // ← your MySQL username
private static final String DB_PASS = "root";    // ← your MySQL password
```

### 6. Configure Tomcat Server in Eclipse
1. Go to `Window → Show View → Servers`
2. Right-click in Servers panel → `New → Server`
3. Select **Apache Tomcat v9.0**, click Next
4. Set Tomcat installation directory, click Finish

### 7. Deploy & Run
1. Right-click project → `Run As → Run on Server`
2. Select your Tomcat server → Finish
3. Browser opens automatically at:
   `http://localhost:8080/SmartCanteenSystem/pages/index.html`

---

## Demo Login Credentials

| Role    | Email                  | Password   |
|---------|------------------------|------------|
| Admin   | admin@canteen.com      | admin123   |
| Student | arjun@college.edu      | student123 |
| Student | priya@college.edu      | student123 |

---

## Features Summary

### Student Flow
1. Register → Login → Browse Menu (with category filter)
2. Add items to Cart → Adjust quantities
3. Place Order (choose payment method)
4. View Order Confirmation with status timeline

### Admin Flow
1. Login → Admin Dashboard
2. View stats (total items, orders, pending, completed)
3. Add / Edit / Delete menu items
4. Toggle item availability (disable when stock runs out)
5. View all orders and update status:
   **Pending → Preparing → Ready → Completed**

---

## Database Tables

| Table        | Purpose                                    |
|--------------|--------------------------------------------|
| users        | Students and admin accounts                |
| menu         | Food items with pricing and availability   |
| orders       | Order headers with status tracking         |
| order_items  | Individual items within each order         |
| payments     | Payment records per order                  |

All tables are in **3rd Normal Form (3NF)**.

---

## Technologies Used

- **Frontend**: HTML5, CSS3, Vanilla JavaScript (Fetch API)
- **Backend**: Java Servlets (Jakarta EE / javax.servlet)
- **Database**: MySQL 8.x
- **Connectivity**: JDBC with PreparedStatements
- **Server**: Apache Tomcat 9
- **IDE**: Eclipse Dynamic Web Project
- **Architecture**: MVC (Model-View-Controller)

---

## Notes for Viva / Presentation

- All DB queries use **PreparedStatement** to prevent SQL injection
- Order placement uses a **database TRANSACTION** (commit/rollback)
- Cart is stored in **HTTP Session** — no DB round-trip until checkout
- **ENUM** columns for `status` and `role` enforce data integrity at DB level
- `order_items.subtotal` uses a **generated column** (computed by MySQL automatically)
- Database is fully normalized to **3NF**
