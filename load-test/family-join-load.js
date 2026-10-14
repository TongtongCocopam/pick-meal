import { Counter } from 'k6/metrics';

import http from 'k6/http';
import { fail } from 'k6';
import { login } from './common/auth.js';


const BASE_URL = 'http://localhost:8080';

const successCount = new Counter('join_success');
const rejectedCount = new Counter('join_rejected');
const unexpectedCount = new Counter('join_unexpected');

export const options = {
    scenarios: {
        familyJoin: {
            executor: 'per-vu-iterations',
            vus: 101,
            iterations: 1
        },
    },
    thresholds: {
        join_success: ['count==1'],
        join_rejected: ['count==100'],
        join_unexpected: ['count==0'],
    },
};

export function setup() {
    const leaderToken = login('k6test2001');
    const applicantToken = login('k6test3301');

    const response = http.get(
        `${BASE_URL}/api/v1/families/me/invitation-code`,
        {
            headers: {
                Authorization: `Bearer ${leaderToken}`,
            },
        }
    );

    if (response.status !== 200) {
        fail(`초대코드 조회 실패: ${response.body}`);
    }

    return {
        invitationCode: response.json('data.invitationCode'),
        applicantToken,
    };
}

export default function (data) {
    const response = http.post(
        `${BASE_URL}/api/v1/families/applications`,
        JSON.stringify({
            invitationCode: data.invitationCode,
        }),
        {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${data.applicantToken}`,
            },
            tags: { api: 'familyJoin' },
        }
    );

    if (response.status === 200) {
        successCount.add(1);
    } else if (response.status === 409) {
        rejectedCount.add(1);
    } else {
        unexpectedCount.add(1);
        console.error(
            `예상 외 응답: ${response.status}, ${response.body}`
        );
    }
}