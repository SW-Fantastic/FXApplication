package org.swdc.fx.jpa;

import org.swdc.fx.ApplicationContainer;
import org.swdc.fx.Container;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;
import org.swdc.fx.extra.ExtraModule;
import org.swdc.fx.jpa.scanner.IPackageScanner;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.*;

public class JPAExtraModule extends ExtraModule<JPARepository> {

    private EntityManagerFactory entityFactory;

    private Map<Class, JPARepository> repositoryMap = new HashMap<>();

    private List<DefaultRepository> handlers = new ArrayList<>();

    public <R extends JPARepository> R getComponent(Class<R> aClass) {
        if (!isComponentOf(aClass)) {
            return null;
        }
        if (repositoryMap.containsKey(aClass)) {
            return (R) repositoryMap.get(aClass);
        }
        for (Class clazzItem : repositoryMap.keySet()) {
            if (aClass.isAssignableFrom(clazzItem)) {
                return (R)repositoryMap.get(clazzItem);
            }
        }
        return (R) register(aClass);
    }

    public <R extends JPARepository> JPARepository register(Class<R> aClass) {
        DefaultRepository repository = new DefaultRepository();
        ParameterizedType parameterizedType = (ParameterizedType) aClass.getGenericInterfaces()[0];

        Class entityClass = (Class) parameterizedType.getActualTypeArguments()[0];

        repository.init(this, entityClass);
        JPARepository jpaRepository = (JPARepository) Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{aClass},repository);
        Scope scope = aClass.getAnnotation(Scope.class);
        if (scope == null || scope.value() == ScopeType.SINGLE) {
            repositoryMap.put(aClass,jpaRepository);
            logger.info("repository loaded: " + aClass.getSimpleName());
        }
        handlers.add(repository);
        return jpaRepository;
    }

    public List<JPARepository> listComponents() {
        return new ArrayList<>(repositoryMap.values());
    }

    @Override
    public boolean isComponentOf(Class aClass) {
        return JPARepository.class.isAssignableFrom(aClass);
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

    public boolean destroy(ApplicationContainer applicationContainer) {
        for(DefaultRepository repository :this.handlers) {
            repository.destroy();
        }
        this.entityFactory.close();
        return true;
    }

    public <T extends Container> boolean support(Class<T> aClass) {
        return false;
    }

    public void activeOnComponent(JPARepository jpaRepository) {

    }

    public void disposeOnComponent(JPARepository jpaRepository) {

    }
}
