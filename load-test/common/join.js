import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

export function join(accessToken, invitationCode) {
    const response = http.post(
        `${BASE_URL}/api/v1/families/applications`,
        JSON.stringify({
            invitationCode: invitationCode,
        }),
        {
            headers: {
                'content-type': 'application/json',
                'Authorization': `Bearer ${accessToken}`,
            },
        }
    );

    const joinSuccess = check(response, {
        'family join status is 200': (res) => res.status === 200,
    });

    if (!joinSuccess) {
        fail(`가족 가입 신청 실패: status=${response.status}, body=${response.body}`);
    }
}