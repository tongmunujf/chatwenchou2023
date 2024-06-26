package com.ucas.chat.utils;

import static com.ucas.chat.MyApplication.getContext;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.ucas.chat.R;
import com.ucas.chat.bean.contact.ConstantValue;
import com.ucas.chat.tor.util.AESCrypto;
import com.ucas.chat.tor.util.FilePathUtils;

import org.apaches.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 文件工具类
 * Created by wudeng on 2017/7/24.
 */

public class FileUtils {
    private static String TAG = ConstantValue.TAG_CHAT + "FileUtils";
    /**
     * 递归创建文件夹，从最上层文件夹开始，只要不存在就会新建
     * @param dirPath 文件夹完整路径
     */
    public static void mkDir(String dirPath) {
        String[] dirArray = dirPath.split("/");
        String pathTemp = "";
        for (int i = 1; i < dirArray.length; i++) {
            pathTemp = pathTemp + "/" + dirArray[i];
            File newF = new File(dirArray[0] + pathTemp);
            if (!newF.exists()) {
                newF.mkdir();
            }
        }
    }

    /**
     * 文件转 byte 数组
     * @param file 待转换文件
     * @return byte[]
     * @throws IOException 转换出错
     */
    public static byte[] file2byte(File file) throws IOException {
        byte[] bytes = null;
        if (file != null) {
            InputStream is = new FileInputStream(file);
            int length = (int) file.length();
            if (length >= Integer.MAX_VALUE) {
                Log.e("FileUtils","this file is max ");
                is.close();
                return null;
            }
            bytes = new byte[length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            is.close();
            if (offset < bytes.length) {
                Log.e("FileUtils","file length is error");
                return null;
            }
        }
        return bytes;
    }

    /**
     * 从Uri获取媒体文件，如图片或音频文件
     * @param context 上下文
     * @param uri 从 content provider 或者 onActivityResult 返回的 Uri
     * @return Uri 的媒体文件
     */
    public static File getFileFromMediaUri(Context context, Uri uri) {
        if (uri.getScheme().compareTo("content") == 0) {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);// 根据Uri从数据库中找
            if (cursor != null) {
                cursor.moveToFirst();
                String filePath = cursor.getString(cursor.getColumnIndex("_data"));// 获取图片路径
                cursor.close();
                if (filePath != null) {
                    return new File(filePath);
                }
            }
        } else if (uri.getScheme().compareTo("file") == 0) {
            return new File(uri.toString().replace("file://", ""));
        }
        return null;
    }

