package hu.perit.spvitamin.spring.data.nativequery;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import hu.perit.spvitamin.core.took.Took;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeQueryRepoImpl
{
    private static final String TIMEOUT_HINT = "org.hibernate.timeout";
    private final int timeout; // seconds
    private final EntityManager em;

    public NativeQueryRepoImpl(EntityManager em, long timeoutMillis)
    {
        this.em = em;
        this.timeout = (int) (timeoutMillis / 1000);
    }

    public List<?> getResultList(String sql)
    {
        return this.getResultList(sql, true);
    }

    public List<?> getResultList(String sql, boolean logSql)
    {
        try (Took took = new Took(false))
        {
            if (logSql)
            {
                log.debug(sql);
            }
            Query query = this.em.createNativeQuery(sql);
            List<?> resultList = query.setHint(TIMEOUT_HINT, timeout).getResultList();
            if (logSql)
            {
                log.debug(String.format("getResultList() returned %d result(s) in %d ms", resultList.size(), took.getDuration()));
            }
            return resultList;
        }
    }


    public Object getSingleResult(String sql)
    {
        return this.getSingleResult(sql, true);
    }


    public Object getSingleResult(String sql, boolean logSql)
    {
        try (Took took = new Took())
        {
            if (logSql)
            {
                log.debug(sql);
            }
            Query query = this.em.createNativeQuery(sql);
            return query.setHint(TIMEOUT_HINT, timeout).getSingleResult();
        }
    }


    public void executeModifyingQuery(String sql)
    {
        try (Took took = new Took())
        {
            log.debug(sql);
            Query query = this.em.createNativeQuery(sql);
            query.setHint(TIMEOUT_HINT, timeout).executeUpdate();
        }
    }
}
