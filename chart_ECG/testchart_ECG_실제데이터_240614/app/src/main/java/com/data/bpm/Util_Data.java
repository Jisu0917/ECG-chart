package com.data.bpm;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

public class Util_Data {

    /**
     * @param date
     * @return
     */
    @SuppressWarnings("deprecation")
    public static byte[] getByteDate(Date date) {
        byte[] byDate = new byte[6];
        Arrays.fill(byDate, (byte) 0);

        try {
            byDate[0] = (byte) (date.getYear() - 100);
            byDate[1] = (byte) (date.getMonth() + 1);
            byDate[2] = (byte) date.getDate();
            byDate[3] = (byte) date.getHours();
            byDate[4] = (byte) date.getMinutes();
            byDate[5] = (byte) date.getSeconds();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return byDate;
    }

    /**
     * @param b
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int getUnsignedByte(byte b) {
        int nValue = 0;

        if (b < 0) {
            nValue = (int) b + 256;
        } else {
            nValue = (int) b;
        }

        return nValue;
    }

    /**
     * @param
     * @return
     */
    @SuppressWarnings("deprecation")
    public static byte[] getLoginByteDate() {
        Date date = new Date();
        //reserved +12
        byte[] byDate = new byte[19];
        Arrays.fill(byDate, (byte) 0);

        try {
            byDate[0] = (byte) (date.getYear() / 256);
            byDate[1] = (byte) (date.getYear() % 256);
            byDate[2] = (byte) (date.getMonth() + 1);
            byDate[3] = (byte) date.getDate();
            byDate[4] = (byte) date.getHours();
            byDate[5] = (byte) date.getMinutes();
            byDate[6] = (byte) date.getSeconds();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return byDate;
    }

    /**
     * @return
     */
    public static byte[] getByteDateNow() {
        Date now = new Date();
        return getByteDate(now);
    }

    @SuppressWarnings("deprecation")
    public static Date getByteToDate(byte[] buf, int nIdx) {
        Date date = null;
        try {
            Date d = new Date(buf[nIdx + 0] + 100, buf[nIdx + 1] - 1, buf[nIdx + 2], buf[nIdx + 3], buf[nIdx + 4], buf[nIdx + 5]);
            date = d;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static byte[] getLittleEndian(int Butter) {
        byte[] buf = new byte[4];

        buf[3] = (byte) ((Butter >>> 24) & 0xFF);
        buf[2] = (byte) ((Butter >>> 16) & 0xFF);
        buf[1] = (byte) ((Butter >>> 8) & 0xFF);
        buf[0] = (byte) ((Butter >>> 0) & 0xFF);

        return buf;
    }

    public static int getIntBigEndian(byte[] buffer, int nIdx, int nByCount) {
        int nRtnValue = 0;

        do {
            nByCount--;
            nRtnValue += ((buffer[nIdx] & 0x000000FF) << (8 * nByCount));
            nIdx++;
        } while (nByCount > 0);

        return nRtnValue;
    }

    public static int getIntLittleEndian(byte[] buffer, int nIdx, int nByCount) {
        int nRtnValue = 0;
        int nInc = 0;

        do {
            nRtnValue += ((buffer[nIdx] & 0x000000FF) << (8 * nInc));
            nIdx++;
            nInc++;
        } while (nInc < nByCount);

        return nRtnValue;
    }


    public static int bytesToInt(byte[] bytes, int nIdx) {
        int result = (int) bytes[nIdx + 1] & 0xFF;
        result |= (int) bytes[nIdx + 0] << 8 & 0xFF00;
        return result;
    }


    public static void LogToHexString(String strTag, byte[] buf, int nLen) {
        String strRcvData = "Hex [" + String.valueOf(nLen) + "] ";
        for (int i = 0; i < nLen; ++i) {
            strRcvData += String.format("%x ", buf[i]);
            //strRcvData += Integer.toHexString(buf[i] & 0x000000FF) + ", ";
        }
        Log.d(strTag, strRcvData);
    }

    public static String ToHexString(byte[] buf, int nLen) {
        String strRcvData = "";
        for (int i = 0; i < nLen; ++i) {
            strRcvData += String.format("%02x ", buf[i]);
            //strRcvData += Integer.toHexString(buf[i] & 0x000000FF) + ", ";
        }
        return strRcvData;
    }

    /**
     *
     * @param fileName
     * @param content
     * @throws IOException , FileNotFoundException , Exception
     */
    public static void writeSDcard(String fileName, String content) throws IOException, FileNotFoundException, Exception {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
        BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file, true));
        buf.write(content.getBytes());

        buf.close();


    }

    public static void copyDbFile(Context context, String dbname) {
        Log.i(context.getClass().getName(), "copyDbFile();");
        AssetManager am = context.getAssets();


        //File toFile = new File(Environment.getDataDirectory() + "/data/"+context.getPackageName()+"/databases/"+dbname);
        File fromFile = new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + dbname);
        File toFile = new File(Environment.getExternalStorageDirectory() + "/" + dbname);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
//		BufferedInputStream bis = null;

        try {
            // checkFile("SSM_TEMP.db", "SSM_TEMP.db", db);
            //InputStream is = am.open("temple_0812.db");

            InputStream is = new FileInputStream(fromFile);

            //	 InputStream bis = new FileInputStream(is);
            //bis = new BufferedInputStream(is);


            //if (!toFile.exists()) {
            toFile.createNewFile();

            fos = new FileOutputStream(toFile);
            bos = new BufferedOutputStream(fos);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();

            fos.close();
            bos.close();
            is.close();
            //	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean InsertDbFile(Context context, String dbname) {
        boolean bRtn = false;
        Log.i(context.getClass().getName(), "copyDbFile();");
        AssetManager am = context.getAssets();


        //File toFile = new File(Environment.getDataDirectory() + "/data/"+context.getPackageName()+"/databases/"+dbname);
        File toFile = new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + dbname);
        //	File fromFile = new File(Environment.getExternalStorageDirectory() + "/"+dbname);
        File fromFile = new File("/storage/usb0" + "/" + dbname);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
//		BufferedInputStream bis = null;

        try {
            // checkFile("SSM_TEMP.db", "SSM_TEMP.db", db);
            //InputStream is = am.open("temple_0812.db");

            InputStream is = new FileInputStream(fromFile);

            //	 InputStream bis = new FileInputStream(is);
            //bis = new BufferedInputStream(is);


            if (!toFile.exists()) {
                toFile.createNewFile();
            }
            fos = new FileOutputStream(toFile);
            bos = new BufferedOutputStream(fos);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();

            fos.close();
            bos.close();
            is.close();
            bRtn = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bRtn;
    }

    public static boolean initDbFile(Context context, String dbname) {
        boolean bRtn = false;
        Log.i(context.getClass().getName(), "copyDbFile();");
        AssetManager am = context.getAssets();


        //File toFile = new File(Environment.getDataDirectory() + "/data/"+context.getPackageName()+"/databases/"+dbname);
        File toFile = new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + dbname);
        //	File fromFile = new File(Environment.getExternalStorageDirectory() + "/"+dbname);
        //File fromFile = new File("/storage/usb0" + "/" + dbname);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
//		BufferedInputStream bis = null;

        try {
            // checkFile("SSM_TEMP.db", "SSM_TEMP.db", db);
            InputStream is = am.open(dbname);

            //InputStream is = new FileInputStream(fromFile);

            //	 InputStream bis = new FileInputStream(is);
            //bis = new BufferedInputStream(is);


            /*if (!toFile.exists()) {
                toFile.createNewFile();
            }*/

            fos = new FileOutputStream(toFile);
            bos = new BufferedOutputStream(fos);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();

            fos.close();
            bos.close();
            is.close();
            bRtn = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bRtn;
    }


    public static boolean saveDbFile(Context context, String dbname) {
        boolean bRtn = false;
        Log.i(context.getClass().getName(), "copyDbFile();");
        AssetManager am = context.getAssets();

        File fromFile = new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + dbname);

        File toFile = new File("/storage/usb0" + "/" + dbname);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
//		BufferedInputStream bis = null;

        try {


            InputStream is = new FileInputStream(fromFile);

            //	 InputStream bis = new FileInputStream(is);
            //bis = new BufferedInputStream(is);


            if (!toFile.exists()) {
                toFile.createNewFile();
            }

            fos = new FileOutputStream(toFile);
            bos = new BufferedOutputStream(fos);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();

            fos.close();
            bos.close();
            is.close();
            bRtn = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bRtn;
    }

    public static boolean loadDbFile(Context context, String dbname) {
        boolean bRtn = false;
        Log.i(context.getClass().getName(), "copyDbFile();");
        AssetManager am = context.getAssets();


        //File toFile = new File(Environment.getDataDirectory() + "/data/"+context.getPackageName()+"/databases/"+dbname);
        File toFile = new File(Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/" + dbname);
        //	File fromFile = new File(Environment.getExternalStorageDirectory() + "/"+dbname);
        File fromFile = new File("/storage/usb0" + "/" + dbname);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
//		BufferedInputStream bis = null;

        try {
            // checkFile("SSM_TEMP.db", "SSM_TEMP.db", db);
            //InputStream is = am.open("roadinfo.db");

            InputStream is = new FileInputStream(fromFile);

            //	 InputStream bis = new FileInputStream(is);
            //bis = new BufferedInputStream(is);


            if (!toFile.exists()) {
                toFile.createNewFile();
            }

            fos = new FileOutputStream(toFile);
            bos = new BufferedOutputStream(fos);

            int read = -1;
            byte[] buffer = new byte[1024];
            while ((read = is.read(buffer, 0, 1024)) != -1) {
                bos.write(buffer, 0, read);
            }
            bos.flush();

            fos.close();
            bos.close();
            is.close();
            bRtn = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return bRtn;
    }

    public static double calDistance(double lat1, double lon1, double lat2, double lon2) {

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;
        dist = dist * 1000.0;

        return dist;
    }

    private static double deg2rad(double deg) {
        return (double) (deg * Math.PI / (double) 180d);
    }

    private static double rad2deg(double rad) {
        return (double) (rad * (double) 180d / Math.PI);
    }


}
