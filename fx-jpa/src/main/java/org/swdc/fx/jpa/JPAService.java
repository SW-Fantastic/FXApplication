package org.swdc.fx.jpa;

import org.swdc.fx.AppComponent;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class JPAService extends AppComponent {

    private JPAExtraModule extraModule;

    public JPAService(JPAExtraModule module) {
        this.extraModule = module;
    }

    public void withTranscation(Runnable runnable) {
        EntityManager entityManager = extraModule.getEntityManager();
        EntityTransaction tx = entityManager.getTransaction();
        if (tx.isActive()) {
            runnable.run();
            return;
        }
        try {
            tx.begin();
            runnable.run();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            tx.commit();
        }
    }

}
