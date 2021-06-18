package com.animation.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements View.OnTouchListener {

    private static final String TAG = "MMainActivity";
    
    ViewFlipper myViewFlipper;
    Button btnOn,btnNext,btnStart,btnStop;
    GestureDetector mDetetor;   //手势检测

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myViewFlipper = findViewById(R.id.myViewFlipper);

        myViewFlipper.setOnTouchListener(this);
        mDetetor=new GestureDetector(new simpleGestureListener());

        btnStart = findViewById(R.id.btn_start);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                myViewFlipper.startFlipping();
                startThreadPool();
                Toast.makeText(MainActivity.this, "开始", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                myViewFlipper.stopFlipping();
                stopThreadPool();
                Toast.makeText(MainActivity.this, "关闭", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        btnOn = findViewById(R.id.btn_on);
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myViewFlipper.showPrevious();
            }
        });
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myViewFlipper.showNext();
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {  return mDetetor.onTouchEvent(motionEvent);  }

    private class simpleGestureListener extends GestureDetector.SimpleOnGestureListener{
        final int FLING_MIN_DISTANCE=120,FLING_MIN_VELOCITY=200;

        @Override
        public boolean onDown(MotionEvent e) {
            Toast.makeText(MainActivity.this, "OnDown", Toast.LENGTH_SHORT).show();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX()-e2.getX()>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                myViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_left_out));
                myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_left_in));
                myViewFlipper.showNext();
                Toast.makeText(MainActivity.this, "Fling left", Toast.LENGTH_SHORT).show();
            }else if (e2.getX()-e1.getX()>FLING_MIN_DISTANCE && Math.abs(velocityX)>FLING_MIN_VELOCITY){
                myViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_right_out));
                myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_right_in));
                myViewFlipper.showPrevious();
                Toast.makeText(MainActivity.this, "Fling right", Toast.LENGTH_SHORT).show();
            }

            if (e1.getY()-e2.getY()>FLING_MIN_DISTANCE && Math.abs(velocityY)>FLING_MIN_VELOCITY){
                myViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_top_out));
                myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_top_in));
                myViewFlipper.showNext();
                Toast.makeText(MainActivity.this, "Fling top", Toast.LENGTH_SHORT).show();
            }else if (e2.getY()-e1.getY()>FLING_MIN_DISTANCE && Math.abs(velocityY)>FLING_MIN_VELOCITY){
                myViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_bottom_out));
                myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_bottom_in));
                myViewFlipper.showPrevious();
                Toast.makeText(MainActivity.this, "Fling bottom", Toast.LENGTH_SHORT).show();
            }

            return true;

        }

    }


    ThreadPoolProxy singlePool=null;
    ViewFlipperRunnable mFlipperRunnable=null;
    private void startThreadPool(){
        synchronized (new Object()){
            if (singlePool == null && mFlipperRunnable==null ) {
                singlePool=new ThreadPoolProxy(1,1,50L);
                mFlipperRunnable=new ViewFlipperRunnable();
                mFlipperRunnable.run();
                singlePool.execute(mFlipperRunnable);
                Log.i(TAG, "startThreadPool: ");
            }
        }
    }

    private void stopThreadPool(){
        if (singlePool !=null && mFlipperRunnable!= null) {
            myViewFlipper.stopFlipping();
            singlePool.cancel(mFlipperRunnable);
            Log.i(TAG, "stopThreadPool: ");
            singlePool.stop();
            singlePool=null;
            mFlipperRunnable=null;
        }
    }

    private class ViewFlipperRunnable implements Runnable{
        @Override
        public void run() {
            Log.i(TAG, "run---->>>>: ");
            myViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_top_out));
            myViewFlipper.setInAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.slide_top_in));
            myViewFlipper.setFlipInterval(5000);
            myViewFlipper.startFlipping();
        }
    }

    public class ThreadPoolProxy {

        private ThreadPoolExecutor mPoolExecutor;
        private final int mCorePoolSize; //线程池维护线程的最少数量
        private final int mMaximumPoolSize; //线程池维护线程的最大数量
        private final long mKeepAliveTime; // 线程池维护线程所允许的空闲时间

        public ThreadPoolProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
            mCorePoolSize = corePoolSize;
            mMaximumPoolSize = maximumPoolSize;
            mKeepAliveTime = keepAliveTime;
        }

        //执行任务，当线程处于关闭，将会重新创建线程池
        public synchronized void execute(Runnable runnable) {
            if (runnable == null) {
                return;
            }
            if (mPoolExecutor ==null || mPoolExecutor.isShutdown()) {
                // 参数说明
                // 当线程池中的线程小于mCorePoolSize，直接创建新的线程加入线程池执行任务
                // 当线程池中的线程数目等于mCorePoolSize，将会把任务放入任务队列BlockingQueue中
                // 当BlockingQueue中的任务放满了，将会创建新的线程去执行，
                // 但是当总线程数大于mMaximumPoolSize时，将会抛出异常，交给RejectedExecutionHandler处理
                // mKeepAliveTime是线程执行完任务后，且队列中没有可以执行的任务，存活的时间，后面的参数是时间单位
                // ThreadFactory是每次创建新的线程工厂
                mPoolExecutor=new ThreadPoolExecutor(
                        mCorePoolSize,
                        mMaximumPoolSize,
                        mKeepAliveTime,
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<Runnable>(),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());

            }
            mPoolExecutor.execute(runnable);
        }

        /** 取消线程池中某个还未执行的任务 */
        @SuppressLint("NewApi")
        public synchronized boolean cancel(Runnable runnable){
            if (mPoolExecutor != null && (!mPoolExecutor.isShutdown()) || Objects.requireNonNull(mPoolExecutor).isTerminating()) {
                return mPoolExecutor.getQueue().remove(runnable);
            }
            return false;
        }


        /** 取消线程池中某个还未执行的任务 */
        @SuppressLint("NewApi")
        public synchronized boolean contains(Runnable runnable){
            if (mPoolExecutor != null && (!mPoolExecutor.isShutdown() || Objects.requireNonNull(mPoolExecutor).isTerminating())) {
                return mPoolExecutor.getQueue().contains(runnable);
            }
            return false;
        }

        /** 立刻关闭线程池，并且正在执行的任务也将会被中断 */
        @SuppressLint("NewApi")
        public void stop(){
            if (mPoolExecutor != null && (!mPoolExecutor.isShutdown() || Objects.requireNonNull(mPoolExecutor).isTerminating())) {
                mPoolExecutor.shutdown();
            }
        }

        /** 平缓关闭单任务线程池，但是会确保所有已经加入的任务都将会被执行完毕才关闭 */
        @SuppressLint("NewApi")
        public synchronized void shutdown(){
            if (mPoolExecutor != null && (!mPoolExecutor.isShutdown() || Objects.requireNonNull(mPoolExecutor).isTerminating())) {
                mPoolExecutor.shutdownNow();
            }
        }

    }


}
