package hu.gaszabo.product.service.infrastructure.web.controller.rest;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class BaseRestController {

	protected BaseRestController() {
	}

	public <R> Callable<ResponseEntity<R>> get(final Supplier<R> call) {
		return () -> ResponseEntity.ok(call.get());
	}

	public Callable<ResponseEntity<Void>> put(final Runnable action) {
		return () -> {
			action.run();
			return ResponseEntity.status(NO_CONTENT).build();
		};
	}

	public <R> Callable<ResponseEntity<R>> post(final Supplier<R> call) {
		return () -> ResponseEntity.ok(call.get());
	}

	public <R extends Resource, I extends InputStream> Callable<ResponseEntity<R>> resource( //
			final Supplier<I> call, //
			final String filename, //
			final String mediaType) {

		return () -> (ResponseEntity<R>) ResponseEntity.ok() //
				.header(CONTENT_DISPOSITION, "attachment; filename=" + filename) //
				.contentType(MediaType.parseMediaType(mediaType)) //
				.body(new InputStreamResource(call.get()));
	}
}
