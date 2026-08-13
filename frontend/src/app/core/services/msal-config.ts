import { IPublicClientApplication, InteractionType, LogLevel, PublicClientApplication } from '@azure/msal-browser';
import { MsalGuardConfiguration, MsalInterceptorConfiguration } from '@azure/msal-angular';
import { environment } from '../../../environments/environment';

/** True once real Entra ID values are configured (Section 3) - false during local-only dev. */
export function isEntraConfigured(): boolean {
  return !!environment.entra.clientId;
}

export function msalInstanceFactory(): IPublicClientApplication {
  return new PublicClientApplication({
    auth: {
      clientId: environment.entra.clientId,
      authority: environment.entra.authority,
      redirectUri: environment.entra.redirectUri
    },
    cache: {
      cacheLocation: 'sessionStorage',
      storeAuthStateInCookie: false
    },
    system: {
      loggerOptions: {
        loggerCallback: () => {},
        logLevel: LogLevel.Warning,
        piiLoggingEnabled: false
      }
    }
  });
}

export function msalGuardConfigFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    // Must request a token audienced for the BACKEND API (api://<backend-client-id>/access_as_user),
    // not the SPA's own client ID - EntraSecurityConfig on the backend validates the
    // token's `aud` claim against x45.entra.api-audience, which is the backend app.
    authRequest: { scopes: [environment.entra.apiScope] }
  };
}

export function msalInterceptorConfigFactory(): MsalInterceptorConfiguration {
  const protectedResourceMap = new Map<string, Array<string>>();
  protectedResourceMap.set(`${environment.apiBaseUrl}/*`, [environment.entra.apiScope]);
  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}
