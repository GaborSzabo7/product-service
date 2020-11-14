package hu.gaszabo.product.service.infrastructure.web.controller.rest;

import static hu.gaszabo.product.service.infrastructure.util.RetryUtil.retry;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionalBaseRestController {

	@Value("${optimistic-lock.retry.count:3}")
	private int retryCount;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	protected TransactionalBaseRestController() {
		transactionTemplate = new TransactionTemplate(requireNonNull(transactionManager, "transactionManager can't be null"));
		transactionTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
		transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
	}

	public <R> Callable<ResponseEntity<R>> get(final Supplier<R> call) {
		return () -> ResponseEntity.ok(withResult(call));
	}

	public Callable<ResponseEntity<Void>> put(final Runnable action) {
		return () -> {
			retry( //
					() -> withoutResult(action), //
					() -> log.warn("Retry to invoke action again because of Optimistic Lock"), //
					retryCount);
			return ResponseEntity.status(NO_CONTENT).build();
		};
	}

	public <R> Callable<ResponseEntity<R>> post(final Supplier<R> call) {
		return () -> ResponseEntity.ok( //
				retry( //
						() -> withResult(call), //
						() -> log.warn("Retry to invoke action again because of Optimistic Lock"), //
						retryCount));
	}

	private <R> R withResult(final Supplier<R> call) {
		return transactionTemplate.execute(status -> call.get());
	}

	private void withoutResult(final Runnable action) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				action.run();
			}
		});
	}
}
