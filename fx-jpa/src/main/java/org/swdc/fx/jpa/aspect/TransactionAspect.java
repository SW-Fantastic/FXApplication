package org.swdc.fx.jpa.aspect;

import org.swdc.fx.anno.Order;
import org.swdc.fx.aop.Advisor;
import org.swdc.fx.aop.ExecutablePoint;
import org.swdc.fx.aop.anno.Around;
import org.swdc.fx.jpa.JPAExtraModule;
import org.swdc.fx.jpa.anno.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class TransactionAspect extends Advisor {

    @Order(-999)
    @Around(annotationWith = Transactional.class)
    public Object transaction(ExecutablePoint point) {
        boolean closeAfterCommit = false;
        JPAExtraModule extraModule = getExtraModule(JPAExtraModule.class);
        EntityManager manager = extraModule.getEntityManager();
        // EntityManager本身就是线程相关的，所以获取之后可以直接使用
        EntityTransaction transaction = manager.getTransaction();
        try {
            if (transaction.isActive()) {
                return point.process();
            } else {
                closeAfterCommit = true;
                transaction.begin();
            }
            return point.process();
        } catch (Exception e) {
            logger.error("fail to process transaction method: ",e);
            transaction.rollback();
            return null;
        } finally {
            transaction.commit();
            if (closeAfterCommit) {
                manager.close();
            }
        }
    }

}
