package com.baofu.disturb;

public class MainClass {
    public static void main(String asdf[]) {

//        disturb("D:\\project\\MovieBroser\\app\\src\\main\\java\\");

//        Guard guard=new Guard("D:\\project\\MovieBroser\\app");
        GuardHelper guard=new GuardHelper("C:\\Users\\Administrator\\Documents\\GitHub\\SaveTiktok\\app");
//        //修改包名
        guard.changePackageName("com.instagram.ig","t.t.g");
////        //修改类名
        guard.changeClassName();
////        //重命名布局文件
        guard.changeLayoutName();
////        //重命名图片
        guard.changeDrawableName();
        //修改colors.xml的name
        guard.changeColorAttrName();

    }


}