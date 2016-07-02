package ua.eldarium.bideo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class VideoInfoAdapter extends ArrayAdapter<VideoInfo> {
    private LayoutInflater mInflater;

    VideoInfoAdapter(Context context, ArrayList<VideoInfo> list) {
        super(context, R.layout.layout_white, list);
        mInflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View row = convertView;
        if (row == null) {
            row = mInflater.inflate(R.layout.layout_white, parent, false);
            holder = new ViewHolder();
            holder.thumbView = (ImageView) row.findViewById(R.id.video_thumb);
            holder.nameView = (TextView) row.findViewById(R.id.video_name);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        VideoInfo info = getItem(position);
        holder.thumbView.setImageBitmap(info.thumbnail);
        holder.nameView.setText(info.name);
        return row;
    }

    class ViewHolder {
        public ImageView thumbView;
        public TextView nameView;
    }
}