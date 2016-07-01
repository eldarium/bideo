package ua.eldarium.bideo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StorageActivity extends ListActivity {

    public ArrayList<VideoInfo> foundVids;
    private ContentResolver finder;
    private Bitmap error_sign;
    private ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        error_sign = BitmapFactory.decodeResource(getResources(), R.drawable.error_sign);

        pbar = (ProgressBar) findViewById(R.id.progbar);

        foundVids = new ArrayList<>();
        finder = getContentResolver();

        if (isStoragePermissionGranted())
            new VideoGetter().execute();
        else {
            Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
            foundVids.add(new VideoInfo("No Videos Available", error_sign, null));
        }

        final VideoInfoAdapter videoAdapter = new VideoInfoAdapter(foundVids);
        setListAdapter(videoAdapter);

        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            VideoInfo selectedVideo;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedVideo = (VideoInfo) parent.getItemAtPosition(position);
                if (selectedVideo.path == null) {
                    Toast.makeText(StorageActivity.this, "Wrong video", Toast.LENGTH_LONG).show();
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
                    ad = new AlertDialog.Builder(StorageActivity.this);
                    ad.setTitle("Remove video?");
                    ad.setMessage("Do you want to remove this video from list?");
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            foundVids.remove(position);
                            videoAdapter.remove(selectedInfo);
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


    public class VideoGetter extends AsyncTask<Void, Void, Void> {

        Cursor vCursor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Uri vidUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Video.VideoColumns.DATA};
            vCursor = finder.query(vidUri, projection, null, null, null);
            if (vCursor != null) {
                while (vCursor.moveToNext()) {
                    Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(vCursor.getString(0),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    String name = vCursor.getString(0);
                    name = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
                    foundVids.add(new VideoInfo(name, thumbnail, vCursor.getString(0)));
                }
            }
            if (foundVids.size() == 0)
                foundVids.add(new VideoInfo("No Videos Available", error_sign, null));
            vCursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            pbar.setVisibility(View.GONE);
        }
    }

    private VideoInfo getModel(int position) {
        return (((VideoInfoAdapter) getListAdapter()).getItem(position));
    }

    public class VideoInfoAdapter extends ArrayAdapter<VideoInfo> {
        private LayoutInflater mInflater;

        VideoInfoAdapter(ArrayList<VideoInfo> list) {
            super(StorageActivity.this, R.layout.layout_white, list);
            mInflater = LayoutInflater.from(StorageActivity.this);
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