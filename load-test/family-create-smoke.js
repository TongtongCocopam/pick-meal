import http from 'k6/http';
import { check, fail } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    vus: 1,
    iterations: 1,
};

/**
 * 로그인
 * @returns {{accessToken: *}}
 */
export function setup() {
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
        'access token exists': (res) =>
            res.json('data.accessToken') !== null,
    });

    if (!loginSuccess) {
        fail(`로그인 실패: ${loginResponse.body}`);
    }

    return {
        accessToken: loginResponse.json('data.accessToken'),
    };
}

/**
 * 가족 생성
 * @param data
 */
export default function (data) {
    const response = http.post(
        `${BASE_URL}/api/v1/families`,

        JSON.stringify({
            familyName: 'testFamily'
        }),

        {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${data.accessToken}`,
            },
        }
    );

    // 3. 성공 여부 확인
    check(response, {
        'family create status is 201':
            (res) => res.status === 201,

        'family create success':
            (res) => res.json('success') === true,
    });

    // 4. 응답시간 확인
    console.log(
        `가족 생성 응답시간: ${response.timings.duration} ms`
    );

    console.log(response.body);
}