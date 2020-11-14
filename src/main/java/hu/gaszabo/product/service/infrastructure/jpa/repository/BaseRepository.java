package hu.gaszabo.product.service.infrastructure.jpa.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

@NoRepositoryBean
interface BaseRepository<P, ID> extends PagingAndSortingRepository<P, ID>, QuerydslPredicateExecutor<P> {

	@Override
	List<P> findAll();

	@Override
	List<P> findAll(Predicate predicate);

	@Override
	List<P> findAll(Predicate predicate, OrderSpecifier<?>... orders);

	@Override
	Page<P> findAll(Predicate predicate, Pageable pageable);

}
