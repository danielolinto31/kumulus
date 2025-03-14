package br.dev.kumulus.arq.exception;

/**
 * Uma exceção de negócio, que é lançada da camada <tt>service</tt> e exposta para os
 * clientes da camada quando uma regra de negócio é violada.
 * Exemplos:
 * <ul>
 * <li>Atributo obrigatório não informado, com valor inválido, fora do esperado etc.</li>
 * <li>Dependências de negócio, como por exemplo "O aluno está com débitos, e por isso não pode fazer nova matrícula."</li>
 * </ul>
 * 
 */
public class ServiceBusinessException extends Exception {

	private static final long serialVersionUID = 1L;

	public ServiceBusinessException(String message) {
        super(message);
    }

    public ServiceBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceBusinessException(Throwable cause) {
        super(cause);
    }

}
