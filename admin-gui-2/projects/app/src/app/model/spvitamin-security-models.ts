/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-03-31 08:34:07.

export namespace SpvitaminSecurity {

    export interface AuthorizationToken extends AbstractAuthorizationToken {
        sub: string;
        iat: Date;
        exp: Date;
        uid: string;
        rls: string[];
        source: string;
        additionalClaims: { [index: string]: any };
        preferred_username: string;
    }

    export interface AuthenticationRepository {
        authenticationTypes: AuthenticationType[];
    }

    export interface AbstractAuthorizationToken {
        jwt: string;
    }

    export interface AuthenticationType {
        type: string;
        label: string;
        provider: string;
    }

}
