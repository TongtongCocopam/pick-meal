import http from 'k6/http';
import { check } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { login } from './common/auth.js';

const BASE_URL = 'http://localhost:8080';
const START_ID = 2000;

const refreshDuration = new Trend('refresh_duration');
const refreshFailed = new Rate('refresh_failed');

export const options = {
    scenarios: {
        refresh: {
            executor: 'per-vu-iterations',
            vus: 1000,
            iterations: 1,
            maxDuration: '1m',
        },
    },
};

export default function () {
    const userNumber = START_ID + __VU;

    // 각 VU가 자기 계정으로 로그인
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

    if (loginResponse.status !== 200) {
        refreshFailed.add(true);
        console.log(
            `VU ${__VU} 로그인 실패: ${loginResponse.status}`
        );
        return;
    }

    // 같은 VU이므로 로그인 때 받은 Refresh Token 쿠키가 자동 전송됨
    const response = http.post(
        `${BASE_URL}/api/v1/auth/refresh`
    );

    refreshDuration.add(response.timings.duration);
    refreshFailed.add(response.status !== 200);

    check(response, {
        'refresh status is 200': (res) =>
            res.status === 200,

        'new access token exists': (res) =>
            res.json('data.accessToken') != null,
    });
}