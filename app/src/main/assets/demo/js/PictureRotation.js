//執行時間
var interValTime = 0;
//下一張圖片顯示時間
var fadeInTime = 0;
//上一張圖片的消失時間
var fadeOutTime = 0;
//保存當前圖片定時的對象
var imageTimer = null;
//當前的索引
var currIndex = null;
//當前圖片的所以
var currImgIndex = null;
//當前圖片
var currImgLi = null;
//記錄需要切換的圖片集合
var IMGLIST = null;
//記錄需要切換的圖片集合的索引。
var IMGINDEX = null;
//最大圖片個數
var MAXINDEX = null;

function InitSwitchObj() {
    IMGLIST = $("#IMG_UL_LIST_1");
    IMGINDEX = $("#IMG_INDEX_UL_LIST_1");
    MAXINDEX = IMGLIST.find("li").length;
    currIndex = 0;
    currImgIndex = IMGINDEX.find("li:eq(" + currIndex + ")");
    currImgLi = IMGLIST.find("li:eq(" + currIndex + ")");
    currImgLi.show();
    if (MAXINDEX > 1) {        
        interValTime = 1500;
        fadeInTime = 0;
        fadeOutTime = 0;
        ImageAutoSwitch();
        ImageManualSwitch();
        ImageStopSwitch();
    }
}

//自動定時切換。
function ImageAutoSwitch() {
    if (imageTimer != null) {
        clearInterval(imageTimer);
    }
    imageTimer = setInterval("IntervalImage()", interValTime);
}
//手動切換圖片
function ImageManualSwitch() {
    IMGINDEX.find("li").each(function (i) {
        $(this).hover(function () {
            if (imageTimer != null) {
                clearInterval(imageTimer);
            }
            currIndex = i;
            ImageSwitchChange();
        }, function () {
            ImageAutoSwitch();
        });

    });
}
//鼠標放到圖片上的換，停止切換
function ImageStopSwitch() {
    IMGLIST.find("li").each(function () {
        $(this).hover(function () {
            if (imageTimer != null) {
                clearInterval(imageTimer);
            }
        }, function () {
            ImageAutoSwitch();
        });
    });

}
//自动切换图片
function IntervalImage() {
    currIndex = parseFloat(currIndex);
    currIndex = currIndex + 1;
    //如果切換到最大數量的時候則從頭開始
    if (currIndex >= MAXINDEX) {
        currIndex = 0;
    }
    //將原來的現實圖片索引的背景透明
    ImageSwitchChange();
}
//切換圖片的時候，響應的圖片要實現漸變效果
function ImageSwitchChange() {
    if (currImgIndex != null) {
        currImgIndex.css({ "background": "#ccc"});
    }
    currImgIndex = IMGINDEX.find("li:eq(" + currIndex + ")");
    imgLi = IMGLIST.find("li:eq(" + currIndex + ")");
    currImgIndex.css({ "background": "#fff"});
    if (currImgLi != null) {
        //currImgLi.fadeOut(fadeOutTime, function () {
            //imgLi.fadeIn(fadeInTime);
        currImgLi.hide(fadeOutTime, function () {
            imgLi.show(fadeInTime);
        });
    }
    currImgLi = imgLi;
}