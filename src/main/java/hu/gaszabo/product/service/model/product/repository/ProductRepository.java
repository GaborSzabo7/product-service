package hu.gaszabo.product.service.model.product.repository;

import org.springframework.stereotype.Repository;

import hu.gaszabo.product.service.infrastructure.jpa.repository.PersistentEntityRepository;
import hu.gaszabo.product.service.model.product.Product;

@Repository
public interface ProductRepository extends PersistentEntityRepository<Product, Long> {

}
