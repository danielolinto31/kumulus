package br.dev.kumulus.arq.commons.utils;

public class ViaCepUtil {

	private ViaCepUtil() {
		// constructor not implement
	}

	/**
	 * Valida a quantidade de caracteres do CEP
	 * 
	 * @param cep
	 * @return
	 */
	public static boolean validaCep(String cep) {
	    return cep.matches("\\d{8}");
	}

	/**
	 * Remove qualquer caracter, deixando apenas números
	 * 
	 * @param cep
	 * @return
	 */
	public static String removerFormatacaoCep(String cep) {
	    return (cep != null) ? cep.replaceAll("\\D", "") : "";
	}
}
