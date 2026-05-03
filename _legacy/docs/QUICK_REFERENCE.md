# Quick Reference (copied)

See root README or docs/CHANGELOG.md for details.

Commands:

```bash
cd fitlife-backend
mvn clean package
mvn flyway:migrate
mvn spring-boot:run
```

API quick test:

```bash
curl -X GET http://localhost:8080/members/me -H "Authorization: Bearer {JWT}"
```

