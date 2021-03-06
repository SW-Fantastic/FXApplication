package org.swdc.fx.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.swdc.fx.anno.Order;
import org.swdc.fx.aop.anno.After;
import org.swdc.fx.aop.anno.AfterReturning;
import org.swdc.fx.aop.anno.Around;
import org.swdc.fx.aop.anno.Before;
import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.container.Container;
import org.swdc.fx.extra.ExtraLoader;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.scanner.IPackageScanner;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AspectExtraModule extends ExtraModule<Advisor> {

    @Override
    protected <R extends Advisor> R instance(Class<R> target) {
        try {

            Advisor advisor = target.getConstructor().newInstance();
            Method[] methods = target.getMethods();

            for (Method item : methods) {
                Order order = item.getAnnotation(Order.class);
                PointExecution execution = new PointExecution();
                if (order != null) {
                    execution.setOrder(order.value());
                }
                if (item.getAnnotation(Before.class) != null) {
                    Before before = item.getAnnotation(Before.class);
                    if (before.pattern().equals("") && before.annotationWith() == Annotation.class) {
                        continue;
                    } else if (before.annotationWith() != Annotation.class) {
                        execution.setAnnotationWith(before.annotationType());
                    } else if (!before.pattern() .equals("")) {
                        execution.setPattern(Pattern.compile(before.pattern()));
                    }
                    execution.setLocation(AspectLocation.BEFORE);
                    execution.setAdvisor(advisor);
                    execution.setInvocation(item);
                    advisor.addExecution(execution);
                } else if (item.getAnnotation(After.class) != null) {
                    After after = item.getAnnotation(After.class);
                    if (after.annotationWith() == Annotation.class && after.pattern().equals("")) {
                        continue;
                    } else if (after.annotationWith() != Annotation.class) {
                        execution.setAnnotationWith(after.annotationType());
                    } else if (!after.pattern().equals("")) {
                        execution.setPattern(Pattern.compile(after.pattern()));
                    }
                    execution.setLocation(AspectLocation.AFTER);
                    execution.setAdvisor(advisor);
                    execution.setInvocation(item);
                    advisor.addExecution(execution);
                } else if (item.getAnnotation(Around.class) != null) {
                    Around around = item.getAnnotation(Around.class);
                    if (around.annotationWith() == Annotation.class && around.pattern().equals("")) {
                        continue;
                    } else if (around.annotationWith() !=  Annotation.class){
                        execution.setAnnotationWith(around.annotationWith());
                    } else if (!around.pattern().equals("")) {
                        execution.setPattern(Pattern.compile(around.pattern()));
                    }
                    execution.setLocation(AspectLocation.AROUND);
                    execution.setAdvisor(advisor);
                    execution.setInvocation(item);
                    advisor.addExecution(execution);
                } else if (item.getAnnotation(AfterReturning.class) != null) {
                    AfterReturning afterReturning = item.getAnnotation(AfterReturning.class);
                    if (afterReturning.annotationWith() == Annotation.class && afterReturning.pattern().equals("")) {
                        continue;
                    } else if (afterReturning.annotationWith() != Annotation.class){
                        execution.setAnnotationWith(afterReturning.annotationWith());
                    } else if (!afterReturning.pattern().equals("")) {
                        execution.setPattern(Pattern.compile(afterReturning.pattern()));
                    }
                    execution.setLocation(AspectLocation.AFTER_RETURN);
                    execution.setAdvisor(advisor);
                    execution.setInvocation(item);
                    advisor.addExecution(execution);
                }
            }
            return (R) advisor;
        } catch (Exception e) {
            logger.error("can not instance advisor : " + target.getName());
            return null;
        }
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return Advisor.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean initialize(ApplicationContainer container) {
        logger.info("aspect module active.");
        Object app = container.getApplication();
        IPackageScanner scanner = IPackageScanner.getScanner(app.getClass());
        // 扫描并且初始化切面
        List<Class<?>> classes = scanner.scanSubClass(Advisor.class);
        for (Class clazz: classes) {
            this.register(clazz);
        }
        ServiceLoader<ExtraLoader> loaders = ServiceLoader.load(ExtraLoader.class);
        List<Class> moduleClasses = loaders.stream()
                .map(ServiceLoader.Provider::get)
                .map(ExtraLoader::getModuleClass)
                .collect(Collectors.toList());
        for (Class moduleClazz : moduleClasses) {
            IPackageScanner extraScanner = IPackageScanner.getScanner(moduleClazz);
            List<Class<?>> extraAdvisors = extraScanner.scanSubClass(Advisor.class);
            for (Class item: extraAdvisors) {
                this.register(item);
            }
        }
        logger.info("aspect module initialized.");
        return true;
    }

    @Override
    public boolean destroy(ApplicationContainer container) {
        return false;
    }

    @Override
    public <T extends Container> boolean support(Class<T> container) {
        return true;
    }

    @Override
    public Object postProcess(Object comp) {
        List<Advisor> advisors = this.listComponents();
        if (advisors == null ||advisors.size() == 0) {
            return comp;
        }
        Map<Method, List<PointExecution>> executions = new HashMap<>();
        for (Advisor advisor : advisors) {
            if (advisor == null) {
                continue;
            }
            Map<Method,List<PointExecution>> matched = advisor.getPointcutFor(comp.getClass());
            for (Map.Entry<Method,List<PointExecution>> ent: matched.entrySet()) {
                if (executions.containsKey(ent.getKey())) {
                    List<PointExecution> existed = executions.get(ent.getKey());
                    existed.addAll(ent.getValue());
                } else {
                    List<PointExecution> target = new ArrayList<>();
                    executions.put(ent.getKey(),target);
                    target.addAll(ent.getValue());
                }
            }
        }
        if (executions.size() == 0){
            return comp;
        }
        try {
            ByteBuddyHandler handler = new ByteBuddyHandler(comp,executions);
            // 进行代理
            ByteBuddy byteBuddy = new ByteBuddy();
            Object proxied = byteBuddy.subclass(comp.getClass())
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(handler))
                    .make()
                    .load(comp.getClass().getModule().getClassLoader())
                    .getLoaded()
                    .getConstructor()
                    .newInstance();
            logger.info("proxied " + comp.getClass());
            return proxied;
        } catch (Exception e) {
            logger.error("fail to proxy",e);
            return null;
        }
    }

    @Override
    public void disposeOnComponent(Object comp) {

    }
}
