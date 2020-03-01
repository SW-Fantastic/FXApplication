package org.swdc.fx.jpa;

import org.swdc.fx.ApplicationContainer;
import org.swdc.fx.Container;
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

    @Override
    protected <R extends JPARepository> R instance(Class<R> target) {
        DefaultRepository repository = new DefaultRepository();
        ParameterizedType parameterizedType = (ParameterizedType) target.getGenericInterfaces()[0];

        Class entityClass = (Class) parameterizedType.getActualTypeArguments()[0];

        repository.init(this, entityClass);
        JPARepository jpaRepository = (JPARepository) Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{target},repository);
        handlers.add(repository);
        return (R)jpaRepository;
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

    public Object postProcess(JPARepository jpaRepository) {
        return jpaRepository;
    }

    public void disposeOnComponent(JPARepository jpaRepository) {

    }
}
