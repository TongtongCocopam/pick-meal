import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

export function approve(accessToken) {
    const listResponse = http.get(
        `${BASE_URL}/api/v1/families/me/applications`,
        {
            headers: {
                'Authorization': `Bearer ${accessToken}`,
            },
        }
    );

    const applications = listResponse.json('data');

    for (const application of applications) {
        const response = http.post(
            `${BASE_URL}/api/v1/families/me/applications/${application.requestId}`,
            JSON.stringify({
                decision: 'APPROVED',
            }),
            {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`,
                },
            }
        );

        const success = check(response, {
            'family join status is 200': (res) => res.status === 200,
        });

        if (!success) {
            fail(`가족 승인 실패: status=${response.status}, body=${response.body}`);
        }
    }


}