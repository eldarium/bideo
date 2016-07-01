package ua.eldarium.bideo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by Dan on 01.07.2016.
 */
public class DatabaseWorker {


    // имя базы данных
    private static final String DATABASE_NAME = "vidsDatabase.db";
    // версия базы данных
    private static final int DATABASE_VERSION = 1;
    // имя таблицы
    private static final String DATABASE_TABLE = "videos";
    // названия столбцов
    public static final String VIDEO_NAME_COLUMN = "video_name";
    public static final String THUMB_COLUMN = "thumbnail";
    public static final String PATH_COLUMN = "path";


    private final Context mCtx;


    private DatabaseHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DatabaseWorker(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DatabaseHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper != null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(mDBHelper.getDatabaseName(), null, null, null, null, null, null);
    }

    // добавить запись в DB_TABLE
    public void addRec(String name, byte[] img, String path) {
        ContentValues cv = new ContentValues();
        cv.put(VIDEO_NAME_COLUMN, name);
        cv.put(THUMB_COLUMN, img);
        cv.put(PATH_COLUMN, path);
        mDB.insert(DATABASE_TABLE, null, cv);
    }

    public void addRec(String name, Bitmap img, String path) {
        ContentValues cv = new ContentValues();
        cv.put(VIDEO_NAME_COLUMN, name);
        cv.put(THUMB_COLUMN, DbBitmapUtility.getBytes(img));
        cv.put(PATH_COLUMN, path);
        mDB.insert(DATABASE_TABLE, null, cv);
    }


    public void addRec(VideoInfo video) {
        ContentValues cv = new ContentValues();
        cv.put(VIDEO_NAME_COLUMN, video.name);
        cv.put(THUMB_COLUMN, DbBitmapUtility.getBytes(video.thumbnail));
        cv.put(PATH_COLUMN, video.path);
        mDB.insert(DATABASE_TABLE, null, cv);
    }

    // удалить запись из DB_TABLE
    public void delRec(long id) {
        mDB.delete(DATABASE_TABLE, DatabaseHelper._ID + " = " + id, null);
    }

    public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {


        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        private static final String DATABASE_CREATE_SCRIPT = "create table "
                + DATABASE_TABLE + " (" + BaseColumns._ID
                + " integer primary key autoincrement, " + VIDEO_NAME_COLUMN
                + " text not null, " + THUMB_COLUMN + " blob, " + PATH_COLUMN
                + " text);";

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_SCRIPT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Запишем в журнал
            Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

            // Удаляем старую таблицу и создаём новую
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            // Создаём новую таблицу
            onCreate(db);
        }
    }

}
