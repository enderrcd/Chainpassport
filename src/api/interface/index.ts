import request from "@/utils/request";

import type { LoginFrom, LoginResponse } from "./type";

enum API {
    LOGIN_URL = '/login',
}

export const reqLogin = (data: LoginFrom) => request.post(API.LOGIN_URL, data) as Promise<LoginResponse>;