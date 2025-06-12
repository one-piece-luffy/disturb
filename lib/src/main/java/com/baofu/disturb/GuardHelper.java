package com.baofu.disturb;

import com.baofu.disturb.utils.DisturbUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 混淆辅助类
 * 用于修改包名、类名、图片、布局文件、color名称
 */
public class GuardHelper {

    //D:\project\MovieBroser\app
    private String appPath;
    //app的src目录路径
    private final String root;
    private final String buildPath ;
    private final String proguardPath ;
    public String skinResPath;
    public String begin;
    public String end;

    public GuardHelper(String appPath){
        this.appPath=appPath;
        root =appPath+ "\\src\\";
        buildPath=appPath+"\\build.gradle";
        proguardPath=appPath+"\\proguard-rules.pro";
    }



    /**
     * 修改包名
     */
    public  void changePackageName(String oldPackageName,String newPackageName) {
        //保存所有文件
        List<String> allFiles = new ArrayList<>();
        File dir = new File(root);
        DisturbUtils.findFolder(dir, allFiles);
        List<File> layoutFiles = new ArrayList<>();
        for (String s : allFiles) {
            File file = new File(s);
            if (s.endsWith(".java") || s.endsWith(".kt")) {
                //重命名java文件
                String newPath = s.replaceAll("\\\\", ".");
                newPath = newPath.replaceAll(oldPackageName, newPackageName);
                newPath = newPath.replaceAll("\\.", "\\\\");
                int index = newPath.lastIndexOf("\\");
                String pre = newPath.substring(0, index);
                String end = newPath.substring(index + 1);
                newPath = pre + "." + end;
                File f2 = new File(newPath);

                index = newPath.lastIndexOf("\\");
                pre = newPath.substring(0, index) + "\\";
                File newDir = new File(pre);
                if (!newDir.exists()) {
                    newDir.mkdirs();
                }
                boolean result = file.renameTo(f2);
//
//            try {
//                Files.copy(Paths.get(s), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
                if (result) {
                    replaceFileData(newPath, oldPackageName, newPackageName);
                }

            } else if (s.endsWith(".xml") && file.getParent().endsWith("layout")) {
                layoutFiles.add(file);
            }
        }
        //修改layout下的包名
        for (File s : layoutFiles) {
            replaceFileData(s.getAbsolutePath(), oldPackageName, newPackageName);
        }
        //修改 build的namespace
        replaceFileData(buildPath, oldPackageName, newPackageName);
        //修改混淆文件的包名
        replaceFileData(proguardPath, oldPackageName, newPackageName);

    }

