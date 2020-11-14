package hu.gaszabo.product.service.infrastructure.web.controller.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	public ErrorMessage resourceNotFoundException(Exception ex, WebRequest request) {
		return new ErrorMessage(getErrorMessages(ex));
	}

	private String getErrorMessages(Exception e) {
		if (e == null) {
			return "";
		}

		Throwable t = e.getCause();
		String errorMessages = e.getMessage();
		while (t != null) {
			errorMessages += ", ";
			errorMessages += t.getMessage();
			t = t.getCause();
		}
		return errorMessages;
	}

}
