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

package hu.perit.spvitamin.spring.security.auth;

import hu.perit.spvitamin.spring.exception.InvalidInputException;

import java.util.Base64;

public class BasicAuthHeader {

    private static final String AUTH_HEADER_PREFIX = "Basic ";

    public final String userName;
    public final String password;

    public static BasicAuthHeader of(String authHeader) {
        return new BasicAuthHeader(authHeader);
    }

    private BasicAuthHeader(String authHeader) {
        if (!authHeader.startsWith(AUTH_HEADER_PREFIX)) {
            throw new InvalidInputException("Authorization header does not start with '" + AUTH_HEADER_PREFIX + "'.");
        }
        String decodedString;
        try {
            decodedString = new String(Base64.getDecoder().decode(authHeader.substring(AUTH_HEADER_PREFIX.length())));
        }
        catch (Exception e) {
            throw new InvalidInputException("Error while base64 decoding auth header: " + e.getMessage());
        }
        String[] splittedString = decodedString.split(":");
        if (splittedString.length != 2) {
            throw new InvalidInputException("Base64 decoding auth header contains " + (splittedString.length - 1) + "colons instead of one.");
        }
        userName = splittedString[0].strip();
        password = splittedString[1].strip();
    }


    public static String fromUsernamePassword(String userName, String password) {
        String authStr = String.format("%s:%s", userName, password);
        String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());
        return "Basic " + base64Creds;
    }
}
