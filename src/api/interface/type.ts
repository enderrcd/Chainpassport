export interface LoginFrom {
    username: string;
    password: string;
}
export interface LoginResponse {
    code: number;
    msg: string;
    data: string;
}