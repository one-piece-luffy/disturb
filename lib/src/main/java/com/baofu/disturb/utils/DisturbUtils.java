package com.baofu.disturb.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DisturbUtils {
    /**
     * 读物文件内容
     * @param path 文件路径
     */
    public static String readFile(String path) {
        String result = "";
        String line = "";
        String encoding = "UTF-8";
        File file = new File(path);
        try {

            InputStreamReader read = new InputStreamReader(
                    new FileInputStream(file), encoding);
            BufferedReader in = new BufferedReader(read);
            while ((line = in.readLine()) != null) {
                result += line + "\n";
//                System.out.println(line);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写文件
     * @param path 文件路径
     */
    public static void write(String path, String data) {
        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter(path));
//            out.write(data);
//            out.close();

            OutputStream outputStream= Files.newOutputStream(Paths.get(path));
            BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(outputStream);
            bufferedOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
            bufferedOutputStream.close();

//            FileOutputStream fos = new FileOutputStream(path);
//            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
//            osw.write(data);
//            osw.flush();
//            osw.close();


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 递归遍历文件夹下的所有文件
     *
     * @param file     文件
     * @param allFiles
     */
    public static void findFolder(File file, List<String> allFiles) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                findFolder(f, allFiles);
            }
        } else {
            allFiles.add(file.getAbsolutePath());
        }
    }
}
