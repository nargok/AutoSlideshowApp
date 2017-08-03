package jp.techacademy.ryoichi.gokan.autoslideshowapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_ERQUEST_CODE = 100;

    Timer mTimer;
    ImageView mImageView;

    // タイマー用の時間のための変数
    double mTimerSec = 0.0;
    Handler mHandler = new Handler();

    Button mNextButton;
    Button mPrevButton;
    Button mStartPauseButton;
    Boolean permissionFlag;
    Boolean timerFlag = false;

    ArrayList<Uri> imageList = new ArrayList<Uri>();
    int i = 0;

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
                imageList = getContentsInfo();
            } else {
                // 許可されてないので許可ダイヤログを表示する
                permissionFlag = false;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_ERQUEST_CODE );
            }
            // Android 5系以下の場合
        } else {
            permissionFlag = true;
            imageList = getContentsInfo();
        }

        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override // 進むボタンをタップした時の処理
            public void onClick(View v) {
                Log.d("SlideShow", "NextButton was tapped.");

                if (permissionFlag == true) {
                    Log.d("SlideShow", "NextButton was executed.");

                    i = setNextImage(imageList, i);
                }

            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override // 戻るボタンをタップした時の処理
            public void onClick(View v) {
                Log.d("SlideShow", "PrevButton was tapped.");

                if (permissionFlag == true) {
                    Log.d("SlideShow", "PrevButton was executed.");

                    i = setPreviousImage(imageList, i);
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

                    // 進むボタンと戻るボタンをタップ不可にする
                    mNextButton.setEnabled(false);
                    mPrevButton.setEnabled(false);

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
                                        Log.d("SlideShow", "Timer is running.");
                                        i = setNextImage(imageList, i);
                                    }
                                });
                            }
                        }, 2000, 2000);   // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒に設定
                    }

                } else {
                    // タイマー停止処理を実施
                    mStartPauseButton.setText("再生");
                    timerFlag = false;

                    // 進むボタンと戻るボタンをタップ可にする
                    mNextButton.setEnabled(true);
                    mPrevButton.setEnabled(true);

                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }

            }
        });

    }

    // 共通処理として１回呼ばれる
    private ArrayList<Uri> getContentsInfo() {

        ArrayList<Uri> myImageList = new ArrayList<Uri>();
        int i = 0;

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

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);

        do {
            //カラムIDの取得
            fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);

            //IDからURIを取得
            Uri bmpUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            imageList.add(bmpUri);
            myImageList.add(bmpUri);
            Log.d("SlideShow", myImageList.get(i).toString());
            i++;
        } while (cursor.moveToNext());

        cursor.close();
        return imageList;
    }

    // 次の画像を表示する処理
    private int setNextImage(ArrayList<Uri> imageList, int i) {

        i++;
        if (i == imageList.size()) {
            i = 0;
        }

        Uri imageUri = imageList.get(i);
        mImageView.setImageURI(imageUri);

        return i;
    }

    // 前の画像を表示する処理
    private int setPreviousImage(ArrayList<Uri> imageList, int i) {

        i--;
        if (i < 0) {
            i = (imageList.size() - 1);
        }

        Uri imageUri = imageList.get(i);
        mImageView.setImageURI(imageUri);
        return i;
    }

    // ImageViewに画像を設定する処理
    private void setImageView(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        mImageView.setImageURI(imageUri);

    }

}
