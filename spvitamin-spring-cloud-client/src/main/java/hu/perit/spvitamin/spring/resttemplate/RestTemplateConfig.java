/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.resttemplate;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RestTemplateConfig
{

	private final ObjectMapper objectMapper;

	public RestTemplateConfig(ObjectMapper objectMapper)
	{
		this.objectMapper = objectMapper;
	}


	@Profile("resttemplate-loadbalanced")
	@LoadBalanced
	@Bean
	RestTemplate restTemplateWithLoadBalance()
	{
		return this.createRestTemplate();
	}


	@Profile("!resttemplate-loadbalanced")
	@Bean
	RestTemplate restTemplateWithoutLoadBalance()
	{
		return this.createRestTemplate();
	}


	private RestTemplate createRestTemplate()
	{
		/*
		 * HttpComponentsClientHttpRequestFactory requestFactory = new
		 * HttpComponentsClientHttpRequestFactory(); // TODO: read from config
		 * requestFactory.setConnectTimeout(5000);
		 * requestFactory.setReadTimeout(120000);
		 * 
		 * RestTemplate restTemplate = new RestTemplate(requestFactory);
		 */
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, createMappingJacksonHttpMessageConverter());
		restTemplate.setErrorHandler(new RestTemplateErrorHandler());
		return restTemplate;
	}


	private MappingJackson2HttpMessageConverter createMappingJacksonHttpMessageConverter()
	{
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(this.objectMapper);
		return converter;
	}
}
