//
//  SystemSupport.cpp
//  libSystemSupport
//
//  Created by mac on 2020/4/22.
//  Copyright © 2020年 mac. All rights reserved.
//

#include "SystemSupportNative.h"
#include <libproc.h>
#include <errno.h>
#include <iostream>

jstring str2jstring(JNIEnv* env, const char* pat);

JNIEXPORT jstring JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_getProcessExecutablePath
(JNIEnv * env, jclass clazz, jlong processId) {
    char infoPath[PROC_PIDPATHINFO_MAXSIZE] = {0};
    int pid = int(processId);
    int result = proc_pidpath(pid, infoPath, sizeof(infoPath));
    if (result <= 0) {
        return NULL;
    }
    return str2jstring(env, infoPath);
}

jstring str2jstring(JNIEnv* env,const char* pat){
    //定义java String类 strClass
    jclass strClass = (env)->FindClass("Ljava/lang/String;");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("UTF8");
    //将byte数组转换为java String,并输出
    return (jstring)(env)->NewObject(strClass, ctorID, bytes, encoding);
}
