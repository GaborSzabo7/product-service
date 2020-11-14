package hu.gaszabo.product.service.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class HttpMessageConverterConfiguration {

	@Autowired
	private ObjectMapper objectMapper;

	@Bean
	public HttpMessageConverters customConverters() {
		return new HttpMessageConverters((HttpMessageConverter<?>) new MappingJackson2HttpMessageConverter(objectMapper));
	}

}
