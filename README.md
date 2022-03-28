# 美顏功能APP
## APP實現步驟流程
Step1. 實作相機功能，如預覽畫面、前後鏡頭轉換、video影像分析等等(使用cameraX實現)

Step2. 人臉偵測，獲取臉部區域、臉部關鍵點(使用Google ML Kit套件實現)

Step3. 根據Step2得到的人臉相關資訊，利用影像處理技術實現各種美顏功能

Step4. 將美顏的結果繪製到自定義的View上，並持續刷新結果

## 人臉偵測
首先需要建立FaceDetector，並定義FaceDetector相關操作配置，如設置效能模式、最低臉部尺寸、是否偵測landmark、contour等等，相關配置可參考[Google ML Kit官方文件](https://developers.google.com/ml-kit/vision/face-detection/android)

在APP實作中，使用了以下配置:
```kotlin
private var faceOptions: FaceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // Accuracy or Fast mode
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()
```
配置好FaceDetector後，添加事件監聽，如果成功偵測，則回傳臉部偵測結果
```kotlin
fun detect(image: ImageProxy, callback: (List<Face>) -> Unit) {
    detector.process(InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees))
        .addOnSuccessListener { faces ->
            Log.d(TAG, "Number of faces: ${faces.size}")
            callback(faces)
        }
        .addOnFailureListener {
            Log.e(TAG, "Detection failed: ${it.stackTrace}")
        }
        .addOnCompleteListener {
            image.close()
        }
}
```

人臉偵測結果展示:

<img src="https://github.com/YoranLiu/AndroidBeautyEffect/blob/master/face_detection_result.jpg" width=200 height=360/>



## 瘦臉
首先，我們將影像切成N x N個網格，將這些網格交叉座標點存在verts裡，操作這些座標點，並利用drawBitmapMesh()方法改變影像，達到瘦臉的效果
```kotlin
for (i in 0 until GRID_H + 1) {
    fy = (bitmapH * i / GRID_H).toFloat()
    for (j in 0 until GRID_W + 1) {
        // X-axis coordinates, put in even position
        fx = (bitmapW * j / GRID_W).toFloat()
        verts[idx * 2] = fx

        // Y-axis coordinates, put in odd position
        verts[idx * 2 + 1] = fy
        idx += 1
    }
}
```
瘦臉算法我們使用了Interactive Image Warping文獻中的 Uwarp's local mapping functions方法，我們需要定義拉動的起始及終點座標點、作用半徑(或稱拉伸力度)，根據這些資訊在作用圓形範圍內的交叉點座標計算扭曲度，進行座標轉換，將求得的值更新至verts。詳細程式碼可見[SmallFaceUtils.kt](https://github.com/YoranLiu/AndroidBeautyEffect/blob/master/app/src/main/java/com/jack/beautyeffect/beautyUtils/SmallFaceUtils.kt)

Google ML Kit套件可以抓取12種不同的臉部關鍵點，拉動起始點的部分選擇了則臉部最外圍的關鍵點(對應官方文件中的FACE_OVAL關鍵點)；拉動終點的部分選擇了臉部中心鼻橋的關鍵點(對應官方文件中的NOSE_BRIDGE關鍵點)，並從中選取某些點進行瘦臉操作，示意圖如下:

<img src="https://github.com/YoranLiu/AndroidBeautyEffect/blob/master/small_face_diagram.JPG" width=250 height=300/>

瘦臉效果展示:

<img src="https://github.com/YoranLiu/AndroidBeautyEffect/blob/master/smallFace_result.gif" width=200 height=360 />

完整Demo連結:
https://www.youtube.com/watch?v=k1UOJm0rrIk
## 磨皮(待更新)
## 美白(待更新)
