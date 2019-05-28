package com.example.mygallery;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    GridView gridView;
    ArrayList<File> list=new ArrayList<>();
    ArrayList<File> multiChoiceList = new ArrayList<>();
    ArrayList<File> AlbumList=new ArrayList<>();
    int multiChoiceCount=0;
    AdapterAlbum adapterAlbum;
    final int REQUEST_CODE_CAMERA=123;
    final int REQUEST_CODE_DEL=12345;
    final int REQUEST_CODE_READSTORAGE=1234;
    File myGallery;
    gridAdapter gridAdapterHinh=new gridAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView= findViewById(R.id.GridView);
        //ask permission read storage
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_READSTORAGE);
        //click vào từng item của gridview
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent fullScreenIntent = new Intent(MainActivity.this,FullScreenActivity.class);
                fullScreenIntent.putExtra("img",list.get(position).toString());
                fullScreenIntent.putExtra("name",list.get(position).getName());
                startActivityForResult(fullScreenIntent,REQUEST_CODE_DEL);
            }
        });
        //multichoice
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if(checked){
                    multiChoiceCount=multiChoiceCount+1;
                    mode.setTitle(multiChoiceCount+"item(s) selected");
                    multiChoiceList.add(list.get(position));
                }
                else{
                    multiChoiceCount=multiChoiceCount-1;
                    mode.setTitle(multiChoiceCount+"item(s) selected");
                    multiChoiceList.remove(list.get(position));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater= mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_multichoice, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_multichoice_del:
                    {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                        alertDialog.setTitle("Xác nhận");
                        alertDialog.setMessage("Bạn có chắc chắn muốn xóa ảnh này không?");
                        alertDialog.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for(File fdel : multiChoiceList){
                                    fdel.delete();
                                }
                                if(multiChoiceCount==1)
                                    Toast.makeText(MainActivity.this, multiChoiceCount+" item deleted", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(MainActivity.this, multiChoiceCount+" items deleted", Toast.LENGTH_SHORT).show();
                                multiChoiceCount=0;
                                list = imageReader(myGallery);
                                mode.finish();
                            }
                        });
                        alertDialog.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                multiChoiceCount=0;
                                mode.finish();
                            }
                        });
                        alertDialog.show();
                    }
                    case R.id.menu_multichoice_addToAlbum:
                    {
                        Dialog dialogAddtomenu=new Dialog(MainActivity.this);
                        dialogAddtomenu.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialogAddtomenu.setContentView(R.layout.dialog_addtomenu);
                        AlbumList=albumReader(myGallery);
                        adapterAlbum = new AdapterAlbum(MainActivity.this, R.layout.activity_album, AlbumList);
                        final ListView listViewAlbumAnh=(ListView) dialogAddtomenu.findViewById(R.id.listViewalbumanh);
                        listViewAlbumAnh.setAdapter(adapterAlbum);
                        listViewAlbumAnh.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                File dest=new File(AlbumList.get(position).getAbsolutePath());
                                for(File fcopy : multiChoiceList){
                                    copyFile(fcopy.getAbsolutePath(),dest.getAbsolutePath());
                                }
                                Toast.makeText(MainActivity.this, "Added to album", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialogAddtomenu.show();
                    }
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                multiChoiceCount=0;
                multiChoiceList.clear();
            }
        });

    }

    private void copyFile(String absolutePath, String absolutePath1) {
        try {
            File src = new File(absolutePath);
            File dst = new File(absolutePath1, src.getName());
            if (!dst.getParentFile().exists())
                dst.getParentFile().mkdirs();

            if (!dst.exists()) {
                dst.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try {
                source = new FileInputStream(src).getChannel();
                destination = new FileOutputStream(dst).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
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

    //khoi tao thu muc chua anh, load gridview
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CODE_READSTORAGE && grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            File storageDic= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            myGallery = new File(storageDic.getAbsolutePath(), "myGallery");
            if(!myGallery.exists()) {
                myGallery.mkdirs();
            }
            list = imageReader(myGallery);
            gridView.setAdapter(gridAdapterHinh);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //cac activityresult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK && data!=null) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA: {
                    Bitmap bitmapHinhVuaChup = (Bitmap) data.getExtras().get("data");
                    String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());
                    String imageFileName = timeStamp + ".jpg";
                    File imageFile = new File(myGallery, imageFileName);
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                        bitmapHinhVuaChup.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    list = imageReader(myGallery);
                    gridView.setAdapter(gridAdapterHinh);
                    break;
                }
                case REQUEST_CODE_DEL: {
                    String nameDel = data.getStringExtra("namedel");
                    deleteImage(myGallery, nameDel);
                    list = imageReader(myGallery);
                    gridView.setAdapter(gridAdapterHinh);
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //menu khoi tao
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem= menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                gridAdapterHinh.getFilter().filter(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    //chọn các item trên menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menuAdd :
            {
                Intent addImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(addImageIntent, REQUEST_CODE_CAMERA);
                break;
            }
            case R.id.menuAlbum:
            {
                Intent albumIntent = new Intent(MainActivity.this,AlbumActivity.class);
                startActivity(albumIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //gridview xu ly
    public class gridAdapter extends BaseAdapter implements Filterable {
        ArrayList<File> filterList=new ArrayList<>();
        ArrayList<File> filters = new ArrayList<>();
        CustomFilter filter;
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

        @Override
        public Filter getFilter() {
            if(filter==null){
                filter =new CustomFilter();
            }
            return filter;
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

        private class CustomFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {


                if(constraint==null || constraint.length()==0) {
                    list = imageReader(myGallery);
                    filters.clear();
                    filters.addAll(list);
                }
                else {
                    filters.clear();
                    filters.addAll(list);
                    for (int i = filters.size() - 1; i >= 0; i--) {
                        if (!filters.get(i).getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filters.remove(i);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values=new ArrayList<>();
                results.count=filters.size();
                results.values=filters;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //results.values=new ArrayList<>();
                if(results.count==list.size())
                {
                    notifyDataSetInvalidated();
                }
                else{
                    list.clear();
                    list.addAll((Collection<? extends File>) results.values);
                    notifyDataSetChanged();
                }

            }
        }
    }
    //hàm load anh
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
    //hàm xóa ảnh
    private void deleteImage(File b,String name){
        String delFilePath=b.getAbsolutePath()+"/"+name;
        File delFile= new File(delFilePath);
        boolean delYet=delFile.delete();
    }
}


