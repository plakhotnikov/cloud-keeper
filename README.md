# Cloud-Keeper

[![Test and deploy](https://github.com/plakhotnikov/cloud-keeper/actions/workflows/test-deploy.yaml/badge.svg?branch=main)](https://github.com/plakhotnikov/cloud-keeper/actions/workflows/test-deploy.yaml)


![Home page view](/.github/img/home_page.png)

## Overview

A cloud file storage application that provides users such features as:
- uploading files to storage;
- downloading from storage;
- renaming file or folder;
- deleting file or folder;
- creating folder;
- searching for specific file or folder.


## Technologies and frameworks
### Core:
![Java](https://img.shields.io/badge/java-white?style=for-the-badge&logo=java)
![Gradle](https://img.shields.io/badge/gradle-white?style=for-the-badge&logo=gradle&logoColor=black)
![Spring](https://img.shields.io/badge/spring-white?style=for-the-badge&logo=spring)
![Spring Boot 3](https://img.shields.io/badge/spring_boot-white?style=for-the-badge&logo=springboot)

### User and session management
![Spring Security](https://img.shields.io/badge/spring_security-white?style=for-the-badge&logo=springsecurity)
![Spring Session](https://img.shields.io/badge/spring_session-white?style=for-the-badge&logo=spring)
![Postgres](https://img.shields.io/badge/postgres-white?style=for-the-badge&logo=postgresql)
![Liquibase](https://img.shields.io/badge/liquibase-white?style=for-the-badge&logo=liquibase&logoColor=%232962FF)
![Redis](https://img.shields.io/badge/redis-white?style=for-the-badge&logo=redis)

### Object storage
![MinIO](https://img.shields.io/badge/minio-white?style=for-the-badge&logo=minio&logoColor=%23C72E49)

### Frontend:
![Thymeleaf](https://img.shields.io/badge/thymeleaf-white?style=for-the-badge&logo=thymeleaf&logoColor=%23005F0F)
![Bootstrap](https://img.shields.io/badge/bootstrap-white?style=for-the-badge&logo=bootstrap)
![JavaScript](https://img.shields.io/badge/javascript-white?style=for-the-badge&logo=javascript)

### Testing:
![JUnit5](https://img.shields.io/badge/junit5-white?style=for-the-badge&logo=junit5)
![Testcontainers](https://img.shields.io/badge/testcontainers-white?style=for-the-badge&logo=testcontainers)

### Deploy:
![Docker](https://img.shields.io/badge/docker-white?style=for-the-badge&logo=docker)
![Github Actions](https://img.shields.io/badge/github_actions-white?style=for-the-badge&logo=githubactions)

### Monitoring:
![Prometheus](https://img.shields.io/badge/prometheus-white?style=for-the-badge&logo=prometheus)
![Grafana](https://img.shields.io/badge/grafana-white?style=for-the-badge&logo=grafana)


## Deploy guide

Prerequisites:
- Docker: [Download Docker](https://www.docker.com/products/docker-desktop)

Local deploy steps:
1. Clone or download project
```shell
git clone https://github.com/plakhotnikov/cloud-keeper.git
```

2. Navigate to the project directory
```shell
cd cloud-keeper
```

3. Change prometheus password in `./monitoring/prometheus/prometheus_password.txt`
4. Configure ".env" file. Fill empty values and change some if you need
5. Ensure that ports 80 and 3000 are free on your local machine
6. Run multi-container application by following command:

```shell
docker compose -f compose-prod.yaml --env-file .env up -d --build
```

After that services will be accessible at:
- Cloud-Keeper application - http://localhost/
- Grafana - http://localhost:3000/

7. Enjoy :D