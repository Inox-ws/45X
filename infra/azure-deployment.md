# Deploying to Azure

Maps the four resources you already created to this repo, per Section 15:

| Component | Resource | Region |
|---|---|---|
| Frontend | Static Web App `inox-45x-web` | East US 2 |
| Backend | App Service `inox-45x-api` | Canada East |
| Database | SQL DB `InoxDb` on server `inox-45x-sql` | Central US |
| Storage | Storage account `inox45xstorage` | Central US |

Run everything below from the [Azure Cloud Shell](https://shell.azure.com) (bash) or a
local machine with `az` installed and `az login` already run.

```bash
RESOURCE_GROUP="rg-inox-45x"
APP_SERVICE_NAME="inox-45x-api"
SQL_SERVER_NAME="inox-45x-sql"
SQL_DB_NAME="InoxDb"
STORAGE_ACCOUNT_NAME="inox45xstorage"
STATIC_WEB_APP_NAME="inox-45x-web"
STATIC_WEB_APP_URL="https://mango-river-0f0c2980f.7.azurestaticapps.net"
TENANT_ID="cc53b754-b4f2-454d-8e85-644309d1354c"
```

## 1. Entra ID app registrations

Two app registrations are needed - one for the backend API (validates tokens), one for
the frontend SPA (signs users in). Neither of these steps touches the four resources
above; they're tenant-level objects. **Do this once.**

### 1a. Backend API app registration

```bash
API_APP_JSON=$(az ad app create \
  --display-name "45X Portal API" \
  --sign-in-audience AzureADMyOrg \
  --app-roles @infra/entra-app-roles.json)

API_APP_ID=$(echo "$API_APP_JSON" | jq -r '.appId')
echo "Backend API client ID: $API_APP_ID"

# Expose an API - defaults the Application ID URI to api://<API_APP_ID>, which is
# also the value EntraSecurityConfig expects as X45_ENTRA_API_AUDIENCE.
az ad app update --id "$API_APP_ID" --identifier-uris "api://$API_APP_ID"

# Add a delegated scope (access_as_user) so the SPA can request a token for this API.
SCOPE_ID=$(python3 -c "import uuid; print(uuid.uuid4())" 2>/dev/null || echo "66666666-6666-4666-8666-666666666666")
az ad app update --id "$API_APP_ID" --set oauth2Permissions="[{\"id\":\"$SCOPE_ID\",\"adminConsentDescription\":\"Allow the app to access 45X Portal API as the signed-in user\",\"adminConsentDisplayName\":\"Access 45X Portal API\",\"isEnabled\":true,\"type\":\"User\",\"userConsentDescription\":\"Allow the app to access 45X Portal API on your behalf\",\"userConsentDisplayName\":\"Access 45X Portal API\",\"value\":\"access_as_user\"}]"

# A service principal is required before role assignments (Section 1c) work.
az ad sp create --id "$API_APP_ID" 2>/dev/null || true
```

> The app roles in `infra/entra-app-roles.json` are named `FINANCE` / `LOGISTICS` /
> `PRODUCTION` / `MANAGEMENT` / `ADMIN` - exactly what `EntraJwtRoleConverter` expects in
> the token's `roles` claim. Don't rename them without updating that class too.

### 1b. Frontend SPA app registration

```bash
SPA_APP_JSON=$(az ad app create \
  --display-name "45X Portal Frontend" \
  --sign-in-audience AzureADMyOrg \
  --spa-redirect-uris "$STATIC_WEB_APP_URL" "http://localhost:4200")

SPA_APP_ID=$(echo "$SPA_APP_JSON" | jq -r '.appId')
echo "Frontend SPA client ID: $SPA_APP_ID"

# Grant the SPA permission to call the backend API's access_as_user scope.
az ad app permission add --id "$SPA_APP_ID" \
  --api "$API_APP_ID" \
  --api-permissions "$SCOPE_ID=Scope"

# Admin-consent it (requires Global Admin / Application Admin in the tenant).
az ad app permission admin-consent --id "$SPA_APP_ID"
```

### 1c. Assign roles to users

Neither app registration grants anyone a role by default. In the Azure Portal:
**Entra ID -> Enterprise Applications -> "45X Portal API" -> Users and groups -> Add
assignment**, and assign each real user one of the five roles. Without this, a user can
sign in but every `@PreAuthorize` check will fail (they'll have no roles at all).

### 1d. Values you now have

```
X45_ENTRA_TENANT_ID   = cc53b754-b4f2-454d-8e85-644309d1354c   (already the default in application.yml)
X45_ENTRA_API_AUDIENCE = api://<API_APP_ID from step 1a>
Frontend clientId       = <SPA_APP_ID from step 1b>
Frontend authority      = https://login.microsoftonline.com/cc53b754-b4f2-454d-8e85-644309d1354c
```

Put the frontend two values into `frontend/src/environments/environment.ts`
(`entra.clientId`), then rebuild/redeploy the frontend.

## 2. App Service (backend) configuration

```bash
# Runtime stack: this MUST be Java SE (embedded Tomcat via Spring Boot's executable
# jar), not the "Tomcat" stack (which expects a .war). Check first:
az webapp config show -g "$RESOURCE_GROUP" -n "$APP_SERVICE_NAME" --query linuxFxVersion
# If it doesn't say something like "JAVA|21-java21", fix it:
az webapp config set -g "$RESOURCE_GROUP" -n "$APP_SERVICE_NAME" --linux-fx-version "JAVA|21-java21"

az webapp config appsettings set -g "$RESOURCE_GROUP" -n "$APP_SERVICE_NAME" --settings \
  SPRING_PROFILES_ACTIVE="azure" \
  X45_DB_URL="jdbc:sqlserver://${SQL_SERVER_NAME}.database.windows.net:1433;database=${SQL_DB_NAME};encrypt=true;trustServerCertificate=false;loginTimeout=30" \
  X45_STORAGE_ACCOUNT_URL="https://${STORAGE_ACCOUNT_NAME}.blob.core.windows.net" \
  X45_FRONTEND_ORIGIN="$STATIC_WEB_APP_URL" \
  X45_ENTRA_TENANT_ID="$TENANT_ID" \
  X45_ENTRA_API_AUDIENCE="api://$API_APP_ID"
```

**SQL credentials** - set these too (see Section 3 for the matching database-side setup).
Do this in the Azure Portal (App Service -> Configuration -> Application settings) or via
CLI with your own shell variables - never paste a real password into a chat or commit it:

```bash
az webapp config appsettings set -g "$RESOURCE_GROUP" -n "$APP_SERVICE_NAME" --settings \
  X45_DB_USERNAME="<your SQL admin login>" \
  X45_DB_PASSWORD="<your SQL admin password>"
```

**Leave the App Service's own CORS blade (API -> CORS) empty.** The Spring Boot app
already handles CORS itself via `X45_FRONTEND_ORIGIN` - enabling both would double up
`Access-Control-Allow-Origin` headers.

### 2a. Managed Identity + Blob access

```bash
az webapp identity assign -g "$RESOURCE_GROUP" -n "$APP_SERVICE_NAME"
PRINCIPAL_ID=$(az webapp identity show -g "$RESOURCE_GROUP" -n "$APP_SERVICE_NAME" --query principalId -o tsv)
STORAGE_ID=$(az storage account show -g "$RESOURCE_GROUP" -n "$STORAGE_ACCOUNT_NAME" --query id -o tsv)

az role assignment create \
  --assignee "$PRINCIPAL_ID" \
  --role "Storage Blob Data Contributor" \
  --scope "$STORAGE_ID"
```

This is what lets `AzureBlobStorageService` (Section 9) authenticate with
`DefaultAzureCredential` and never touch a storage account key.

## 3. SQL Server / database

```bash
# Let Azure services (this App Service) reach the SQL server.
az sql server firewall-rule create -g "$RESOURCE_GROUP" -s "$SQL_SERVER_NAME" \
  --name AllowAzureServices --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0
```

The SQL admin login/password you set when creating `inox-45x-sql` are what go into
`X45_DB_USERNAME` / `X45_DB_PASSWORD` above. If you'd rather avoid a SQL-auth password
entirely, Azure SQL also supports Azure AD-only auth using the same Managed Identity from
step 2a - that's a reasonable hardening step later, but needs its own Spring datasource
config change (not wired up yet).

**First deploy will fail here** - Flyway's migrations (`V1__init_schema.sql`,
`V2__seed_master_data.sql`, `V3__seed_demo_customer_supplier.sql`) run automatically on
app startup against whatever `X45_DB_URL` points at, creating the schema for you. No
manual `CREATE TABLE` needed.

## 4. GitHub Actions secrets

Add these in the GitHub repo (`Inox-ws/45X`) -> Settings -> Secrets and variables ->
Actions, so [deploy-backend.yml](../.github/workflows/deploy-backend.yml) and
[deploy-frontend.yml](../.github/workflows/deploy-frontend.yml) can run:

| Secret | Where to get it |
|---|---|
| `AZURE_WEBAPP_PUBLISH_PROFILE` | Portal -> `inox-45x-api` -> Overview -> "Get publish profile" (downloads an XML file - paste its full contents) |
| `AZURE_STATIC_WEB_APPS_API_TOKEN` | Portal -> `inox-45x-web` -> Overview -> "Manage deployment token" |

Then:

```bash
git add -A && git commit -m "Initial commit"
git push -u origin main
```

## 5. Verify

```bash
curl https://${APP_SERVICE_NAME}.azurewebsites.net/actuator/health
```

Then open `$STATIC_WEB_APP_URL`, sign in via the Entra ID button, and confirm the
dashboard loads. If login redirects but then fails, re-check Section 1c (role
assignment) - a user with zero assigned roles can authenticate but every page will look
empty/forbidden.
