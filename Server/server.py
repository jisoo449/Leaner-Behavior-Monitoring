import socket
import cv2
import numpy as np
import time
import threading
from analysis_video import AnalyzeVideo
import datetime

#socket에서 수신한 버퍼를 반환하는 함수
def recvall(sock, count):
    # 바이트 문자열
    buf = b''
    while count:
        newbuf = sock.recv(count)
        if not newbuf: return None
        buf += newbuf
        count -= len(newbuf)
    return buf

def handle_client(client, addr, index, analyze) :
    print("Connect with ", addr)
    
    data = client.recv(12)
    client_id = data.decode()
    client_id = client_id.split("0")[-1]
    analyze.userID = client_id      #userID 저장
    print("Id : ", client_id)

    analyze._createOneVideo()
    analyze._createStuTab()
    # 디비 테이블 생성

    q = 0

    start_time = datetime.datetime.now()
    total_time = 0
    total_emotion = ""
    total_score = 0

    while True :
        length = recvall(client, 12)
        print("Receive :", length)

        if length == b'000000000009' :
            total_time, total_emotion, total_score = analyze._break(start_time)
            break

        stringData = recvall(client, int(length))
        data = np.fromstring(stringData, dtype = 'uint8')

        width = 640
        height = 360

        b = np.zeros([height,width,1])

        t = 0
        for i in range(height) :
            for k in range(width) :
                # for d in range(3) :
                b[i, k, 0] = data[t]
                t += 1

        print("Done", q)
        name = str(index)+'photo'+str(q)+'.jpg'

        cv2.imwrite(name, b)
        t = threading.Thread(target = analyze._analyzeFace, args = (name,q, ))
        t.daemon = True
        t.start()
        t.join()

        q += 1
    
    print('Sending...')
    msg = str(total_time)+ '/' + str(total_emotion) +  '/' + str(total_score)
    print(msg)
    send_data = msg.encode()
    length = len(send_data)
    client.sendall(send_data)

    client.close()

HOST='18.191.113.34'
PORT=8080

#TCP 사용
s = socket.socket(socket.AF_INET)
print('Socket created')
 
#서버의 아이피와 포트번호 지정
s.bind((HOST,PORT))
print('Socket bind complete')
# 클라이언트의 접속을 기다린다. (클라이언트 연결을 10개까지 받는다)
s.listen(10)
print('Socket now listening')

index = 0

while True :
    try :
        client, addr = s.accept()
    except KeyboardInterrupt :
        s.close()
    analyze = AnalyzeVideo()
    t = threading.Thread(target = handle_client, args = (client, addr, index, analyze))
    index += 1
    t.daemon = True
    t.start()