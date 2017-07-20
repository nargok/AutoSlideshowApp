package jp.techacademy.ryoichi.gokan.autoslideshowapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ERQUEST_CODE = 100;

    Timer mTimer;
    ImageView mImageView;

    // 画像データ取得用のCursor
    Cursor myCursor;

    // タイマー用の時間のための変数
    double mTimerSec = 0.0;
    Handler mHandler = new Handler();

    Button mNextButton;
    Button mPrevButton;
    Button mStartPauseButton;
    Boolean permissionFlag;
    Boolean timerFlag = false;

    // 使わないかも・・・
//    String images[] = {"Dog", "Cat", "Sloth"};
//    int i = 0; // 画像表示のためのカウンタ


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        mStartPauseButton = (Button) findViewById(R.id.startPause_button);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている flag on/offに切り替える
                permissionFlag = true;
                myCursor = getContentsInfo();
            } else {
                // 許可されてないので許可ダイヤログを表示する
                permissionFlag = false;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_ERQUEST_CODE );
            }
            // Android 5系以下の場合
        } else {
            permissionFlag = true;
            myCursor = getContentsInfo();
        }

        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override // 進むボタンをタップした時の処理
            public void onClick(View v) {
                Log.d("SlideShow", "NextButton was tapped.");

                if (permissionFlag == true) {
                    Log.d("SlideShow", "NextButton was executed.");

                    setNextImage(myCursor);
                }

            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override // 戻るボタンをタップした時の処理
            public void onClick(View v) {
                Log.d("SlideShow", "PrevButton was tapped.");

                if (permissionFlag == true) {
                    Log.d("SlideShow", "PrevButton was executed.");

                    setPreviousImage(myCursor);
                }

            }
        });

        mStartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SlideShow", "StartPauseButton was tapped. flag is " + timerFlag);

                if (timerFlag == false) {
                    // 再生処理を実施
                    mStartPauseButton.setText("停止");
                    timerFlag = true;

                    if (mTimer == null) {
                        // タイマーの作成
                        mTimer = new Timer();
                        // タイマーの始動
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mTimerSec += 2.0;

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
//                                        mTimerText.setText(String.format("%.1f", mTimerSec));
                                        Log.d("SlideShow", "Timer is running.");
                                        setNextImage(myCursor);
                                    }
                                });
                            }
                        }, 2000, 2000);   // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒に設定
                    }

                } else {
                    // タイマー停止処理を実施
                    mStartPauseButton.setText("再生");
                    timerFlag = false;

                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }

            }
        });

    }

    // 共通処理として１回呼ばれる
    private Cursor getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタ無し)
                null, // フィルタ用パラメータ
                null  // ソート(null ソートなし)
        );

        // 先頭の画像を表示する
        cursor.moveToFirst();
        setImageView(cursor);
        // ここでクローズしてよいのか？
//        cursor.close();
        return cursor;
    }

    // 次の画像を表示する処理
    private void setNextImage(Cursor cursor) {

        if (cursor.moveToNext()) {
            Log.d("SlideShow", "moveToNext");
            setImageView(cursor);
        } else if (cursor.moveToFirst()){
            Log.d("SlideShow", "moveToFirst");
//            cursor.moveToFirst();
//            setImageView(cursor);
        }

        // cursorは使用した後はcloseを必ず行う
//        cursor.close();
    }

    // 前の画像を表示する処理
    private void setPreviousImage(Cursor cursor) {

        if (cursor.moveToPrevious()) {
            Log.d("SlideShow", "moveToPrevious");
            setImageView(cursor);
        } else {
            Log.d("SlideShow", "moveTo");
            cursor.moveToLast();
            setImageView(cursor);
        }

        // cursorは使用した後はcloseを必ず行う
//        cursor.close();
    }



    // ImageViewに画像を設定する処理
    private void setImageView(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        mImageView.setImageURI(imageUri);
    }

}