    /**
     * 修改类名
     */
    public  void changeClassName() {
        //保存所有文件
        List<String> allFiles = new ArrayList<>();
        File dir = new File(root);
        DisturbUtils.findFolder(dir, allFiles);
        Map<String, String> allFileMap = new HashMap<>();
        for (String item : allFiles) {
            allFileMap.put(item, item);
        }
        for (String item : allFiles) {
//                        item="D:\\project\\MovieBroser\\app\\src\\main\\java\\com\\moviebrowser\\video\\db\\bean\\HistoryBean.java";
            if (!item.contains("src\\main")) {
                //多渠道的配置不参与修改类名
                continue;
            }
            File file = new File(item);

            if (item.endsWith(".java") || item.endsWith(".kt")) {
                String data = DisturbUtils.readFile(item);
                if (data.contains("@Entity") || data.contains("@Dao")) {
                    continue;
                }

                String extention;
                if (item.endsWith(".java")) {
                    extention = ".java";
                } else {
                    extention = ".kt";
                }
                String name = generateGuard();
                name = name.substring(0, 1).toUpperCase() + name.substring(1);

                File newFile = new File(file.getParent() + "\\" + name + extention);
                boolean result = file.renameTo(newFile);
                if (result) {
                    allFileMap.remove(item);
                    allFileMap.put(newFile.getAbsolutePath(), newFile.getAbsolutePath());
                    String oleFileName = file.getName().replace(extention, "");
                    String newFileName = newFile.getName().replace(extention, "");

                    //修改类里面的名称
                    replaceClassName(newFile, oleFileName, newFileName);
                    Iterator<Map.Entry<String, String>> subIterator = allFileMap.entrySet().iterator();
                    while (subIterator.hasNext()) {
                        Map.Entry<String, String> subentry = subIterator.next();
                        String subItem = subentry.getKey();

//                        subItem="D:\\project\\MovieBroser\\app\\src\\main\\java\\com\\moviebrowser\\video\\db\\AppDatabase.kt";
                        File subFile = new File(subItem);
                        if (subItem.endsWith(".xml") && subFile.getParent().endsWith("layout")) {
                            //修改layout下的引用名称
                            String patern = ".java.";
                            String path = file.getAbsolutePath().replaceAll("\\\\", ".");

                            int index = path.indexOf(patern);
                            path = path.substring(index + patern.length())
                                    .replace(extention, "");

                            String newPath = newFile.getAbsolutePath().replaceAll("\\\\", ".");
                            index = newPath.indexOf(patern);
                            newPath = newPath.substring(index + patern.length())
                                    .replace(extention, "");

                            replaceFileData(subFile.getAbsolutePath(), path, newPath);

                        } else if (subItem.endsWith(".java") || subItem.endsWith(".kt")) {
                            if (subFile.exists()) {
                                replaceClassName(subFile, oleFileName, newFileName);
                            }
                        } else if (subItem.endsWith("AndroidManifest.xml")) {
                            replaceFileData(subFile.getAbsolutePath(), "." + oleFileName, "." + newFileName);
                        }
//                        break;
                    }

                }

            }
//            break;

        }

    }

    /**
     * 修改布局文件的名称
     */
    public  void changeLayoutName() {
        //保存所有文件
        List<String> allFiles = new ArrayList<>();
        File dir = new File(root);
        DisturbUtils.findFolder(dir, allFiles);
        Map<String, String> allFileMap = new HashMap<>();
        //多渠道的文件
        Map<String, String> productFlavorsMap = new HashMap<>();
        for (String item : allFiles) {
            allFileMap.put(item, item);
            if (!item.contains("src\\main")) {
                productFlavorsMap.put(item,item);
            }
        }
        for (String item : allFiles) {
//            item="D:\\project\\MovieBroser\\app\\src\\main\\res\\layout\\home_start.xml";
//            if(!(item.contains("fragment_web_tab")||item.contains("home_start"))){
//                continue;
//            }
            if (!item.contains("src\\main")) {
                //多渠道的配置不参与修改
                continue;
            }
            File file = new File(item);

            if (item.endsWith(".xml") && file.getParent().endsWith("layout")) {


                String name = generateGuard();
                String extention = ".xml";
                File newFile = new File(file.getParent() + "\\" + name + extention);

                boolean result=file.renameTo(newFile);
                if (result) {
                    allFileMap.remove(item);
                    allFileMap.put(newFile.getAbsolutePath(), newFile.getAbsolutePath());
                    String oleFileName = file.getName().replace(extention, "");
                    String newFileName = newFile.getName().replace(extention, "");
                    //重命名后同时重命名多渠道的文件名
                    for (Map.Entry<String, String> product : productFlavorsMap.entrySet()) {
                        String subItem = product.getKey();
                        File productFile = new File(subItem);
                        if (!productFile.exists()) {
                            continue;
                        }
                        if (productFile.getName().equals(file.getName())) {
                            File newProductFile = new File(productFile.getParent() + "\\" + name + extention);
                            boolean productResult = productFile.renameTo(newProductFile);
                            if (productResult) {
                                allFileMap.remove(subItem);
                                allFileMap.put(newProductFile.getAbsolutePath(), newProductFile.getAbsolutePath());
//                                productIterator.remove();
//                                productFlavorsMap.put(newProductFile.getAbsolutePath(), newProductFile.getAbsolutePath());
                            }
                        }
                    }

                    for (Map.Entry<String, String> subentry : allFileMap.entrySet()) {
                        String subItem = subentry.getKey();
//                        subItem=newFile.getAbsolutePath();

                        File subFile = new File(subItem);
                        if (subItem.endsWith(".xml") && subFile.getParent().endsWith("layout")) {

                            String data = DisturbUtils.readFile(subFile.getAbsolutePath());
                            String regex = "(@layout/)" + oleFileName + "[\"]";
                            String content = replaceData(data, regex, oleFileName,newFileName);
                            if (content != null) {
                                DisturbUtils.write(subFile.getAbsolutePath(), content);
                            }
                        } else if (subItem.endsWith(".java") || subItem.endsWith(".kt")) {
                            if (subFile.exists()) {
                                replacejavaLayoutName(subFile, oleFileName, newFileName);
                            }
                        }
//                        break;
                    }

                }

            }
//            break;

        }

    }

