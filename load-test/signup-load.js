import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

const START_ID = 1000;

export const options = {
    scenarios: {
        signup: {
            executor: 'per-vu-iterations',
            vus: 1000,
            iterations: 1,
        },
    },
};

export default function () {
    const userNumber = START_ID + __VU;

    const response = http.post(
        `${BASE_URL}/api/v1/users/signup`,
        JSON.stringify({
            loginId: `k6test${userNumber}`,
            password: 'Test1234!',
            passwordCheck: 'Test1234!',
            email: `k6test${userNumber}@test.com`,
            nickname: `k6test${userNumber}`,
            birthDate: '2000-01-01',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
        }
    );

    check(response, {
        'status is 201': (res) => res.status === 201,
        'signup success': (res) => res.json('success') === true,
    });
}