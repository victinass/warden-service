package com.example.warden_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.slf4j.MDC;

@SpringBootApplication
public class WardenServiceApplication {

	@Bean
	RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add((request, body, execution) -> {
			String traceId = MDC.get(TraceIdFilter.TRACE_ID);
			if (traceId != null) {
				request.getHeaders().set(TraceIdFilter.HEADER_NAME, traceId);
			}
			return execution.execute(request, body);
		});
		return restTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(WardenServiceApplication.class, args);
	}

}