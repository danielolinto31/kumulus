package br.dev.kumulus.arq.commons.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	private static final String ERRO_DATA_NULA = "A data informada para formatação não pode ser nula";
	private static final String PATTERN_DATA = "dd/MM/yyyy";

	private DateUtils() {
		// constructor not implement
	}

	public static Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

	public static String dataFormatada(Date data) {
		if (data != null) {
			DateFormat df = new SimpleDateFormat(PATTERN_DATA);
			return df.format(data);
		} else {
			throw new IllegalArgumentException(ERRO_DATA_NULA);
		}
	}

}
