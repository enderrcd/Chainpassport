import request from "@/utils/request";

import type { LoginFrom, LoginResponse } from "./type";
import type { giteeResponse } from "./type";
import type { IdLoginResponse, IdentificationResponse } from "./type";
import type { LogoutResponse } from "./type";

enum API {
    LOGIN_URL = '/login',
    GITEE_URL = '/public/login/gitee/config',
    ID_URL = '/public/register',  
    IDENTITY_URL = '/public/login/button',
    LOGOUT_URL = '/logout/user'
}

export const reqGitee = () => request.post<giteeResponse>(API.GITEE_URL);
export const reqLogin = (data: LoginFrom) => request.post<LoginResponse>(API.LOGIN_URL, data);

export const reqIdentity = () => request.post<IdentificationResponse>(API.IDENTITY_URL);
export const reqIdLogin = () => request.post<IdLoginResponse>(API.ID_URL);
export const reqLogout = (data: string) => request.post<LogoutResponse>(API.LOGOUT_URL, data);