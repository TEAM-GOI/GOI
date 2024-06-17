import React, { useCallback, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { setRoomNum } from '../../features/square/roomSlice.js';
import axios from "axios";
import styles from "./RoomEnterModal.module.css";
import { useDispatch, useSelector } from "react-redux";

export default function RoomEnterModal({ onClose, roomId }) {
  const dispatch = useDispatch();
  const roomNums = useSelector((state) => state.room.roomNum);
  const [isRoomNum, setIsRoomNum] = useState(roomNums);
  const [isPassword, setIsPassword] = useState(""); // 비밀번호 상태 관리
  const accessToken = sessionStorage.getItem("accessToken");
  const navigate = useNavigate();
  const [showRoomEnterModal, setShowRoomEnterModal] = useState(false);
  const propsRoomId = roomId;
  useEffect(() => {
    // console.log("비밀 번호:", isPassword);
  }, [isPassword]);

  // 비밀번호 숫자 4자리로 제한
  const handlePasswordChange = (e) => {
    // 입력값이 숫자이고 4자리 이하인지 확인
    const value = e.target.value;
    // console.log("입력 값:", value);
    if (/^\d{0,4}$/.test(value)) {
      // 정규표현식을 사용하여 검증
      // 상태 업데이트 로직
      setIsPassword(value); // 상태 업데이트
      // console.log("비밀 번호:", isPassword);
    }
  };

  // 입장
  const handleEnterClick = () => {
    // console.log("props 받은 룸ID :", propsRoomId);
    // console.log("입력한 비밀번호 :", isPassword);
    const realRoomId = sessionStorage.getItem("roomId");
    axios
      .post(
        "https://toogui.site/api/room/enter",
        {
          roomId: propsRoomId ? propsRoomId : realRoomId,
          password: isPassword,
        },
        {
          headers: { Authorization: `Bearer ${accessToken}` },
        }
      )
      .then((response) => {
        // console.log("입장 성공:", response);
        if (response.status === 200) {
          // console.log("입장 성공:", response);
          dispatch(setRoomNum(isRoomNum))
          sessionStorage.setItem("roomId", propsRoomId)
          navigate(`/room/${propsRoomId}`, {
            state: JSON.parse(JSON.stringify({ response })),
          });
        }
      })
      .catch((error) => {
        // console.log("비번:", isPassword);
        // console.log("입장 실패:", error);
        if (!error.response) {
          alert("알 수 없는 오류가 발생했습니다.");
          onClose();
          return;
        }
        switch (error.response.data.statusCode) {
          case 423: // 방 비밀번호 틀렸을 때
            setShowRoomEnterModal(true);
            break;

          case 426: // 방이 가득 찼을 때
            alert("방이 가득 차서 입장할 수 없어요!");
            onClose(); // 모달 닫기
            break;

          case 404: // 방이 존재하지 않을 때
            alert("존재하지 않는 방번호입니다!");
            onClose(); // 모달 닫기
            break;

          default:
            // 예외 처리
            alert("알 수 없는 오류가 발생!");
            onClose(); // 모달 닫기
            break;
        }
      });
  };

  return (
    <div className={styles.background}>
      {/* 모달 컨테이너 */}
      <div
        className={`${styles.container} flex flex-col items-center justify-center`}
      >
        {/* 모달 타이틀 */}
        <h1 className={`font-Bit text-5xl mb-10 ${styles.passwordHeader}`}>비밀방 입장</h1>
        <input
          type="text"
          placeholder="비공개 방이에요 ! 비밀번호를 입력하세요"
          maxLength={4}
          value={isPassword || ""} // 입력 상태를 value와 바인딩
          onChange={handlePasswordChange} // 입력 처리 함수를 이벤트 핸들러로 지정
          className={`text-center font-bold text-lg ${styles.passwordInput}`}
        />

        {/* 버튼 그룹 */}
        <div className="flex justify-center w-full mt-5">
          {/* 입장 버튼 */}
          <div className={`rounded-xl mr-5 ${styles.roomBtn}`}>
            <button
              onClick={handleEnterClick} // 입장 버튼 클릭 시 handleEnterClick 함수 호출
              className="w-28 h-12 text-white text-2xl px-4 rounded-xl focus:outline-none focus:shadow-outline"
              type="button"
            >
              입 장
            </button>
          </div>

          {/* 취소 버튼 */}
          <div className={`rounded-xl ${styles.roomBtn}`}>
            <button
              onClick={onClose}
              className="w-28 h-12 text-white text-2xl px-4 rounded-xl focus:outline-none focus:shadow-outline"
              type="button"
            >
              취 소
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
