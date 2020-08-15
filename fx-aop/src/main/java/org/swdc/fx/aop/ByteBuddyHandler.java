package org.swdc.fx.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ByteBuddyHandler implements InvocationHandler {

    private Map<Method, List<PointExecution>> executions;

    private Object originalTarget;

    public ByteBuddyHandler(Object target,Map<Method,List<PointExecution>> executions) {
        this.executions = executions;
        this.originalTarget = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<PointExecution> advisorPoints = executions.get(method);
        if (advisorPoints == null) {
            return method.invoke(originalTarget,args);
        }
        Map<AspectLocation,List<PointExecution>> executionMap = advisorPoints
                .stream()
                .collect(Collectors.groupingBy(PointExecution::getLocation));
        try {
            if (!executions.containsKey(method)) {
                return method.invoke(originalTarget,args);
            }
            Object result = null;

            List<PointExecution> before = executionMap.get(AspectLocation.BEFORE);
            if (before != null && before.size() > 0) {
                before.sort(Comparator.comparingInt(PointExecution::getOrder));
                for (PointExecution execution: before) {
                    execution.getInvocation().invoke(execution.getAdvisor(),args);
                }
            }
            List<PointExecution> around = executionMap.get(AspectLocation.AROUND);
            if (around != null && around.size() > 0) {
                around.sort(Comparator.comparingInt(PointExecution::getOrder));
                ExecutablePoint point = ExecutablePoint.resolve(originalTarget,method,around,args,0);
                result = point.process();
            } else {
                result = method.invoke(originalTarget,args);
            }
            List<PointExecution> after = executionMap.get(AspectLocation.AFTER);
            if (after != null && after.size() > 0) {
                after.sort(Comparator.comparingInt(PointExecution::getOrder));
                for (PointExecution execution: after) {
                    execution.getInvocation().invoke(execution.getAdvisor(),args);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            List<PointExecution> executions = executionMap.get(AspectLocation.AFTER_RETURN);
            if (executions != null && executions.size() > 0) {
                executions.sort(Comparator.comparingInt(PointExecution::getOrder));
                for (PointExecution execution: executions) {
                    execution.getInvocation().invoke(execution.getAdvisor(),args);
                }
            }
        }
    }
}
