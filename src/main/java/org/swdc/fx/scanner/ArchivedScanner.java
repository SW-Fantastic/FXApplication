package org.swdc.fx.scanner;

import java.io.File;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;


public class ArchivedScanner implements IPackageScanner{

	
	private URL packageURL;
	private String packagePath;
	private List<Class<?>> result;
	
	/**
	 * 初始化压缩格式扫描器
	 * @param clazz 从这个类所在的包开始
	 */
	public ArchivedScanner(Class<?> clazz) {
		URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
		packagePath = location.getPath();
		packageURL = location;
	}
	
	public ArchivedScanner(String path) {
		try {
			URL location = new File(path).toURI().toURL();
			packagePath = location.getPath();
			packageURL = location;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<Class<?>> scanPackage() {
		LinkedList<Class<?>> container = new LinkedList<>();
		this.scanClasses(this::justAdded, container, null);
		this.result = container;
		return new LinkedList<>(container);
	}

	@Override
	public List<Class<?>> scanAnnotation(Class annotationClazz) {
		if (result != null) {
			return result.stream()
					.filter(cls -> cls.getAnnotation(annotationClazz) != null)
					.collect(Collectors.toList());
		}
		LinkedList<Class<?>> container = new LinkedList<>();
		this.scanClasses(this::annotationAdded, container, annotationClazz);
		return new LinkedList<>(container);
	}

	@Override
	public List<Class<?>> scanSubClass(Class<?> parent) {
		if (result != null) {
			return result.stream()
					.filter(parent::isAssignableFrom)
					.collect(Collectors.toList());
		}
		LinkedList<Class<?>> container = new LinkedList<>();
		this.scanClasses(this::assignableAdded, container, null);
		return new LinkedList<>(container);
	}

	/**
	 * 进行类扫描
	 * @param whenClassScaned 发现class后要做的事情
	 * @param container 存放结果的容器
	 * @param reference 参照类
	 */
	public void scanClasses(ActionOnClassFound whenClassScaned, List<Class<?>> container, Class<?> reference) {
		try {
			if (packageURL.getProtocol().equals("file")) {
				packageURL = new URL("jar:"+packageURL.toExternalForm() + "!/");
				packagePath = "jar:" + packagePath + "!/";
			}
			JarURLConnection jarConn = (JarURLConnection)packageURL.openConnection();
			JarFile jar = jarConn.getJarFile();
			Enumeration<JarEntry> itr = jar.entries();
			while(itr.hasMoreElements()) {
				JarEntry ent = itr.nextElement();
				if(!ent.isDirectory() && ent.getName().endsWith("class") && !ent.getName().contains("module-info")) {
					try {
						String clazzName = ent.getName().replaceAll("/", ".").replace(".class", "");
						Class<?> cls = Class.forName(clazzName);
						whenClassScaned.accept(cls, container, reference);
					}catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
