package net.ut11.ccmp.lib.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.jakewharton.disklrucache.DiskLruCache;

import net.ut11.ccmp.api.domain.DeviceAttachmentResponse;
import net.ut11.ccmp.lib.LibApp;
import net.ut11.ccmp.lib.db.Attachment;
import net.ut11.ccmp.lib.db.AttachmentsDb;
import net.ut11.ccmp.lib.net.api.endpoint.DeviceEndpoint;
import net.ut11.ccmp.lib.net.api.response.ApiException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dwohlmuth on 25.03.14.
 */
public class AttachmentCache {

    public static final String INTENT_ACTION_ATTACHMENT_DOWNLOADED = "net.ut11.ccmp.lib.INTENT_ACTION_ATTACHMENT_DOWNLOADED";
    public static final String INTENT_ACTION_ATTACHMENT_INSERTED = "net.ut11.ccmp.lib.INTENT_ACTION_ATTACHMENT_INSERTED";
    public static final String INTENT_EXTRA_ATTACHMENT_ID = "attachment_id";
    public static final String INTENT_EXTRA_ATTACHMENT_URL = "attachment_url";

    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int APP_VERSION = 1;
    private static final int VALUE_COUNT = 1;
    private static final String CACHE_DIR = "attachments";
    private static final int CACHE_SIZE = 20 * 1024 * 1024;
    private static AttachmentCache instance = null;
    private static final int KEEP_ALIVE_TIME = 90;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private DiskLruCache mDiskCache;
    private ThreadPoolExecutor threadPool;

    public static AttachmentCache getInstance(Context context) {
        if (instance == null) {
            instance = new AttachmentCache(context);
        }

        return instance;
    }

    private AttachmentCache(Context context) {
        try {
            final File diskCacheDir = getDiskCacheDir(context);
            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, CACHE_SIZE);

            final BlockingQueue<Runnable> mDecodeWorkQueue;
            mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();

            threadPool = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,       // Initial pool size
                    NUMBER_OF_CORES,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    mDecodeWorkQueue);

        } catch (IOException e) {
            if (Logger.DEBUG) {
                Logger.debug("Attachment Cache - Couldn't initialize DiskLruCache: " + e.toString());
            }
        }
    }

    private File getDiskCacheDir(Context context) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = context.getExternalCacheDir() != null &&
                (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) ?
                        context.getExternalCacheDir().getPath() :
                        context.getCacheDir() != null ? context.getCacheDir().getPath() : "";

        return new File(cachePath, CACHE_DIR);
    }

    public File getDiskCacheFile(Context context, long attachmentId) {
        return new File(getDiskCacheDir(context), String.valueOf(attachmentId) + ".0");
    }

    public void put(long attachmentId, byte[] data) {
        DiskLruCache.Editor editor = null;
        String key = String.valueOf(attachmentId);

        try {
            editor = mDiskCache.edit(key);
            if ( editor == null ) {
                return;
            }

            if (writeByteArrayToFile(data, editor)) {
                mDiskCache.flush();
                editor.commit();
                if (Logger.DEBUG) {
                    Logger.debug("Attachment Cache - file put on cache - ID: " + key);
                }
            } else {
                editor.abort();
                if (Logger.DEBUG) {
                    Logger.debug("Attachment Cache - ERROR file couldn't be put on cache - ID: " + key);
                }
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.debug("Attachment Cache - ERROR file couldn't be put on cache - ID: " + key);
            }
            try {
                if (editor != null ) {
                    editor.abort();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void deleteAttachmentFromCache(long attachmentId) {
        String key = String.valueOf(attachmentId);
        try {
            mDiskCache.remove(key);
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.debug("Attachment Cache - ERROR couldn't delete file from Cache: " + e.toString());
            }
        }
    }

    private boolean writeByteArrayToFile(byte[] data, DiskLruCache.Editor editor)
            throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(editor.newOutputStream(0), IO_BUFFER_SIZE);
            out.write(data);

            return true;
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }

    public Bitmap getBitmap(String urlString, long attachmentId) {
        Bitmap bitmap = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            snapshot = mDiskCache.get(String.valueOf(attachmentId));
            if (snapshot == null) {
                downloadFile(urlString, attachmentId);
                return null;
            }
            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                bitmap = BitmapFactory.decodeStream(in);
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.debug("Attachment Cache - Couldn't get image from URL: " + urlString + ", with attachmentId: " + attachmentId + " - " + e.toString() );
            }
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return bitmap;
    }

    public byte[] getFile(String urlString, long attachmentId) {
        DiskLruCache.Snapshot snapshot = null;
        byte[] data = null;

        try {
            snapshot = mDiskCache.get(String.valueOf(attachmentId));
            if (snapshot == null) {
                downloadFile(urlString, attachmentId);
                return null;
            }

            final InputStream in = snapshot.getInputStream(0);
            if (in != null) {
                data = convertInputStreamToByteArray(in);
            }
        } catch (Exception e) {
            if (Logger.DEBUG) {
                Logger.debug("Attachment Cache - Couldn't get file from URL: " + urlString + ", with attachmentId: " + attachmentId + " - " + e.toString() );
            }
        } finally {
            if (snapshot != null) {
                snapshot.close();
            }
        }

        return data;
    }

    private byte[] convertInputStreamToByteArray(InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return buffer.toByteArray();
    }

    public void downloadFile(String urlString, long attachmentId) {
        threadPool.execute(new DownloadFileRunnable(urlString, attachmentId));
    }

    private class DownloadFileRunnable implements Runnable {

        private String urlString;
        private long attachmentId;

        public DownloadFileRunnable(String url, long attachmentId) {
            this.urlString = url;
            this.attachmentId = attachmentId;
        }

        @Override
        public void run() {
            try {
                final URL url = new URL(urlString);
                AttachmentCache cache = AttachmentCache.getInstance(LibApp.getContext());
                if (cache != null) {
                    Attachment attachment = AttachmentsDb.getAttachment(attachmentId);
                    if (attachment != null) {
                        byte[] data = convertInputStreamToByteArray(url.openConnection().getInputStream());
                        if (data != null) {
                            cache.put(attachmentId, data);
                        }

                        Intent intent = new Intent(INTENT_ACTION_ATTACHMENT_DOWNLOADED);
                        intent.putExtra(INTENT_EXTRA_ATTACHMENT_ID, attachmentId);
                        LibApp.getContext().sendBroadcast(intent);
                    }
                }
            } catch (final Exception e) {
                if (Logger.DEBUG) {
                    Logger.debug("Attachment Cache - Download Error: " + e);
                }
            }
        }
    }

    public static class GetAttachmentRunnable implements Runnable {

        private long attachmentId;

        public GetAttachmentRunnable(long attachmentId) {
            this.attachmentId = attachmentId;
        }

        @Override
        public void run() {
            DeviceAttachmentResponse response;
            try {
                response = DeviceEndpoint.getAttachment((int) attachmentId);
                AttachmentsDb.insert(response.getId(), response.getUri(), response.getName(), response.getMimeType(), response.getSize());
                Intent intent = new Intent(INTENT_ACTION_ATTACHMENT_INSERTED);
                intent.putExtra(INTENT_EXTRA_ATTACHMENT_ID, attachmentId);
                LibApp.getContext().sendBroadcast(intent);
            } catch (ApiException e) {
                if (Logger.DEBUG) {
                    Logger.debug("Get attachment failed: " + e.toString());
                }
            }
        }
    }
}
