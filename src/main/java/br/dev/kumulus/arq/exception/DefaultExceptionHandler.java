package br.dev.kumulus.arq.exception;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.dev.kumulus.arq.commons.utils.FacesUtil;

/**
 * Classe utilitária voltada para o tratamento de exceções que não tenham sido
 * tratadas pela aplicação cliente. Isto ocorre normalmente em casos de exceções
 * inesperadas, em especial exceções não checadas (herdeiras de
 * {@link java.lang.RuntimeException}).
 * 
 * @see javax.faces.context.ExceptionHandlerWrapper
 * 
 * @version 0.2.0 - 2011/06/15
 * @since 0.2.0
 */
public class DefaultExceptionHandler extends ExceptionHandlerWrapper {

    private static Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);
    private ExceptionHandler wrapped;

    public DefaultExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }
   
    @SuppressWarnings("rawtypes")
	@Override
    public void handle() throws FacesException {
        /*
         * Obtem o iterador para percorrer todas as exceções não tratadas
         * (unhandled exceptions), disparadas pela aplicação cliente.
         */
        Iterator i = getUnhandledExceptionQueuedEvents().iterator();
        while (i.hasNext()) {
            ExceptionQueuedEvent event = (ExceptionQueuedEvent) i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

            // Obtendo o objeto que representa a exceção.
            Throwable t = context.getException();
            try {
                // Realiza o tratamento da exceção.
                log.error("Erro inesperado capturado na camada de controle da visão. ", t);
                FacesUtil.addError("Aconteceu um erro inesperado. Contacte os administradores do sistema. Detalhes: " + t.getMessage());
            } finally {
                // Depois de tratada a exceção, elá é removida da fila de exceções.
                i.remove();
            }
        }   
        
        super.handle();
    }
}
