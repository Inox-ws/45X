export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api/v1',
  entra: {
    // Local dev fallback auth is used by default (see Section 3, Milestone 3).
    // Fill these in only when testing against a real Entra ID app registration.
    clientId: '',
    authority: '',
    redirectUri: 'http://localhost:4200',
    apiScope: ''
  }
};
