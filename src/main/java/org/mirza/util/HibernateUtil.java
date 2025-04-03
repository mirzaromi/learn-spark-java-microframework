package org.mirza.util;

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.mirza.entity.Post;
import org.mirza.exception.DatabaseException;

public class HibernateUtil {
    @Getter
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            configuration.setProperties(PropertyUtil.getProperties());
            configuration.addAnnotatedClass(Post.class);

            return configuration.buildSessionFactory();
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

}
