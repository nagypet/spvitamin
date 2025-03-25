/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-03-25 06:56:51.

export namespace SpvitaminSecurity {

    export interface AuthorizationToken extends AbstractAuthorizationToken {
        sub: string;
        iat: Date;
        exp: Date;
        uid: number;
        rls: string[];
        source: string;
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
