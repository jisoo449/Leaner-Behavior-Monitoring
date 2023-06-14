import cv2
import dlib
import numpy as np
import os
import datetime
from load_model import LoadModel
from analysis_frame import AnalyzeFrame
from database.connect import Database

class AnalyzeVideo(object):
    def __init__(self):
        #필요한 모델들을 로드
        self.models = LoadModel()
        self.face = AnalyzeFrame(self.models)
        self.db = Database()

        self.total_focus = []   #다섯 프레임 단위로 [시선, 눈깜빡임, 기울기, 손, 다섯 프레임 동안의 대표 감정, score(100점 만점)]
        self.EMOTIONS = ["Angry","Disgusting","Fearful","Happy","Sad","Surprising","Neutral","NoPerson"]

        self.moment_focus = [[] for _ in range(5)]  #각 요소마다 한 프레임씩 상태 저장(5프레임마다 초기화 됨), 0=비집중 / 1=집중, [[시선], [눈깜빡임], [기울기], [손], [감정]]
        self.total_emotions = [0] * len(self.EMOTIONS)  #각 프레임의 대표 감정 확인
        self.count_info = [0] * 4   #각 요소마다 비집중 횟수 카운트 [시선회피횟수, 졸음시간, 자세불량횟수, 산만함횟수]

        self.check_5sec = 0
        self.cnt=0
        self.userID = ''
        self.tableName = ''

    # 학생의 한 회차의 영상 테이블 생성 studentID_today_cnt
    def _createOneVideo(self):
        today = datetime.datetime.today().strftime("%Y%m%d")
        args = (self.userID,today)
        self.cnt = int(self.db.checkCnt(args))+1
        args=(self.userID,today,self.cnt)

        self.tableName = self.db.createOneVideo(args)

    # 한 학생 테이블 생성
    def _createStuTab(self):
        self.db.createStuTab(self.userID)

    def _analyzeFace(self, fname, p):
        frame = cv2.imread(fname, cv2.IMREAD_GRAYSCALE)

        result = self.face._analyze(frame, fname) # result = [gaze, blink, slope, hand, emotion]
        #얼굴이 인식된 경우
        if result :
            for i in range(4) : self.moment_focus[i].append(result[i])
            self.moment_focus[4].append(result[4])
            self.total_emotions[result[4]] += 1
        #얼굴이 인식되지 않은 경우
        else : 
            for t in range(4) : self.moment_focus[t].append(0)
            self.moment_focus[4].append(7) # NoPerson
            self.total_emotions[-1] += 1

        os.remove(fname)

        #5장의 프레임을 모두 인식했을 경우 집중 여부 판단 수행
        self.check_5sec += 1
        if self.check_5sec >= 5 :
            self._score5moment()
            self.check_5sec = 0

    def _score5moment(self) :
        emotion = [0] * len(self.EMOTIONS)
        for i in range(5) :
            # 다섯 프레임에 나타난 감정 카운트
            emotion[self.moment_focus[4][i]] += 1

        if not sum(self.moment_focus[1]) : self.total_focus.append([0, 0, 0, 0, emotion.index(max(emotion)), 0])    #졸고 있으면 무조건 전체 비집중
        else :
            gaze = sum(self.moment_focus[0])
            blink = 5
            slope = sum(self.moment_focus[2])
            hand = sum(self.moment_focus[3])

            self.total_focus.append([gaze, blink, slope, hand, emotion.index(max(emotion)), (gaze+blink+slope+hand)*5])
            self.count_info[1] += 5     #졸음 시간 카운트

        if len(self.total_focus) >= 2 :     #만약 이전 테이블이 존재한다면 비집중->집중의 상태 변화 확인으로 횟수 카운트
            if self.total_focus[-2][0] != 5 and self.total_focus[-1][0] == 5 : self.count_info[0] += 1
            if self.total_focus[-2][2] != 5 and self.total_focus[-1][2] == 5 : self.count_info[2] += 1
            if self.total_focus[-2][3] != 5 and self.total_focus[-1][3] == 5 : self.count_info[3] += 1

        # 5 frame 분석한 결과를 테이블에 넣어준다. gaze, blink, slope, hand, emotion, score
        args = [self.tableName, self.total_focus[-1]]
        self.db.insertOneVideo(args)

        self.moment_focus = [[] for _ in range(5)]

        # args : emotion, blink, gaze, slope, hand, score


    def _break(self, start_time) :
        total_time = datetime.datetime.now()-start_time
        maximum_emotion = self.total_emotions.index(max(self.total_emotions))

        today = datetime.datetime.today().strftime("%Y%m%d")
        round = self.cnt+1 # 테이블 이름 : 0~ , 회차 1~

        if len(self.total_focus) >= 2 :    #만약 마지막에 비집중->집중의 상태변화가 없어 비집중 횟수 카운트가 안 됐는지 확인
            if self.total_focus[-2][0] != 5 and self.total_focus[-1][0] != 5 : self.count_info[0] += 1
            if self.total_focus[-2][2] != 5 and self.total_focus[-1][2] != 5 : self.count_info[2] += 1
            if self.total_focus[-2][3] != 5 and self.total_focus[-1][3] != 5 : self.count_info[3] += 1
        elif len(self.total_focus) == 1 :   #만약 5프레임 이상 10프레임 미만으로 찍혔을 때 비집중 횟수 카운트
            if self.total_focus[-1][0] != 5 : self.count_info[0] += 1
            if self.total_focus[-1][2] != 5 : self.count_info[2] += 1
            if self.total_focus[-1][3] != 5 : self.count_info[3] += 1

        total_score = 0

        for score in self.total_focus :
            total_score += score[-1]
        if len(self.total_focus) : total_score /= len(self.total_focus)

        # 한 영상에 대한 최종 결과 저장
        # args : timestamp, round, totalTime, emotion, blink, gaze, slope, hand, score, feedback
        data=[today,round,str(total_time).split('.')[0],self.count_info[1],self.count_info[0],self.count_info[2],self.count_info[3],total_score]
        for e in range(8) :
            data.append(int((self.total_emotions[e]/sum(self.total_emotions))*100))
        args=[self.userID,data]
        self.db.insertFinalRes(args)
        
        return str(total_time).split('.')[0], maximum_emotion, "%06.2f" % total_score