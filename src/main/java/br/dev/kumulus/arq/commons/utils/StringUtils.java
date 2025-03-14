package br.dev.kumulus.arq.commons.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

	private StringUtils() {
		// constructor not implement
	}

	/**
	 * Método que retira acentos, tremas, crases , ç e outros caracteres especiais
	 * de uma String. Método semelhante ao "retirarCaracteresEspeciais" acima.
	 * 
	 * @param text
	 * @return
	 */
	public static String convertToASCII2(String text) {
		return text.replaceAll("[ãâàáä]", "a").replaceAll("[êèéë]", "e").replaceAll("[îìíï]", "i")
				.replaceAll("[õôòóö]", "o").replaceAll("[ûúùü]", "u").replaceAll("[ÃÂÀÁÄ]", "A")
				.replaceAll("[ÊÈÉË]", "E").replaceAll("[ÎÌÍÏ]", "I").replaceAll("[ÕÔÒÓÖ]", "O")
				.replaceAll("[ÛÙÚÜ]", "U").replace('ç', 'c').replace('Ç', 'C').replace('ñ', 'n').replace('Ñ', 'N');
	}

	/**
	 * Converte uma String com itens separados por vírgula em uma lista de Strings.
	 * Obs: Os espaços em branco entre as vírgulas serão desprezados, vide exemplo
	 * abaixo.
	 * 
	 * Ex: "misael barreto, de, queiroz, arquitetura" Resultado: [misael barreto,
	 * de, queiroz, arquitetura ]
	 * 
	 * @param commaSeparatedString String com itens separados por vírgula
	 * @return the list
	 */
	public static List<String> convertCommaSeparatedStringToList(String commaSeparatedString) {
		if (commaSeparatedString == null || commaSeparatedString.trim().isEmpty()) {
			return new ArrayList<>();
		} else {
			return Arrays.asList(commaSeparatedString.split("\\s*,\\s*"));
		}
	}
}
