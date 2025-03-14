package br.dev.kumulus.arq.exception;

/**
 * <p>
 * Uma exceção lançada caso haja alguma falha irreversível durante a geração de
 * um relatório utilizando JasperReport.
 * </p>
 *
 * @version 0.1.0
 * @since 0.1.0
 */
public class MimeTypeException  extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MimeTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public MimeTypeException(String msg) {
        super(msg);
    }

}
