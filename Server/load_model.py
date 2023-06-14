import dlib
import os
import cv2
from keras.models import load_model

class LoadModel(object) :
    def __init__(self):
        cwd = os.path.abspath(os.path.dirname(__file__))

        #face recognition model
        self.detector = dlib.get_frontal_face_detector()
        self.predictor = dlib.shape_predictor(os.path.abspath(os.path.join(cwd, "trained_models/shape_predictor_68_face_landmarks.dat")))

        #hand recognition model
        self.protoFile = os.path.abspath(os.path.join(cwd,"trained_models/pose_deploy.prototxt"))
        self.weightsFile = os.path.abspath(os.path.join(cwd,"trained_models/pose_iter_102000.caffemodel"))
        self.net = cv2.dnn.readNetFromCaffe(self.protoFile, self.weightsFile)

        #emotion recognition model
        self.emotion_classifier=load_model('trained_models/emotion_model.hdf5',compile=False)