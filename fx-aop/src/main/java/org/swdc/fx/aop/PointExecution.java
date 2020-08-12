package org.swdc.fx.aop;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class PointExecution {

    private Pattern pattern;

    private AspectLocation location;

    private Advisor advisor;

    private Method invocation;

    private Class annotationWith;

    private int order;

    public int getOrder() {
        return order;
    }

    public Class getAnnotationWith() {
        return annotationWith;
    }

    public void setAnnotationWith(Class annotationWith) {
        this.annotationWith = annotationWith;
    }

    public Advisor getAdvisor() {
        return advisor;
    }

    public AspectLocation getLocation() {
        return location;
    }

    public Method getInvocation() {
        return invocation;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setLocation(AspectLocation location) {
        this.location = location;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setAdvisor(Advisor advisor) {
        this.advisor = advisor;
    }

    public void setInvocation(Method invocation) {
        this.invocation = invocation;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
