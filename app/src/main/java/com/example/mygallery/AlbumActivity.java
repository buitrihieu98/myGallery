package com.example.mygallery;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class AlbumActivity extends AppCompatActivity {
    ListView listViewAlbum;
    GridView gridView;
    ArrayList<File> albumList,list;
    AdapterAlbum adapterAlbum;
    final int REQUEST_CODE_DEL=12345;
    gridAdapter adapter=new gridAdapter();


    File myGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        setTitle("Albums");
        listViewAlbum = (ListView) findViewById(R.id.listView);
        gridView=(GridView) findViewById(R.id.gridViewwwww);
        albumList = new ArrayList<>();
        File storageDic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        myGallery = new File(storageDic.getAbsolutePath() + "/myGallery");
        albumList = albumReader(myGallery);
        adapterAlbum = new AdapterAlbum(AlbumActivity.this, R.layout.activity_album, albumList);
        listViewAlbum.setAdapter(adapterAlbum);
        listViewAlbum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list=imageReader(albumList.get(position));
                gridView.setAdapter(adapter);
                listViewAlbum.setVisibility(View.INVISIBLE);
                gridView.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent fullScreenIntent = new Intent(AlbumActivity.this,FullScreenActivity.class);
                        fullScreenIntent.putExtra("img",list.get(position).toString());
                        fullScreenIntent.putExtra("name",list.get(position).getName());
                        startActivityForResult(fullScreenIntent,REQUEST_CODE_DEL);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==REQUEST_CODE_DEL&&resultCode==RESULT_OK && data!=null){
            String nameDel = data.getStringExtra("namedel");
            deleteImage(myGallery, nameDel);
            list = imageReader(myGallery);
            gridView.setAdapter(adapter);
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void deleteImage(File b,String name){
        String delFilePath=b.getAbsolutePath()+"/"+name;
        File delFile= new File(delFilePath);
        boolean delYet=delFile.delete();
    }

    private ArrayList<File> imageReader(File b) {
        ArrayList<File> a = new ArrayList<>();
        File[] files = b.listFiles();
//        if(files !=null){
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    a.addAll(imageReader(file));
//                } else {
//                    if (file.getName().endsWith(".jpg")) {
//                        a.add(file);
//                        }
//                    }
//                }
//            }
        for (File file : files) {
            if (file.getName().endsWith(".jpg")) {
                a.add(file);
            }
        }
        Collections.reverse(a);
        return a;
    }

    public class gridAdapter extends BaseAdapter {
        ArrayList<File> filterList=new ArrayList<>();
        ArrayList<File> filters = new ArrayList<>();
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }


        @Override
        public long getItemId(int position) {
            return 0;
        }

        private class ViewHolder{
            ImageView imageViewHinhHolder;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView =inflater.inflate(R.layout.row_layout,null);
                holder=new ViewHolder();
                holder.imageViewHinhHolder= convertView.findViewById(R.id.ImageViewHinh);
                convertView.setTag(holder);
            }
            else{
                holder=(ViewHolder) convertView.getTag();
            }
            holder.imageViewHinhHolder.setImageURI(Uri.parse(list.get(position).toString()));
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_album_add :
            {
                Dialog dialog= new Dialog(AlbumActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog);
                final EditText editTextTenAlbum=(EditText) dialog.findViewById(R.id.editTextTenAlbum);
                Button btnTao;
                btnTao=(Button) dialog.findViewById(R.id.buttonTao);
                btnTao.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String albumName= editTextTenAlbum.getText().toString();
                        File album= new File(myGallery.getAbsolutePath(),albumName);
                        boolean sucess=album.mkdirs();
                        if(sucess)
                            Toast.makeText(AlbumActivity.this,"Album "+ albumName+" created", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(AlbumActivity.this,"Failed to create new album", Toast.LENGTH_SHORT).show();
                        albumList = albumReader(myGallery);
                    }
                });
                dialog.show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList<File> albumReader(File b) {
        ArrayList<File> a = new ArrayList<>();
        File[] files = b.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                a.add(file);
            }
        }
        Collections.reverse(a);
        return a;
    }
}