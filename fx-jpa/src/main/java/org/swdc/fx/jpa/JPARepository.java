package org.swdc.fx.jpa;

import java.util.List;

public interface JPARepository<E, ID> {

    E getOne(ID id);

    List<E> getAll();

    void save(E entry);

    void remove(E entry);

}
