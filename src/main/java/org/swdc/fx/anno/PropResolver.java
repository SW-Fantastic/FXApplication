package org.swdc.fx.anno;

public interface PropResolver<T> {

    void resolve(T data) throws Exception;

    String supportName();

    String[] extensions();

}
