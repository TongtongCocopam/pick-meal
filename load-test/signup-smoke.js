import http from 'k6/http';
import {check, fail, sleep} from 'k6';

const BASE_URL = 'http://localhost:8080';
const TEST_LOGINID = 'tester1';
const TEST_PASSWORD = 'test1234';

export const options = {
    vus: 1,
    iterations: 1,
};

export default function (data) {
    const response = http.post(
        `${BASE_URL}/api/v1/users/signup`,
        JSON.stringify({
            loginId: 'k6_test_user_1',
            password: 'Test1234!',
            passwordCheck: 'Test1234!',
            email: 'k6test1@test.com',
            nickname: 'k6test1',
            birthDate: '1998-01-01',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                phase: 'setup',
                api: 'signup',
            },
        }
    );

    check(response, {
        'status is 201': (res) => res.status === 201
    });

    console.log(response.body);

}