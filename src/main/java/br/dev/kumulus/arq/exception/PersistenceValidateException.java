/*
 * Copyright (c) 2011, Tribunal de Justiça do RN. 
 * Todos os direitos reservados.
 */
package br.dev.kumulus.arq.exception;

import java.util.Collection;

/**
 * Uma exceção que representa um erro de validação, no qual determinado atributo
 * sofreu alguma violação, como por exemplo:
 * <ul>
 * <li>atributo obrigatório não informado.</li>
 * <li>atributo com valor ou faixa de valores inválido.</li>
 * <li>atributo com tamanho fora do esperado.</li>
 * </ul>
 * 
 */

public class PersistenceValidateException extends Exception {

	private static final long serialVersionUID = 1L;

	private Collection<String> messages;

	public PersistenceValidateException(String message) {
		super(message);
	}

	public PersistenceValidateException(String message, Throwable cause) {
		super(message);
	}

	public PersistenceValidateException(Collection<String> messages) {
		setMessages(messages);
	}

	public PersistenceValidateException(Throwable cause) {
		super(cause);
	}

	public Collection<String> getMessages() {
		return messages;
	}

	public void setMessages(Collection<String> messages) {
		this.messages = messages;
	}

	public String getMessage() {
		if (messages == null) {
			return super.getMessage();
		}
		String retorno = "";
		for (String msg : messages) {
			retorno += msg + " ";
		}
		return retorno.trim();
	}

}
