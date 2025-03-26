# Data Processing Application

This is a Spring Boot application that processes match data from a file, stores the results in a MySQL database, and handles events asynchronously. The application reads a data file, processes each line, and saves the events in a priority order into a MySQL database.

## Features

- Reads data from a file (`fo_random.txt`).
- Processes match events asynchronously using Spring's `@Async`.
- Stores events in MySQL with automatic table creation.
- Tracks event sequences and prioritizes based on the event type.

## Prerequisites

Before running the application, ensure that you have the following installed on your system:

- **Java 17** or higher
- **Maven**
- **Docker** (for running MySQL in a container)

## Steps to Install and Run the Application

### 1. Clone the Repository

Clone the project repository to your local machine:

```bash
git clone <repository_url>
cd <project_directory>
```

### 2. Set Up MySQL Database

This project uses **MySQL** to store event data. You can set up MySQL using Docker. Follow these steps to run MySQL in a Docker container:

1. **Create a `docker-compose.yml` file** in your project directory (if not already provided):

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:latest
    container_name: mysql-db
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: match_data
      MYSQL_USER: user
      MYSQL_PASSWORD: password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

2. **Start the MySQL container** using Docker Compose:

```bash
docker-compose up -d
```

This command will pull the MySQL Docker image, create a container, and set up the database.

### 3. Configure Database Settings

In the `application.properties` file, ensure the correct database settings are used:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/match_data?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=user
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

This ensures the Spring Boot application connects to the MySQL database running on Docker.

### 4. Build the Project

Run the following command to build the project and download the required dependencies:

```bash
mvn clean install
```

### 5. Run the Application

Once the build is complete, run the application using the following command:

```bash
mvn spring-boot:run
```

This will start the Spring Boot application, and it will begin processing the data file located at `src/main/resources/fo_random.txt`. The events will be saved to the `match_data` database.

### 6. Verify Data in MySQL

You can verify that the data has been inserted into the MySQL database by accessing the MySQL container and running SQL commands:

1. **Access MySQL in the Docker container**:

```bash
docker exec -it mysql-db mysql -uuser -ppassword -D match_data
```

2. **View the data in the `match_events` table**:

```sql
SELECT * FROM match_events LIMIT 10;
```

### 7. Stop the Application and Docker Containers

Once you're done, you can stop the application and Docker containers:

- Stop the application: `Ctrl + C` in the terminal.
- Stop MySQL container:

```bash
docker-compose down
```

## Project Structure

The project consists of the following key components:

1. **`Exercise2Application.java`**: The main entry point of the application that starts the Spring Boot application and initiates the file processing.

2. **`DataProcessorService.java`**: The service that reads the data file, processes each line, and saves events to the database asynchronously. It uses a priority queue to ensure events are processed in the correct order.

3. **`MatchEvent.java`**: The model class representing a match event with fields such as `matchId`, `marketId`, `outcomeId`, and `specifiers`. This class is mapped to the `match_events` table in the database.

4. **`application.properties`**: Configuration file for setting up the Spring Boot application, including database connection settings.


## Conclusion

This project demonstrates an asynchronous data processing pipeline using Spring Boot, MySQL, and Docker. The application reads match event data, processes it in order, and stores it efficiently in a MySQL database.

---
