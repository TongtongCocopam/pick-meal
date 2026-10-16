import http from 'k6/http';
import { login } from './common/auth.js';
import { Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const START_ID = 1000;
const USER_COUNT = 50;

const successCount = new Counter('generation_success');
const unexpectedCount = new Counter('generation_unexpected');

export const options = {
    scenarios: {
        dietGenerationAsyncLoad: {
            executor: 'per-vu-iterations',
            vus: USER_COUNT,
            iterations: 1,
        },
    },

    thresholds: {
        generation_success: ['count==50'],
        generation_unexpected: ['count==0'],
    },
};

export function setup() {
    const accessTokens = [];

    for (let i = 0; i < USER_COUNT; i++) {
        const loginId = `k6test${START_ID + i}`;

        const accessToken = login(loginId);

        accessTokens.push(accessToken);
    }

    return {
        accessTokens,
    };
}

export default function (data) {

    const accessToken = data.accessTokens[__VU - 1];

    const response = http.post(
        `${BASE_URL}/api/v1/diets/ai-generate`,
        JSON.stringify({
            targetMonth: '2026-10',
            dailyMealCount: 2,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`,
            },
            tags: {
                api: 'diet-generation',
            },
        }
    );

    if (response.status === 200) {
        successCount.add(1);
    } else {
        unexpectedCount.add(1);

        console.error(
            `VU=${__VU}, status=${response.status}, body=${response.body}`
        );
    }
}