    /**
     * 修改res下drawable文件的名称
     */
    public  void changeDrawableName() {
        //保存所有文件
        List<String> allFiles = new ArrayList<>();
        File dir = new File(root);
        DisturbUtils.findFolder(dir, allFiles);
        Map<String, String> allFileMap = new HashMap<>();
        //多渠道的文件
        Map<String, String> productFlavorsMap = new HashMap<>();
        for (String item : allFiles) {
            allFileMap.put(item, item);
            if (!item.contains("src\\main")) {
                productFlavorsMap.put(item,item);
            }
        }
        //多皮肤的资源的文件
        Map<String, String> multyRessMap = new HashMap<>();
        for (String item : allFiles) {

            if (item.contains("src\\main\\"+skinResPath)) {
                multyRessMap.put(item,item);
            }
        }
        for (String item : allFiles) {
//            item="D:\\project\\MovieBroser\\app\\src\\main\\res\\layout\\home_start.xml";
//            if(!(item.contains("fragment_web_tab")||item.contains("home_start"))){
//                continue;
//            }
            if (!item.contains("src\\main")) {
                //多渠道的配置不参与修改
                continue;
            }
            File file = new File(item);

            if ( file.getParent().contains("res\\drawable")) {


                int exIndex=item.lastIndexOf(".");
                //文件扩展名
                String extention = item.substring(exIndex);
                String oleFileName = file.getName().replace(extention, "");
                String name = generateGuard();
                if (oleFileName.startsWith(begin)) {
                    name = begin + name;
                }

                File newFile = new File(file.getParent() + "\\" + name + extention);
                String newFileName = newFile.getName().replace(extention, "");

                boolean result=file.renameTo(newFile);
                if (result) {
                    allFileMap.remove(item);
                    allFileMap.put(newFile.getAbsolutePath(), newFile.getAbsolutePath());
                    //重命名后同时重命名多皮肤资源的文件名
                    for (Map.Entry<String, String> product : multyRessMap.entrySet()) {
                        String subItem = product.getKey();
                        File productFile = new File(subItem);
                        if (!productFile.exists()) {
                            continue;
                        }
                        String productFileName=productFile.getName().replace(end,"");
                        if (productFileName.equals(file.getName())) {
                            File newProductFile = new File(productFile.getParent() + "\\" + name +end+ extention);
                            boolean productResult = productFile.renameTo(newProductFile);
                            if (productResult) {
                                allFileMap.remove(subItem);
                                allFileMap.put(newProductFile.getAbsolutePath(), newProductFile.getAbsolutePath());
//                                productIterator.remove();
//                                productFlavorsMap.put(newProductFile.getAbsolutePath(), newProductFile.getAbsolutePath());
                            }
                        }
                    }

                    //重命名后同时重命名多渠道的文件名
                    for (Map.Entry<String, String> product : productFlavorsMap.entrySet()) {
                        String subItem = product.getKey();
                        File productFile = new File(subItem);
                        if (!productFile.exists()) {
                            continue;
                        }
                        if (productFile.getName().equals(file.getName())) {
                            File newProductFile = new File(productFile.getParent() + "\\" + name + extention);
                            boolean productResult = productFile.renameTo(newProductFile);
                            if (productResult) {
                                allFileMap.remove(subItem);
                                allFileMap.put(newProductFile.getAbsolutePath(), newProductFile.getAbsolutePath());
//                                productIterator.remove();
//                                productFlavorsMap.put(newProductFile.getAbsolutePath(), newProductFile.getAbsolutePath());
                            }
                        }
                    }
                    for (Map.Entry<String, String> subentry : allFileMap.entrySet()) {
                        String subItem = subentry.getKey();

                        File subFile = new File(subItem);
                        if ((subItem.endsWith(".xml") && subFile.getParent().endsWith("layout"))
                                || subItem.endsWith("styles.xml")
                                || (subItem.endsWith(".xml") && subFile.getParent().contains("res\\drawable"))
                                || (subItem.endsWith(".xml") && subFile.getParent().contains(skinResPath))
                        ) {

                            String data = DisturbUtils.readFile(subFile.getAbsolutePath());
                            String regex = "@drawable/" + oleFileName + "(" + end + ")?" + "[\"|<]";
                            String content = replaceData(data, regex, oleFileName, newFileName);
                            if (content != null) {
                                DisturbUtils.write(subFile.getAbsolutePath(), content);
                            }
                        } else if (subItem.endsWith(".java") || subItem.endsWith(".kt")) {
                            if (subFile.exists()) {
                                replaceJavaFileDrawableOrColorName(subFile, oleFileName, newFileName);
                            }
                        }
//                        break;
                    }

                }
//                break;
            }


        }

    }

