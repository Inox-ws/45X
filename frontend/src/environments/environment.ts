export const environment = {
  production: true,
  // Separate origin from the Static Web App (inox-45x-web) - the backend is a
  // standalone App Service, not a SWA-linked backend, so this is a normal
  // cross-origin call. The backend's CORS allow-list (X45_FRONTEND_ORIGIN) must
  // match the SWA's origin for this to work - see infra/azure-deployment.md.
  apiBaseUrl: 'https://inox-45x-api.azurewebsites.net/api/v1',
  entra: {
    // Frontend SPA app registration's Application (client) ID (infra/azure-deployment.md Section 1b).
    clientId: 'eaff8b76-85a5-422c-9587-9fadb00f9f60',
    authority: 'https://login.microsoftonline.com/cc53b754-b4f2-454d-8e85-644309d1354c',
    redirectUri: window.location.origin,
    // The backend API app registration's exposed scope (Section 1a) - this is what
    // must appear in the token's `aud` claim for x45.entra.api-audience to match.
    apiScope: 'api://8c4dc697-bf4f-451e-a055-a6fddafe9541/access_as_user'
  }
};
