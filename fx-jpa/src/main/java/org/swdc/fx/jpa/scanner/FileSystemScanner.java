package org.swdc.fx.jpa.scanner;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FileSystemScanner implements IPackageScanner {

	private File baseDir;
	private String base;
	private List<Class<?>> result;
	
	public FileSystemScanner(Class<?> baseClass) {
		String packagePath = baseClass.getPackageName().replace('.', File.separatorChar);
		baseDir = new File(baseClass.getResource("").getFile());
		base = baseDir.getAbsolutePath().replace(packagePath, ""); 
	}
	
	public FileSystemScanner(String path) {
		baseDir = new File(path);
		base = path;
	}
	
	@Override
	public List<Class<?>> scanPackage() {
		if (baseDir == null || !baseDir.exists() || baseDir.isFile()) {
			throw new RuntimeException("文件不存在。");
		}
		LinkedList<Class<?>> container = new LinkedList<>();
		this.scanClasses(this::justAdded , container, null);
		result = container;
		return new LinkedList<>(container);
	}

	@Override
	public List<Class<?>> scanAnnotation(Class annotationClazz) {
		if (this.result != null) {
			return  this.result.stream()
					.filter(clazz -> clazz.getAnnotation(annotationClazz) != null)
					.collect(Collectors.toList());
		}
		if (baseDir == null || !baseDir.exists() || baseDir.isFile()) {
			throw new RuntimeException("文件不存在。");
		}
		try {
			LinkedList<Class<?>> container = new LinkedList<>();
			this.scanClasses(this::annotationAdded ,base, baseDir, container, annotationClazz);
			return new LinkedList<>(container);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void scanClasses(ActionOnClassFound founded, String base, File file, List<Class<?>> container, Class<?> reference) throws ClassNotFoundException {
		if (file.isDirectory()) {
			List<File> files = Arrays.asList(file.listFiles());
			for (File elem : files) {
				scanClasses(founded, base, elem,container,reference);
			}
		} else {
			String className = file.getAbsolutePath().replace(base, "");
			if (!className.toLowerCase().endsWith("class") || className.contains("module-info")) {
				return;
			}
			className = className.replace(".class", "");
			if (className.startsWith(File.separator)) {
				className = className.substring(1);
			}
			className = className.replace(File.separatorChar, '.');
			try {
				Class<?> clazz = Class.forName(className);
				founded.accept(clazz, container, reference);
			} catch (Throwable e) {
			}
		}
	}

	@Override
	public void scanClasses(ActionOnClassFound found, List<Class<?>> container, Class<?> reference) {
		try {
			this.scanClasses(found, base, baseDir, container, reference);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
