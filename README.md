
# Hotel Room Reservation System

An Object-Oriented Programming (OOP) group project developed for the **SE1020 module**.

This web-based hotel reservation system allows customers to search for available rooms, make reservations, manage payments, and generate receipts. Administrators can manage rooms and monitor hotel operations through a dashboard.

## Features

- **User Authentication:** Customer registration, login, logout, and profile management.
- **Room Management:** Add, view, update, and delete hotel rooms.
- **Search & Availability:** Search rooms by dates, guest count, room type, and price range. Save and manage search preferences.
- **Reservation Management:** Create, view, update, and cancel reservations with overlapping-booking prevention.
- **Payment & Receipt:** Process simulated payments, manage payment records, and generate printable receipts.
- **Admin Dashboard & Reports:** View hotel statistics and generate, manage, and review reports.

## Technologies Used

| Component | Technology |
|---|---|
| Backend | Java 21 |
| Web Framework | Javalin 7.2.3 |
| Frontend | HTML, CSS, JavaScript |
| Data Storage | JSON Files (Jackson) |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito |
| IDE | IntelliJ IDEA |
| Version Control | Git & GitHub |

## OOP Concepts & Algorithms

The project demonstrates:

- **Encapsulation:** Private fields with controlled access through methods.
- **Inheritance:** Feature repositories extend a shared generic repository.
- **Abstraction:** Repository interfaces and abstract classes.
- **Polymorphism:** Method overriding for repository-specific validation.
- **Exception Handling:** Custom exceptions for validation and business rules.
- **Binary Search Tree (BST):** Organizes filtered available rooms and supports room lookup.
- **Quick Sort:** Sorts reservations by check-in date.

## Project Structure

```text
src/
├── main/
│   ├── java/com/hotel/
│   │   ├── admin/
│   │   ├── algorithm/
│   │   ├── common/
│   │   ├── payment/
│   │   ├── reservation/
│   │   ├── room/
│   │   ├── search/
│   │   ├── user/
│   │   └── HotelApplication.java
│   └── resources/public/
│       ├── css/
│       ├── js/
│       └── *.html
└── test/java/com/hotel/
data/
pom.xml
README.md
```

The application uses JSON files in the `data/` directory for persistent storage instead of a database.

## How to Run

**Prerequisites:**
- JDK 21
- Maven
- IntelliJ IDEA (recommended)

**Steps:**

1. Clone the repository or download the project.
2. Open the project in IntelliJ IDEA.
3. Set the Project SDK to **Java 21**.
4. Reload the Maven project to install dependencies.
5. Run the `main()` method in `HotelApplication.java`.
6. Open the application in your browser:

   `http://localhost:7070`

The API health endpoint is available at:

`http://localhost:7070/api/health`

### Run Automated Tests

From the project root, execute:

```bash
mvn clean test
```

## Team Contributions

| Member | Responsible Feature |
|---|---|
| Ashini | User Authentication & Management |
| Renu | Room Management |
| Sithum | Search & Availability |
| Ashan | Reservation Management |
| Kaveesha | Payment & Receipt |
| Mihiri | Admin Dashboard & Reports |

## Important Note

Payment processing is simulated for educational purposes. The application does not perform real financial transactions.

---

**SE1020 – Object-Oriented Programming Group Project**
