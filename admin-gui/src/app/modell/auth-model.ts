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

/* tslint:disable */
/* eslint-disable */

export interface AuthToken
{
  sub: string;
  jwt: string;
  iat: Date;
  exp: Date;
}


export interface ServerExceptionProperties
{
  message: string;
  exceptionClass: string;
  superClasses: string[];
  stackTrace: StackTraceElement[];
  cause: ServerExceptionProperties;
}

export interface JsonSerializable
{
}

export interface StackTraceElement extends Serializable
{
  classLoaderName: string;
  moduleName: string;
  moduleVersion: string;
  methodName: string;
  fileName: string;
  lineNumber: number;
  className: string;
  nativeMethod: boolean;
}

export interface Serializable
{
}
