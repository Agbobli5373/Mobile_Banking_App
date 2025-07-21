# 📱 MyBank – Simple Mobile Banking App – Product Requirements Document

_Last updated: 2025-07-21_

---

## 1. 🎯 Objective

Build a simple Full Stack mobile banking backend application using Java and Spring Boot. The app will allow users to register, log in, check wallet balances, transfer money to other users, and view transaction history.

---

## 2. 🧩 Features

### 2.1. User Authentication
- Register with name, phone number, and PIN
- Login using phone number and PIN
- JWT-based session authentication

### 2.2. Wallet
- View current wallet balance
- Send money to another user
- Add funds (simulated)
- Transaction history

---

## 3. 🧱 Core Entities

### 3.1. User
- id (UUID)
- name (String)
- phone (String)
- pin (String - hashed)
- balance (BigDecimal)

### 3.2. Transaction
- id (UUID)
- sender (User)
- receiver (User)
- amount (BigDecimal)
- timestamp (LocalDateTime)

---

## 4. 🔐 Security
- JWT-based token authentication
- PINs are securely hashed
- API endpoints protected using Spring Security
- Transactions wrapped in @Transactional

---

## 5. 📊 API Endpoints

### /api/auth/register [POST]
Registers a new user.

### /api/auth/login [POST]
Authenticates user and returns JWT token.

### /api/wallet/balance [GET]
Returns current balance of logged-in user.

### /api/wallet/send [POST]
Transfers funds to another user.

### /api/wallet/transactions [GET]
Returns list of transactions (sent and received).

---

## 6. 💾 Database Tables

### users
| Column   | Type       |
|----------|------------|
| id       | UUID       |
| name     | String     |
| phone    | String     |
| pin      | String     |
| balance  | BigDecimal |

### transactions
| Column      | Type       |
|-------------|------------|
| id          | UUID       |
| sender_id   | Long       |
| receiver_id | Long       |
| amount      | BigDecimal |
| timestamp   | DateTime   |

---

## 7. 🧪 Test Cases

| Scenario | Steps |
|----------|-------|
| Register | Register 2 users with name/phone/pin |
| Fund     | Top up one user's balance (manually) |
| Transfer | Send money from user A to user B |
| Balance  | Confirm both users’ new balances |
| History  | Fetch each user’s transaction history |

---

## 8. 🚀 Future Enhancements

- Add OTP for transactions
- Introduce savings wallets
- Support mobile money funding (MTN, Vodafone, etc.)
- Integration with a notification service (SMS/email)

