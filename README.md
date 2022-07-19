# CZERTAINLY Network Discovery Provider

> This repository is part of the commercial open-source project CZERTAINLY, but the connector is available under subscription. 
> You can find more information about the project at [CZERTAINLY](https://github.com/3KeyCompany/CZERTAINLY) repository, 
> including the contribution guide.

Network Discovery provider, more commonly known as IP/Hostname Discovery provider, implements the logic of discovering certificates that are distributed over the network.

Network Discovery Provider can discover certificates from:
- Intranet - Scan the entire infrastructure inside an organization and discover the certificates from application and 
sites that are not exposed to the outside worked
- Internet - If the provider has access to the internet, It can discover certificates from any publicly accessible URLs

The `Connector` provides various options during the certificate, including:
- Single Host Scan
- Multiple Host Scan
- Single / Multi Subnet Scan
- Single / All port Scan

## Short Process Description

`Connector` discovers the certificates from the host without increasing the network traffic and congestion. When the connector receives the request to scan the host, it tries to connect to the ssl port (which can be left default to 443 or provided with custom value), captures the certificates and parses them. Once the certificates are successfully gathered, it is then sent back to the `Core` for storage and parsing. `Core` takes care of the rest.

To know more about `Core`, refer to [CZERTAINLY Core](https://github.com/3KeyCompany/CZERTAINLY-Core).

## Interfaces

Network discovery provider implements the `Discovery Provider` Interface from the CZERTAINLY Interfaces. To learn more about the interfaces and end points, refer to the [CZERTAINLY Interfaces](https://github.com/3KeyCompany/CZERTAINLY-Interfaces).

For more information regarding the `Discovery`, please refer to the [CZERTAINLY documentation](https://docs.czertainly.com).

## Docker container

Network Discovery Provider is provided as a Docker container. Use the `docker pull harbor.3key.company/czertainly/czertainly-ip-discovery-provider:tagname` to pull the required image from the repository. It can be configured using the following environment variables:

| Variable        | Description                       | Required | Default value |
|-----------------|-----------------------------------|----------|---------------|
| `JDBC_URL`      | JDBC URL for database access      | Yes      | N/A           |
| `JDBC_USERNAME` | Username to access the database   | Yes      | N/A           |
| `JDBC_PASSWORD` | Password to access the database   | Yes      | N/A           |
| `DB_SCHEMA`     | Database schema to use            | No       | network       |
| `PORT`          | Port where the service is exposed | No       | 8090          |