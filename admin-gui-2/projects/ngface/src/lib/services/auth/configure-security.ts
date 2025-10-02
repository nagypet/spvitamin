import {firstValueFrom, from, of} from 'rxjs';
import {catchError, switchMap, tap} from 'rxjs/operators';
import {configureOAuthService, OAuthService, SimpleOAuthConfig} from '../oauth2/oauth.service';
import {AuthenticationRepositoryService} from './authentication-repository.service';
import {AuthConfig, AuthService, configureAuthService} from './auth.service';
import {SpvitaminSecurity} from './spvitamin-security-models';


export function configureSecurity(
  repositoryService: AuthenticationRepositoryService,
  authService: AuthService, authConfig: AuthConfig,
  oAuthService: OAuthService, oAuthConfig: SimpleOAuthConfig
)
{
  return firstValueFrom(
    repositoryService.getAuthenticationRepository().pipe(
      switchMap(authRepo =>
      {
        if (isSpvitaminOAuthSupported(authRepo))
        {
          repositoryService.authService = oAuthService;
          // Promise -> Observable
          return from(configureOAuthService(oAuthService, oAuthConfig));
        }
        else
        {
          repositoryService.authService = authService;
          // Promise -> Observable
          return from(configureAuthService(authService, authConfig));
        }
      }),
      catchError(err =>
      {
        console.error('Security lib init error', err);
        return of(void 0);
      })
    )
  );
}


function isSpvitaminOAuthSupported(authRepo: SpvitaminSecurity.AuthenticationRepository)
{
  return authRepo.authenticationTypes.filter(authType => authType.type === 'oauth2' && authType.provider === 'spvitamin').length > 0;
}
