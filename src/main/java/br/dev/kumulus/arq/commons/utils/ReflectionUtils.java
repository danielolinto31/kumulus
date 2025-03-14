package br.dev.kumulus.arq.commons.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para utilização de reflection.
 *
 */
public class ReflectionUtils {

	private static Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

	private ReflectionUtils() {
		// constructor not implement
	}

	@SuppressWarnings("unchecked")
	public static <T> T createFrom(Class<?> clazz, Map<String, Object> fieldsWithValues) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		T instance = (T) clazz.newInstance();
		setFieldValues(instance, fieldsWithValues);
		return instance;
	}

	public static <T> T createFrom(Class<?> clazz, String fieldsWithValuesAsString) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException {

		Pattern p = Pattern.compile("([\\w\\+\\.]+):([\\w+^[a-z\u00C0-\u00ff =\\-A-Z]+$]+)");
		Matcher m = p.matcher(fieldsWithValuesAsString);

		Map<String, Object> fieldsWithValues = new HashMap<>();
		while (m.find()) {
			log.debug("Adicionando " + m.group(1) + " - " + m.group(2));
			fieldsWithValues.put(m.group(1), m.group(2));
		}

		return createFrom(clazz, fieldsWithValues);
	}

	public static <T> T createFrom(T instance)
			throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		return createFromInternal(instance, null, null);
	}

	public static <T> T createFrom(T instance, String fieldNamesToInclude)
			throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		return createFromInternal(instance, null, fieldNamesToInclude);
	}

	@SuppressWarnings("unchecked")
	private static <T> T createFromInternal(T instance, T instanceCopy, String fieldNamesToInclude)
			throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {
		List<String> fieldNamesToProcess = new ArrayList<>();

		if (fieldNamesToInclude != null) {
			fieldNamesToInclude = fieldNamesToInclude.trim();
		} else {
			fieldNamesToInclude = "";
		}

		if (!fieldNamesToInclude.isEmpty()) {
			fieldNamesToProcess.addAll(StringUtils.convertCommaSeparatedStringToList(fieldNamesToInclude));
		} else {
			for (Field field : getFieldsFromHierarchy(instance.getClass())) {
				if (!fieldNamesToProcess.contains(field.getName())) {
					fieldNamesToProcess.add(field.getName());
				}
			}
		}

		// Se instanceCopy é nulo, então se cria uma nova instância.
		if (instanceCopy == null) {
			instanceCopy = (T) instance.getClass().newInstance();
		}

		for (String fieldName : fieldNamesToProcess) {
			Field field = null;
			Boolean fieldIsAcessibleOriginalValue = null;

			// Se for um atributo de um atributo Ex: "cidade.nome"
			if (fieldName.contains(".")) {
				// Ex: No caso o atributo raiz é "cidade".
				String fieldNameRoot = fieldName.substring(0, fieldName.indexOf("."));

				// Obtendo o field que representa o atributo raiz. Ex: "cidade"
				field = getFieldFromHierarchy(instance.getClass(), fieldNameRoot);
				fieldIsAcessibleOriginalValue = field.isAccessible();
				field.setAccessible(true);
				try {
					Object fieldValue = field.get(instance);

					// Se o atributo tem valor, ou seja, "cidade" está instanciada.
					if (fieldValue != null) {
						/*
						 * fieldNameSub representa o atributo do field "root" que se deseja. Ex: "nome"
						 * da cidade. Também pode ser algo além, como nome do estado da cidade, ou seja,
						 * fieldNameSub será "estado.nome".
						 */
						String fieldNameSub = fieldName.substring(fieldName.indexOf(".") + 1, fieldName.length());

						/*
						 * fieldValueCopy representa um novo objeto "cidade". Este novo objeto será
						 * preenchido com os atributos espeficidados pelo desenvolvedor.
						 * 
						 * Se fieldValueCopy da instanceCopy for nula, então o objeto será criado a
						 * partir retorno do método createFromInternal. Caso contrário, fieldValueCopy
						 * será repassado para createFromInternal para que os outros campos do objeto
						 * sejam atualizados.
						 */
						Object fieldValueCopy = field.get(instanceCopy);
						if (fieldValueCopy == null) {
							fieldValueCopy = createFromInternal(fieldValue, null, fieldNameSub);
						} else {
							createFromInternal(fieldValue, fieldValueCopy, fieldNameSub);
						}

						// Agora atualiza-se o atributo da nova instância.
						if (!Modifier.isFinal(field.getModifiers())) {
							field.set(instanceCopy, fieldValueCopy);
						}
					}
				} finally {
					field.setAccessible(fieldIsAcessibleOriginalValue);
				}
			} else {
				field = getFieldFromHierarchy(instance.getClass(), fieldName);
				fieldIsAcessibleOriginalValue = field.isAccessible();
				try {
					field.setAccessible(true);
					Object fieldValue = field.get(instance);

					if (!Modifier.isFinal(field.getModifiers())) {
						field.set(instanceCopy, fieldValue);
					}
				} finally {
					field.setAccessible(fieldIsAcessibleOriginalValue);
				}
			}
		}

		return instanceCopy;
	}

	private static Field getField(Class<?> clazz, String fieldName) throws SecurityException, NoSuchFieldException {
		Field result = null;
		result = clazz.getDeclaredField(fieldName);
		return result;
	}

	private static List<Field> getFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return fields;
	}

	private static Field getFieldFromHierarchy(Class<?> clazz, String fieldName)
			throws SecurityException, NoSuchFieldException {
		Field result = null;
		Boolean hasSuperClass = (clazz.getSuperclass() != null);
		try {
			result = getField(clazz, fieldName);
		} catch (SecurityException e) {
			if (!hasSuperClass) {
				throw e;
			}
		} catch (NoSuchFieldException e) {
			if (!hasSuperClass) {
				throw e;
			}
		}

		// Se o field não foi obitdo, então se tentará obter o field
		// através das heranças existentes.
		if (result == null) {
			for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
				try {
					result = getField(c, fieldName);
					break;
				} catch (SecurityException | NoSuchFieldException e) {
					if (c.getSuperclass() == null) {
						throw e;
					}
				}
			}
		}
		// Se result for nulo, é sinal de que o field não foi achado
		// na classe informada e nem as suas super classes.
		return result;
	}

	private static List<Field> getFieldsFromHierarchy(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		fields.addAll(getFields(clazz));

		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			fields.addAll(getFields(c));
		}

		return fields;
	}

	@SuppressWarnings("unused")
	private static Map<String, Object> getBasicFieldsValues(Object object) {
		List<Field> fields = getBasicFieldsAsList(object.getClass());
		List<String> fieldNames = new ArrayList<>();
		for (Field f : fields) {
			fieldNames.add(f.getName());
		}

		return getFieldsValuesInternal(object, fieldNames, null, null, null, false);
	}

	private static List<Field> getBasicFieldsAsList(Class<?> clazz) {
		List<Field> fields = getFields(clazz);
		List<Field> result = new ArrayList<>();

		for (Field f : fields) {
			if (fiedIsBasicType(f)) {
				result.add(f);
			}
		}

		return result;
	}

	public static Map<String, Object> getFieldsValues(Object object) {
		return getFieldsValuesInternal(object, null, null, null, null, false);
	}

	@SuppressWarnings("rawtypes")
	public static Map<String, Object> getFieldsValues(Object object, List<String> fieldNamesToInclude,
			List<Class> fieldClassesToInclude, List<String> fieldNamesToExclude, List<Class> fieldClassesToExclude,
			boolean excludeNullOrEmptyFields) {
		return getFieldsValuesInternal(object, fieldNamesToInclude, fieldClassesToInclude, fieldNamesToExclude,
				fieldClassesToExclude, excludeNullOrEmptyFields);
	}

	@SuppressWarnings("rawtypes")
	private static Map<String, Object> getFieldsValuesInternal(Object object, List<String> fieldNamesToInclude,
			List<Class> fieldClassesToInclude, List<String> fieldNamesToExclude, List<Class> fieldClassesToExclude,
			boolean excludeNullOrEmptyFields) {
		Map<String, Object> result = new HashMap<>();
		List<Field> fields = getFields(object.getClass());

		if (fieldNamesToInclude != null && fieldNamesToExclude != null) {
			for (String fieldNameToInc : fieldNamesToInclude) {
				if (fieldNamesToExclude.contains(fieldNameToInc)) {
					throw new IllegalArgumentException(
							"Os atributos 'fieldNameToInclude' e 'fieldNameToExclude' possuem pelo menos um campo em comum");
				}
			}
		}

		if (fieldClassesToInclude != null && fieldClassesToExclude != null) {
			for (Class fieldClassToInc : fieldClassesToInclude) {
				if (fieldClassesToExclude.contains(fieldClassToInc)) {
					throw new IllegalArgumentException(
							"Os atributos 'fieldClassesToInclude' e 'fieldClassesToExclude' possuem pelo menos um classe em comum");
				}
			}
		}

		log.debug("Varrendo os fields de um objeto da entidade " + object.getClass().getName());
		for (Field field : fields) {
			field.setAccessible(true);
			log.debug("Analisando o field " + field.getName());
			if ((fieldNamesToExclude == null || fieldNamesToExclude.indexOf(field.getName()) < 0)
					&& (fieldClassesToExclude == null || fieldClassesToExclude.indexOf(field.getType()) < 0)
					&& (fieldNamesToInclude == null || fieldNamesToInclude.indexOf(field.getName()) >= 0)
					&& (fieldClassesToInclude == null || fieldClassesToInclude.indexOf(field.getType()) >= 0)) {
				try {
					Object fieldValue = field.get(object);
					if (!excludeNullOrEmptyFields || fieldValue != null) {
						if (!(fieldValue instanceof String) || ((String) fieldValue).trim().length() > 0) {
							log.debug("Obtendo o valor do field " + field.getName());
							result.put(field.getName(), fieldValue);
						}
					}

				} catch (IllegalAccessException e) {
					log.error("Erro ao tentar obter os atributos da classe " + object + ". " + e.getMessage());
				}
			}

		}
		return result;
	}

	public static void setFieldValues(Object instance, Map<String, Object> fieldsWithValues) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InstantiationException {
		for (Map.Entry<String, Object> entry : fieldsWithValues.entrySet()) {
			// Chave pode ser um atributo simples (Ex: nome, idade) e pode
			// ser um atributo complexo (Ex: cidade.nome, cidade.estado.sigla).
			String chave = entry.getKey();
			Object valor = entry.getValue();

			// Caso a "chave" seja um atributo complexo...
			if (chave.contains(".")) {
				String atributo = chave.substring(0, chave.indexOf("."));
				String atributoDoAtributo = chave.substring(chave.indexOf(".") + 1, chave.length());

				Field atributoField = getFieldFromHierarchy(instance.getClass(), atributo);
				Class<?> atributoClazz = atributoField.getType();
				Boolean atributoFieldIsAcessible = atributoField.isAccessible();
				try {
					atributoField.setAccessible(true);
					Object atributoInstancia = atributoField.get(instance);
					if (atributoInstancia == null) {
						atributoInstancia = atributoClazz.newInstance();
					}

					// Chamada recursiva para montar os atributos do atributo em questão.
					// setFieldValues(atributoInstancia, atributosDoAtributo);
					setFieldValue(atributoInstancia, atributoDoAtributo, valor);
					// Uma vez montando o atributo em questão, então ele é setado na instância
					// "parent".
					setFieldValue(instance, atributoField, atributoInstancia);
				} finally {
					atributoField.setAccessible(atributoFieldIsAcessible);
				}

				// Caso seja um atributo simples...
			} else {
				Field chaveField = getFieldFromHierarchy(instance.getClass(), chave);
				Boolean chaveFieldIsAcessible = chaveField.isAccessible();
				try {
					setFieldValue(instance, chaveField, valor);
				} finally {
					chaveField.setAccessible(chaveFieldIsAcessible);
				}
			}
		}
	}

	private static void setFieldValue(Object instance, Field field, Object value)
			throws IllegalArgumentException, IllegalAccessException {
		if (!field.getName().equals("serialVersionUID")) {
			Boolean fieldIsAcessible = field.isAccessible();
			try {
				field.setAccessible(true);

				if (value == null) {
					field.set(instance, null);
				} else if (field.getType().isPrimitive()) {
					field.set(instance, value);
				} else if (field.getType().isAssignableFrom(Boolean.class)) {
					field.set(instance, Boolean.valueOf(value.toString()));
				} else if (field.getType().isAssignableFrom(Short.class)) {
					field.set(instance, Short.valueOf(value.toString()));
				} else if (field.getType().isAssignableFrom(Integer.class)) {
					field.set(instance, Integer.valueOf(value.toString()));
				} else if (field.getType().isAssignableFrom(Long.class)) {
					field.set(instance, Long.valueOf(value.toString()));
				} else if (field.getType().isAssignableFrom(Float.class)) {
					field.set(instance, Float.valueOf(value.toString()));
				} else if (field.getType().isAssignableFrom(Double.class)) {
					field.set(instance, Double.valueOf(value.toString()));
				} else {
					field.set(instance, value);
				}
			} finally {
				field.setAccessible(fieldIsAcessible);
			}
		}
	}

	public static void setFieldValue(Object instance, String fieldName, Object value) throws IllegalArgumentException,
			IllegalAccessException, SecurityException, NoSuchFieldException, InstantiationException {
		// Caso a "chave" seja um atributo complexo...
		if (fieldName.contains(".")) {
			String atributo = fieldName.substring(0, fieldName.indexOf("."));
			String atributoDoAtributo = fieldName.substring(fieldName.indexOf(".") + 1, fieldName.length());

			Field atributoField = getField(instance.getClass(), atributo);
			Class<?> atributoClazz = atributoField.getType();
			Boolean atributoIsAcessible = atributoField.isAccessible();
			try {
				atributoField.setAccessible(true);
				Object atributoInstancia = atributoField.get(instance);
				if (atributoInstancia == null) {
					atributoInstancia = atributoClazz.newInstance();
				}
				// Chamada recursiva para montar os atributos do atributo em questão.
				setFieldValue(atributoInstancia, atributoDoAtributo, value);
				// Uma vez montando o atributo em questão, então ele é setado na instância
				// "parent".
				setFieldValue(instance, atributoField, atributoInstancia);
			} finally {
				atributoField.setAccessible(atributoIsAcessible);
			}

		} else {
			Field field = getField(instance.getClass(), fieldName);
			setFieldValue(instance, field, value);
		}

	}

	private static Boolean fiedIsBasicType(Field field) {
		Class<?> fieldType = field.getType();

		return (fieldType.isPrimitive() || fieldType.isAssignableFrom(Boolean.class)
				|| fieldType.isAssignableFrom(Character.class) || fieldType.isAssignableFrom(Byte.class)
				|| fieldType.isAssignableFrom(Short.class) || fieldType.isAssignableFrom(BigDecimal.class)
				|| fieldType.isAssignableFrom(Integer.class) || fieldType.isAssignableFrom(Long.class)
				|| fieldType.isAssignableFrom(Float.class) || fieldType.isAssignableFrom(Double.class)
				|| fieldType.isAssignableFrom(Date.class) || fieldType.isAssignableFrom(String.class));
	}

}
