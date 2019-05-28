package com.example.mygallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdapterAlbum extends BaseAdapter {
    private Context context;
    private int layout;
    private ArrayList<File> albumList;

    public AdapterAlbum(Context context, int layout, ArrayList<File> albumList) {
        this.context = context;
        this.layout = layout;
        this.albumList = albumList;
    }

    @Override
    public int getCount() {
        return albumList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolderr{
        ImageView anhDaiDien;
        TextView textViewAlbumName, textViewCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderr viewHolderr;
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =inflater.inflate(R.layout.row_menu_layout,null);
            viewHolderr=new ViewHolderr();
            viewHolderr.textViewAlbumName= (TextView) convertView.findViewById(R.id.textViewAlbumName);
            viewHolderr.textViewCount=(TextView) convertView.findViewById(R.id.textViewCount);
            viewHolderr.anhDaiDien= convertView.findViewById(R.id.imageViewDaiDien);
            convertView.setTag(viewHolderr);
        }
        else{
            viewHolderr=(ViewHolderr) convertView.getTag();
        }
        File A[]=albumList.get(position).listFiles();
        if(A.length==0) {
            viewHolderr.textViewCount.setText("0 item");
        }
        else {
            String itemCount = A.length + "item(s)";
            viewHolderr.textViewCount.setText(itemCount);
            viewHolderr.anhDaiDien.setImageURI(Uri.parse(A[0].toString()));
        }
        viewHolderr.textViewAlbumName.setText(albumList.get(position).getName());

        return convertView;
    }
}
