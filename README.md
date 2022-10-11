# Build
Make sure that Docker is installed. Build application's dockerimage with `sudo docker-compose build`.

# Launch
Launch dockerimage with `sudo docker-compose up -d`.

# Operation 
The application is able to process `/imports`, `/delete/{id}` and `/nodes/{id}` requests.

# Connection adjustments for local launch and inside Docker
Datasource for local launch:

`spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`

Datasource for launch inside Docker:

`spring.datasource.url=jdbc:postgresql://mypostgres:5432/postgres`

Host for RabbitMQ for local launch:

`rabbitmq.host=localhost`

Host for RabbitMQ for launch inside Docker:

`rabbitmq.host=rabbitmq`
