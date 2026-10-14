import http from 'k6/http';
import { check, fail } from 'k6';
import { login } from './common/auth.js';
import { Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const START_ID = 2001;

const successCount = new Counter('generation_success');
const rejectedCount = new Counter('generation_rejected');
const unexpectedCount = new Counter('generation_unexpected');

export const options = {
    scenarios: {
        dietGenerationLoad: {
            executor: 'per-vu-iterations',
            vus: 50,
            iterations: 1
        },
    },
    thresholds: {
        generation_success: ['count==1'],
        generation_rejected: ['count==49'],
        generation_unexpected: ['count==0']
    }
}

export function setup() {
    // 로그인
    const accessToken = login(`k6test${START_ID}`);

    return {
        accessToken : accessToken
    };
}

export default function (data){
    const response = http.post(
        `${BASE_URL}/api/v1/diets/ai-generate`,
        JSON.stringify({
            targetMonth: "2026-10",
            dailyMealCount: 3
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${data.accessToken}`
            }
        }
    );

    if(response.status === 200){
        successCount.add(1);
    } else if (response.status === 409){
        rejectedCount.add(1);
    }else{
        unexpectedCount.add(1);
        console.error(
            `예상 외 응답: ${response.status}, ${response.body}`
        );
    }

}