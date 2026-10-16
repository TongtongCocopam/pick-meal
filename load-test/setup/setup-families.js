import { login } from '../common/auth.js';
import {createFamily} from "../common/family.js";
import {join} from "../common/join.js";
import {approve} from "../common/approve.js";

const START_ID = 1001;
const MEMBER_START_ID = 1301;

export const options = {
    vus: 1,
    iterations: 1,
};

export default function(){
    let memberId = MEMBER_START_ID;

    for(let i = 0; i < 50; i++){
        // 사용자 계정 준비
        const id = START_ID + i;
        const leaderToken = login(`k6test${id}`);

        // 50명 가족 생성
        const code = createFamily(leaderToken, `test${id}`);

        // 가족에 각 4건 가족 신청
        for(let j = 0; j < 4; j++){
            // 1101 1102 1103 1104
            const memberToken = login(`k6test${memberId + j}`);

            join(memberToken, code);
        }
        // 가족장이 승인
        approve(leaderToken);
        memberId += 4;
    }
}


