import http from 'k6/http';
import { check, fail } from 'k6';
import { login } from './common/auth.js';

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
    const accessToken = login('k6test3000');
    return {
        accessToken: accessToken
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