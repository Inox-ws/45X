# 45X Portal

Manufacturing tax-credit compliance and reporting platform for IRC Section 45X
(Advanced Manufacturing Production Credit), built for INOX Solar Americas. See
the full build spec for scope, architecture, and milestone plan.

## Stack

- **Frontend:** Angular 18, standalone components, Angular Material, MSAL (Entra ID), ECharts
- **Backend:** Java 21, Spring Boot 3.3.5, Spring Data JPA, Spring Security, springdoc OpenAPI
- **Database:** Azure SQL Database (SQL Server) in prod, H2 (SQL Server compatibility mode) for local dev, Flyway migrations
- **Storage:** Azure Blob Storage
- **Auth:** Microsoft Entra ID (JWT bearer), with a local-dev fallback

## Repository layout

```
45x-portal/
├── frontend/       Angular app
├── backend/        Spring Boot API
├── infra/          Azure deployment guide + Entra app-role definitions
└── .github/workflows/   CI + Azure deploy workflows
```

## Prerequisites

- Java 21 (`java -version`)
- Maven 3.9+ (`mvn -version`)
- Node 20+ and npm (`node -v`, `npm -v`)

## Running locally

### Backend

```bash
cd backend
mvn spring-boot:run
```

Runs on `http://localhost:8080` with the `local` Spring profile (default): an
in-memory H2 database in SQL Server compatibility mode, no Azure/Entra
dependency. Swagger UI: `http://localhost:8080/swagger-ui.html`. Health check:
`http://localhost:8080/actuator/health`. Demo login: `demo.admin@example.com` / `ChangeMe123!`.

> **Note on this dev environment:** the sandbox this was built in has Maven
> pinned to offline mode with an incomplete local artifact cache, so backend
> code has been reviewed statically rather than compiled/run here. `pom.xml`
> targets the spec-mandated versions regardless - run `mvn spring-boot:run` on
> a machine with normal Maven Central access (or let CI do it).

### Frontend

```bash
cd frontend
npm install
npm start
```

Runs on `http://localhost:4200` against `http://localhost:8080/api/v1` (see
`src/environments/environment.development.ts`). Built, run, and clicked
through end-to-end in this environment at every milestone.

## Deploying to Azure

See [infra/azure-deployment.md](infra/azure-deployment.md) - covers the Entra
ID app registrations, App Service configuration, Managed Identity + Blob
Storage role assignment, SQL Server firewall, and the GitHub Actions secrets
[deploy-backend.yml](.github/workflows/deploy-backend.yml) and
[deploy-frontend.yml](.github/workflows/deploy-frontend.yml) need.

## Milestone status

| # | Milestone | Status |
|---|-----------|--------|
| 1 | Scaffold | ✅ |
| 2 | Data & migrations | ✅ |
| 3 | Auth & RBAC | ✅ |
| 4 | Storage & OCR | ✅ (Document Intelligence integration is best-effort/unverified - see code comments) |
| 5 | Core modules | ✅ |
| 6 | Engines (FEOC, credit calc, audit) | Not started |
| 7 | Dashboards & Reports | Not started |
| 8 | Integrations (SAP/MES) | Not started |
| 9 | Hardening | Not started |
| 10 | Deploy | In progress - CI/CD workflows and Azure config guide exist; not yet run against real resources |

## Notes on scope decisions

- The left-nav includes both the 8 modules from the functional spec (Dashboard,
  Upload Invoice, Upload POD, Search Invoice, Traceability, FEOC Compliance,
  Credit Calculator, Reports) **and** the document-repository-style screens
  seen in the reference portal screenshots (Company Documents, Customer
  Contracts, Financial Statement), per an earlier "build both" decision.
- Master Data (Suppliers, Customers, Credit Rates, FEOC list, Materials) uses
  one generic reusable table+form component rather than five near-identical
  hand-written screens.
