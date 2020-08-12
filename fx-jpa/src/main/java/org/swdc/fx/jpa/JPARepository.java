package org.swdc.fx.jpa;

import java.util.Collection;
import java.util.List;

public interface JPARepository<E, ID> {

    E getOne(ID id);

    List<E> getAll();

    E save(E entry);

    void remove(E entry);

    void removeAll(Collection<E> entities);

}
