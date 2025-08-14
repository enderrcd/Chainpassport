import request from "@/utils/request";

import type { LoginFrom, LoginResponse } from "./type";
import type { giteeResponse } from "./type";

enum API {
    LOGIN_URL = '/login',
    GITEE_URL = '/public/login/gitee/config',
}

export const reqGitee = () => request.post<giteeResponse>(API.GITEE_URL);
export const reqLogin = (data: LoginFrom) => request.post<LoginResponse>(API.LOGIN_URL, data);