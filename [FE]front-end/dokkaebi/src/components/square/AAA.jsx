import React from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function AAA() {
  let retryCount = 0;
  const MAX_RETRY_COUNT = 5;

  const navigate = useNavigate();
  const accessToken = sessionStorage.getItem("accessToken");
  const handleEnterClick = () => {
    if (retryCount >= MAX_RETRY_COUNT) {
      alert("비밀번호 입력 횟수를 초과했습니다.");
      onClose();
      return;
    }

    axios
      .post(
        "https://toogui.site/api/square/fast",
        {},
        {
          headers: { Authorization: `Bearer ${accessToken}` },
        }
      )
      .then((response) => {
        // console.log("빠른 입장 응답:", response.data.roomId);
        navigate(`/room/${response.data.roomId}`, {
          state: JSON.parse(JSON.stringify({ response })),
        });
      })
      .catch((error) => {
        alert("빠른 입장 가능한 방을 찾을 수 없습니다. 다시 시도해주세요.");
      });

    axios
      .post(
        "https://toogui.site/api/room/enter",
        {
          roomId: roomId,
        },
        {
          headers: { Authorization: `Bearer ${accessToken}` },
        }
      )
      .then((response) => {
        // console.log("입장 성공:", response);
        if (response.status === 200) {
          // console.log("입장 성공:", response);
          navigate(`/room/${roomId}`);
        }
      })
      .catch((error) => {
        // console.log("입장 실패:", error);
        if (!error.response) {
          alert("알 수 없는 오류가 발생했습니다.");
          onClose();
          return;
        }
        switch (error.response.data.statusCode) {
          case 423: // 방 비밀번호 틀렸을 때
            setShowRoomEnterModal(true);
            retryCount++;
            handleEnterClick();
            break; // 이 break를 추가했습니다.

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

  return <div></div>;
}
