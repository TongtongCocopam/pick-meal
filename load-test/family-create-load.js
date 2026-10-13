import http from 'k6/http';
import { check, fail } from 'k6';

const BASE_URL = 'http://localhost:8080';
const FIRST_USER_ID = 4000;
const USER_COUNT = 101;

export const options = {
    scenarios: {
        familyCreate: {
            executor: 'per-vu-iterations',
            vus: USER_COUNT,
            iterations: 1,
        },
    },
};

export default function () {
    const userIndex = __VU;
    const userNumber = FIRST_USER_ID + userIndex;

    const firstLetter = String.fromCharCode(97 + Math.floor(userIndex / 26));
    const secondLetter = String.fromCharCode(97 + (userIndex % 26));
    const familyName = `ff${firstLetter}${secondLetter}`;

    const loginResponse = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            loginId: `k6test${userNumber}`,
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
        fail(`k6test${userNumber} 로그인 실패: ${loginResponse.body}`);
    }

    const accessToken = loginResponse.json('data.accessToken');
    const createResponse = http.post(
        `${BASE_URL}/api/v1/families`,
        JSON.stringify({ familyName }),
        {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${accessToken}`,
            },
        }
    );

    check(createResponse, {
        'family create status is 201': (res) => res.status === 201,
        'family create success': (res) => res.json('success') === true,
    });
}
