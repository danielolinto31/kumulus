package br.dev.kumulus.arq.commons.utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeanUtils;

import br.dev.kumulus.arq.exception.ServiceBusinessException;
import br.dev.kumulus.arq.persistence.Persistent;
import br.dev.kumulus.arq.service.CrudServiceImpl;

public class Utils {

	private Utils() {
		// constructor not implement
	}

	public static final Object getPropertyValueViaReflection(Object o, String field)
			throws ReflectiveOperationException, IllegalArgumentException, IntrospectionException {
		return new PropertyDescriptor(field, o.getClass()).getReadMethod().invoke(o);
	}

	public static String normalizarAcentos(String str) {
		/** Troca os caracteres acentuados por não acentuados **/
		String[][] caracteresAcento = { { "Á", "A" }, { "á", "a" }, { "É", "E" }, { "é", "e" }, { "Í", "I" },
				{ "í", "i" }, { "Ó", "O" }, { "ó", "o" }, { "Ú", "U" }, { "ú", "u" }, { "À", "A" }, { "à", "a" },
				{ "È", "E" }, { "è", "e" }, { "Ì", "I" }, { "ì", "i" }, { "Ò", "O" }, { "ò", "o" }, { "Ù", "U" },
				{ "ù", "u" }, { "Â", "A" }, { "â", "a" }, { "Ê", "E" }, { "ê", "e" }, { "Î", "I" }, { "î", "i" },
				{ "Ô", "O" }, { "ô", "o" }, { "Û", "U" }, { "û", "u" }, { "Ä", "A" }, { "ä", "a" }, { "Ë", "E" },
				{ "ë", "e" }, { "Ï", "I" }, { "ï", "i" }, { "Ö", "O" }, { "ö", "o" }, { "Ü", "U" }, { "ü", "u" },
				{ "Ã", "A" }, { "ã", "a" }, { "Õ", "O" }, { "õ", "o" }, { "Ç", "C" }, { "ç", "c" }, };

		for (int i = 0; i < caracteresAcento.length; i++) {
			str = str.replaceAll(caracteresAcento[i][0], caracteresAcento[i][1]);
		}

		/** Troca os espaços no início por "" **/
		str = str.replaceAll("^\\s+", "");
		/** Troca os espaços no início por "" **/
		str = str.replaceAll("\\s+$", "");
		/** Troca os espaços duplicados, tabulações e etc por " " **/
		str = str.replaceAll("\\s+", " ");

		return str;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void validateUniqueness(Persistent p, CrudServiceImpl service, String... campos)
			throws ServiceBusinessException {
		PropertyDescriptor[] propriedadesObjeto = BeanUtils.getPropertyDescriptors(p.getClass());
		Persistent objetoPesquisado = BeanUtils.instantiate(p.getClass());
		List<String> inclusos = new ArrayList<>();
		Collections.addAll(inclusos, campos);
		List<String> excluidos = new ArrayList<>();
		for (PropertyDescriptor pp : propriedadesObjeto) {
			if (!inclusos.contains(pp.getName())) {
				excluidos.add(pp.getName());
			}
		}
		BeanUtils.copyProperties(p, objetoPesquisado, excluidos.toArray(new String[excluidos.size()]));
		List<Persistent> lista = service.findByAttributes(objetoPesquisado);
		try {
			if (null != lista && !lista.isEmpty()) {
				for (Persistent persistent : lista) {
					Map<String, Object> caracteres = new HashMap<>();
					PropertyDescriptor[] po = BeanUtils.getPropertyDescriptors(persistent.getClass());
					for (PropertyDescriptor pp : po) {
						if (inclusos.contains(pp.getName())) {
							Object property = PropertyUtils.getProperty(persistent, pp.getName());
							caracteres.put(pp.getName(), property);
						}
					}
					if (pesquisarIgualdade(caracteres, propriedadesObjeto, persistent.getId(), p.getId(), p)) {
						throw new ServiceBusinessException(
								"A operação não pode ser realizada por violar a integridade dos dados");
					}
				}
			}
		} catch (Exception e) {
			throw new ServiceBusinessException("A operação não pode ser realizada por violar a integridade dos dados");
		}
	}

	private static boolean pesquisarIgualdade(Map<String, Object> caracteres, PropertyDescriptor[] propriedadesObjeto,
			Integer pesquisado, Integer entidadePesquisada, Persistent persistent)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		List<Boolean> resultado = new ArrayList<>();
		for (int i = 0; i < propriedadesObjeto.length; i++) {
			PropertyDescriptor pd = propriedadesObjeto[i];
			if (caracteres.containsKey(pd.getName())) {
				Object property = PropertyUtils.getProperty(persistent, pd.getName());
				resultado.add(Boolean.valueOf(property.equals(caracteres.get(pd.getName()))));
			}
		}
		if (resultado.contains(false)) {
			return false;
		} else {
			return !pesquisado.equals(entidadePesquisada);
		}
	}

