# Auto-École Management System

## Database Connection Setup

To fix the database connection issue, you need to add the MySQL connector to your classpath:

1. Make sure you have the MySQL connector JAR file in your project's `external_dependencies` folder.
2. Add the MySQL connector to your classpath using one of these methods:

   ### Method 1: Using an IDE (IntelliJ IDEA or Eclipse)
   - In IntelliJ IDEA: File > Project Structure > Libraries > + > Java > Navigate to `external_dependencies/mysql-connector-j-9.2.0.jar`
   - In Eclipse: Right-click on project > Build Path > Configure Build Path > Libraries > Add External JARs > Navigate to `external_dependencies/mysql-connector-j-9.2.0.jar`

   ### Method 2: Using Maven
   - If you're using Maven, make sure the following dependency is in your pom.xml:
   ```xml
   <dependency>
       <groupId>com.mysql</groupId>
       <artifactId>mysql-connector-j</artifactId>
       <version>9.2.0</version>
   </dependency>
   ```

   ### Method 3: Using Command Line
   - When running the application from the command line, include the connector in the classpath:
   ```
   java -cp ".;external_dependencies/mysql-connector-j-9.2.0.jar" org.cpi2.Main
   ```
   (Use `:` instead of `;` on Unix/Mac systems)
Feel free to edit this file and/or modify it with new features
**Ideas**:
- **Use Icons instead of buttons** (kinda cool tbh)
## Emphasis on Threads and Parallel Programming
The application could leverages **multithreading and parallel programming** to enhance performance and responsiveness:
- **Background Tasks**: Real-time notifications for document expiration and session reminders
- **User Input Prioritization**: Ensures that while performing background tasks the system should remains highly responsive to user input providing a smooth user experience during form filling or navigation (Ayoub's Idea tho)
- **Data Processing**: Efficient data retrieval and updates in the dashboard using asynchronous threads

## Business Rules
### B6
- Each candidate should consistently use the same vehicle and  preferably  the same instructor for all practical sessions

### B7
- Vehicles older than 10 years must undergo a technical inspection every year
- Comprehensive history of each vehicle including repairs and maintenance  is maintained

### B8
- Future updates will include advanced features for tracking success rates and optimizing resource allocation

## Architecture
The application must use the layered Architecture (yall aleardy know this)


## UML Diagrams
Still don't know shit about this
