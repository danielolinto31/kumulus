package br.dev.kumulus.arq.persistence.hibernate;

import br.dev.kumulus.arq.exception.DataAccessException;

/**
 * <p>
 * Classe que tem por objetivo gerar uma exeção {@link DataAccessException} com
 * uma mensagem de erro amigável através do tratamento da família de exceções
 * {@link org.springframework.dao.DataAccessException} oriunda do Spring.
 * </p>
 * 
 * <p>
 * Referências: <br/>
 * http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/
 * html/dao.html <br/>
 * http://static.springsource.org/spring/docs/1.2.9/api/org/springframework/dao/
 * DataAccessException.html
 * </p>
 * 
 * @see {@link DataAccessException}
 * 
 * @version 0.5.0.Final
 * @since 0.5.0.Final
 */
/*
 * IMPORTANTE! Ver se esta realmente é a melhor forma de deixar as mensagens de erro
 * oriundas do Spring. No caso procurei ver se havia algum properties, ou algo
 * similar, para fazer isso mas não encontrei.
 * 
 * No caso durante o tratamento das exceções procurei, primeiramente, levar em
 * conta a classe da exceção em si, que é a boa e recomendável prática. Quando a
 * família de exceções não é tão rica, então num segundo momento foi necessário
 * fazer um tratamento levando em conta conteúdo da mensagem, mesmo esta prática
 * não sendo recomendável.
 * 
 * A classe está visível apenas neste pacote por não ser necessário a visualização
 * em outros locais e também pelo fato de eu não ter tido muito tempo para elaborar
 * e estudar este mecanismo. Mais a frente se for implementada uma maneira mais
 * eficiente então o impacto para as camadas superiores será zero.
 */
class DataAcessExceptionTranslator {
	
	private DataAcessExceptionTranslator() {
		// constructor not implement
	}

    /**
     * Traduz a mensagem de erro de forma a torná-la mais amigável.
     * 
     * @param dae a exceção
     * @return a exeção com uma mensagem de erro amigável
     */
    public static DataAccessException translate(
            org.springframework.dao.DataAccessException dae) {
        String msgExcecaoTraduzida = "";
        String msgExcecaoOriginal = dae.getMessage().toLowerCase();

        if (dae instanceof org.springframework.dao.DataIntegrityViolationException) {
            msgExcecaoTraduzida = "A operação não pode ser realizada por violar a integridade dos dados";

            // DuplicateKeyException é filha de DataIntegrityViolationException.
            if (dae instanceof org.springframework.dao.DuplicateKeyException) {
                msgExcecaoTraduzida = "Operação não permitida pois este registro tem de ser único";
                
                if (dae.getCause() instanceof org.hibernate.NonUniqueObjectException) {
                    msgExcecaoTraduzida = "Operação não permitida por já existir um objeto idêntico na sessão";
                }
            } else if (msgExcecaoOriginal.contains("could not delete")) {
                msgExcecaoTraduzida = "O registro não pode ser removido por haver registros dependentes";
            }
        }

        msgExcecaoTraduzida = msgExcecaoTraduzida.trim();

        if (!msgExcecaoTraduzida.isEmpty()) {
            return new DataAccessException(msgExcecaoTraduzida, dae);
        } else {
            return new DataAccessException(dae);
        }
    }

}
