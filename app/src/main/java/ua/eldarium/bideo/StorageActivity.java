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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StorageActivity extends ListActivity {

    private Bitmap errorSign;
    ArrayList<VideoInfo> foundVids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        Log.d("text", "Set content view, exec async now");
        new VideoGetter().execute();
        errorSign = BitmapFactory.decodeResource(getResources(), R.drawable.error_sign);
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
            AlertDialog.Builder dialogBuilder;

            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

                final VideoInfo selectedInfo = (VideoInfo) parent.getItemAtPosition(position);
                if (dialogBuilder == null) {
                    dialogBuilder = new AlertDialog.Builder(StorageActivity.this);
                    dialogBuilder.setTitle(R.string.dialog_remove_tite);
                    dialogBuilder.setMessage(R.string.dialog_remove_message);
                    dialogBuilder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            foundVids.remove(position);
                            ((VideoInfoAdapter) getListAdapter()).remove(selectedInfo);
                        }
                    });
                    dialogBuilder.setNegativeButton(R.string.button_no, new DialogInterface
                            .OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialogBuilder.setCancelable(true);
                }
                dialogBuilder.show();
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
        private ContentResolver finder;
        private ProgressBar pbar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("text", "on pre exec async now");
            pbar = (ProgressBar) findViewById(R.id.progbar);
            pbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("text", "exec async now");

            foundVids = new ArrayList<>();
            finder = getContentResolver();
            if (!isStoragePermissionGranted()) {
                foundVids.add(new VideoInfo(getString(R.string.empty_video), errorSign, null));
                return null;
            }
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
                foundVids.add(new VideoInfo("No Videos Available", errorSign, null));
            vCursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            VideoInfoAdapter videoAdapter = new VideoInfoAdapter(StorageActivity.this, foundVids);
            setListAdapter(videoAdapter);

            pbar.setVisibility(View.GONE);
        }
    }

}
