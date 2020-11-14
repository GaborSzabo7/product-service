package hu.gaszabo.product.service.infrastructure.web.controller.advice;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ErrorMessage {

	@JsonProperty("error")
	private final String errorMessage;

}
