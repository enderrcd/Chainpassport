export interface LoginFrom {
    userName: string;
    password: string;
}
export interface LoginResponse {
    code: number;
    msg: string;
    data: string;
}

export interface giteeResponse {
    code: number;
    msg: string;
    data: {
        client_id: string;
        redirect_uri: string;
        response_type: string;
        scope: string;
    }
}

export interface IdLoginResponse {
    code: number;
    msg: string;
    data: object;
}

export interface IdentificationResponse {
    code: number;
    msg: string;
    data: string;
}