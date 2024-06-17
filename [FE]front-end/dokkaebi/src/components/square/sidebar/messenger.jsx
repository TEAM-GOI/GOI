import React, { useEffect, useState, useRef } from 'react';
import { useSelector } from 'react-redux';
import friendClose from '../../../images/backButton/friendBack.gif';
import styles from './messenger.module.css';
import axios from 'axios';

const Messenger = ({ selectedFriend, toggleMessageBar, handleSendMSG, isFriendChat, setIsFriendChat }) => {
  const [inputMessage, setInputMessage] = useState('');
  const recentMsg = useRef(null);
  const accessToken = sessionStorage.getItem("accessToken");
  const userNickname = useSelector((state) => state.auth.userNickname);

  const handleSubmit = (e) => {
    e.preventDefault(); // 메세지 전달하고 페이지 리로드 방지
    handleSendMSG(inputMessage); // 메시지 전송 함수에 사용자가 입력한 inputMessage 담아서 전달
    setInputMessage(""); // 채팅 치고 나면 입력 필드 초기화
  };

  const handleChange = (e) => {
    setInputMessage(e.target.value); // 입력 필드 값 업데이트
  };

  // 채팅창 스크롤
  const scrollToBottom = () => {
    // console.log("최신으로 친구와의 채팅 불러옴");
    recentMsg.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [selectedFriend?.friendListId, isFriendChat[selectedFriend?.friendListId]?.length]);

  useEffect(() => {
    const chatHistory = async () => {
      if (selectedFriend) {
        const friendListId = selectedFriend.friendListId;
        try {
          const response = await axios.get(`https://toogui.site/api/chat/${friendListId}/list`, {
            headers: { Authorization: `Bearer ${accessToken}` },
          });
          if (response.status === 200) {
            console.log("이전 채팅 내역 불러옴!!", response);
            setIsFriendChat(prev => ({
              ...prev,
              [friendListId]: response.data.data // 배열을 직접 할당
            }));
          } else {
            console.error("채팅 기록 불러오기 실패:", response);
          }
        } catch (error) {
          console.error("채팅 기록 불러오기 중 에러 발생:", error);
        }
      }
    };
  
    chatHistory();
  }, [selectedFriend, accessToken, setIsFriendChat]);

  return (
    <aside className={styles.messenger}>
      <nav>
        <div className={`flex justify-between items-center ${styles.closeDiv}`}>
          <div>
            <span className={`text-md font-bold ml-1 ${styles.whoTalk}`}>{selectedFriend.nickName}</span>
            {/* <span className='text-sm'> 님과의 대화</span> */}
          </div>
          <button
            onClick={toggleMessageBar}
            className={`text-md font-bold text-black text-center ${styles.closeButton}`}
          >
            <img src={friendClose} alt="메신저닫기" className='ml-5' />
          </button>
        </div>
      </nav>
      
      {/* 선택된 친구의 이름 또는 정보를 표시 */}
      <nav>
        <div className={`flex flex-col ${styles.chatList}`}> 
          {/* 대화내역 */}
          <div className={styles.chatting}>
            {isFriendChat[selectedFriend.friendListId]?.map((chat, index) => (
              <div
                key={index}
                ref={recentMsg}
                className={
                  chat.sender === userNickname ? styles.chatMine : styles.chatTheirs
                }
              >
                <div className={styles.chatBubble}>
                  <span className='font-bold'>{chat.message}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </nav>

      {/* 채팅 칠 곳 */}
      <nav className={styles.chatcont}>
        <div className={`flex justify-center items-center my-auto ${styles.inputDiv}`}>
          <form onSubmit={handleSubmit}>
            <div className='flex justify-center my-5'>
              <input
                type="text"
                className={`${styles.chatInput}`}
                maxLength={100}
                value={inputMessage}
                onChange={handleChange}
              />
                <button
                  type="submit"
                  className={`${styles.inputButton} bg-red-300 text-sm font-bold`}
                >
                  입력
                </button>
            </div>
          </form>
        </div>
      </nav>
    </aside>
  );
};

export default Messenger;