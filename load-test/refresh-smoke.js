import http from 'k6/http';
import { check, fail } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    vus: 1,
    iterations: 1
};

export default function () {

    // 로그인
    const loginResponse = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            loginId: 'k6test3000',
            password: 'Test1234!',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    const loginSuccess = check(loginResponse, {
        'login status is 200': (res) => res.status === 200,
    });

    if (!loginSuccess) {
        fail(`로그인 실패: ${loginResponse.body}`);
    }


    // 토큰 재발급
    // 로그인에서 받은 Refresh Token 쿠키가 자동으로 함께 전송됨
    const response = http.post(
        `${BASE_URL}/api/v1/auth/refresh`
    );

    check(response, {
        'refresh status is 200': (res) =>
            res.status === 200,

        'refresh success': (res) =>
            res.json('success') === true,

        'new access token exists': (res) =>
            res.json('data.accessToken') != null,
    });

    console.log(
        `토큰 재발급 응답시간: ${response.timings.duration} ms`
    );

    console.log(response.body);
}