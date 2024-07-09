### OntoEval

OntoEval is a tool designed to evaluate ontologies. This guide will help you set up a development server, run the project, and build the kjar file.

#### Installing a Development Server

1. **Clone the repository:**
   ```sh
   git clone <repository_url>
   cd <repository_folder>

2. **Create the `application.properties` file:**

- Navigate to the `src/main/resources` directory.
- Copy the `application.properties.sample` file to create `application.properties`.
  ```sh
  cp src/main/resources/application.properties.sample src/main/resources/application.properties
- Edit the application.properties file to configure your environment-specific properties as needed.

3. **Run the project(in dev mode):**
   You can run project in two ways:
- using maven from the command line
   ```sh
   cd <repository_folder>
   mvn spring-boot:run

- using maven from the command line:

    - Open IntelliJ IDEA.
    - Select "Open" and navigate to the project directory.
    - Open the project.
    - Wait for IntelliJ to index the project and download dependencies.
    - Navigate to the main class (e.g., com.example.OntoEvalApplication).
    - Right-click the main class and select "Run 'OntoEvalApplication'".

4. Build the project (for deployment):

- build the project
   ```sh
   mvn clean install

- Navigate to the target folder
   ```sh
   cd target

- Run the generated jar file
   ```sh
   java -jar ontoeval-1.0-SNAPSHOT.jar

5. Endpoint Testing:
  ```
  curl -X POST http://localhost:8000/normalization \  -F 'ontologyFile=@/path/to/your/ontologyFile'
