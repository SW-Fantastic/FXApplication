package org.swdc.fx.jpa;

import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.container.Container;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.scanner.IPackageScanner;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JPAExtraModule extends ExtraModule<Object> {

    private EntityManagerFactory entityFactory;

    // ThreadLocal的原理运用，以线程为key维护他们的EntityManager
    private static Map<Thread, EntityManager> localEm = new ConcurrentHashMap<>();

    private List<DefaultRepository> handlers = new ArrayList<>();

    @Override
    protected Object instance(Class target) {
        if (target == JPAService.class) {
            return new JPAService(this);
        }
        DefaultRepository repository = new DefaultRepository();
        ParameterizedType parameterizedType = (ParameterizedType) target.getGenericInterfaces()[0];

        Class entityClass = (Class) parameterizedType.getActualTypeArguments()[0];

        repository.init(this, entityClass);
        JPARepository jpaRepository = (JPARepository) Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{target},repository);
        handlers.add(repository);
        return jpaRepository;
    }

    @Override
    public boolean isComponentOf(Class aClass) {
        return JPARepository.class.isAssignableFrom(aClass) || aClass == JPAService.class;
    }

    public EntityManagerFactory getEntityFactory() {
        return entityFactory;
    }

    public boolean initialize(ApplicationContainer applicationContainer) {
        try {
            logger.info("hibernate jpa module active.");
            Object app = applicationContainer.getApplication();
            Module appModule = app.getClass().getModule();
            Properties properties = new Properties();
            InputStream inputStream = appModule.getResourceAsStream("hibernate.properties");
            if (inputStream == null) {
                inputStream = JPAExtraModule.class.getModule().getResourceAsStream("hibernate.properties");
            }
            properties.load(inputStream);
            inputStream.close();

            IPackageScanner scanner = IPackageScanner.getScanner(app.getClass());
            List<Class<?>> classes = scanner.scanAnnotation(Entity.class);

            properties.put(org.hibernate.jpa.AvailableSettings.LOADED_CLASSES,classes);

            entityFactory = Persistence.createEntityManagerFactory("default", properties);
            logger.info("database is ready.");
        } catch (Exception ex) {
            logger.error("can not create entity manager:" ,ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean destroy(ApplicationContainer applicationContainer) {
        for(DefaultRepository repository :this.handlers) {
            repository.destroy();
        }
        for (EntityManager em: localEm.values()){
            if (em.getTransaction().isActive()) {
                try {
                    em.getTransaction().commit();
                } catch (Exception e) {
                    logger.error("fail to commit transaction",e);
                }
            }
            em.close();
        }
        this.entityFactory.close();
        return true;
    }

    @Override
    public <T extends Container> boolean support(Class<T> aClass) {
       return false;
    }

    @Override
    public Object postProcess(Object instance) {
        return instance;
    }

    public void disposeOnComponent(Object jpaRepository) {

    }

    public EntityManager getEntityManager() {
        // 检测并移除结束的线程的entityManager。
        List<Thread> deadThreads = localEm.keySet()
                .stream()
                .filter(i -> !i.isAlive())
                .collect(Collectors.toList());
        for (Thread deadThread: deadThreads) {
            EntityManager manager = localEm.get(deadThread);
            if (manager.isOpen()) {
                if (manager.getTransaction().isActive()) {
                    try {
                        manager.getTransaction().commit();
                    } catch (Exception e){
                        logger.error("fail to commit transaction of one dead thread.", e);
                    }
                }
                manager.clear();
                manager.close();
            }
            localEm.remove(deadThread);
        }

        EntityManager entityManager = localEm.get(Thread.currentThread());
        if (entityManager == null) {
            entityManager = entityFactory.createEntityManager();
            localEm.put(Thread.currentThread(),entityManager);
        }
        return entityManager;
    }

}
