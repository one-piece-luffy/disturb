package com.baofu.disturb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 打乱java代码的排序
 * Created by lihaiyi on 21/8/31.
 */
public class JavaDisturb {

    public static final boolean DEBUG = false;

    //文件路径
    String path;
    //文件内容
    String fileData;
    //成员函数声明代码的集合
    List<String> methodList;
    Map<String, String> methodMap = new HashMap<>();


    public JavaDisturb(String path) {
        this.path = path;
    }


    public void run() {
        fileData = readFile(path);
        int index = 0;
        int last= 0;
        if (fileData != null && (fileData.contains("@Dao")||fileData.contains("@GET")||fileData.contains("@POST"))) {
            System.out.println("数据库、网络配置文件，不混淆：" + path);
            return;
        }
//        String array[]=fileData.split("class ");
//        if(array!=null&&array.length>2){
//            System.out.println("该文件包含多个类，不混淆：" + path);
//            return;
//        }
        methodList = getMethod(path);
        if (isDuplicateMethod()) {
            System.out.println("该文件包含多个重名方法，不混淆：" + path);
            return;
        }
        String reg = "((    |\t)@[_a-zA-Z0-9()= ,.\n\r\"-*:]*){0,10}";
        if (methodList.size() > 0) {
            reg += "(";
            for (int i = 0, size = methodList.size(); i < size; i++) {
                String name = methodList.get(i).replaceAll("\\[", "\\\\[")
                        .replaceAll("\\]", "\\\\]")
                        .replaceAll("\\(", "\\\\(")
                        .replaceAll("\\)", "\\\\)")
                        .replaceAll("\\?", "\\\\?");

                if (i == size - 1) {
                    reg += name + ")";
                    break;
                } else {
                    reg += name + "|";
                }
            }

            reg += "[\\s\\S]*?(";
            for (int i = 0, size = methodList.size(); i < size; i++) {
                String name = methodList.get(i).replaceAll("\\[", "\\\\[")
                        .replaceAll("\\]", "\\\\]")
                        .replaceAll("\\(", "\\\\(")
                        .replaceAll("\\)", "\\\\)")
                        .replaceAll("\\?", "\\\\?");

                if (i == size - 1) {
                    reg += name + ")";
                    break;
                } else {
                    reg += name + "|";
                }
            }
            List<String> list = new ArrayList<>();
            match(list, reg);
            if (!DEBUG) {
                sort(list);
            }
            try {

                index = fileData.lastIndexOf("}");

                fileData = fileData.substring(0, index);
                for (int i = 0, size = list.size(); i < size; i++) {

                    String content = list.get(i);
                    if (isErrorRules(content)) {
                        return;
                    }

                    if (DEBUG) {
                        System.out.println("===" + i + "  " + content + "==");
                    }
                    fileData = fileData + list.get(i) + "\n";
                }
                fileData += "}";

                index = fileData.lastIndexOf("}");
                last= findLastPosition();
                if (last != index) {

                    System.out.println("可能存在不规范的成员内部类无法解析，不混淆：" + path);
//                    System.out.println(fileData.substring(0,last));
                    return;
                }
                if (!DEBUG) {
                    write(path, fileData);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("出错路径：" + path);
            }
        }


    }

    private boolean isErrorRules(String content) {
        if (content.contains("@OnClick")) {
            System.out.println(content);
            System.out.println("规则不匹配:包含@OnClick，不混淆==>" + path);
            return true;
        }
        String temp = content.trim();
        int lastIndex = temp.lastIndexOf("\n");
        if (lastIndex > 0) {
            temp = temp.substring(lastIndex);
            if (temp.contains("@")) {
                System.out.println(content);
                System.out.println("规则不匹配:末尾包含@，不混淆==>" + path);
                return true;
            }
        }


        return false;
    }

    /**
     * 对list进行随机排序
     *
     * @param list
     */
    private void sort(List<String> list) {
        Random random = new Random();
        for (int i = 0, size = list.size(); i < size; i++) {
            int j = random.nextInt(list.size());
            String temp = list.get(j);
            list.set(j, list.get(i));
            list.set(i, temp);
        }
    }

    public void match(List<String> list, String reg) {
        try {
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(fileData);
            if (matcher.find()) {
                String content = matcher.group();
                if (content.startsWith("\n")) {
                    content = content.substring(1);
                }

                for (int j = 0, size = methodList.size(); j < size; j++) {
                    String name = methodList.get(j);
                    try {
                        if (content.endsWith(name)) {
                            content = content.substring(0, content.length() - name.length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (content.endsWith("\n")) {
                    content = content.substring(0, content.length() - 1);
                }
                if (content.endsWith("//")) {
                    content = content.substring(0, content.length() - 2);

                }

                while (true) {
                    //查找注解
                    Pattern p1 = Pattern.compile("(    |\t)@[_a-zA-Z0-9()= ,.\"-*:]*");
                    Matcher m = p1.matcher(content);
                    String annotation = null;
                    boolean find = m.find();
                    if (find) {
                        while (find) {
                            annotation = m.group();

                            find = m.find();

                        }

                        if (annotation != null && !annotation.equals("") && content.endsWith(annotation)) {
                            content = content.substring(0, content.length() - annotation.length());
                        } else {
                            break;
                        }
                        if (content.endsWith("\n")) {
                            content = content.substring(0, content.length() - 1);
                        }
                    } else {
                        break;
                    }

                }


                list.add(content);

                fileData = fileData.replace(content, "");
                match(list, reg);


            }
        } catch (Exception e) {
            System.out.println("出错路径:" + path);
            e.printStackTrace();
        }

    }

    private int findLastPosition(){
        Stack<Integer> leftStack=new Stack();
        Stack<Integer> rightStack=new Stack();
        int classPosition=fileData.indexOf("class");
        int enumPosition=fileData.indexOf("enum");
        int interfacePosition=fileData.indexOf("interface");
        int objectPosition=fileData.indexOf("object");
        int min=10000;
        if(classPosition>0&&classPosition<min){
            min=classPosition;
        }
        if(enumPosition>0&&enumPosition<min){
            min=enumPosition;
        }
        if(interfacePosition>0&&interfacePosition<min){
            min=interfacePosition;
        }
        if(objectPosition>0&&objectPosition<min){
            min=objectPosition;
        }
        Pattern pattern = Pattern.compile("\\{|\\}");
        Matcher matcher = pattern.matcher(fileData);
        while (matcher.find()){
            String str=matcher.group();
            int start=matcher.start();
            if(start<min)
                continue;
            if (str.equals("{")) {
                leftStack.push(start);
            } else {
                if(leftStack.isEmpty()){
                    if(!rightStack.isEmpty()){
                        //不匹配的时候返回距离最近的位置
                        return rightStack.pop();
                    }
                    return -1;
                }
                leftStack.pop();
                rightStack.push(start);
            }
            if(leftStack.empty()){
                return rightStack.pop();
            }
        }
        if(!rightStack.isEmpty()){
            //不匹配的时候返回距离最近的位置
            return rightStack.pop();
        }
        return -1;
    }


    /**
     * 判断是不是一个成员函数
     * 必须是4个空格或者1个tab开头的才认为是成员函数
     *
     * @param name
     * @return
     */
    private boolean isMethod(String name) {
        if (name.startsWith("\n")) {
            name = name.substring(1);
        }
        //以4个空格开头
        if (name.startsWith("    ")) {
            String c = name.substring(4, 5);
            if (!c.endsWith(" ")) {
                return true;
            }
        } else if (name.startsWith("\t")) {
            String c = name.substring(1, 2);
            if (!c.endsWith(" ") && !c.endsWith("\t")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取java文件的成员函数声明
     *
     * @return
     */
    private List<String> getMethod(String path) {
        //一个包含public/private等修饰符的,一个是缺少修饰符的
        Pattern pattern = Pattern.compile("(((\\s+)(\\w+)(\\s+)(synchronized\\s+){0,1}(final\\s+){0,1}(static\\s+){0,1}(final\\s+){0,1}(\\w+))|((\\s+)(synchronized\\s+){0,1}(static\\s+){0,1}(\\w+)))(\\s+)([_a-zA-Z]+[_a-zA-Z0-9]*)([(]([^()]*)[)])");

        List<String> list = new ArrayList<>();
        try (LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(path))) {
            String line = null;

            while ((line = lineNumberReader.readLine()) != null) {
                if (line.contains("\"")||line.trim().startsWith("return")) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String name = matcher.group().replaceAll("\n", "");
                    if (line.trim().startsWith("//")) {
                        //如果注释的方法和成员方法一致会导致解析错误,在这里把注释的方法名改掉,方法名随意
                        if(line.contains("{")){
                            fileData = fileData.replace(line, "//public void asdfasdfalasdfjl(){");
                        }else {
                            fileData = fileData.replace(line, "//public void asdfasdfalasdfjl()");
                        }

                        break;
                    }
                    if (isMethod(name)) {
                        list.add(name);
                    } else {
                        methodMap.put(name.trim(), name);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return list;


    }


    private String readFile(String path) {
        String result = "";
        String line = "";
        String encoding = "utf-8";
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

    private void write(String path, String data) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(data);
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 是否包含多个同名函数
     *
     * @return
     */
    private boolean isDuplicateMethod() {
        Iterator<Map.Entry<String, String>> iterator = methodMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String value = entry.getValue();
            for (String str : methodList) {
                if (str.trim().equals(value.trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