    /**
     * 修改res下color属性的名称
     */
    public  void changeColorAttrName() {
        guardMap.clear();
        //保存所有文件
        List<String> allFiles = new ArrayList<>();
        File dir = new File(root);
        DisturbUtils.findFolder(dir, allFiles);

        List<String> colorList=new ArrayList<>();
        colorList.add(root+"main\\res\\values\\colors.xml");
//        colorList.add(root+"main\\"+skinResPath+"\\values\\colors.xml");

        //正则前缀，以xxx开头
        String prefix="name=\"";
        //正则后缀，以xxx结尾
        String suffix="\"";
        String regex = prefix+"[a-zA-Z0-9_]*\"" ;
        for(String colorPath:colorList){
            String colorXmlData=DisturbUtils.readFile(colorPath);
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(colorXmlData);
            while (matcher.find()) {
                String group = matcher.group();
                String newName=generateGuard();
                String oldName=group.substring(prefix.length(),group.length()-1);
                if (oldName.startsWith(begin)) {
                    newName = begin + newName;
                }
                colorXmlData=colorXmlData.replace(group,prefix+newName+suffix);
                for (String subItem : allFiles) {
                    File subFile=new File(subItem);
                    if ((subItem.endsWith(".xml") && subFile.getParent().endsWith("layout"))
                            || subItem.endsWith("styles.xml")
                            || subItem.endsWith("themes.xml")
                            || (subItem.endsWith(".xml") && subFile.getParent().contains("res\\drawable"))
                            || (subItem.endsWith(".xml") && subFile.getParent().contains(skinResPath))
                    ) {
                        String data = DisturbUtils.readFile(subFile.getAbsolutePath());
                        //前半段替换xml，后半段替换colors.xml
                        String subregex = "(@color/" + oldName  + "(" + end + ")?"  + "[\"<>])|(<color name=\""+oldName  + "(" + end + ")?"+"[\"<>])";
                        String content = replaceData(data, subregex,  oldName,newName);
                        if (content != null) {
                            DisturbUtils.write(subFile.getAbsolutePath(), content);
                        }
                    } else if (subItem.endsWith(".java") || subItem.endsWith(".kt")) {
                        if (subFile.exists()) {
                            replaceJavaFileDrawableOrColorName(subFile, oldName, newName);
                        }
                    } else if (!subItem.contains("src\\main") && subItem.endsWith("colors.xml")) {
                        String data = DisturbUtils.readFile(subItem);
                        String content = data.replace(group, prefix + newName + suffix);
                        if (content != null) {
                            DisturbUtils.write(subFile.getAbsolutePath(), content);
                        }
                    }
                }
            }
            DisturbUtils.write(colorPath,colorXmlData);
        }


    }
    private  void replaceJavaFileDrawableOrColorName(File file, String oldStr, String newStr) {

        String data = DisturbUtils.readFile(file.getAbsolutePath());
        //匹配名字
        String regex = "(color|drawable)[.]" + oldStr+   "(" + end + ")?"+ "[\\s){;,}]";
        String content=replaceData(data,regex,oldStr,newStr);
        if (content!=null) {
            data=content;
            DisturbUtils.write(file.getAbsolutePath(), data);
        }

    }

