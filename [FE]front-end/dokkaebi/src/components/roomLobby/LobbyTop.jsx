import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useBGM } from "../bgm/bgmContext.jsx";
import Sidebar from "../square/sidebar/sidebar.jsx";
import styles from "./LobbyTop.module.css";
import GoSqaure from "../../components/back/goSquare.jsx";
import messenger from "../../images/square/icon_messenger.png";
import axios from "axios";

export default function LobbyTop({ userList, isStart }) {
  const { toggleBGMVisibility } = useBGM();
  // const userNickname = useSelector((state) => state.auth.userNickname);
  const accessToken = sessionStorage.getItem("accessToken");
  const roomId = sessionStorage.getItem("roomId");
  const userId = sessionStorage.getItem("userId");
  const [isSidebarOpen, setIsSidebarOpen] = useState(false); // 사이드바 상태 관리
  const [start, setStart] = useState(isStart);
  const [isReady, setIsReady] = useState(false);
  const [amIManager, setAmIManager] = useState(false);

  // 효과음 재생 함수
  const playGameStartSound = () => {
    const sound = new Audio("/bgm/gameStart.mp3");
    sound.play();
  };

  useEffect(() => {
    userList.forEach((user) => {
      if (user.userId === Number(userId)) {
        setAmIManager(user.isManager);
      }
    });
  }, [userList, amIManager]);

  useEffect(() => {
    // console.log("나는 방장 : ", amIManager);
  }, [amIManager]);

  useEffect(() => {
    if (isStart === true) {
      setStart(true);
    } else if (isStart === false) {
      setStart(false);
    }
  }, [isStart]);

  const handleReadyButtonClick = () => {
    playGameStartSound();
    axios
      .post(
        `https://toogui.site/api/room/ready/${roomId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        }
      )
      .then((res) => {
        console.log(res);
        // console.log("레디 바뀜 확인");
        // console.log("레디요청 엑세스 토큰", accessToken);
        setIsReady(!isReady);
      })
      .catch((err) => {
        console.log(err);
        // console.log("레디 바뀜 실패");
        // console.log("레디요청 엑세스 토큰", accessToken);
      });
  };

  const handleStartButtonClick = () => {
    playGameStartSound();
    if (userList.length < 2) {
      alert("2명 이상의 유저가 필요합니다.");
      return;
    } else {
      axios
        .get(`https://toogui.site/api/game?id=${roomId}`, {
          params: { id: roomId },
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        })
        .then((res) => {
          console.log(res);
          console.log("게임 시작");
        })
        .catch((err) => {
          console.log(err);
          console.log("게임 시작 실패");
          alert("아직 준비가 덜 된거 같아요 !");
        });
    }
  };

  // 사이드바 토글 함수
  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
    // 사이드바 상태에 따라 BGMPlayer의 가시성을 토글합니다.
    toggleBGMVisibility(!isSidebarOpen);
  };

  return (
    <>
      <div className="flex items-center justify-between p-5">
        {/* 뒤로가기 버튼 */}
        <GoSqaure roomId={roomId} />
        <div className="w-48 mr-36">
          <div className="flex justify-center">
            {/* 레디 버튼 */}
            {amIManager ? (
              <button
                onClick={handleStartButtonClick}
                // disabled={!isStart}
                className={`flex items-center justify-center font-Bit text-3xl ${styles.textButton} `}
              >
                START
              </button>
            ) : (
              <button
                onClick={handleReadyButtonClick}
                className={`flex items-center justify-center font-Bit text-3xl ${styles.textButton}`}
              >
                {isReady ? "Cancel" : "Ready"}
              </button>
            )}
          </div>
        </div>

        {/* 메신저 버튼 */}
        <div className="flex">
          <button
            onClick={toggleSidebar}
            className={`${styles.messengerButton}`}
          >
            <img src={messenger} alt="MessengerButton" />
          </button>
        </div>
      </div>
      {isSidebarOpen && <Sidebar toggleSidebar={toggleSidebar} />}
    </>
  );
}
