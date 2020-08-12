package org.swdc.fx.aop;

import java.lang.reflect.Method;
import java.util.List;

public class ExecutablePoint {

    private Object[] params;

    private Method method;

    private Object instance;

    private ExecutablePoint next;

    public Object process() throws Exception {
        if (next != null) {
            return method.invoke(instance,next);
        } else {
            return method.invoke(instance,params);
        }
    }

    public void setNext(ExecutablePoint next) {
        this.next = next;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }

    public Method getMethod() {
        return method;
    }

    public static ExecutablePoint resolve(List<PointExecution> executions, Object[] param, int index) {
        ExecutablePoint point = new ExecutablePoint();
        point.setParams(param);
        if (index < executions.size() - 1) {
            point.setNext(resolve(executions, param, index + 1));
        }
        point.setInstance(executions.get(index).getAdvisor());
        point.setMethod(executions.get(index).getInvocation());
        return point;
    }

    public static ExecutablePoint getLast(ExecutablePoint point) {
        ExecutablePoint executablePoint = point;
        while (executablePoint.next != null) {
            executablePoint = executablePoint.next;
        }
        return executablePoint;
    }

}
