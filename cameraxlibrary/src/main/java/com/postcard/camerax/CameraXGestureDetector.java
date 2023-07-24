package com.postcard.camerax;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class CameraXGestureDetector {
    private GestureDetector mGestureDetector;
    /**
     * 缩放相关
     */
    private float currentDistance = 0;
    private float lastDistance = 0;
    public  CameraXGestureDetector(Context context) {
        mGestureDetector = new GestureDetector(context, onGestureListener);
        mGestureDetector.setOnDoubleTapListener(onDoubleTapListener);
    }
    public boolean onTouchEvent(MotionEvent event){
        return  mGestureDetector.onTouchEvent(event);
    }
    GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.i("","onDown: 按下");
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.i("","onShowPress: 刚碰上还没松开");
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("","onSingleTapUp: 轻轻一碰后马上松开");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i("","onScroll: 按下后拖动");
            // 大于两个触摸点
            if (e2.getPointerCount() >= 2) {

                //event中封存了所有屏幕被触摸的点的信息，第一个触摸的位置可以通过event.getX(0)/getY(0)得到
                float offSetX = e2.getX(0) - e2.getX(1);
                float offSetY = e2.getY(0) - e2.getY(1);
                //运用三角函数的公式，通过计算X,Y坐标的差值，计算两点间的距离
                currentDistance = (float) Math.sqrt(offSetX * offSetX + offSetY * offSetY);
                if (lastDistance == 0) {//如果是第一次进行判断
                    lastDistance = currentDistance;
                } else {
                    if (currentDistance - lastDistance > 10) {
                        // 放大
                         zoom();
                    } else if (lastDistance - currentDistance > 10) {
                        // 缩小
                         zoomOut();
                    }
                }
                //在一次缩放操作完成后，将本次的距离赋值给lastDistance，以便下一次判断
                //但这种方法写在move动作中，意味着手指一直没有抬起，监控两手指之间的变化距离超过10
                //就执行缩放操作，不是在两次点击之间的距离变化来判断缩放操作
                //故这种将本次距离留待下一次判断的方法，不能在两次点击之间使用
                lastDistance = currentDistance;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("","onLongPress: 长按屏幕");
            longClick(e.getX(), e.getY());
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i("","onFling: 滑动后松开");
            currentDistance = 0;
            lastDistance = 0;
            return true;
        }
    };

    GestureDetector.OnDoubleTapListener onDoubleTapListener = new GestureDetector.OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("click","onSingleTapConfirmed: 严格的单击");
            click(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("","onDoubleTap: 双击");
            doubleClick(e.getX(), e.getY());
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            Log.i("","onDoubleTapEvent: 表示发生双击行为");
            return true;
        }
    };

    /**
     * 放大
     */
    abstract void zoom();

    /**
     * 缩小
     */
    abstract void zoomOut();

    /**
     * 点击
     */
    abstract void click(float x, float y);

    /**
     * 双击
     */
    abstract  void doubleClick(float x, float y);

    /**
     * 长按
     */
    abstract void longClick(float x, float y);
}
