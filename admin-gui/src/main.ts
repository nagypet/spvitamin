import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';

import {enableProdMode, importProvidersFrom, LOCALE_ID} from '@angular/core';
import {environment} from './environments/environment';
import {registerLocaleData} from '@angular/common';
import {bootstrapApplication, BrowserModule} from '@angular/platform-browser';
import {AppComponent} from './app/app.component';
import {provideRouter, withHashLocation} from '@angular/router';
import {HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {APP_ROUTES} from './app/app-routing.module';
import {TokenInterceptor} from './app/interceptors/token-interceptor';
import {AuthGuard} from './app/services/auth/auth.guard';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideToastr} from 'ngx-toastr';

registerLocaleData(localeDe, 'de-DE', localeDeExtra);

if (environment.production)
{
  enableProdMode();
}

bootstrapApplication(AppComponent, {
  providers: [
    importProvidersFrom(BrowserModule),
    provideRouter(APP_ROUTES, withHashLocation()),
    AuthGuard,
    provideAnimations(),
    provideToastr(),
    {provide: LOCALE_ID, useValue: 'de-DE'},
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },

    provideHttpClient(withInterceptorsFromDi())
  ]
})
  .catch(err => console.error(err));
