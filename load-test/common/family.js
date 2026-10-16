import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = 'http://localhost:8080';

export function createFamily(accessToken, familyName) {
    http.post(
        `${BASE_URL}/api/v1/families`,

        JSON.stringify({
            familyName: familyName
        }),

        {
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Bearer ${accessToken}`,
            },
        }
    );

    const invitationResponse = http.get(
        `${BASE_URL}/api/v1/families/me/invitation-code`,
        {
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${accessToken}`,
            },
        }
    );

    const invitationSuccess = check(invitationResponse, {
        'invitation code status is 200': (res) => res.status === 200,
        'invitation code exists': (res) =>
            res.json('data.invitationCode') !== null,
    });

    if (!invitationSuccess) {
        fail(`초대코드 조회 실패: ${invitationResponse.body}`);
    }

    return invitationResponse.json('data.invitationCode');
}