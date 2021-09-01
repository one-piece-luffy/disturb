package com.example.lib;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public class MainClass {
    public static void main(String asdf[]) {

        start();

    }

    private static void start() {
        //要混淆的目录路径
        String path = "/Users/macmini/xcys/app/src/main/java/";
        int fileNum = 0, folderNum = 0,javaNum=0,kotlinNum=0;
        File file = new File(path);
        LinkedList<File> list = new LinkedList<>();

        if (file.exists()) {
            if (file.isDirectory()) {
                if (null == file.listFiles()) {
                    return;
                }
                list.addAll(Arrays.asList(file.listFiles()));
                while (!list.isEmpty()) {
                    File[] files = list.removeFirst().listFiles();
                    if (null == files) {
                        continue;
                    }
                    for (final File f : files) {
                        if (f.isDirectory()) {
                            System.out.println("文件夹:" + f.getAbsolutePath());
                            list.add(f);
                            folderNum++;
                        } else {
                            System.out.println("文件:" + f.getAbsolutePath());
                            fileNum++;
                            if (f.getAbsolutePath().endsWith(".java")) {
                                JavaDisturb disorganize = new JavaDisturb(f.getAbsolutePath());
                                disorganize.run();
                                javaNum++;

                            } else if (f.getAbsolutePath().endsWith(".kt")) {
                                JavaDisturb disorganize = new JavaDisturb(f.getAbsolutePath());
                                disorganize.run();
                                kotlinNum++;
                            }

                        }
                    }
                }
            } else if (file.isFile()) {
                if (file.getAbsolutePath().endsWith(".java")) {
                    JavaDisturb disorganize = new JavaDisturb(file.getAbsolutePath());
                    disorganize.run();

                } else if (file.getAbsolutePath().endsWith(".kt")) {
                    JavaDisturb disorganize = new JavaDisturb(file.getAbsolutePath());
                    disorganize.run();
                }
            }

        } else {
            System.out.println("文件不存在!");
        }
        System.out.println("文件夹数量:" + folderNum + ",文件数量:" + fileNum + ",java数量:" + javaNum + ",kotlin数量:" + kotlinNum);
    }

}
