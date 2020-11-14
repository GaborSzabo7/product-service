package hu.gaszabo.product.service.infrastructure.jpa.repository;

import org.springframework.data.repository.NoRepositoryBean;

import hu.gaszabo.product.service.infrastructure.jpa.persistentobject.PersistentEntity;

@NoRepositoryBean
public interface PersistentEntityRepository<E extends PersistentEntity, ID> extends BaseRepository<E, ID> {

}
