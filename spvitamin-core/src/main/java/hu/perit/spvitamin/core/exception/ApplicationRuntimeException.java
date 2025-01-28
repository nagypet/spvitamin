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

package hu.perit.spvitamin.core.exception;

/**
 * @author nagy_peter
 */
public class ApplicationRuntimeException extends RuntimeException implements ApplicationSpecificException {

	private static final long serialVersionUID = 5390293268764684183L;

	private final AbstractMessageType type;

	public ApplicationRuntimeException(AbstractMessageType type) {
		this(type, null);
	}

	public ApplicationRuntimeException(AbstractMessageType type, Throwable cause) {
		super(type.getMessage(), cause);
		this.type = type;
	}

	public ApplicationRuntimeException(ApplicationException ex) {
		super(ex.getMessage(), ex);
		this.type = ex.getType();
	}

	@Override
	public AbstractMessageType getType() {
		return type;
	}
}
