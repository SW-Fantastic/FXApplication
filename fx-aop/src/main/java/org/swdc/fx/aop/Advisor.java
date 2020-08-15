package org.swdc.fx.aop;

import net.bytebuddy.matcher.ElementMatchers;
import org.swdc.fx.AppComponent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Advisor extends AppComponent {

    private Map<AspectLocation, List<PointExecution>> executions = new HashMap<>();

    public List<PointExecution> getExecutionPoints(AspectLocation location) {
        return executions.get(location);
    }

    public void addExecution(PointExecution execution) {
        List<PointExecution> executionList = executions.get(execution.getLocation());
        if (executionList == null) {
            executionList = new ArrayList<>();
            executions.put(execution.getLocation(),executionList);
        }
        executionList.add(execution);
    }

    /**
     * 使用正则表达式匹配此类的切点。
     * @param clazz 查找切点的类
     * @return 切点列表
     */
    public Map<Method,List<PointExecution>> getPointcutFor(Class clazz) {
        String clazzPrefix = clazz.getName() + ".";
        Method[] methods = clazz.getMethods();

        List<PointExecution> executionList = executions.values()
                .stream().flatMap(i -> i.stream())
                .collect(Collectors.toList());

        Map<Method,List<PointExecution>> result = new HashMap<>();

        for (Method method: methods) {
            String methodName = clazzPrefix + method.getName();
            List<PointExecution> matchedPoints = executionList.stream()
                    .filter(i -> {
                        if (i.getPattern() != null) {
                            return i.getPattern().matcher(methodName).find();
                        } else if (i.getAnnotationWith() != Annotation.class){
                            return method.getAnnotation(i.getAnnotationWith()) != null;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            if (matchedPoints.size() == 0) {
                continue;
            }
            result.put(method,matchedPoints);
        }
        return result;
    }

}
