package hu.perit.spvitamin.spring.data.pessimistic;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PessimisticJpaRepository<T, ID> extends JpaRepository<T, ID>
{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    default Optional<T> findByIdWithWriteLock(ID id)
    {
        return findById(id);
    }


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    default List<T> findAllByIdWithWriteLock(Iterable<ID> ids)
    {
        return findAllById(ids);
    }
}
