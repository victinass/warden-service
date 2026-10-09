package com.example.report_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ReportServiceApplication {

    @Bean
    RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String traceId = org.slf4j.MDC.get(TraceIdFilter.TRACE_ID);
            if (traceId != null) {
                request.getHeaders().set(TraceIdFilter.HEADER_NAME, traceId);
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }

    @Bean
    FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}