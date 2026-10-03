/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpRequest} from '@angular/common/http';
import {ErrorInterceptorCustomizer} from './error-interceptor-customizer';


/**
 * Default implementation of {@link ErrorInterceptorCustomizer}.
 *
 * Suppresses 401 and 404 errors (returns {@code false}), forwards everything else
 * to the {@link ErrorService}.
 *
 * Application-level customizers can inject this class and delegate to it
 * as a fallback when they have no specific rule for a given request/error.
 */
@Injectable({providedIn: 'root'})
export class DefaultErrorInterceptorCustomizer extends ErrorInterceptorCustomizer
{
  shouldHandleError(request: HttpRequest<unknown>, error: HttpErrorResponse): boolean
  {
    return error.status !== 401 && error.status !== 404;
  }
}
