package ua.eldarium.bideo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseActivity extends ListActivity {
    private DatabaseWorker dbWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bitmap error_sign = BitmapFactory.decodeResource(getResources(), R.drawable.error_sign);

        dbWorker = new DatabaseWorker(this);
        dbWorker.open();
        dbWorker.addRec(new VideoInfo("No Videos Available", error_sign, null));

        String[] from = new String[]{DatabaseWorker.VIDEO_NAME_COLUMN, DatabaseWorker.THUMB_COLUMN};
        int[] to = new int[]{R.id.video_name, R.id.video_thumb};

        setContentView(R.layout.activity_database);
        final SimpleCursorAdapter scAdapter = new SimpleCursorAdapter(this, R.layout.layout_white, null,
                from, to, 0);
        setListAdapter(scAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, 5);
                Uri data = intent.getData();
                String path = data.getPath();
                String name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images
                        .Thumbnails.MINI_KIND);
                VideoInfo info = new VideoInfo(name, thumb, path);
                dbWorker.addRec(info);
                scAdapter.notifyDataSetChanged();
            }
        });

        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            VideoInfo selectedVideo;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedVideo = (VideoInfo) parent.getItemAtPosition(position);
                if (selectedVideo.path == null) {
                    Toast.makeText(DatabaseActivity.this, "Wrong video", Toast.LENGTH_LONG).show();
                    return;
                }
                Uri path = Uri.parse(selectedVideo.path);
                Intent VideoViewIntent = new Intent(Intent.ACTION_VIEW, path);
                VideoViewIntent.setDataAndType(path, "video/*");
                PackageManager packageManager = getPackageManager();
                List activities = packageManager.queryIntentActivities(VideoViewIntent, 0);
                boolean isIntentSafe = activities.size() > 0;
                if (isIntentSafe)
                    startActivity(VideoViewIntent);
                else
                    Toast.makeText(getApplicationContext(), "Нет проигрывателей!", Toast.LENGTH_LONG).show();

                Toast.makeText(getApplicationContext(), "Аж " + activities.size() + " проигрывателей!", Toast.LENGTH_LONG).show();

            }
        };
        AdapterView.OnItemLongClickListener longListener = new AdapterView.OnItemLongClickListener() {
            AlertDialog.Builder ad;

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                final VideoInfo selectedInfo = (VideoInfo) parent.getItemAtPosition(position);
                if (ad == null) {
                    ad = new AlertDialog.Builder(DatabaseActivity.this);
                    ad.setTitle("Remove video?");
                    ad.setMessage("Do you want to remove this video from list?");
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbWorker.delRec(position);
                            scAdapter.notifyDataSetChanged();
                        }
                    });
                    ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    ad.setCancelable(true);
                }
                ad.show();
                return false;
            }
        };
        getListView().setOnItemClickListener(itemListener);
        getListView().setOnItemLongClickListener(longListener);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Asking for permission", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation

            return true;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbWorker.close();
    }

    private VideoInfo getModel(int position) {
        return (((VideoInfoAdapter) getListAdapter()).getItem(position));
    }

    public class VideoInfoAdapter extends ArrayAdapter<VideoInfo> {
        private LayoutInflater mInflater;

        VideoInfoAdapter(ArrayList<VideoInfo> list) {
            super(DatabaseActivity.this, R.layout.layout_white, list);
            mInflater = LayoutInflater.from(DatabaseActivity.this);
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
            VideoInfo info = getModel(position);
            holder.thumbView.setImageBitmap(info.thumbnail);
            holder.nameView.setText(info.name);
            return row;
        }

        class ViewHolder {
            public ImageView thumbView;
            public TextView nameView;
        }
    }

}
