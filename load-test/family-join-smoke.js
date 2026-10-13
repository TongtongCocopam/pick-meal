import http from 'k6/http';
import { check, fail } from 'k6';

const BASE_URL = 'http://localhost:8080';
const START_ID = 3200;

export const options = {
    scenarios: {
        familyJoin: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1
        },
    },
};

function login(loginId) {
    const loginResponse = http.post(
        `${BASE_URL}/api/v1/auth/login`,
        JSON.stringify({
            loginId,
            password: 'Test1234!',
        }),
        {
            headers: {
                'Content-Type': 'application/json',
            },
            tags: {
                api: 'login',
            },
        }
    );

    const loginSuccess = check(loginResponse, {
        'status is 200': (res) => res.status === 200,
        'access token exists': (res) =>
            res.json('data.accessToken') !== null,
    });

    if (!loginSuccess) {
        fail(`${loginId} 로그인 실패: ${loginResponse.body}`);
    }

    return loginResponse.json('data.accessToken');
}

export function setup() {
    const accessToken = login('k6test2001');

    // 리더 가족의 초대코드 조회
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

    return {
        invitationCode: invitationResponse.json('data.invitationCode'),
    };
}

export default function (data) {
    const userNumber = START_ID + __VU;
    const accessToken = login(`k6test${userNumber}`);

    const response = http.post(
        `${BASE_URL}/api/v1/families/applications`,
        JSON.stringify({
            invitationCode: data.invitationCode,
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
