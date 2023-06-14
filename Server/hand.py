import cv2

class Hand(object):
    def __init__(self, models):
        self.nPoints=22
        self.POSE_PAIRS = [ [0,1],[1,2],[2,3],[3,4],[0,5],[5,6],[6,7],[7,8],[0,9],[9,10],[10,11],[11,12],[0,13],[13,14],[14,15],[15,16],[0,17],[17,18],[18,19],[19,20] ]
        self.threshold=0.2

        # 위의 path에 있는 network 불러오기
        self.net = models.net

    def _inputImg(self,fname):
        img = cv2.imread(fname, cv2.IMREAD_COLOR)
        # img = cv2.rotate(img, cv2.ROTATE_90_CLOCKWISE)
        imgWidth = img.shape[1]
        imgHeight = img.shape[0]

        aspect_ratio = imgWidth/imgHeight
        inHeight=368
        inWidth = int(((aspect_ratio*inHeight)*8)//8)
        # network에 넣기 위해 전처리(BGR image를 blob으로 변환)
        inpBlob = cv2.dnn.blobFromImage(img, 1.0/255,(inWidth,inHeight),(0,0,0),swapRB=False,crop=False)
        # network에 넣어주기
        self.net.setInput(inpBlob)
        # 결과 받아오기
        output = self.net.forward()
        point = self._extractPoints(img, output)
        return point

    def _extractPoints(self, img, output):
        imageHeight, imageWidth, _ = img.shape

        # 손 검출한 포인트
        min_point = 1e9

        for i in range(self.nPoints):
            
            # 해당 신체부위의 신뢰도
            probMap = output[0,i,:,:]
            probMap = cv2.resize(probMap,(imageWidth,imageHeight))
            # global 최대값
            minVal, prob, minLoc, point = cv2.minMaxLoc(probMap)

            if prob > self.threshold : 
                # threshold( 0.1 )보다 크면 points에 추가
                min_point = min(min_point, int(point[1]))

        return min_point
