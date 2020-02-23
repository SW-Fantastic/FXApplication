package org.swdc.fx.jpa.scanner;

import java.util.List;

@FunctionalInterface
public interface ActionOnClassFound {
	
	void accept(Class<?> clazz, List<Class<?>> container, Class<?> reference);
	
}