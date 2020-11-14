package hu.gaszabo.product.service.infrastructure.component.message;

import java.time.Instant;

public interface IHaveTimestamp {

	default Instant timestamp() {
		return Instant.now();
	}

}
