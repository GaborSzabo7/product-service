package hu.gaszabo.product.service.infrastructure.component;

public interface Publisher<T> {

	void publish(T object);

}
