package org.swdc.fx.jpa.scanner;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.List;

public interface IPackageScanner {

	/**
	 * 扫描指定目标为基础，所有的class
	 * @return
	 */
	List<Class<?>> scanPackage();

	/**
	 * 扫描含有某注解的类
	 * @param annotationClazz
	 * @return
	 */
	List<Class<?>> scanAnnotation(Class<?> annotationClazz);
	
	/**
	 * 执行扫描的方法
	 * @param found 发现类后的动作
	 * @param container 存放结果的容器
	 * @param reference 参照类（如果需要）
	 */
	void scanClasses(ActionOnClassFound found, List<Class<?>> container, Class<?> reference);
	
	/**
	 * 提供lambda调用，发现一个Class，那么直接加入容器
	 * @param clazz 发现的class
	 * @param container 存放结果的容器
	 * @param reference 参照类
	 */
	default void justAdded(Class<?> clazz, List<Class<?>> container, Class<?> reference) {
		if (isValidClass(clazz)) {
			container.add(clazz);
		}
	}
	
	/**
	 * 提供lambda调用，发现一个class，如果是参照类的子类或实现，就加入容器
	 * @param clazz 发现的class
	 * @param container 存放结果的容器
	 * @param reference 参照类
	 */
	default void assignableAdded(Class<?> clazz, List<Class<?>> container, Class<?> reference) {
		if (isValidClass(clazz) && reference.isAssignableFrom(clazz) ) {
			container.add(clazz);
		}
	}
	
	/**
	 * 提供lambda调用，发现一个class，如果含有参照类的注解，就加入容器
	 * @param clazz 发现的class
	 * @param container 存放结果的容器
	 * @param reference 参照类
	 */
	default void annotationAdded(Class<?> clazz, List<Class<?>> container, Class reference) {
		if (isValidClass(clazz) && clazz.getAnnotation(reference)!= null) {
			container.add(clazz);
		}
	}
	
	
	/**
	 * 获取一个扫描器
	 * @param clazz 被扫描的类
	 * @return
	 */
	public static IPackageScanner getScanner(Class<?> clazz) {
		URL path = clazz.getProtectionDomain().getCodeSource().getLocation();
		if(path.getProtocol().equals("file")) {
			if (path.getPath().toLowerCase().endsWith("jar")) {
				return new ArchivedScanner(clazz);
			} else {
				return new FileSystemScanner(clazz);
			}
		} else {
			return new ArchivedScanner(clazz);
		}
	}
	
	public static IPackageScanner getScanner(String path) {
		if (path.toLowerCase().endsWith("jar")) {
			return new ArchivedScanner(path);
		} else {
			return new FileSystemScanner(path);
		}
	}
	
	static boolean isValidClass(Class<?> clazz) {
		if (clazz.isAnnotation()) {
			return false;
		} else if (clazz.isInterface()) {
			return false;
		} else if (clazz.isAnonymousClass()) {
			return false;
		} else if (Modifier.isAbstract(clazz.getModifiers())) {
			return false;
		}
		return true;
	}
	
}
