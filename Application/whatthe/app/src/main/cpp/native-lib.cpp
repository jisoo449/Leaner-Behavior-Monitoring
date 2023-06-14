#include <jni.h>
#include <opencv2/opencv.hpp>

#define PORT 5001

using namespace cv;
using namespace std;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_whatthe_CameraActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                         jlong mat_addr_input,
                                                         jlong mat_addr_result) {
    // TODO: implement ConvertRGBtoGray()
    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    //cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
    resize(matInput, matResult, Size(256, 256), 0, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_whatthe_CameraActivity_smaller(JNIEnv *env, jobject thiz, jlong mat_addr_input,
                                                jlong mat_addr_result) {
    // TODO: implement smaller()

    Mat &matInput = *(Mat *)mat_addr_input;
    Mat &matResult = *(Mat *)mat_addr_result;

    //int w = matInput.cols/3;
    //int h = matInput.rows/3;
    int w = 640;
    int h = 360;

    resize(matInput, matResult, Size(w, h), 0, 0);

    cvtColor(matResult, matInput, COLOR_BGR2GRAY);
}