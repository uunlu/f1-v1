Here is a markdown file you can use with Cursor AI for generating the required Java Spring Boot service and integration tests.

Java Spring Boot Service for Fetching Season Champions
Overview

This project will use the Ergast Developer API to fetch data about Formula 1 season champions from 2005 to 2024. The service will extract only the winner's details, including the driver's and constructor's information, and provide a clean, modular implementation.

Requirements
Use the Ergast Developer API endpoint:
https://api.jolpi.ca/ergast/f1/<year>/results/
Fetch data for all seasons from 2005 to 2024.
Extract only the winner's details (Driver and Constructor information).
Implement a clean, modular Java Spring Boot service.
Write integration tests for the service.
Project Structure
Service Layer

Create a service named SeasonChampionService that will:

Fetch data from the Ergast Developer API.
Parse the JSON response to extract the required details.
Return a list of season champions, each containing:
Driver details (name, nationality, date of birth, etc.).
Constructor details (name, nationality, etc.).
Model Classes

Define the following model classes:

Driver:

driverId
permanentNumber
code
givenName
familyName
dateOfBirth
nationality

Constructor:

constructorId
name
nationality

SeasonChampion:

season
driver (of type Driver)
constructor (of type Constructor)
Integration Tests

Write integration tests to:

Verify that the service correctly fetches data for all seasons from 2005 to 2024.
Validate that only the winner's details are extracted.
Ensure proper handling of API errors or unexpected responses.
Implementation Details
Dependencies

Add the following dependencies to your pom.xml:

xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

Service Interface
java
public interface SeasonChampionService {
    List<SeasonChampion> getSeasonChampions(int startYear, int endYear);
}

Service Implementation
java
@Service
public class SeasonChampionServiceImpl implements SeasonChampionService {

    private final RestTemplate restTemplate;

    public SeasonChampionServiceImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public List<SeasonChampion> getSeasonChampions(int startYear, int endYear) {
        List<SeasonChampion> champions = new ArrayList<>();

        for (int year = startYear; year <= endYear; year++) {
            String url = String.format("https://api.jolpi.ca/ergast/f1/%d/results/", year);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                champions.add(parseChampionFromResponse(response.getBody(), year));
            }
        }

        return champions;
    }

    private SeasonChampion parseChampionFromResponse(String responseBody, int year) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            JsonNode driverNode = rootNode.path("MRData")
                                          .path("StandingsTable")
                                          .path("StandingsLists")
                                          .get(0)
                                          .path("DriverStandings")
                                          .get(0)
                                          .path("Driver");

            JsonNode constructorNode = rootNode.path("MRData")
                                               .path("StandingsTable")
                                               .path("StandingsLists")
                                               .get(0)
                                               .path("DriverStandings")
                                               .get(0)
                                               .path("Constructors")
                                               .get(0);

            Driver driver = new Driver(
                driverNode.path("driverId").asText(),
                driverNode.path("permanentNumber").asText(),
                driverNode.path("code").asText(),
                driverNode.path("givenName").asText(),
                driverNode.path("familyName").asText(),
                driverNode.path("dateOfBirth").asText(),
                driverNode.path("nationality").asText()
            );

            Constructor constructor = new Constructor(
                constructorNode.path("constructorId").asText(),
                constructorNode.path("name").asText(),
                constructorNode.path("nationality").asText()
            );

            return new SeasonChampion(year, driver, constructor);

        } catch (Exception e) {
            throw new RuntimeException("Error parsing API response for year " + year, e);
        }
    }
}

Integration Test
java
@SpringBootTest
@AutoConfigureMockMvc
public class SeasonChampionServiceIntegrationTest {

    @Autowired
    private SeasonChampionService seasonChampionService;

    @Test
    public void testGetSeasonChampions() {
        List<SeasonChampion> champions = seasonChampionService.getSeasonChampions(2005, 2024);

        assertNotNull(champions);
        assertEquals(20, champions.size()); // 2005 to 2024

        SeasonChampion firstChampion = champions.get(0);
        assertEquals(2005, firstChampion.getSeason());
        assertNotNull(firstChampion.getDriver());
        assertNotNull(firstChampion.getConstructor());
    }

    @Test
    public void testApiErrorHandling() {
        // Simulate API error and validate proper handling
    }
}

Expected Output

The service should return a list of SeasonChampion objects, each containing:

Season year
Driver details (name, nationality, etc.)
Constructor details (name, nationality, etc.)
Notes for Cursor AI
Generate all required files, including SeasonChampionService, SeasonChampionServiceImpl, model classes (Driver, Constructor, SeasonChampion), and integration tests.
Ensure the code adheres to clean coding principles and is modular.
Use appropriate annotations and configurations for Spring Boot.

You can now share this markdown file with Cursor AI to generate the required files.