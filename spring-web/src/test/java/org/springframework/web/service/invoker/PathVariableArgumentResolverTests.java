/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.web.service.invoker;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;


/**
 * Tests for {@link PathVariableArgumentResolver}.
 *
 * @author Olga Maciaszek-Sharma
 */
class PathVariableArgumentResolverTests {

	private final TestHttpClientAdapter clientAdapter = new TestHttpClientAdapter();

	private final Service service = this.clientAdapter.createService(
			Service.class, new PathVariableArgumentResolver(new DefaultConversionService()));


	@Test
	void shouldResolvePathVariableWithNameFromParameter() {
		this.service.execute("test");
		assertPathVariable("id", "test");
	}

	@Test
	void shouldResolvePathVariableWithNameFromAnnotationName() {
		this.service.executeNamed("test");
		assertPathVariable("id", "test");
	}

	@Test
	void shouldResolvePathVariableNameFromValue() {
		this.service.executeNamedWithValue("test");
		assertPathVariable("id", "test");
	}

	@Test
	void shouldOverrideNameIfValuePresentInAnnotation() {
		this.service.executeValueNamed("test");
		assertPathVariable("id", "test");
	}

	@Test
	void shouldResolvePathVariableWithConversion() {
		this.service.execute(Boolean.TRUE);
		assertPathVariable("id", "true");
	}

	@Test
	void shouldResolvePathVariableFromOptionalArgumentWithConversion() {
		this.service.executeOptional(Optional.of(Boolean.TRUE));
		assertPathVariable("id", "true");
	}

	@Test
	void shouldResolvePathVariableFromOptionalArgument() {
		this.service.execute(Optional.of("test"));
		assertPathVariable("id", "test");
	}

	@Test
	void shouldThrowExceptionForNull() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.service.executeNamedWithValue(null));
	}

	@Test
	void shouldThrowExceptionForEmptyOptional() {
		assertThatIllegalArgumentException().isThrownBy(() -> this.service.execute(Optional.empty()));
	}

	@Test
	void shouldIgnoreNullWithConversionServiceWhenNotRequired() {
		this.service.executeNotRequired(null);
		assertThat(getActualUriVariables().get("id")).isNull();
	}

	@Test
	void shouldIgnoreNullWhenNotRequired() {
		this.service.executeNotRequired(null);
		assertPathVariable("id", null);
	}

	@Test
	void shouldIgnoreEmptyOptionalWhenNotRequired() {
		this.service.executeOptionalNotRequired(Optional.empty());
		assertPathVariable("id", null);
	}

	@Test
	void shouldResolvePathVariablesFromMap() {
		this.service.executeValueMap(Map.of("id", "test"));
		assertPathVariable("id", "test");
	}

	@Test
	void shouldResolvePathVariableFromOptionalMapValue() {
		this.service.executeOptionalValueMap(Map.of("id", Optional.of("test")));
		assertPathVariable("id", "test");
	}

	@Test
	void shouldIgnoreNullMapValue() {
		this.service.executeValueMap(null);
		assertThat(getActualUriVariables()).isEmpty();
	}

	@Test
	void shouldThrowExceptionForEmptyOptionalMapValue() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.service.executeOptionalValueMap(Map.of("id", Optional.empty())));
	}

	@SuppressWarnings("SameParameterValue")
	private void assertPathVariable(String name, @Nullable String expectedValue) {
		assertThat(getActualUriVariables().get(name)).isEqualTo(expectedValue);
	}

	private Map<String, String> getActualUriVariables() {
		return this.clientAdapter.getRequestSpec().getUriVariables();
	}


	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private interface Service {

		@HttpExchange
		void execute(@PathVariable String id);

		@HttpExchange
		void executeNotRequired(@Nullable @PathVariable(required = false) String id);

		@HttpExchange
		void executeOptional(@PathVariable Optional<Boolean> id);

		@HttpExchange
		void executeOptionalNotRequired(@PathVariable(required = false) Optional<String> id);

		@HttpExchange
		void executeNamedWithValue(@Nullable @PathVariable(name = "test", value = "id") String employeeId);

		@HttpExchange
		void executeNamed(@PathVariable(name = "id") String employeeId);

		@HttpExchange
		void executeValueNamed(@PathVariable("id") String employeeId);

		@HttpExchange
		void execute(@PathVariable Object id);

		@HttpExchange
		void execute(@PathVariable Boolean id);

		@HttpExchange
		void executeValueMap(@Nullable @PathVariable Map<String, String> map);

		@HttpExchange
		void executeOptionalValueMap(@PathVariable Map<String, Optional<String>> map);
	}

}