    private  void replacejavaLayoutName(File file, String oldStr, String newStr) {
        String[] oldArr = oldStr.split("_");
        StringBuilder oldDataBindingName = new StringBuilder();
        for (String s : oldArr) {
            if(s.length()==0){
                continue;
            }
            oldDataBindingName.append(s.substring(0, 1).toUpperCase());
            if(s.length()>1){
                oldDataBindingName.append(s.substring(1));
            }
        }
        oldDataBindingName.append("Binding");
        String[] newArr = newStr.split("_");
        StringBuilder newDataBindingName = new StringBuilder();
        for (String s : newArr) {
            if(s.length()==0){
                continue;
            }
            newDataBindingName.append(s.substring(0, 1).toUpperCase());
            if(s.length()>1){
                newDataBindingName.append(s.substring(1));
            }

        }
        newDataBindingName.append("Binding");

        String data = DisturbUtils.readFile(file.getAbsolutePath());
        //匹配名字
        String regex = "[\\[\\s.]" + oldStr +  "(" + end + ")?"+ "[\\s){;,]";
        String content=replaceData(data,regex,oldStr,newStr);
        if (content!=null) {
            data=content;
            DisturbUtils.write(file.getAbsolutePath(), data);
        }
        //匹配databinding
        regex = "[\\[\\s.:(]" + oldDataBindingName + "[\\s){?;.]";
        content=replaceData(data,regex,oldDataBindingName.toString(),newDataBindingName.toString());
        if (content!=null) {
            data=content;
            DisturbUtils.write(file.getAbsolutePath(), data);
        }

    }

    private  void replaceClassName(File file, String old, String newData) {
        String data = DisturbUtils.readFile(file.getAbsolutePath());
        if (data.contains("tableName") || data.contains("@Dao")) {
            //room数据库相关的不修改
            return;
        }
        String regex = "[\\[\\s.:<+(=!&|,?@-]" + old + "[\\s.?<>){;:,(\\[]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        boolean find = false;
        while (matcher.find()) {
            String group = matcher.group();
            String start = group.substring(0, 1);
            String end = group.substring(group.length() - 1);
            data = data.replace(group, start + newData + end);
            find = true;
        }
        if (find) {
            DisturbUtils.write(file.getAbsolutePath(), data);
        }
    }

