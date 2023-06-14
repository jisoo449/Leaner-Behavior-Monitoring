import pymysql
from database.config import config

class Database(object):

    def __init__(self):

        # MySQL Connection 연결
        self.conn = pymysql.connect(host=config['host'],
                                user = config['user'],
                                password=config['password'],
                                db=config['database'],
                                charset='utf8')

        # Connection으로부터 Cursor 생성
        self.curs = self.conn.cursor(pymysql.cursors.DictCursor)


    # # frame별로 분석한 데이터 저장
    # def insertTmpResData(self, args):
        
    #     # args : emotion, blink, gaze, slope, hand
    #     sql = "INSERT INTO tmpResult VALUES (%s,%s,%s,%s,%s);"
    #     self.curs.execute(sql,args)
    #     self.conn.commit()


    # # tmpResult 테이블 데이터 전체 삭제
    # def resetTmpTable(self):

    #     sql = "DELETE FROM tmpResult"
    #     self.curs.execute(sql)
    #     self.conn.commit()


    # 학생이 하루동안 촬영한 영상 몇 개 있는지 확인
    def checkCnt(self, args):

        sql = "SHOW TABLES"
        self.curs.execute(sql)
        rows = self.curs.fetchall()

        tabName = args[0]+"_"+str(args[1])
        cnt=-1
        for row in rows:
            for db, table in row.items():
                if tabName in table:
                    cnt=cnt+1

        return cnt
    

    # 하나의 영상에 대한 분석 결과 테이블 생성
    def createOneVideo(self,args):

        # args : 학생 ID, 날짜, 회차
        tabName = args[0]+"_"+str(args[1])+"_"+str(args[2])
        sql = "CREATE TABLE "+tabName+"(id INT NOT NULL AUTO_INCREMENT, emotion INT NOT NULL, blink INT NOT NULL, gaze INT NOT NULL, slope INT NOT NULL, hand INT NOT NULL, score INT NOT NULL, PRIMARY KEY (id));"

        self.curs.execute(sql)
        self.conn.commit()

        return tabName

    # 하나의 영상에 대한 분석 결과 한 row 저장
    def insertOneVideo(self,args):

        # gaze, blink, slope, hand, emotion, score
        sql = "INSERT INTO "+ args[0]+ "(gaze, blink, slope, hand, emotion, score) VALUES (%s,%s,%s,%s,%s,%s)"
        self.curs.execute(sql,args[1])
        self.conn.commit()


    # 한 학생의 모든 영상에 대한 정보 기록용 테이블 생성
    # 해당 학생 테이블이 존재하는지 먼저 확인 필요
    def createStuTab(self, args):

        # args : 학생 ID

        sql = "SHOW TABLES"
        self.curs.execute(sql)
        rows = self.curs.fetchall()
        exist = False
        for row in rows:
            if exist:
                break
            for db, table in row.items():
                if args == table:
                    exist=True

        if not exist:

            sql = "CREATE TABLE "+args+ "(id INT NOT NULL AUTO_INCREMENT, timestamp VARCHAR(20) NOT NULL, round INT NOT NULL, totalTime VARCHAR(20) NOT NULL, blink INT NOT NULL, gaze INT NOT NULL, slope INT NOT NULL, hand INT NOT NULL, score DECIMAL(5,2) NOT NULL, feedback VARCHAR(100) NULL,e0 INT NOT NULL,e1 INT NOT NULL,e2 INT NOT NULL,e3 INT NOT NULL,e4 INT NOT NULL,e5 INT NOT NULL,e6 INT NOT NULL, e7 INT NOT NULL,PRIMARY KEY(id));"
            
            self.curs.execute(sql)
            self.conn.commit()


    # 한 영상 분석이 끝난 후, 최종적인 데이터들 저장
    # 학생 ID, 날짜, 영상 회차 정보 필요 + 분석 결과 정보
    def insertFinalRes(self,args):
        # args : timestamp, round, totalTime, emotion, blink, gaze, slope, hand, score, feedback
        sql = "INSERT INTO "+args[0]+"(timestamp, round, totalTime, blink, gaze, slope, hand, score, e0,e1,e2,e3,e4,e5,e6,e7) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"
        self.curs.execute(sql,args[1])
        self.conn.commit()


    # 데이터 조회
    def selectAllData(self):

        sql = "SELECT * from tmpResult"
        self.curs.execute(sql)
        rows = self.curs.fetchall()
        print(rows)                                                                                                                                     