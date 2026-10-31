# University Maintenance Management System

## Project Overview

The University Maintenance Management System is a web application developed using Spring Boot, Thymeleaf and MySQL.

The system allows university students to report maintenance problems in buildings and laboratories. Administrators can view complaints, manage their status and use Gemini AI to get suggestions about complaint priority, category and the appropriate maintenance team.

## Technologies Used

- Java 17
- Spring Boot 4
- Spring MVC and Thymeleaf
- Spring Data JPA / Hibernate
- Spring Security
- MySQL
- HTML and CSS
- Gemini API
- Maven
- Git and GitHub

## Main Features

### Student
- Login using a student account
- Submit maintenance complaints
- Select the building and enter the problem location
- View previously submitted complaints
- Check complaint status

### Administrator
- Login using an administrator account
- View all submitted complaints
- Add, edit and delete complaints
- Update complaint status
- Search, filter and sort complaints
- Analyze complaints using Gemini AI

### AI Complaint Analysis

The system uses the Gemini API to analyze maintenance complaints.

Based on the complaint title and description, Gemini suggests a category, priority, maintenance team and a reason for its recommendation.

AI suggestions are displayed for administrator review. They do not automatically change the stored complaint.

## Database Design

The application uses a MySQL database named `university_maintenance`.

The main tables are:

**buildings**
- id (Primary Key)
- name (Unique)

**complaints**
- id (Primary Key)
- title
- description
- location
- category
- priority
- status
- building_id (Foreign Key)
- submitted_by

One building can have multiple complaints. Each complaint can be associated with one building.

The `submitted_by` field records the username of the account that submitted the complaint.

## How to Run the Project

### Requirements

- Java 17
- MySQL Server
- Git
- An IDE such as VS Code
- Internet connection for Gemini AI

### Setup

1. Clone this GitHub repository.
2. Open the project in VS Code.
3. Create a MySQL database named `university_maintenance`.
4. Configure the database connection using environment variables.
5. Configure the demo login passwords.
6. Configure a Gemini API key if AI analysis is required.
7. Open a terminal in the project directory.
8. Run the application using Maven.

Windows command:

`.\mvnw.cmd spring-boot:run`

The application runs locally at:

`http://localhost:8080`

### Environment Variables

The application uses these environment variables:

| Variable | Purpose |
|---|---|
| DB_PASSWORD | MySQL database password |
| DB_USERNAME | MySQL database username (optional locally) |
| DB_URL | MySQL JDBC connection URL (optional locally) |
| DEMO_STUDENT_PASSWORD | Password for the demonstration student account |
| DEMO_ADMIN_PASSWORD | Password for the demonstration administrator account |
| GEMINI_API_KEY | Gemini API key for complaint analysis |

The local application uses demo accounts named `student1` and `admin`. Their passwords are configured separately and are not included in the repository.

Building records should be inserted into the `buildings` table before testing the complaint submission form.

## Project Structure

- `entity` — Database entities
- `repository` — Database access using Spring Data JPA
- `service` — Application logic and Gemini API integration
- `controller` — Handles user requests and page navigation
- `config` — Spring Security configuration
- `templates` — Thymeleaf HTML pages
- `static` — CSS files

## Current Limitations

- User accounts are configured in memory for the demonstration.
- Public user registration is not implemented.
- AI suggestions require an internet connection and a valid Gemini API key.
- AI recommendations require human review.

## Project Documentation

The project documentation includes an ER diagram, screenshots of the main interfaces and a demonstration of the application.

## Author

University Mini Project — Web Application Development