    private  String replaceDataByJava(String data,String regex,String newStr){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        boolean find = false;
        while (matcher.find()) {
            String group = matcher.group();
            String start = group.substring(0, 1);
            String end = group.substring(group.length() - 1);
            //去匹配到的前一位和后一位加起来一起替换，避免出现部分重名的情况，
            // ex:将替换 R.layout.item替换为R.layout.a, 不取前后的话可能R.layout.item_home会被替换为R.layout.a_home
            data = data.replace(group, start + newStr + end);
            find = true;
        }
        if (find) {
            return data;
        }
        return null;
    }
    private  String replaceData(String data,String regex,String oldStr,String newStr){
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        boolean find = false;
        while (matcher.find()) {
            String group = matcher.group();
            String newGroup=group.replace(oldStr,newStr);
            data = data.replace(group, newGroup);
            find = true;
        }
        if (find) {
            return data;
        }
        return null;
    }


    //class名list
    Map<String, String> guardMap = new HashMap<>();

    /**
     * 随机生成一个名称
     */
    private  String generateGuard() {
        final String[] array = {
                "a", "b", "c", "d", "e", "f", "g",
                "h", "i", "j", "k", "l", "m", "n",
                "o", "p", "q", "r", "s", "t",
                "u", "v", "w", "x", "y", "z",
                "o0", "oo00"
        };
        List<String> whiteList=new ArrayList<String>();
        whiteList.add("while");
        whiteList.add("if");
        whiteList.add("true");
        whiteList.add("false");
        whiteList.add("break");
        whiteList.add("else");
        whiteList.add("continue");
        whiteList.add("static");
        whiteList.add("void");
        whiteList.add("private");
        whiteList.add("default");
        whiteList.add("switch");
        whiteList.add("do");
        whiteList.add("public");
        whiteList.add("null");
        whiteList.add("return");
        whiteList.add("static");
        whiteList.add("final");
        whiteList.add("string");
        whiteList.add("int");
        whiteList.add("long");
        whiteList.add("char");
        whiteList.add("double");
        whiteList.add("boolean");
        whiteList.add("byte");
        whiteList.add("abstract");
        whiteList.add("assert");
        whiteList.add("catch");
        whiteList.add("class");
        whiteList.add("const");
        whiteList.add("enum");
        whiteList.add("extends");
        whiteList.add("finally");
        whiteList.add("for");
        whiteList.add("import");
        whiteList.add("implements");
        whiteList.add("instanceof");
        whiteList.add("interface");
        whiteList.add("new");
        whiteList.add("package");
        whiteList.add("short");
        whiteList.add("static");
        whiteList.add("super");
        whiteList.add("this");
        whiteList.add("throw");
        whiteList.add("volatile");

        String defaultKey="R";
        String result = defaultKey;
        guardMap.put(result, result);


        while (guardMap.get(result) != null) {
            Random random = new Random();
            //2-6位数
            int digit = random.nextInt(5) + 2;
            StringBuilder stringBuffer = new StringBuilder();
            for (int i = 0; i < digit; i++) {
                int index = random.nextInt(array.length);
                if ((stringBuffer.length() == 0||stringBuffer.length()== digit-1) && array[index].equals("_")) {
                    //第一位和最后一位不要下划线,第一位下划线class编译会有问题，最后一位下划线databinding有可能重名
                    i--;
                    if(i<0){
                        i=0;
                    }
                    continue;
                }
                stringBuffer.append(array[index]);
                if (i == digit - 1 && i > 1) {
                    //随机添加一个数字
                    int numIndex = random.nextInt(2);
                    if (numIndex == 0) {
                        int num = random.nextInt(99);
                        stringBuffer.append(num);
                    }
                }

            }

            if(whiteList.contains(stringBuffer.toString())){
                continue;
            }
            result = stringBuffer.toString();
            if("".equals(result)){
                result=defaultKey;
            }

        }

        guardMap.put(result, result);
        return result;
    }

    /**
     * 对指定路径的文件内容进行替换
     *
     * @param path    目标文件路径
     * @param oldName 要替换的内容
     * @param newName 新内容
     */
    private static void replaceFileData(String path, String oldName, String newName) {
        String data = DisturbUtils.readFile(path);
        data = data.replaceAll(oldName, newName);
        DisturbUtils.write(path, data);
    }

}
