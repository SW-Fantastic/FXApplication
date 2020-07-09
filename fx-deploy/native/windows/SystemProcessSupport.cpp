#include"SystemProcessNative.h"

#include<vector>
#include<Windows.h>
#include <Psapi.h>
#include <TlHelp32.h>
#include <tchar.h>

jstring w2js(JNIEnv* env, wchar_t* src);
wchar_t* js2w(JNIEnv* env, jstring str);


/*
 * Class:     org_swdc_fx_deploy_system_impl_SystemSupportNative
 * Method:    getRunningProcesses
 * Signature: (J)Z
 * 获取运行中的进程列表
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
 * 停止指定的pid对应的进程
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
 * 获取此pid进程对应的可执行文件的路径
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

/*
   * Class:     org_swdc_fx_deploy_system_impl_SystemSupportNative
   * Method:    uninstallSystemModule
   * Signature: (J)Ljava/lang/Boolean;
   * 从系统中移除此模块的注册信息（DLLRegsvr服务）
   */
JNIEXPORT jboolean JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_uninstallSystemModule
(JNIEnv* env, jclass clazz, jstring path) {
	if (path == NULL) {
		return false;
	}
	const wchar_t* wPath = js2w(env, path);
	HINSTANCE moduleInstance = LoadLibrary(wPath);
	delete wPath;
	if (moduleInstance == NULL) {
		FreeLibrary(moduleInstance);
		return false;
	}
	FARPROC dllEntryPoint = GetProcAddress(moduleInstance, "DllUnregisterServer");
	if (dllEntryPoint != NULL && S_OK == (*dllEntryPoint)()) {
		FreeLibrary(moduleInstance);
		return true;
	}
	FreeLibrary(moduleInstance);
	return false;
}

/*
   * Class:     org_swdc_fx_deploy_system_impl_SystemSupportNative
   * Method:    installSystemModule
   * Signature: (J)Ljava/lang/Boolean;
   * 向系统注册一个新的模块（DLLRegsvr服务）
   */
JNIEXPORT jboolean JNICALL Java_org_swdc_fx_deploy_system_impl_SystemSupportNative_installSystemModule
(JNIEnv* env, jclass clazz, jstring path) {
	if (path == NULL) {
		return false;
	}
	const wchar_t* wPath = js2w(env, path);
	HINSTANCE moduleInstance = LoadLibrary(wPath);
	delete wPath;
	if (moduleInstance == NULL) {
		FreeLibrary(moduleInstance);
		return false;
	}
	FARPROC dllEntryPoint = GetProcAddress(moduleInstance, "DllRegisterServer");
	if (dllEntryPoint != NULL && S_OK == (*dllEntryPoint)()) {
		FreeLibrary(moduleInstance);
		return true;
	}
	FreeLibrary(moduleInstance);
	return false;
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

wchar_t* js2w(JNIEnv* env, jstring str) {
	const char* nStr = env->GetStringUTFChars(str, NULL);
	const int size = env->GetStringLength(str);
	wchar_t* wStr = new wchar_t[size];
	memset(wStr, 0, sizeof(wchar_t) * size);
	int charCounts = MultiByteToWideChar(CP_UTF8, 0, nStr, -1, wStr, 0);
	MultiByteToWideChar(CP_UTF8, 0, nStr, -1, wStr, charCounts);
	env->ReleaseStringUTFChars(str, nStr);
	return wStr;
}