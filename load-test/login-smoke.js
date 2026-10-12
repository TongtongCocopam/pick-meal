import http from 'k6/http';
import {check, fail, sleep} from 'k6';

const BASE_URL = 'http://localhost:8080';
const TEST_LOGINID = 'tester1';
const TEST_PASSWORD= 'test1234';

export const options = {
    vus :1,
    iterations: 1,
};

export default function(data) {
    const response  = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            loginId: TEST_LOGINID,
            password: TEST_PASSWORD
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                phase: 'setup',
                api: 'login',
            },
        }
    );

    check(response, {
        'status is 200': (res) => res.status === 200,

        'access token exists': (res) =>
            res.json('data.accessToken') !== null,
    });

}