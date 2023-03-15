export interface AuthToken {
    sub: string;
    jwt: string;
    iat: Date;
    exp: Date;
}
