package com.ffcc66.diary.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;


public class FileUtils {


    /**
     * 根据图片名称获取资源id,
     * @param imageName
     * @param context
     * @return
     */
    public static int getResource(String imageName,String directory,Context context){
        Context ctx=context;
        int resId = context.getResources().getIdentifier(imageName, directory, ctx.getPackageName());
        return resId;
    }


    /**
     * 根据文件路径拷贝文件
     * @param src 源文件
     * @param destPath 目标文件路径
     * @return boolean 成功true、失败false
     */
    public static boolean copyFile(File src, String destPath, String newFileName) {
        boolean result = false;
        if ((src == null) || !src.exists() || (destPath== null)) {
            return result;
        }
        File destFolder = new File(destPath);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }
        File dest= new File(destPath + newFileName);
        if (dest.exists()) {
            dest.delete(); // delete file
        }
        try {
            dest.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dest).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }


    public static void writeDiaryToTXT(File file, String content, Context context) {
        if (file==null || content==null) {
            return;
        }
        BufferedWriter bufferedWriter = null;
        try {
            Log.e("--------", "writeDiaryToTXT: "+content );
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),"gbk"));
            bufferedWriter.write(content+"\n");
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
