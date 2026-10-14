import http from 'k6/http';
import { check, fail } from 'k6';

const BASE_URL = 'http://localhost:8080';

export function login(loginId) {
    const response = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            loginId,
            password: 'Test1234!',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                api: 'login',
            },
        }
    );

    const success = check(response, {
        'login status is 200': (res) => res.status === 200,
        'access token exists': (res) =>
            res.json('data.accessToken') !== null,
    });

    if (!success) {
        fail(`${loginId} 로그인 실패: ${response.body}`);
    }

    return response.json('data.accessToken');
}