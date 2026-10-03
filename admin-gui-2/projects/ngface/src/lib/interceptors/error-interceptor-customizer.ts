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

import {HttpErrorResponse, HttpRequest} from '@angular/common/http';


/**
 * Optional customizer for {@link ErrorInterceptor}.
 *
 * Provide a subclass via DI to control whether a given HTTP error
 * should be forwarded to the {@link ErrorService} (and thus shown to the user).
 * Return {@code true} to let the interceptor handle the error normally,
 * {@code false} to suppress the error dialog.
 *
 * Example:
 * ```ts
 * @Injectable()
 * export class MyErrorCustomizer extends ErrorInterceptorCustomizer {
 *   shouldHandleError(request: HttpRequest<unknown>, error: HttpErrorResponse): boolean {
 *     return error.status !== 503;
 *   }
 * }
 *
 * // in providers:
 * { provide: ErrorInterceptorCustomizer, useClass: MyErrorCustomizer }
 * ```
 */
export abstract class ErrorInterceptorCustomizer
{
  abstract shouldHandleError(request: HttpRequest<unknown>, error: HttpErrorResponse): boolean;
}
