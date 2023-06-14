# 눈깜빡임, 시선, 얼굴 기울기, 손, 감정 인식
import dlib
import cv2
import numpy as np
import math
from keras.preprocessing.image import img_to_array
from gaze_tracking import GazeTracking
from hand import Hand

class AnalyzeFrame(object):
    def __init__(self, models):
        self.detector = models.detector
        self.predictor = models.predictor
        self.emotion_classifier=models.emotion_classifier
        self.hand = Hand(models)

    def _gazeTracking(self,frame): 
        gaze = GazeTracking(self.detector, self.predictor)
        gaze.refresh(frame)

        frame = gaze.annotated_frame()
        result_blink = 1
        result_gaze = 1

        if gaze.is_blinking() or gaze.is_right() or gaze.is_left() or gaze.is_center() or gaze.is_up():
            if gaze.is_blinking() : result_blink = 0
            result_gaze = 0         

        return result_gaze, result_blink

    def _facialSlope(self,landmarks):
        # facial slope 측정을 위한 삼각형
        eye1_x=eye1_y=eye2_x=eye2_y=mouth_x=mouth_y=0
        result_slope = 0

        for n in range(36,42):
            eye1_x += landmarks.part(n).x
            eye1_y += landmarks.part(n).y
            eye2_x += landmarks.part(n+6).x
            eye2_y += landmarks.part(n+6).y

        eye1_x /= 6
        eye1_y /= 6
        eye2_x /= 6
        eye2_y /= 6

        for n in range(48, 68):
            mouth_x += landmarks.part(n).x
            mouth_y += landmarks.part(n).y

        mouth_x /= 20
        mouth_y /= 20

        if (eye2_x-eye1_x) :
            if (eye2_y-eye1_y) / (eye2_x-eye1_x) < 0.4 :
                result_slope = self._calculateAngle([(eye2_x+eye1_x)/2, (eye2_y+eye1_y)/2], [mouth_x,mouth_y], [[eye1_x,eye1_y],[eye2_x,eye2_y]])

        return result_slope

    def _handTracking(self, fname) :
        return self.hand._inputImg(fname)

    def _analyze(self, frame, fname):
        #얼굴 인식 수행
        faces = self.detector(frame)
        result = []

        for face in faces:
            x1=face.left()
            y1=face.top()
            x2=face.right()
            y2=face.bottom()

            landmarks = self.predictor(frame, face)
            
            result.extend(list(self._gazeTracking(frame)))
            result.append(self._facialSlope(landmarks))
            
            hand_min_point = self._handTracking(fname)

            if hand_min_point != 1e9 and landmarks.part(8).y :
                if hand_min_point <= landmarks.part(8).y :
                    result.append(0)
                else : result.append(1)
            else : result.append(0)

            if result :
                result.append(self._analyzeEmotion(frame, [x1, y1, x2, y2]))

        return result

    def _calculateAngle(self, center, mouth, eyes) :
        dy = center[1]-mouth[1]
        dx = center[0]-mouth[0]
        angle = abs(math.atan(dy/dx))*(180/math.pi)

        if 90*0.7 <= angle <= 90*1.3 : return 1
        else : return 0

    def _analyzeEmotion(self, frame, face_location) :
        # 이미지 사이즈 조정 for neural network
        roi = frame[face_location[1]:face_location[3], face_location[0]:face_location[2]]
        roi = cv2.resize(roi, (48, 48))
        roi = roi.astype("float") / 255.0
        roi = img_to_array(roi)
        roi = np.expand_dims(roi, axis=0)
                    
        # Emotion predict
        preds = self.emotion_classifier.predict(roi)[0]
        # emotion_probability = np.max(preds) #대표하는 감정

        return preds.argmax()