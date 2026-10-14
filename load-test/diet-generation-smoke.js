import http from 'k6/http';
import { check, fail } from 'k6';
import { login } from './common/auth.js';

const BASE_URL = 'http://localhost:8080';
const START_ID = 2001;

export const options = {
    vus:1,
    iterations: 1
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

    const success = check(response, {
        '식단 생성 요청 성공': (res) => res.status === 200,
        'status PENDING': (res) => res.json('data.status') === 'PENDING'
    });

    if(!success){
        fail(`식단 생성 요청 실패 : status=${response.status}, body=${response.body}`);
    }

}