	public static String getConfig(String key) {
		Logger logger = Logger.getLogger(Utils.class.getName());
		ResourceBundle resource = ResourceBundle.getBundle("config");
		String envValue = null;
		try {
			String envVar = resource.getString(key);
			envValue = envVar.matches("\\$\\{[A-Za-z0-9_]+\\}") ? getSystemEnv(envVar.substring(2, envVar.length() - 1))
					: envVar;

			if (envValue == null) {
				logger.warning("Variável de ambiente inexistente: " + envVar);
				envValue = envVar; // Mantém o valor original se a variável de ambiente não existir
			}
		} catch (MissingResourceException e) {
			logger.warning(e.getMessage());
			logger.warning("Propriedade inexistente: " + key);
		}
		return envValue;
	}

	public static String getSystemEnv(String property) {
		return System.getenv(property);
	}

	public static String normalizar(String str) {

		/** Troca os caracteres acentuados por não acentuados **/
		String[][] caracteresAcento = { { "Á", "A" }, { "á", "a" }, { "É", "E" }, { "é", "e" }, { "Í", "I" },
				{ "í", "i" }, { "Ó", "O" }, { "ó", "o" }, { "Ú", "U" }, { "ú", "u" }, { "À", "A" }, { "à", "a" },
				{ "È", "E" }, { "è", "e" }, { "Ì", "I" }, { "ì", "i" }, { "Ò", "O" }, { "ò", "o" }, { "Ù", "U" },
				{ "ù", "u" }, { "Â", "A" }, { "â", "a" }, { "Ê", "E" }, { "ê", "e" }, { "Î", "I" }, { "î", "i" },
				{ "Ô", "O" }, { "ô", "o" }, { "Û", "U" }, { "û", "u" }, { "Ä", "A" }, { "ä", "a" }, { "Ë", "E" },
				{ "ë", "e" }, { "Ï", "I" }, { "ï", "i" }, { "Ö", "O" }, { "ö", "o" }, { "Ü", "U" }, { "ü", "u" },
				{ "Ã", "A" }, { "ã", "a" }, { "Õ", "O" }, { "õ", "o" }, { "Ç", "C" }, { "ç", "c" }, };

		for (int i = 0; i < caracteresAcento.length; i++) {
			str = str.replaceAll(caracteresAcento[i][0], caracteresAcento[i][1]);
		}

		/** Troca os caracteres especiais da string por "" **/
		String[] caracteresEspeciais = { "\\.", ",", "-", ":", "\\(", "\\)", "ª", "\\|", "\\\\", "°", "´", "`", "\\+",
				"\\=", "\\_", "\\{", "\\}", "\\[", "\\]", "\\?", "\\;", "\\^", "\\<", "\\>", "\\/", "~" };

		for (int i = 0; i < caracteresEspeciais.length; i++) {
			str = str.replaceAll(caracteresEspeciais[i], "");
		}

		/** Troca os espaços no início por "" **/
		str = str.replaceAll("^\\s+", "");
		/** Troca os espaços no início por "" **/
		str = str.replaceAll("\\s+$", "");
		/** Troca os espaços duplicados, tabulações e etc por " " **/
		str = str.replaceAll("\\s+", " ");

		return str;
	}

	public static boolean verifyEndDateGreaterThanInitialDate(Date initialDate, Date endDate) {
		return endDate.compareTo(initialDate) < 0;
	}

}
