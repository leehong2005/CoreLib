package com.lee.sdk.test.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class ZipUtil {
    public interface IZipActionListener {
        public static final int ACTION_ZIP = 0x01;
        public static final int ACTION_UNZIP = 0x02;

        public void onZipStart(int action, String zipFile);

        public void onZipFailed(int action, String zipFile, int errorCode);

        public void onZipFinish(int action, String zipFile);
    }

    static final int BUFFER_SIZE = 1024;
    static final int MSG_ZIP_START = 0x01;
    static final int MSG_ZIP_FAILED = 0x02;
    static final int MSG_ZIP_FINISH = 0x03;

    long mChecksum = 0;
    boolean mCancel = false;
    IZipActionListener mListener = null;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (null == mListener) {
                return;
            }

            switch (msg.what) {
            case MSG_ZIP_START:
                mListener.onZipStart(msg.arg1, (String) msg.obj);
                break;

            case MSG_ZIP_FAILED:
                mListener.onZipFailed(msg.arg1, (String) msg.obj, msg.arg2);
                break;

            case MSG_ZIP_FINISH:
                mListener.onZipFinish(msg.arg1, (String) msg.obj);
                break;
            }
        }
    };

    public void onZipStart(int action, String zipFile) {
        mHandler.obtainMessage(MSG_ZIP_START, IZipActionListener.ACTION_UNZIP, 0, zipFile).sendToTarget();
    }

    public void onZipFailed(int action, String zipFile, int errorCode) {
        mHandler.obtainMessage(MSG_ZIP_FAILED, errorCode, -1, zipFile).sendToTarget();
    }

    public void onZipFinish(int action, String zipFile) {

    }

    public void cancel() {
        mCancel = true;
    }

    public boolean hasCanceled() {
        return mCancel;
    }

    public long getChecksum() {
        return mChecksum;
    }

    public void setZipActionListener(IZipActionListener listener) {
        mListener = listener;
    }

    public long zip(ArrayList<String> files, String outzipFile) {
        if (null == files || 0 == files.size()) {
            return 0;
        }

        ZipOutputStream zos = null;
        CheckedOutputStream cos = null;

        try {
            cos = new CheckedOutputStream(new FileOutputStream(outzipFile), new CRC32());
            zos = new ZipOutputStream(new BufferedOutputStream(cos));

            for (String strFile : files) {
                mHandler.obtainMessage(MSG_ZIP_START, IZipActionListener.ACTION_ZIP, 0, strFile).sendToTarget();

                boolean succeed = doZip(new File(strFile), zos);

                mHandler.obtainMessage(succeed ? MSG_ZIP_FINISH : MSG_ZIP_FAILED, IZipActionListener.ACTION_ZIP, 0,
                        strFile).sendToTarget();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(zos);
        }

        mChecksum = cos.getChecksum().getValue();

        return mChecksum;
    }

    public boolean zipAsync(ArrayList<String> files, String outzipFile) {
        final ArrayList<String> tempFiles = files;
        final String tempOutzipFile = outzipFile;

        new Thread(null, new Runnable() {
            @Override
            public void run() {
                try {
                    zip(tempFiles, tempOutzipFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },

        "Zip_Thread").start();

        return true;
    }

    public boolean unZip(String zipFilePath, String outputDirName) {
        // The ZIP file does not exist.
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            return false;
        }

        // If the destination directory does not exist or fails to create it.
        if (!createDirectory(outputDirName)) {
            return false;
        }

        // Start.
        onZipStart(IZipActionListener.ACTION_UNZIP, zipFilePath);

        boolean succeed = false;

        FileInputStream in = null;

        try {
            in = new FileInputStream(zipFile);
            succeed = doUnZip(in, outputDirName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(in);
        }

        if (succeed) {
            // Finish.
            onZipFinish(IZipActionListener.ACTION_UNZIP, zipFilePath);
        } else {
            // Fail.
            onZipFailed(IZipActionListener.ACTION_UNZIP, zipFilePath, -1);
        }

        return false;
    }

    public boolean unZipAsync(String zipFilePath, String outputDirName) {
        final String zipFilePathTemp = zipFilePath;
        final String destDirTemp = outputDirName;

        new Thread(null, new Runnable() {
            @Override
            public void run() {
                try {
                    unZip(zipFilePathTemp, destDirTemp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },

        "UnZip_Thread").start();

        return true;
    }

    protected boolean doZip(File file, ZipOutputStream zos) throws IOException {
        if (!file.exists()) {
            return false;
        }

        if (hasCanceled()) {
            return true;
        }

        if (file.isDirectory()) {
            // If you want to add a directory, the path should ends with "/";
            ZipEntry entry = new ZipEntry(file.getAbsolutePath() + File.separator);
            entry.setExtra(entry.getName().getBytes());
            zos.putNextEntry(entry);
            File[] fileList = file.listFiles();
            if (null != fileList) {
                for (File aFile : fileList) {
                    if (hasCanceled()) {
                        break;
                    }

                    doZip(aFile, zos);
                }
            }
            zos.closeEntry();
        } else {
            ZipEntry entry = new ZipEntry(file.getAbsolutePath());
            entry.setExtra(entry.getName().getBytes());
            zos.putNextEntry(entry);
            byte bytes[] = new byte[BUFFER_SIZE];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()), BUFFER_SIZE);

            int count = 0;
            while ((count = in.read(bytes)) != -1) {
                zos.write(bytes, 0, count);
            }

            in.close();
            zos.closeEntry();
        }

        return true;
    }

    protected boolean doUnZip(InputStream is, String outputDirName) throws IOException {
        boolean succeed = true;

        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(is));
        ZipEntry ze = null;
        File outputDir = new File(outputDirName);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        while ((ze = zin.getNextEntry()) != null) {
            if (hasCanceled()) {
                break;
            }

            // If the entry is directory.
            if (ze.isDirectory()) {
                createDirectory(outputDirName, ze.getName());
                continue;
            }

            if (!writeFile(zin, outputDir, ze.getName())) {
                succeed = false;
                break;
            }
        }

        zin.close();

        return succeed;
    }

    public long readChecksum(String fileName) {
        long checksum = 0;

        try {
            FileInputStream fis = new FileInputStream(fileName);
            CheckedInputStream cis = new CheckedInputStream(new BufferedInputStream(fis), new CRC32());
            byte buffer[] = new byte[1024];
            while ((cis.read(buffer)) != -1) {
            }

            fis.close();
            cis.close();

            checksum = cis.getChecksum().getValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return checksum;
    }

    public static byte[] long2bytes(long num) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (num >>> (56 - i * 8));
        }
        return b;
    }

    public static long bytes2long(byte[] b) {
        int mask = 0xff;
        int temp = 0;
        int res = 0;
        for (int i = 0; i < 8; i++) {
            res <<= 8;
            temp = b[i] & mask;
            res |= temp;
        }
        return res;
    }

    private void closeStream(InputStream in) {
        try {
            if (null != in) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeStream(OutputStream os) {
        try {
            if (null != os) {
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createDirectory(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.exists());
    }

    private boolean createDirectory(String dirPath, String name) {
        File file = new File(dirPath, name);
        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.exists());
    }

    private boolean writeFile(ZipInputStream zin, File outputDir, String name) {
        boolean succeed = false;

        try {
            // Make sure the target file directory exists.
            File file = new File(outputDir, name);
            File parentFile = file.getParentFile();
            if (null != parentFile && !parentFile.exists()) {
                parentFile.mkdirs();
            }

            FileOutputStream fout = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            while ((count = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                // Check user has canceled.
                if (hasCanceled()) {
                    break;
                }

                fout.write(buffer, 0, count);
            }

            zin.closeEntry();
            fout.flush();
            fout.close();
            succeed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return succeed;
    }
}
