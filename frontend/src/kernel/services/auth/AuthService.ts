import type {UserAuthResponseDTO, UserPayload} from './types'
import {AUTH_CLIENT_ID, AUTH_REDIRECT_URI, LETSMEET_API_BASE_URL} from "@/kernel";

export class AuthService {
    private static readonly AUTH_TOKEN_KEY = 'access_token';

    public static getUserContext(): UserPayload | null {
        try {
            const token = AuthService.getAuthToken();
            const payloadBase64 = token?.split(".")?.at(1);

            if (!payloadBase64) return null;

            const payloadJson = atob(payloadBase64);

            return JSON.parse(payloadJson);
        } catch (err) {
            console.error("Invalid token:", err);
            return null;
        }
    }

    public static isUserLoggedIn(): boolean {
        const userContext = AuthService.getUserContext();
        const exp = userContext?.value?.exp;

        const now = Date.now(); // Current time in ms
        const inputTimeMs = exp < 1e12 ? exp * 1000 : exp; // Normalize to ms
        const isExpired = now >= inputTimeMs;

        if (isExpired) {
            AuthService.removeAuthToken();
        }

        return !!(userContext && !isExpired);
    }

    public static removeAuthToken() {
        localStorage.removeItem(AuthService.AUTH_TOKEN_KEY);
    }

    public static getLoginUri() {
        const authScope = 'openid+profile+email';
        const authState = 'abc124';

        return `https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=${AUTH_CLIENT_ID}&scope=${authScope}&redirect_uri=${encodeURIComponent(AUTH_REDIRECT_URI)}&state=${authState}`;
    }

    public static async loginWithCode(code: string): Promise<UserAuthResponseDTO> {
        try {
            const response = await AuthService.submitAuthCode(code);

            localStorage.setItem(AuthService.AUTH_TOKEN_KEY, response?.access_token);
        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    public static async logout() {
        try {
            await AuthService.logoutUser();
            AuthService.removeAuthToken();
        } catch (e) {
            console.error(e);
            throw e;
        }
    }

    private static getAuthToken() {
        return localStorage.getItem(AuthService.AUTH_TOKEN_KEY);
    }

    private static async submitAuthCode(code: string): Promise<UserAuthResponseDTO> {
        const url = `${LETSMEET_API_BASE_URL}/auth/code?code=${encodeURIComponent(code)}`;
        const res = await fetch(url);

        if (!res.ok) {
            throw new Error(`Failed to submit auth code: ${res.status}`);
        }

        return await res.json();
    };

    private static async logoutUser(): Promise<void> {
        const token = localStorage.getItem("access_token");
        const res = await fetch(`${LETSMEET_API_BASE_URL}/auth/logout`, {
            headers: {
                Authorization: token ? `Bearer ${token}` : "",
            },
        });

        if (!res.ok) {
            throw new Error(`Logout failed: ${res.status}`);
        }
    };
}
