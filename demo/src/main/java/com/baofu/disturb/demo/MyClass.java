package com.baofu.disturb.demo;

import com.baofu.disturb.Guard;

public class MyClass {
    public static void main(String[] args) {
        Guard guard=new Guard("D:\\project\\git\\disturb\\app");
//        //修改包名
        guard.changePackageName("com.example.myapplication","c.e.m");
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