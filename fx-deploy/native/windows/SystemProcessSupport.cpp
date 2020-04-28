#include"SystemProcessNative.h"

#include<vector>
#include<Windows.h>
#include <Psapi.h>
#include <TlHelp32.h>

jstring w2js(JNIEnv* env, wchar_t* src);


/*
 * Class:     org_swdc_fx_deploy_system_impl_SystemSupportNative
 * Method:    stopProcess
 * Signature: (J)Z
 */
JNIEXPORT jobject JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_getRunningProcesses
(JNIEnv* env, jclass clazz) {
	jclass procItemClazz = env->FindClass("org/swdc/fx/deploy/system/SystemProc");
	jclass listClazz = env->FindClass("java/util/ArrayList");

	jmethodID listConstructor = env->GetMethodID(listClazz, "<init>", "()V");
	jmethodID procConstructor = env->GetMethodID(procItemClazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;J)V");
	jmethodID addItem = env->GetMethodID(listClazz, "add", "(Ljava/lang/Object;)Z");

	STARTUPINFO st;
	PROCESS_INFORMATION pi;
	PROCESSENTRY32 ps;

	HANDLE hSnapshot;

	ZeroMemory(&st, sizeof(STARTUPINFO));
	ZeroMemory(&pi, sizeof(PROCESS_INFORMATION));
	st.cb = sizeof(STARTUPINFO);
	ZeroMemory(&ps, sizeof(PROCESSENTRY32));
	ps.dwSize = sizeof(PROCESSENTRY32);

	hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);

	if (hSnapshot == INVALID_HANDLE_VALUE) {
		return NULL;
	}

	if (!Process32First(hSnapshot, &ps)) {
		return NULL;
	}

	jobject arrayList = env->NewObject(listClazz, listConstructor);

	do {
		HANDLE process = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, ps.th32ProcessID);

		wchar_t ProcessName[MAX_PATH] = { 0 };
		if (GetModuleFileNameEx(process, NULL, ProcessName, MAX_PATH) == 0) {
			CloseHandle(process);
			continue;
		}

		CloseHandle(process);
		jobject item = env->NewObject(procItemClazz, procConstructor, w2js(env, ps.szExeFile), w2js(env, ProcessName), unsigned long(ps.th32ProcessID));
		env->CallVoidMethod(arrayList, addItem, item);

	} while (Process32Next(hSnapshot, &ps));

	CloseHandle(hSnapshot);
	return arrayList;
}


/*
 * Class:     org_swdc_fx_deploy_system_impl_SystemSupportNative
 * Method:    stopProcess
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_stopProcess
(JNIEnv* env, jclass clazz, jlong processId) {
	HANDLE hProcess = OpenProcess(PROCESS_TERMINATE, FALSE, processId);
	if (hProcess == NULL) {
		return FALSE;
	}
	if (!TerminateProcess(hProcess, 0)) {
		return FALSE;
	}
	return TRUE;
}

/*
 * Class:     org_swdc_fx_deploy_system_impl_SystemProcessNative
 * Method:    getProcessExecutablePath
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_getProcessExecutablePath
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