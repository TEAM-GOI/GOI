import React, { useState } from 'react';
// import { useDispatch } from 'react-redux';
// import { setUserProfileImage } from '../../features/login/authSlice';
import blue from '../../images/signUp/blue.gif';
import brown from '../../images/signUp/brown.gif';
import green from '../../images/signUp/green.gif';
import yellow from '../../images/signUp/yellow.gif';
import pink from '../../images/signUp/pink.gif';
import orange from '../../images/signUp/orange.gif';
import styles from './changeModal.module.css';
import axios from 'axios';

export default function ChangeModal({ onClose, onSelectImage }) {
  // const dispatch = useDispatch();
  const [selectedImage, setSelectedImage] = useState('');

  // 이미지들에게 번호 부여
  const images = [
    { id: 1, src: blue, alt: "파랑도깨비" },
    { id: 2, src: brown, alt: "밤색도깨비" },
    { id: 3, src: yellow, alt: "노랑도깨비" },
    { id: 4, src: pink, alt: "핑크도깨비" },
    { id: 5, src: orange, alt: "오렌지도깨비" },
    { id: 6, src: green, alt: "초록도깨비" },
  ];

  // 이미지 선택 핸들러
  const handleImageSelect = async (image) => {
    const userId = sessionStorage.getItem("userId"); // 로컬 스토리지에서 userId 가져오기
    const accessToken = sessionStorage.getItem("accessToken"); // 로컬 스토리지에서 accessToken 가져오기
    const selectedImageObj = images.find(img => img.src === image);
    setSelectedImage(image);

    if (selectedImageObj) {
      try {
        const response = await axios.put(`https://toogui.site/api/users/${userId}/image-id`, {
          imageId: selectedImageObj.id
        }, {
          headers: {
            'Authorization': `Bearer ${accessToken}`
          }
        });
        // console.log('프로필 이미지 변경 완료', response.data);
        onSelectImage(selectedImageObj);
      } catch (error) {
        console.error('이미지 변경 실패:', error);
        alert("이미지 변경에 실패했어요. 다시 시도해주세요 !");
      }
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-20 flex justify-center items-center">
      <div className="bg-gray-800/95 p-5 rounded-lg w-1/2">
        <h1 className="text-3xl text-center text-white mb-10 font-bold">프로필 이미지 변경</h1>        
        <div className="flex justify-between items-center mb-5">
          <div className="flex-1 flex flex-col items-center justify-center">
            <p className="text-white mb-4 font-bold">미리보기</p>
            <div className={`${styles.previewBoxModal} bg-white flex justify-center items-center`}>
              {selectedImage && <img src={selectedImage} alt="미리보기" className="max-w-full max-h-full"/>}
            </div>
          </div>
          
          <div className="flex-1 grid grid-cols-3 gap-0 mr-10">
            {[blue, brown, yellow, pink, orange, green].map((image, index) => (
              <img
                key={index}
                src={image}
                alt={`프로필 이미지-${index}`}
                className={`w-full h-32 cursor-pointer border-2 ${selectedImage === image ? 'border-4 border-green-700' : 'border-white'}`}
                onClick={() => handleImageSelect(image)}/>
            ))}
          </div>
        </div>

        <div className="flex justify-center mt-10">
          <button onClick={onClose} className="bg-white w-24 h-10 font-bold text-black rounded">
            선택
          </button>
        </div>
      </div>
    </div>
  );
}