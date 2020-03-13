package org.swdc.fx.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.LifeCircle;
import org.swdc.fx.jpa.anno.Modify;
import org.swdc.fx.jpa.anno.Param;
import org.swdc.fx.jpa.anno.SQLQuery;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultRepository<E, ID> implements InvocationHandler,JPARepository<E, ID>, LifeCircle {

    private ThreadLocal<EntityManager> localEm = new ThreadLocal<>();

    private List<EntityManager> entityManagerList = new ArrayList<>();

    private JPAExtraModule extraModule;

    private Class<E> eClass;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void init(JPAExtraModule module, Class<E> eClass) {
        this.extraModule = module;
        this.eClass = eClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        EntityManager manager = localEm.get();
        if (manager == null) {
            manager = extraModule.getEntityFactory().createEntityManager();
            localEm.set(manager);
            entityManagerList.add(manager);
        }
        String name = method.getName();
        if (name.equals("getOne") || name.equals("getAll") || name.equals("save") || name.equals("remove")) {
            return method.invoke(this,args);
        }
        try {
            Object.class.getMethod(method.getName(),method.getParameterTypes());
            return method.invoke(this,args);
        } catch (Exception ex) {

        }
        Query query = resolveByQuery(manager, method, args);
        Modify modify = method.getAnnotation(Modify.class);
        if (query != null) {
            // 是一个修改的query，需要事务
            if (modify != null) {
                manager.getTransaction().begin();
            }
            try {
                Class returnClazz = method.getReturnType();
                if (Set.class.isAssignableFrom(returnClazz)) {
                    return query.getResultStream().collect(Collectors.toSet());
                } else if (List.class.isAssignableFrom(returnClazz)) {
                    return query.getResultStream().collect(Collectors.toList());
                } else if (Collection.class.isAssignableFrom(returnClazz)) {
                    return query.getResultStream().collect(Collectors.toList());
                } else if (returnClazz == eClass) {
                    return query.getResultList().get(query.getFirstResult());
                } else if (returnClazz == Integer.class || returnClazz == Long.class) {
                    return modify == null ?
                            // 没有modify，普通查询
                            BigDecimal.class.cast(query.getSingleResult()).intValue():
                            // 有modify，进行update
                            query.executeUpdate();
                }
                return null;
            } catch (Exception ex) {
                // 回滚事务
                if (modify != null) {
                    manager.getTransaction().rollback();
                }
                logger.error("fail to execute query: " + method.getName(), ex);
            } finally {
                // 提交事务
                if (modify != null) {
                    manager.getTransaction().commit();
                }
            }

        }
        return null;
    }

    public Query resolveByQuery(EntityManager em, Method method, Object[] args) {
        SQLQuery sqlQuery = method.getAnnotation(SQLQuery.class);
        Query query = em.createQuery(sqlQuery.value(),eClass);
        Parameter[] params = method.getParameters();
        if (query.getParameters().size() != method.getParameters().length) {
            logger.error("can not create query because parameters size dose not matches");
            logger.error("method: " + method.getName());
            return null;
        }
        for (int index = 0; index <params.length; index ++) {
            Param qParam = params[index].getAnnotation(Param.class);
            String name = qParam.value();
            query.setParameter(name,args[index]);
        }
        if (sqlQuery.firstResult() != -1) {
            query.setFirstResult(sqlQuery.firstResult());
        }
        if(sqlQuery.maxResult() != -1) {
            query.setMaxResults(sqlQuery.maxResult());
        }
        return query;
    }

    @Override
    public E getOne(ID id) {
        EntityManager entityManager = localEm.get();
        if (entityManager == null) {
            logger.error("no entity manager at current thread");
            return null;
        }
        return entityManager.find(eClass,id);
    }

    @Override
    public List<E> getAll() {
        EntityManager entityManager = localEm.get();
        if (entityManager == null) {
            logger.error("no entity manager at current thread");
            return new ArrayList<>();
        }
        Query query = entityManager.createQuery("from " + eClass.getSimpleName(),eClass);
        return query.getResultList();
    }

    @Override
    public void save(E entry) {
        EntityManager entityManager = localEm.get();
        entityManager.getTransaction().begin();
        if (entityManager == null) {
            logger.error("no entity manager at current thread");
            return;
        }
        Field idField = getIdField(entry.getClass());
        if (idField == null) {
            logger.error("no id field found");
            return;
        }
        try {
            idField.setAccessible(true);
            Object id = idField.get(entry);
            if (id == null) {
                entityManager.persist(entry);
                entityManager.getTransaction().commit();
                return;
            }
            E entExisted = this.getOne((ID) id);
            if (entExisted == null) {
                idField.set(entry, null);
                entityManager.persist(entry);
                entityManager.getTransaction().commit();
                return;
            }
            entityManager.merge(entry);
            entityManager.getTransaction().commit();
        } catch (Exception ex) {
            logger.error("error persistent entry: " + entry.getClass().getSimpleName(), ex);
        }
        return;
    }

    private Field getIdField(Class target) {
        Class clazz = target;
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                Id id = field.getAnnotation(Id.class);
                if (id == null) {
                    continue;
                } else {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
            if (clazz == Object.class) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void remove(E entry) {
        EntityManager entityManager = localEm.get();
        if (entityManager == null) {
            logger.error("no entity manager at current thread");
            return;
        }
        boolean tx = false;
        if (!entityManager.getTransaction().isActive()) {
            tx = true;
            entityManager.getTransaction().begin();
        }
        entityManager.remove(entry);
        if(tx) {
            entityManager.getTransaction().commit();
        }
    }

    @Override
    public void destroy() {
        for (EntityManager em: entityManagerList){
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
        }
    }
}
