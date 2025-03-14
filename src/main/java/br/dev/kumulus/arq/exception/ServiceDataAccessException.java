package br.dev.kumulus.arq.exception;

/**
 * <p>
 * Uma exceção lançada caso haja alguma falha irreversível durante o acesso a
 * informações do repositório de dados.
 * </p>
 * 
 * <p>
 * Exemplos: Uma consulta mal formada (erro de sintaxe), parâmetros erroneamente
 * tipificados, fonte de dados inacessível entre outros.
 * </p>
 * 
 * @version 0.1.0.Final
 * @since 0.1.0.Final
 */
public class ServiceDataAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceDataAccessException(String message) {
        super(message);
    }

    public ServiceDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceDataAccessException(Throwable cause) {
        super(cause);
    }

}
