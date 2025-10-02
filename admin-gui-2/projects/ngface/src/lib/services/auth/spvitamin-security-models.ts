/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-10-01 06:37:42.

export namespace SpvitaminSecurity {

    export interface AuthorizationToken extends AbstractAuthorizationToken {
        type: Type;
        sub: string;
        iat: Date;
        exp: Date;
        uid: string;
        clientId: string;
        rls: string[];
        scope: string[];
        source: string;
        sid: string;
        additionalClaims: { [index: string]: any };
        preferred_username: string;
    }

    export interface AuthenticationRepository {
        authenticationTypes: AuthenticationType[];
    }

    export interface AbstractAuthorizationToken extends Serializable {
        jwt: string;
    }

    export interface AuthenticationType {
        type: string;
        label: string;
        provider: string;
        grantTypes: string[];
    }

    export interface Serializable {
    }

    export type Type = "JWT" | "ACCESS" | "REFRESH";

}
