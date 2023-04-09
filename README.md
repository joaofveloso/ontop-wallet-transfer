# OnTop Wallet Management
This is a Wallet Management project built with Java 17 and Spring Boot 2.7.*. The goal of this application is to handle money transfers from OnTop accounts to recipient accounts while applying an OnTop fee. To implement all functionalities, I have used instances of Kafka and MongoDB through docker-compose.

## Architecture
The application is developed using Hexagonal Architecture, which separates the system into three main layers: Application, Core, and Infrastructure. This allows for a more maintainable and testable codebase with clear separation of concerns.

### Application Layer
The Application Layer contains the entry points to the application, such as API controllers and messaging listeners.

### Core Layer
The Core Layer contains the business logic of the application and is written purely in Java, with no dependencies on any external frameworks. This layer is responsible for processing requests, applying business rules, and producing the appropriate responses. It also contains the domain models and interfaces for services and repositories.

### Infrastructure Layer
The Infrastructure Layer is responsible for handling external communication and data persistence. This layer includes the implementation of services and repositories, as well as integrations with Kafka and MongoDB. By keeping these concerns separate from the Core Layer, we can easily swap out different technologies or implementations if needed.

## Instalation
To install and run this application, you can use the probided docker-compose file. Firstly, clone the repository:

`
git@github.com:joaofveloso/ontop-wallet-transfer.git
`

Then build the application:

`
mvn clean install
`

Finally, start the application by running:

`
docker-compose up -d
`

The application will start running on port 8080

## Usage
To use the application, open your web browser and navigate to http://localhost:8080/swagger-ui/index.html. This will open a Swagger-UI interface where you can interact with the API. For more information on how to use the tool, please refer to the file named **[USAGE.md](./USAGE.md)**.'

## Contributing
If you'd like to contribute to this project, please follow these guidelines:

- Fork the repository and create a new branch for your changes.
- Make your changes and test them thoroughly.
- Submit a pull request with a clear description of your changes and why they are necessary.

## License
This project is licensed under the MIT License. See the LICENSE file for details.

## Credits
This project was created by João Veloso for the Ontop job opportunity
