import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

const START_ID = 2000;

export const options = {
    scenarios: {
        login: {
            executor: 'per-vu-iterations',
            vus: 1000,
            iterations: 1,
        },
    },
};

export default function () {
    const userNumber = START_ID + __VU;

    const response = http.post(
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

    check(response, {
        'status is 200': (res) => res.status === 200,

        'access token exists': (res) =>
            res.json('data.accessToken') !== null,
    });
}