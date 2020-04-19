#include"SystemProcessNative.h"

#include<iostream>
#include<Windows.h>
#include <Psapi.h>

jstring w2js(JNIEnv* env, wchar_t* src);

/*
 * Class:     org_swdc_fx_deploy_system_impl_SystemProcessNative
 * Method:    getProcessExecutablePath
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_swdc_fx_deploy_system_impl_SystemProcessNative_getProcessExecutablePath
(JNIEnv* env, jclass clazz, jlong procId) {
    long pid = long(procId);
    HANDLE hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, pid);
    if (hProcess == NULL) {
        return jstring(NULL);
    }
    wchar_t ProcessName[MAX_PATH] = { 0 };
    if (GetModuleFileNameEx(hProcess, NULL, ProcessName, MAX_PATH) == 0) {
        return jstring(NULL);
    }
    return w2js(env, ProcessName);
}

//wchar_t 转换成 jstring
//env :JNIEnv jni操作 不可或缺的
//src：wchar_t 源字符 四字节似乎linux专用
//return :  转换完成以后的结果jstring
jstring w2js(JNIEnv * env, wchar_t* src){
   int src_len = wcslen(src);
   jchar * dest = new jchar[src_len + 1];
   memset(dest, 0, sizeof(jchar) * (src_len + 1));

   for (int i = 0; i < src_len; i++) {
       memcpy(&dest[i], &src[i], 2);
   }
   jstring dst = env->NewString(dest, src_len);
   delete[] dest;
   return dst;
}