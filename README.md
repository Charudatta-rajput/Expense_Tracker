# Expense Tracker App

An Android application designed to track daily expenses, manage income, and provide meaningful insights into spending behavior.

---

## Features

### Expense Management
- Add, delete, and manage transactions
- Separate tracking for income and expenses
- Filter transactions by type (income/expense)
- View complete transaction history

### Home Dashboard
- Overview of total balance
- Display of total income and expenses
- Weekly expense graph
- Recent transactions preview

### Smart Transaction Detection
- Detects transactions from SMS (UPI and bank messages)
- Extracts amount and transaction details
- Sends a notification to review and add the transaction
- Reduces manual data entry

### Goals and Challenges
- Set monthly spending limits
- Track no-spend days
- Start no-spend challenges
- Maintain spending streaks

### Insights and Analytics
- Top spending categories
- Weekly comparison with previous week
- Daily average spending
- Monthly comparison with last three months
- Category-wise spending breakdown

### Data Export
- Export transaction data to CSV
- Export transaction data to PDF

---

## Screens

- Home Screen – Balance, income, expenses, weekly graph, recent transactions  
- Transactions Screen – Transaction list with delete option and filters  
- Goal Screen – Monthly limits, no-spend tracking, and challenges  
- Insights Screen – Analytics and comparisons  

---

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM Architecture
- Room Database

---

## Note

- Dark theme is not supported yet and will be added in future updates

---

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Charudatta-rajput/Expense_Tracker.git
2. Open the project in Android Studio
3. Sync Gradle
4. Run the application on an emulator or physical device
