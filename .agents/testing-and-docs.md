# Testing and Docs

- Contract behavior is verified by Spring MockMvc integration tests.
- Isolated validation and bootstrap behavior use narrow unit tests.
- All entities remain covered by `BaseEntityArchitectureTest`.
- API-only behavior remains covered by `ApiOnlyArchitectureTest`.
- Tests run against H2 in MySQL compatibility mode; production remains MySQL.
- REST Docs snippets are generated into `build/generated-snippets`.
- AsciiDoc HTML is generated into `build/docs/asciidoc`.
- OpenAPI JSON is generated into `build/api-spec/openapi3.json`.

Run on JDK 21:

```bash
./gradlew test
./gradlew test asciidoctor openapi3
```

External contract changes must update tests, `docs/frontend-api`, AsciiDoc and OpenAPI artifacts together.
