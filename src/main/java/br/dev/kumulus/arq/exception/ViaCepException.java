package br.dev.kumulus.arq.exception;

public class ViaCepException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ViaCepException(String message) {
		super("Erro ao consultar CEP: " + message);
	}

	public ViaCepException(Throwable cause) {
        super(cause);
    }

}