    /**
     * 解压文件或文件夹到指定路径
     *
     * @param zipFilePath  待解压文件路径
     * @param outPath      输出根目录路径
     * @throws Exception 解压出错
     */
    public static void unZipFolder(String zipFilePath, String outPath) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPath + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPath + File.separator + szName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    /**
     * 读取assets中的文件
     * @param fileName
     * @param context
     * @return
     * String result = getJson("province.txt");
     * //将读出的字符串转换成JSONobject
     *  JSONObject jsonObject = new JSONObject(str);
     *  //获取JSONObject中的数组数据
     *  jsonArray = jsonObject.getJSONArray(arrName);
     *  Type listType = new TypeToken<List<SearchCompanyResultBean>>() {
     *         }.getType();
     *   //这里的json是字符串类型 = jsonArray.toString();
     * List<SearchCompanyResultBean> list = new Gson().fromJson(json, listType );
     */
    public static String getAssetsFileToJson(String fileName,Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void copy_file_from_sdcard(Context context){
        Log.d(" copy_file_from_sdcard", " begin work.....");
        String v3Dirpath =  FilePathUtils.v3Dirpath;
        String sdcard_chat_user_hostname = FilePathUtils.USER_INFO_FILE + "/" + FilePathUtils.HOSTNAME;
        String sdcard_chat_user_ed25519 = FilePathUtils.USER_INFO_FILE + "/" + FilePathUtils.hs_ed25519_secret_key;
        Log.d(TAG, " copy_file_from_sdcard:: sdcard_chat_user_hostname = " + sdcard_chat_user_hostname);
        Log.d(TAG, " copy_file_from_sdcard:: sdcard_chat_user_ed25519 = " + sdcard_chat_user_ed25519);
        File file = new File(v3Dirpath);
        if(!file.exists()){
            file.mkdirs();
        }
        AssetManager assetManager = context.getAssets();
        BufferedInputStream bis=null;
        BufferedOutputStream bos=null;
        try {
            bis = new BufferedInputStream(new FileInputStream(sdcard_chat_user_hostname));
            bos = new BufferedOutputStream(new FileOutputStream(v3Dirpath+"/hostname"));

            byte[] buffer = new byte[1024];
            int readLen=0;
            while((readLen = bis.read(buffer))!=-1){
                bos.write(buffer,0,readLen);
            }
            if(bis!=null){
                bis.close();
            }
            if(bos!=null){
                bos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //bis = new BufferedInputStream(assetManager.open("hs_ed25519_secret_key"));
            bis = new BufferedInputStream(new FileInputStream(sdcard_chat_user_ed25519));
            bos = new BufferedOutputStream(new FileOutputStream(v3Dirpath+"/" + FilePathUtils.hs_ed25519_secret_key));

            byte[] buffer = new byte[1024];
            int readLen=0;
            while((readLen = bis.read(buffer))!=-1){
                bos.write(buffer,0,readLen);
            }
            if(bis!=null){
                bis.close();
            }
            if(bos!=null){
                bos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String getFileName(String path){
        File file = new File(path);
        return file.getName();
    }

    /**
     * 获取文件后缀的方法
     *
     * @param path 要获取文件后缀的文件
     * @return 文件后缀
     */
    public static String getFileExtension(String path) {
        String extension = "";
        File file = new File(path);
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }
        return extension;
    }

    public static int getFileSize(String path){
        File f= new File(path);
        if (f.exists() && f.isFile()){
            return (int) f.length();
        }else {
            return 0;
        }
    }

    public static void delectPicture(String picturePath){
        File file = new File(picturePath);
        file.delete();
    }

    public static void initFile(){
        createdDirectory(FilePathUtils.TEMP_FILE);
    }

    public static String copy_file(String fileName){
        String content="";
        AssetManager assetManager = getContext().getResources().getAssets();
        try {
            InputStream is = assetManager.open(fileName);
            int length = is.available();// TODO: 2021/7/14 直接读取文件内容大小
            byte[] buf =new byte[length];// TODO: 2021/7/14 改为文件大小 // TODO: 2021/7/13 改为1024
            is.read(buf);
            content = new String(buf,"utf-8");// TODO: 2021/7/13 utf-8格式
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    /**
     * 复制文件 从一个位置 复制到 Sdcard另外的一个位置
     * @param context
     * @return
     */
    public static boolean copyFile(Context context, String fromFile, String toFile){
        File source = new File(fromFile);
        File dest = new File(toFile);
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;

        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            inputChannel.close();
            outputChannel.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;

    }

    /**
     * 删除文件 或 文件夹
     * @param filePath
     * @return
     */
    public static boolean deleteFile(String filePath){
        if (TextUtils.isEmpty(filePath)){
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()){
            return false;
        }
        if (file.isFile()){
            return file.delete();
        }else {
            if (file.isDirectory()){
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0){
                    return file.delete();
                }
                //山粗文件夹内容
                boolean result = true;
                for (File item : file.listFiles()){
                    result = result && item.delete();
                }
                return result && file.delete();
            }
        }
        return false;
    }

    /**
     * 一次性读取文件内容
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readAllFileContent(String filePath){
        File file = new File(filePath);
        byte[] bytes = new byte[(int) file.length()];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int ret  = 0;
        try {
            ret = fileInputStream.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String content = new String(bytes, 0, ret);
        return content;
    }

    public static void createdFile(String path){
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.d(TAG, " createdFile:: IOException e = " + e.toString());
            }
        }
    }
    public static void createdDirectory(String path){
        File directory = new File(path);
        if (!directory.exists()) {
            boolean result = directory.mkdirs();
            if (result) {
                Log.d(TAG, " createdDirectory:: 文件夹创建成功：" + directory);
            } else {
                Log.d(TAG, " createdDirectory:: 文件夹创建失败：" + directory);
            }
        } else {
            Log.d(TAG, " createdDirectory:: 文件夹已存在：" + directory);
        }
    }

    /**
     * 一次性读取内容
     * @param path
     * @return
     */
    public static byte[] noSegmentedReadContent(String path){
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            DataInputStream ins = new DataInputStream(new FileInputStream(path));

            int num = 0;
            int bytes;
            byte[] bufferOut = new byte[3060];
            //从in里读向bufferOut写
            while ((bytes = in.read(bufferOut)) != -1) {
                num += bytes;
            }
            Log.d(TAG, " noSegmentedReadContent:: num = " + num);
            byte[] fileContent = new byte[num];
            ins.read(fileContent);//一次性读完所有
            Log.d(TAG, " noSegmentedReadContent:: 文件内容读取完成");
            Log.d(TAG, " noSegmentedReadContent:: fileContent = " + new String(fileContent));
            return fileContent;
        } catch (Exception e) {
            Log.d(TAG, " noSegmentedReadContent:: error = " + e.toString());
        }
        return null;
    }

    public static int getFileTotalBytesLen(String path){
        Log.d(TAG, " getFileTotalBytesLen:: path =  " + path);
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            int num = 0;
            int bytes;
            byte[] bufferOut = new byte[3060];
            //从in里读向bufferOut写
            while ((bytes = in.read(bufferOut)) != -1) {
                num += bytes;
            }
            Log.d(TAG, " getFileTotalBytesLen:: num = " + num);
            Log.d(TAG, " getFileTotalBytesLen:: 文件内容读取完成");
            return num;
        } catch (Exception e) {
            Log.d(TAG, " getFileTotalBytesLen:: error = " + e.toString());
        }
        return 0;
    }
    /**
     * 把 path文件 转成 密文String
     */
    public static String getFileCiphertext(String path){

        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            DataInputStream ins = new DataInputStream(new FileInputStream(path));

            int num = 0;
            int bytes;
            byte[] bufferOut = new byte[3060];
            //从in里读向bufferOut写
            while ((bytes = in.read(bufferOut)) != -1) {
                num += bytes;
            }
            Log.d(TAG, " getFileCiphertext:: num = " + num);
            byte[] fileContent = new byte[num];
            ins.read(fileContent);//一次性读完所有
            Log.d(TAG, " getFileCiphertext:: 文件内容读取完成");
            Log.d(TAG, " getFileCiphertext:: fileContent = " + fileContent.toString());
            String strContent = new String(fileContent, "UTF-8");
            Log.d(TAG, " getFileCiphertext:: strContent = " + strContent);
            String encryptContent = AesTools.getEncryptContent(strContent, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
            Log.d(TAG, " getFileCiphertext:: encryptContent = " + encryptContent);
            String dcryptContent = AesTools.getDecryptContent(encryptContent, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
            Log.d(TAG, " getFileCiphertext:: dcryptContent = " + dcryptContent);
            return encryptContent;
        } catch (Exception e) {
            Log.d(TAG, " getFileCiphertext:: error = " + e.toString());
        }
        return null;
    }

    /**
     * 把 path文件 转成 密文byte[]
     */
    public static byte[] getFileCipherBytes(String path){
        Log.d(TAG, " getFileCipherBytes:: path = " + path);
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            DataInputStream ins = new DataInputStream(new FileInputStream(path));

            int num = 0;
            int bytes;
            byte[] bufferOut = new byte[3060];
            //从in里读向bufferOut写
            while ((bytes = in.read(bufferOut)) != -1) {
                num += bytes;
            }
            Log.d(TAG, " getFileCipherBytes:: num = " + num);
            byte[] fileContent = new byte[num];
            ins.read(fileContent);//一次性读完所有
            Log.d(TAG, " getFileCipherBytes:: 文件内容读取完成");
            Log.d(TAG, " getFileCipherBytes:: 源文件明文fileContent = " + new String(fileContent));


            byte[] encryptBytes = AesTools.getEncryptBytes(fileContent, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
            Log.d(TAG, " getFileCipherBytes:: 密文encryptBytes的Hash = " + DigestUtils.sha256Hex(encryptBytes));
            Log.d(TAG, " getFileCipherBytes:: 密文encryptBytes = " + new String(encryptBytes));
            byte[] dcryptBytes = AesTools.getDecryptBytes(encryptBytes, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
            Log.d(TAG, " getFileCipherBytes:: 解密内容dcryptBytes = " + new String(dcryptBytes));
            Log.d(TAG, " getFileCipherBytes:: 解密内容dcryptBytes的Hash = " + DigestUtils.sha256Hex(dcryptBytes));

            return encryptBytes;
        } catch (Exception e) {
            Log.d(TAG, " getFileCipherBytes:: error = " + e.toString());
        }
        return null;
    }

    /**
     * 把 path文件 转成密文byte[]， 然后把 byte[] 写入到新的文件中
     * @param path 原始文件路径
     * @return 密文路径
     */
    public static String writeBytesCiphertextToFile(String path){

        createdDirectory(FilePathUtils.TEMP_FILE);

        String filename = FileUtils.getFileName(path);
        Log.d(TAG, " writeBytesCiphertextToFile:: filename = " + filename);

        String tempDecryptFile = FilePathUtils.TEMP_FILE + filename;
        Log.d(TAG, " writeBytesCiphertextToFile:: tempDecryptFile = " + tempDecryptFile);

        // 把 path文件 转成 密文
        byte[] cipherBytes = getFileCipherBytes(path);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempDecryptFile);
            try {
                outputStream.write(cipherBytes);
                outputStream.flush(); // 清空缓冲区，确保所有数据都被写入
            } catch (IOException e) {
                Log.d(TAG, " writeBytesCiphertextToFile:: IOException e = " + e.toString());
            }

        } catch (FileNotFoundException e) {
            Log.d(TAG, " writeBytesCiphertextToFile:: FileNotFoundException e = " + e.toString());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close(); // 关闭流释放资源
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        String targetFile = FilePathUtils.TEMP_FILE + "/" + "333.jpg";
 //      readCiphertextToFile(tempDecryptFile, targetFile);

        return tempDecryptFile;
    }
    public static String writeCiphertextToFile(String path){

        createdDirectory(FilePathUtils.TEMP_FILE);

        String filename = FileUtils.getFileName(path);
        Log.d(TAG, " writeStrToFile:: filename = " + filename);

        String tempDecryptFile = FilePathUtils.TEMP_FILE + filename;
        Log.d(TAG, " writeStrToFile:: tempDecryptFile = " + tempDecryptFile);


        String ciphertext = getFileCiphertext(path);
        try {
            FileWriter fileWriter = new FileWriter(tempDecryptFile);
            fileWriter.write(ciphertext);
            fileWriter.close();
            Log.d(TAG, " writeStrToFile:: 文件写入成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, " writeStrToFile:: 文件写入失败");
        }

//        String targetFile = FilePathUtils.TEMP_FILE + "/" + "333.jpg";
//        readCiphertextToFile(tempDecryptFile, targetFile);

        return tempDecryptFile;
    }


    /**
     * 把 密文流文件 转成 正常可读文件
     * @param path 密文流文件路径
     */
    public static void cipherBytesToFile(String  path){
        Log.d(TAG, " cipherBytesToFile:: path = " + path);
        FileOutputStream outputStream = null;
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            DataInputStream ins = new DataInputStream(new FileInputStream(path));
            int num = 0;
            int bytes;
            byte[] bufferOut = new byte[3060];
            //从in里读向bufferOut写
            while ((bytes = in.read(bufferOut)) != -1) {
                num += bytes;
            }
            Log.d(TAG, " getFileCipherBytes:: 源文件密文num = " + num);
            byte[] fileContent = new byte[num];
            ins.read(fileContent);//一次性读完所有
            Log.d(TAG, " cipherBytesToFile:: 文件内容读取完成");
           // Log.d(TAG, " cipherBytesToFile:: 源文件密文fileContent = " + new String(fileContent));
            Log.d(TAG, " cipherBytesToFile:: 源文件密文fileContent的Hash = " + DigestUtils.sha256Hex(fileContent));

            byte[] dcryptBytes = AesTools.getDecryptBytes(fileContent, AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
           // Log.d(TAG, " cipherBytesToFile:: 解密内容dcryptBytes = " + new String(dcryptBytes));
            Log.d(TAG, " cipherBytesToFile:: 解密内容dcryptBytes的Hash = " + DigestUtils.sha256Hex(dcryptBytes));

            File file = new File(path);
            String name = file.getName();
            String offLineFilePath = FilePathUtils.OFF_LINE_FILE_PATH + name;
            createdFile(offLineFilePath);
            Log.d(TAG, " cipherBytesToFile:: offLineFilePath = " + offLineFilePath);
            //String offLineFilePath = FilePathUtils.OFF_LINE_FILE_PATH + "/" + "333.jpg";
            outputStream = new FileOutputStream(offLineFilePath);
            outputStream.write(dcryptBytes);
            outputStream.flush(); //

        } catch (Exception e) {
            Log.d(TAG, " getFileCipherBytes:: error = " + e.toString());
        }finally {
            //TODO 删除临时接收的密文
//        File file = new File(path);
//        if (file.exists()){
//            file.delete();
//        }
            try {
                if (outputStream != null){
                    outputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void readCiphertextToFile(String fromPath, String toPath){
        Log.d(TAG, " readCiphertextToFile:: fromPath = " + fromPath);
        Log.d(TAG, " readCiphertextToFile:: toPath = " + toPath);

        String result = null;
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fromPath));
            BufferedWriter bw = new BufferedWriter(new FileWriter(toPath));
            while ((result = bufferedReader.readLine()) != null) {
                buffer.append(result);
            }
            Log.d(TAG, " readCiphertextToFile:: 获取的加密buffer = " + buffer.toString());
            String  decryptBuffer = AesTools.getDecryptContent(buffer.toString(), AesTools.AesKeyTypeEnum.MESSAGE_TYPE);
            Log.d(TAG, " readCiphertextToFile:: 获取的解密decryptBuffer = " + decryptBuffer);
            bw.write(buffer.toString());
            bw.newLine();

            if(bufferedReader!=null){
                bufferedReader.close();
            }
            if(bw!=null){
                bw.close();
            }

        }catch (IOException e){
            Log.d(TAG, " readCiphertextToFile:: e = " + e.toString());
        }

    }

    public static File str2File(String content){
        File file = null;
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int bytesRead = 0;
        byte[] buffer = new byte[8192];

        try {
            while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            stream.close();
        }catch (IOException e){

        }
        return file;
    }

}
