package com.example.car_ushi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    static int n =4;
    boolean isDoubleTapped = false;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between two clicks to be considered as a double click


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);
        gameSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    // Double click detected
                    System.out.println(" double click!!!! " );
                    isDoubleTapped = !isDoubleTapped;
                    if(isDoubleTapped){
                        Log.d("Tag","n 8");
                        n=8;
                    } else {
                        n =4;
                        Log.d("Tag","n 4");
                    }
                }
                lastClickTime = clickTime;
                /*
                isDoubleTapped = !isDoubleTapped;
                if(isDoubleTapped){
                    Log.d("Tag","n 8");
                    n=8;
                } else {
                    n =4;
                    Log.d("Tag","n 4");
                }
                */

            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }



    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable,SensorEventListener{

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap ball;
        Bitmap police;
        int ballX=0;
        int x=200;
        String sensorOutput="This is test";
        Paint paintProperty;
        Random random = new Random();
        float minX = -430.0f;
        float maxX = 430.0f;

        float policeY =0.0f;
        float randomX = 0.0f;

        int screenWidth;
        int screenHeight;
        int secondsRemaining = 60;
        int score = 0;
        MediaPlayer mp;
        MediaPlayer crashmp;
        Canvas canvas;
        boolean isHit = false;



        public GameSurface(Context context) {
            super(context);
            holder = getHolder();
            if (isHit) {
                ball = BitmapFactory.decodeResource(getResources(), R.drawable.aru1);
                ball = Bitmap.createScaledBitmap(ball, 250, 250, false);
            } else {
                ball = BitmapFactory.decodeResource(getResources(), R.drawable.car);
                ball = Bitmap.createScaledBitmap(ball, 250, 250, false);
            }


            police = BitmapFactory.decodeResource(getResources(),R.drawable.policecar);
            police = Bitmap.createScaledBitmap(police,250,250,false);
            randomX = minX + random.nextFloat() * (maxX - minX);
            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,accelerometerSensor,sensorManager.SENSOR_DELAY_GAME);

            paintProperty= new Paint();
            paintProperty.setTextSize(100);
            mp = MediaPlayer.create(MainActivity.this,R.raw.policesiren);
            crashmp = MediaPlayer.create(MainActivity.this,R.raw.collision);
            mp.start();
            //mp.pause();
            //mp.isPlaying();
            //mp.prepare();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new GameTimer(), 0, 1000);// every second

        }

        @Override
        public void run() {
            while (running == true){

                // create a TimeTask to crash the game after 5 mins
                if (holder.getSurface().isValid() == false)
                    continue;
                canvas= holder.lockCanvas();

                canvas.drawRGB(0,255,0);

                canvas.drawText(sensorOutput,0,200,paintProperty);

                canvas.drawBitmap( ball,(screenWidth/2) - ball.getWidth()/2 +ballX ,(screenHeight/2) - ball.getHeight()+800,null);
                canvas.drawBitmap( police,(screenWidth/2) - police.getWidth()/2 +randomX ,policeY,null);
                //canvas.drawBitmap(police,randomX,policeY,null);
                if(!mp.isPlaying()){
                    mp.start();
                }
                if(policeY < 1900.00){
                   policeY = policeY + n ;
                } else {
                    policeY = 0;
                    randomX = minX + random.nextFloat() * (maxX - minX);
                    //increase score
                    score++;
                    if(!mp.isPlaying()){
                        mp.start();
                    }
                }
                Rect playerHitbox = new Rect((screenWidth/2) - ball.getWidth()/2 +ballX, (screenHeight/2) - ball.getHeight()+800, (screenWidth/2) - ball.getWidth()/2 +ballX + 200, (screenWidth/2) - ball.getWidth()/2 +ballX + +800+200);
                Rect enemyHitbox = new Rect((screenWidth/2) - police.getWidth()/2 +(int)randomX, (int)policeY, (screenWidth/2) - police.getWidth()/2 +(int)randomX + 200, (int)policeY + 200);
                //Log.d("player", playerHitbox.left + ":" + playerHitbox.top + ":" + playerHitbox.right + ":" + playerHitbox.bottom);
                //Log.d("enemy", playerHitbox.left + ":" + playerHitbox.top + ":" + playerHitbox.right + ":" + playerHitbox.bottom);

                if(playerHitbox.intersect(enemyHitbox)){
                    isHit = true;
                    ball= BitmapFactory.decodeResource(getResources(),R.drawable.aru1);
                    ball = Bitmap.createScaledBitmap(ball,250,250,false);
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new CrashTimer(), 0, 1000);// every second

                    policeY = 0;
                    randomX = minX + random.nextFloat() * (maxX - minX);
                    Log.d("Tag","collision******************************* " );
                    if(score > 0) {
                        score--;
                    }
                    if(mp.isPlaying()){
                        mp.pause();
                        crashmp.start();
                    }
                }
                sensorOutput = "score:"+score + "" + " Time left:" + secondsRemaining;
                //Log.d("Tag","on police car " + policeY);
                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
                //Log.d("Tag","on sensor called : " + ballX);
                if (event.values[0] < -0.2 && ballX < 430) {
                    if (event.values[0] < -2.0) {
                        ballX += 1.5;
                    } else {
                        ballX += 4;
                    }
                } else if(event.values[0] > 0.2 && ballX > -430){
                    if (event.values[0] < 2.0) {
                        ballX -= 1.5;
                    } else {
                        ballX -= 4;
                    }

                }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        class CrashTimer extends TimerTask {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // Wait for 2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isHit = false;
                ball= BitmapFactory.decodeResource(getResources(),R.drawable.car);
                ball = Bitmap.createScaledBitmap(ball,250,250,false);

            }
        }
        class GameTimer extends TimerTask {
            @Override
            public void run() {
                if (secondsRemaining > 0) {
                    //System.out.println("Time remaining: " + secondsRemaining + " seconds");
                    secondsRemaining--;
                } else {
                    cancel(); // Cancel the timer when the countdown is finished
                    if(mp.isPlaying()){
                        mp.pause();
                    }
                    running = false;

                    Canvas canvas = holder.lockCanvas();
                    canvas.drawColor(Color.RED);

                    Bitmap gover = BitmapFactory.decodeResource(getResources(), R.drawable.gameover);
                    gover = Bitmap.createScaledBitmap(gover,450,450,false);
                    canvas.drawBitmap(gover, 200, 200, null);

                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(100);
                    canvas.drawText("Score: " + score, 210, 1000, paint);
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }//GameSurface
}//Activity