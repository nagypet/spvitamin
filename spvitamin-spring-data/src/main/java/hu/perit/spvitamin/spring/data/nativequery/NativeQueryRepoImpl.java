/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.data.nativequery;

import hu.perit.spvitamin.core.exception.CodingException;
import hu.perit.spvitamin.core.took.Took;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<?> getResultList(String sql, List<Object> params, boolean logSql)
    {
        return getResultList(sql, params, logSql, null);
    }

    public List<?> getResultList(String sql, List<Object> params, boolean logSql, Integer limit)
    {
        if (params == null)
        {
            throw new CodingException("\"params\" cannot be null!");
        }

        try (Took took = new Took(false))
        {
            if (logSql)
            {
                log.debug(sql);
                log.debug("params: " + params.stream().map(Object::toString).collect(Collectors.joining("\n")));
            }
            Query query = this.em.createNativeQuery(sql);
            if (limit != null)
            {
                query.setMaxResults(limit);
            }
            int i = 0;
            for (Object p : params)
            {
                query.setParameter(++i, p);
            }
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
        try (Took took = new Took(logSql))
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
