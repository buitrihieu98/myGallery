package com.example.mygallery;

import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class FullScreenActivity extends Activity {

    ImageView imageViewFull;
    ImageButton imageButtonShare, imageButtonEdit, imageButtonDelete, imageButtonMore;
    TextView textViewDate;
    boolean show=false;
    WallpaperManager wallpaperManager ;
    BitmapDrawable bitmapDrawable ;
    Bitmap bitmap1, bitmap2;
    int width, height;
    DisplayMetrics displayMetrics;
    int REQUEST_CODE_DEL=12345;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //xóa actionbar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        imageViewFull=(ImageView) findViewById(R.id.imageViewFull);
        imageButtonDelete= (ImageButton) findViewById(R.id.imageButtonDelete);
        imageButtonEdit=(ImageButton) findViewById(R.id.imageButtonEdit);
        imageButtonShare=(ImageButton) findViewById(R.id.imageButtonShare);
        imageButtonMore=(ImageButton) findViewById(R.id.imageButtonMore);
        textViewDate=(TextView) findViewById(R.id.textViewDate);

        Intent fullScreenIntent=getIntent();
        String data=fullScreenIntent.getStringExtra("img");
        final String name=fullScreenIntent.getStringExtra("name");
        String date="Date:"+name.substring(0,10);
        textViewDate.setText(date);
        imageViewFull.setImageURI(Uri.parse(data));
        //click vào ảnh đẻ hiện các nút
        imageViewFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!show){
                    imageButtonDelete.setVisibility(View.VISIBLE);
                    imageButtonEdit.setVisibility(View.VISIBLE);
                    imageButtonShare.setVisibility(View.VISIBLE);
                    imageButtonMore.setVisibility(View.VISIBLE);
                    textViewDate.setVisibility(View.VISIBLE);
                    show=true;
                }
                else{
                    imageButtonDelete.setVisibility(View.INVISIBLE);
                    imageButtonShare.setVisibility(View.INVISIBLE);
                    imageButtonEdit.setVisibility(View.INVISIBLE);
                    imageButtonMore.setVisibility(View.INVISIBLE);
                    textViewDate.setVisibility(View.INVISIBLE);
                    show=false;
                }
            }
        });
        //chọn nút xóa
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(FullScreenActivity.this);
                alertDialog.setTitle("Xác nhận");
                alertDialog.setMessage("Bạn có chắc chắn muốn xóa ảnh này không?");
                alertDialog.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent returnIntent=new Intent(FullScreenActivity.this,MainActivity.class);
                        returnIntent.putExtra("namedel",name);
                        setResult(RESULT_OK,returnIntent);
                        finish();
                    }
                });
                alertDialog.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        });
        //button more
        imageButtonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(FullScreenActivity.this,imageButtonMore);
                popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menuWallpaper://set wallpaper
                            {
                                bitmapDrawable=(BitmapDrawable) imageViewFull.getDrawable();
                                bitmap1=bitmapDrawable.getBitmap();
                                displayMetrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                width = displayMetrics.widthPixels;
                                height = displayMetrics.heightPixels;
                                bitmap2 = Bitmap.createScaledBitmap(bitmap1, width, height, false);
                                wallpaperManager = WallpaperManager.getInstance(FullScreenActivity.this);
                                try {
                                    wallpaperManager.setBitmap(bitmap2);
                                    wallpaperManager.suggestDesiredDimensions(width, height);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case R.id.menu_rotate_right:
                            {
                                imageViewFull.setRotation(imageViewFull.getRotation()+90);
                                break;
                            }
                            case R.id.menu_rotate_left:
                            {
                                imageViewFull.setRotation(imageViewFull.getRotation()-90);
                                break;
                            }
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }



    }

