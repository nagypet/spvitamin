package hu.perit.spvitamin.spring.data.cache;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWriteBehindCache<T, ID> extends WriteBehindCache<T>
{
    void setRepo(JpaRepository<T, ID> jpaRepository);
}
