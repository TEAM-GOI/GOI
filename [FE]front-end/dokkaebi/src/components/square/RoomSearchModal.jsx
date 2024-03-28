import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import RoomEnterModal from './RoomEnterModal';
import styles from './RoomSearchModal.module.css';


export default function RoomSearchModal({ onClose }) {
  const accessToken = sessionStorage.getItem("accessToken");
  const navigate = useNavigate();

  // 입력창에 입력된 방 번호를 관리하는 상태
  const [roomId, setRoomId] = useState('');

  // 비밀방이라면 -> 입력창에 입력된 비밀번호 관리
  const [password, setPassword] = useState('');

  // 비밀방 입장 모달 표시 상태
  const [showRoomEnterModal, setShowRoomEnterModal] = useState(false);

  // 방 번호 입력 이벤트 핸들러
  const handleRoomIdChange = (e) => {
    setRoomId(e.target.value);
  };

  // 입장 버튼 클릭 핸들러
  const handleEnterClick = async () => {
    try {
      console.log('요청한 데이터 확인:', { roomId: roomId, password: password })
      const response = await axios.post('https://j10d202.p.ssafy.io/api/room/enter', {
        roomId: roomId,
      }, {
        headers: { Authorization: `Bearer ${accessToken}` },
      });
  
      // 성공적인 응답 처리
      if (response.status === 200) {
        console.log('입장 성공:', response);
        // console.log('서버로부터 받은 roomId :', response.data.data.roomId);
        // console.log('서버에서 받은 status 확인 :', response.data.data.status);
  
        navigate(`/room/${roomId}`); // 해당 방으로 이동
        
        // navigate(`/room/${response.data.data.roomId}`); // 해당 방으로 이동
      }
    } catch (error) {
      // 에러코드에 따른 조건을 switch로 나누기
      console.log('에러 종류 확인:', error.response.data.statusCode)
      switch(error.response.data.statusCode) {
        case 423: // 방 비밀번호 틀렸을 때
        setShowRoomEnterModal(true)  
        break; 
  
        case 426: // 방이 가득 찼을 때
          alert('방이 가득 차서 입장할 수 없어요!');
          onClose(); // 모달 닫기
          break;
  
        case 404: // 방이 존재하지 않을 때
          alert('존재하지 않는 방번호입니다!');
          onClose(); // 모달 닫기
          break;
        
        default:
          // 예외 처리
          alert('알 수 없는 오류가 발생!');
          onClose(); // 모달 닫기
          break;
      }
    }
  };
  
  return (
      <div className={styles.background}>
        {/* 모달 컨테이너 */}
        <div className={`${styles.container} flex flex-col items-center justify-center`}>
          {/* 모달 타이틀 */}
          <h1 className="font-Bit text-5xl mb-10">방 찾기</h1>
          <input
            type="text"
            value={roomId}
            onChange={handleRoomIdChange} // 입력 변화를 처리하는 함수 연결
            placeholder="방 번호 입력"
            className="border-2 border-gray-300 p-1 w-48"
          />
          
          {/* 버튼 그룹 */}
          <div className="flex justify-center w-full mt-5">
          {/* 입장 버튼 */}
          <button
            onClick={handleEnterClick} // 클릭 이벤트 핸들러 연결
            className="w-24 h-12 bg-blue-500 hover:bg-blue-600 text-white text-2xl px-4 rounded-xl focus:outline-none focus:shadow-outline"
            type="button"
          >
            입장
          </button>

          {/* 취소 버튼 */}
          <button
            onClick={onClose}
            className="w-24 h-12 bg-red-500 hover:bg-red-600 text-white text-2xl px-4 rounded-xl focus:outline-none focus:shadow-outline"
            type="button"
          >
            취소
          </button>
          </div>
          {/* 조건부 렌더링을 사용하여 RoomEnterModal 표시 */}
          {showRoomEnterModal && <RoomEnterModal roomId={roomId} onClose={() => setShowRoomEnterModal(false)} />}
        </div>
      </div>
    );
  }
