package com.zakiis.boot.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.zakiis.boot.autoconfigure.properties.TraceIdProperties;
import com.zakiis.spring.interceptor.traceid.TraceIdClientHttpRequestFilter;
import com.zakiis.spring.interceptor.traceid.TraceIdFeignRequestFilter;
import com.zakiis.spring.interceptor.traceid.TraceIdHttpRequestFilter;

import feign.RequestInterceptor;

@Configuration
@ConditionalOnProperty(name = "log.trace-id.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TraceIdProperties.class)
public class TraceIdAutoConfiguration {

	static Logger log = LoggerFactory.getLogger(TraceIdAutoConfiguration.class);
	
	@Bean("zakiis_traceIdFilter")
	public TraceIdHttpRequestFilter traceIdFilter(TraceIdProperties props) {
		log.info("Trace id filter for http request enabled.");
		TraceIdHttpRequestFilter traceIdFilter = new TraceIdHttpRequestFilter(props.getAppName(), props.getHeader());
		return traceIdFilter;
	}
	
	@Configuration
	@ConditionalOnClass(RequestInterceptor.class)
	protected static class FeignClientTraceIdConfiguration {

		@Bean
		public TraceIdFeignRequestFilter feignTraceIdFilter(TraceIdProperties props) {
			log.info("Trace id filter for feign client request enabled.");
			return new TraceIdFeignRequestFilter(props.getAppName(), props.getHeader());
		}
	}
	
	@Configuration
	@ConditionalOnClass(RestTemplate.class)
	protected static class HttpClientTraceIdConfiguration {

		@Bean
		public TraceIdClientHttpRequestFilter clientHttpTraceIdFilter(TraceIdProperties props) {
			log.info("Trace id filter for http client request enabled.");
			return new TraceIdClientHttpRequestFilter(props.getAppName(), props.getHeader());
		}
		
		@Bean
		public RestTemplateCustomizer traceIdRestTemplateCustomizer(TraceIdClientHttpRequestFilter filter) {
			return new RestTemplateCustomizer() {
				@Override
				public void customize(RestTemplate restTemplate) {
					if (!restTemplate.getInterceptors().contains(filter)) {
						restTemplate.getInterceptors().add(filter);
					}
				}
			};
		}
	}
}
