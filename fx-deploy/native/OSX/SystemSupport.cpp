//
//  SystemSupport.cpp
//  libSystemSupport
//
//  Created on 2020/4/22.
//

#include "SystemSupportNative.h"
#include <libproc.h>
#include <errno.h>
#include <iostream>
#include <sys/sysctl.h>
#include <signal.h>

jstring str2jstring(JNIEnv* env, const char* pat);
char* getProcessName(pid_t pid);
jstring getProcessPath(JNIEnv * env,int pid);

JNIEXPORT jstring JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_getProcessExecutablePath
(JNIEnv * env, jclass clazz, jlong processId) {
    return getProcessPath(env, processId);
}

JNIEXPORT jobject JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_getRunningProcesses
  (JNIEnv * env, jclass clazz) {
    int mib[4] = {CTL_KERN, KERN_PROC, KERN_PROC_ALL, 0};
    u_int miblen = 4;
    
    size_t size;
    int st = sysctl(mib, miblen, NULL, &size, NULL, 0);
    
    struct kinfo_proc * process = NULL;
    struct kinfo_proc * newprocess = NULL;
    do {
        size += size / 10;
        newprocess = (kinfo_proc*)realloc(process, size);
        if (!newprocess){
            if (process){
                free(process);
            }
            return NULL;
        }
        process = newprocess;
        st = sysctl(mib, miblen, process, &size, NULL, 0);
        
    } while (st == -1 && errno == ENOMEM);
    if (st == 0){
        if (size % sizeof(struct kinfo_proc) == 0){
            int nprocess = (int)(size / sizeof(struct kinfo_proc));
            jclass procItemClazz = env->FindClass("org/swdc/fx/deploy/system/SystemProc");
            jclass listClazz = env->FindClass("java/util/ArrayList");

            jmethodID listConstructor = env->GetMethodID(listClazz, "<init>", "()V");
            jmethodID procConstructor = env->GetMethodID(procItemClazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;J)V");
            jmethodID addItem = env->GetMethodID(listClazz, "add", "(Ljava/lang/Object;)Z");

            if (nprocess){
                jobject arrayList = env->NewObject(listClazz,listConstructor);
                for (int i = nprocess - 1; i >= 0; i--){
                    int pid = process[i].kp_proc.p_pid;
                    char* processName = getProcessName(pid);
                    if (strlen(processName) > 0 && pid > 0) {
                        jobject item = env->NewObject(procItemClazz,procConstructor,str2jstring(env,processName),getProcessPath(env, pid),(long)pid);
                        env->CallVoidMethod(arrayList,addItem,item);
                    }
                    free(processName);
                }
                free(process);
                return arrayList;
            }
        }
    }
    return NULL;
}

JNIEXPORT jboolean JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_stopProcess
  (JNIEnv * env, jclass clazz, jlong processId) {
      int result = kill(processId, SIGTERM);
      if (result > 0){
          return true;
      }
      return false;
}

jstring str2jstring(JNIEnv* env,const char* pat){
    jclass strClass = (env)->FindClass("Ljava/lang/String;");
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (env)->NewByteArray(strlen(pat));
    (env)->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
    jstring encoding = (env)->NewStringUTF("UTF8");
    return (jstring)(env)->NewObject(strClass, ctorID, bytes, encoding);
}

char* getProcessName(pid_t pid) {
    char pathBuffer [PROC_PIDPATHINFO_MAXSIZE];
    proc_pidpath(pid, pathBuffer, sizeof(pathBuffer));
    
    char *nameBuffer = (char*)malloc(sizeof(char) * 256);
    
    int position = (int)strlen(pathBuffer);
    while(position >= 0 && pathBuffer[position] != '/'){
        position--;
    }
    strcpy(nameBuffer, pathBuffer + position + 1);
    return nameBuffer;
}

jstring getProcessPath(JNIEnv * env,int pid) {
    char infoPath[PROC_PIDPATHINFO_MAXSIZE] = {0};
    int result = proc_pidpath(pid, infoPath, sizeof(infoPath));
    if (result <= 0) {
        return NULL;
    }
    return str2jstring(env, infoPath);
}

int GetModuleFileName( char* sFileName, int nSize){
    int ret = -1;
    char sLine[1024] = { 0 };
    void* pSymbol = (void*)"";
    FILE *fp;
    char *pPath;

    fp = fopen ("/proc/self/maps", "r");
    if ( fp != NULL ){
        while (!feof (fp)){
            unsigned long start, end;
            if ( !fgets (sLine, sizeof (sLine), fp)){
                continue;
            }
            if ( !strstr (sLine, " r-xp ") || !strchr (sLine, '/')){
                continue;
            }
            sscanf (sLine, "%lx-%lx ", &start, &end);
            if (pSymbol >= (void *) start && pSymbol < (void *) end){
                char *tmp;
                size_t len;

                /* Extract the filename; it is always an absolute path */
                pPath = strchr (sLine, '/');

                /* Get rid of the newline */
                tmp = strrchr (pPath, '\n');
                if (tmp) *tmp = 0;
                ret = 0;
                strcpy( sFileName, pPath );
            }
        }
        fclose (fp);
    } 
    return ret;
}
