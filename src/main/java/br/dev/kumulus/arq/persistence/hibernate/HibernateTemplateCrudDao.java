package br.dev.kumulus.arq.persistence.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.PersistentObjectException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

import br.dev.kumulus.arq.commons.utils.ReflectionUtils;
import br.dev.kumulus.arq.exception.DataAccessException;
import br.dev.kumulus.arq.exception.PersistenceValidateException;
import br.dev.kumulus.arq.persistence.CrudDao;
import br.dev.kumulus.arq.persistence.DefaultOrderBy;
import br.dev.kumulus.arq.persistence.FindByAttribute;
import br.dev.kumulus.arq.persistence.FindByAttribute.DateComparation;
import br.dev.kumulus.arq.persistence.FindByAttribute.StringComparation;
import br.dev.kumulus.arq.persistence.PageData;
import br.dev.kumulus.arq.persistence.PageDataImpl;
import br.dev.kumulus.arq.persistence.Persistent;
import br.dev.kumulus.arq.persistence.SortOrder;
import br.dev.kumulus.arq.persistence.UniqueAttributes;

public abstract class HibernateTemplateCrudDao<T extends Persistent> implements CrudDao<T> {

	private static final long serialVersionUID = 1L;

	private static final int ONE_DAY_IN_MILLISECONDS = 86400000 - 1;
	private static final String PARAM_ENTITY_FORCECA_OBJETO_NAO_NULO = "Para o parâmetro entity, forneça um objeto não nulo";
	private final Class<T> persistentClass;

	protected transient Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	protected HibernateTemplate ht;

	@Inject
	protected Validator validator;

	@SuppressWarnings("unchecked")
	protected HibernateTemplateCrudDao() {
		this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	@Override
	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	@Override
	public void validate(T entity) throws PersistenceValidateException {
		Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);
		List<String> errors = new ArrayList<>();
		for (ConstraintViolation<T> constraintViolation : constraintViolations) {
			errors.add(constraintViolation.getMessage());
		}

		validateUniqueAttributes(entity);

		if (!errors.isEmpty()) {
			throw new PersistenceValidateException(errors);
		}
	}

	private void validateUniqueAttributes(T entity) throws PersistenceValidateException {
		// Verifica se a classe possui a anotação
		if (getPersistentClass().isAnnotationPresent(UniqueAttributes.class)) {
			String[] validateUniqueFields = getPersistentClass().getAnnotation(UniqueAttributes.class).value();
			// Se possuir, verifica se há campos anotados
			if (validateUniqueFields != null) {

				Map<String, Object> entidadeRefletida = ReflectionUtils.getFieldsValues(entity);

				// Para cada conjunto de campo anotado, efetua a consulta, verificando se é um
				// conjunto único
				for (String validateUnique : validateUniqueFields) {

					DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
					log.debug("Montando criteria para validate da classe de dominio");
					// Se o atributo id for informado
					if (entity.getId() != null) {
						log.debug("Adicionou o id: {} ", entity.getId());
						criteria.add(Restrictions.ne("id", entity.getId()));
					}
					List<String> fields = Arrays.asList(validateUnique.split("\\s*,\\s*"));
					for (Map.Entry<String, Object> entry : entidadeRefletida.entrySet()) {
						String key = entry.getKey();
						Object value = entry.getValue();

						if (fields.contains(key) && value != null) {
							log.debug("Adicionando Unique: {} - valor: {}", key, value);
							criteria.add(Restrictions.eq(key, value));
						}
					}

					if (!ht.findByCriteria(criteria).isEmpty()) {
						throw new PersistenceValidateException(
								"Já existe um registro com o " + validateUnique.toUpperCase() + " informado");
					}
				}
			}
		}
	}

	@Override
	@Transactional(rollbackFor = PersistenceValidateException.class)
	public void insert(T entity) throws PersistenceValidateException {
		try {
			validate(entity);
			/*
			 * O "ht.persist(entity);" em geral funciona normalmente (Ex: SQL Server 2008
			 * R2, MySQL 5, Postgres 9.X), mas em alguns bancos, como o SQL Server 2019
			 * (versão em container), acaba ocorrendo o erro abaixo.
			 * org.hibernate.PersistentObjectException: detached entity passed to persist:
			 * br.dev.kumulus.domain.Curriculo Como solução, usar o
			 * "ht.merge(entity);"
			 */
			try {
				ht.persist(entity);
			} catch (PersistentObjectException e) {
				log.warn(String.format(
						"Ocorreu o erro \"%s\", do tipo PersistentObjectException, ao tentar inserir o objeto no banco de dados usando ht.persist().",
						e.getMessage()));
				log.warn("Será feita uma nova tentativa de salvar o objeto, usando ht.merge().");
				ht.merge(entity);
				log.warn("Objeto inserido com sucesso.");
			}
			ht.flush();
		} catch (PersistenceValidateException e) {
			throw e;
		} catch (org.springframework.dao.DataAccessException e) {
			throw DataAcessExceptionTranslator.translate(e);
		} catch (Exception e) {
			/*
			 * Em último caso, caso ocorra uma exceção inesperada, ela vai encapsulada numa
			 * exceção DataAccessException, garantindo assim que a camada cliente esteja
			 * isolada das complicações da camada de persistência.
			 *
			 * A idéia é que para a camada cliente só sejam disparadas exceções: -
			 * Implementadas na camada arq-persistence; - Exceções padrão JAVA, que podem
			 * "passear" por todas as camadas de uma aplicação. Ex:
			 * IllegalArgumentException.
			 */
			throw new DataAccessException(e);
		}
	}

	@Override
	@Transactional(rollbackFor = PersistenceValidateException.class)
	public void update(T entity) throws PersistenceValidateException {
		try {
			validate(entity);

			/*
			 * Para evitar o erro
			 * "org.springframework.orm.hibernate3.HibernateSystemException: a different
			 * object with the same identifier value was already associated with the
			 * session", ao invés de utilizar a chamada "ht.update(entity)" a partir de
			 * agora será utilizado o método "ht.merge(entity);".
			 * 
			 * O problema ocorre quando há um objeto na sessão e há um outro objeto
			 * "detached", com o mesmo identificador, e esse objeto detached está tentando
			 * ser atualizado. Um exemplo disso foi no sistema "Recursos de Materias", no
			 * cadastro de Servidores. Antes de gravar a alteração, uma consulta era
			 * disparada para ver se já existia um outro servidor com o mesmo login. Caso
			 * existisse um mesmo servidor, o cenário acima apresentado se concretizava e
			 * uma exceção era lançada.
			 * 
			 * Referências: ------------ http://pro-programmers.blogspot.com.br/2009
			 * /03/hibernate-nonuniqueobjectexception.html
			 * 
			 * What is the difference between merge and update? - Update should be used to
			 * save the data when the session does not contain an already persistent
			 * instance with the same identifier. - Merge should be used to save the
			 * modificatiions at any time without knowing about the state of a session.
			 * http://www.careerride.com/Hibernate -difference-between-merge-and-update.aspx
			 * 
			 * http://forum.springframework.net/showthread.php?3427-A-different- object
			 * -with-same-identifier-was-already-associated-with-the-session http
			 * ://static.springsource.org/spring/docs/1.2.9/api/org/springframework /
			 * orm/hibernate3/HibernateTemplate.html#merge%28java.lang.String,% 20
			 * java.lang.Object%29 http://docs.oracle.com/javaee/5/api/javax/persistence
			 * /EntityManager.html#merge%28T%29
			 */
			ht.merge(entity);
			ht.flush();
		} catch (PersistenceValidateException e) {
			throw e;
		} catch (org.springframework.dao.DataAccessException e) {
			throw DataAcessExceptionTranslator.translate(e);
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public T findById(Serializable id) {
		try {
			return ht.get(persistentClass, id);
		} catch (org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Integer countAll() {
		Long result = 0L;

		try {
			List listCountAll = ht.find("select count(*) from " + persistentClass.getName() + " persistent ");
			if (listCountAll != null && !listCountAll.isEmpty()) {
				result = (Long) listCountAll.get(0);
			}
		} catch (Exception e) {
			throw new DataAccessException(e);
		}

		return result.intValue();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Integer countByAttributes(T entity) {
		Integer result = 0;

		try {
			DetachedCriteria criteria = mountCriteriaForFindByAttributes(entity, null, null, true);

			/*
			 * Obs: Por algum motivo, a projeção rowCount não retorna um valor correto caso
			 * a entidade em questão possua algum atributo coleção, com mapeamento OneToMany
			 * e carregamento EAGER. Verifiquei que o criteria está totalmente correto, que
			 * os registros trazidos pelo criteria também estão corretos, mas ao jogar a
			 * projeção "Projections.rowCount()" a consulta executada ao final sempre é algo
			 * do tipo "select count(*) from entidade", ou seja, os relacionamentos EAGER
			 * não são levados em conta pela projeção. Ver se há uma maneira de contornar
			 * essa situação, mesmo considerando não ser uma boa prática usar mapeamendo
			 * EAGER.
			 */
			criteria.setProjection(Projections.rowCount());

			List resultCountList = ht.findByCriteria(criteria);
			if (resultCountList != null && !resultCountList.isEmpty()) {
				Long longResult = (Long) ht.findByCriteria(criteria).get(0);
				result = longResult.intValue();
			}
		} catch (Exception e) {
			throw new DataAccessException(e);
		}

		return result;
	}

	@Override
	public List<T> findAll() {
		return findByCriteria();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(String sortField, Boolean sortOrder) {
		DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
		if (sortField != null) {
			if (Boolean.TRUE.equals(sortOrder)) {
				criteria.addOrder(Order.asc(sortField));
			} else {
				criteria.addOrder(Order.desc(sortField));
			}
		}
		return ht.findByCriteria(criteria);
	}

	@Override
	public List<T> findAll(int firstResult, int maxResult) {
		return findByCriteria(firstResult, maxResult);
	}

	/*
	 * IMPORTANTE: Ver a necessidade de criar uma exceção mais específica, para
	 * classificar melhor o problema. O Spring criou uma especialização de
	 * DataAccessException, como por exemplo IncorrectResultSizeDataAccessException.
	 */
	@Override
	public T findByAttributesUniqueResult(T entity) {
		List<T> result = findByAttributes(entity);

		if (result.size() > 1) {
			throw new DataAccessException(new IncorrectResultSizeDataAccessException(
					"Foi encontrado mais de um registro para a entidade solicitada", 1));
		} else if (result.size() == 1) {
			return result.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<T> findByAttributes(T entity) {
		return findByAttributes(entity, 0, 0);
	}

	@Override
	public PageData<T> findPageDataByAttributes(T entity, int firstResult, int pageSize, String sortField,
			Boolean sortOrder) {
		return new PageDataImpl<>(countByAttributes(entity),
				findByAttributes(entity, firstResult, pageSize, sortField, sortOrder));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findByAttributes(T entity, int firstResult, int maxResult) {

		if (entity == null) {
			throw new IllegalArgumentException(PARAM_ENTITY_FORCECA_OBJETO_NAO_NULO);
		}

		List<T> result = new ArrayList<>();

		try {
			DetachedCriteria criteria = mountCriteriaForFindByAttributes(entity, null, null, false);
			result.addAll(ht.findByCriteria(criteria, firstResult, maxResult));
		} catch (Exception e) {
			throw new DataAccessException(e);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findByAttributes(T entity, final int firstResult, final int maxResult, final String sortField,
			final Boolean sortOrder) {
		List<T> result = new ArrayList<>();

		if (firstResult < 0) {
			throw new IllegalArgumentException("Para o parâmetro firstResult, forneça um valor maior ou igual a zero");
		}
		if (maxResult < 0) {
			throw new IllegalArgumentException("Para o parâmetro maxResult, forneça um valor maior ou igual a zero");
		}
		if (entity == null) {
			throw new IllegalArgumentException(PARAM_ENTITY_FORCECA_OBJETO_NAO_NULO);
		}

		try {
			DetachedCriteria criteria = mountCriteriaForFindByAttributes(entity, sortField, sortOrder, false);
			result.addAll(ht.findByCriteria(criteria, firstResult, maxResult));
		} catch (Exception e) {
			throw new DataAccessException(e);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findByAttributes(T entity, final String sortField, final Boolean sortOrder) {
		List<T> result = new ArrayList<>();

		if (entity == null) {
			throw new IllegalArgumentException(PARAM_ENTITY_FORCECA_OBJETO_NAO_NULO);
		}

		try {
			DetachedCriteria criteria = mountCriteriaForFindByAttributes(entity, sortField, sortOrder, false);
			result.addAll(ht.findByCriteria(criteria));
		} catch (Exception e) {
			throw new DataAccessException(e);
		}

		return result;
	}

	private DetachedCriteria mountCriteriaForFindByAttributes(T entity, String sortField, Boolean sortOrder,
			Boolean isToCount) {
		DetachedCriteria criteria = DetachedCriteria.forClass(entity.getClass());
		CriteriaAssistent criteriaAssistent = new CriteriaAssistent();
		populateCriteriaAssistentToFindByAttributes(criteriaAssistent, entity, "");
		if (Boolean.FALSE.equals(isToCount)) {
			populateCriteriaAssistentToSort(criteriaAssistent, sortField, sortOrder);
		}
		criteriaAssistent.populateCriteria(criteria);

		return criteria;
	}

	@SuppressWarnings({ "unchecked" })
	private void populateCriteriaAssistentToFindByAttributes(CriteriaAssistent criteriaAssistent, T entity,
			String parentAlias) {
		if (parentAlias != null) {
			parentAlias = parentAlias.trim();
			if (!parentAlias.isEmpty()) {
				parentAlias += ".";
			}
		} else {
			parentAlias = "";
		}

		SessionFactory sessionFactory = ht.getSessionFactory();
		ClassMetadata meta = sessionFactory.getClassMetadata(entity.getClass());

		String[] propertyNames = meta.getPropertyNames();
		Type[] propertyTypes = meta.getPropertyTypes();
		Object[] propertyValues = meta.getPropertyValues(entity, EntityMode.POJO);

		if (entity.getId() != null && meta.getIdentifierPropertyName() != null) {
			criteriaAssistent
					.addCriterion(Restrictions.eq(parentAlias + meta.getIdentifierPropertyName(), entity.getId()));
		} else {
			log.debug("Iniciando o processo de composição da consulta para método FindByAttributes.");
			for (int i = 0; i < propertyNames.length; i++) {
				Type type = propertyTypes[i];
				String name = propertyNames[i];
				String nameWithParentAlias = parentAlias + name;
				Object value = propertyValues[i];

				Object[] objects = new Object[]{parentAlias, name, type.getName(), value};
				log.debug("- Parent alias: {} - Nome do atributo: {} - Tipo (Hibernate): {} - Valor: {}", objects);

				if (value != null) {
					if (!type.isCollectionType()) {
						log.debug("Atributo adicionado: {} ", name);

						FindByAttribute findByAttibute = getFindByAttributeAnnotation(name);
						if (findByAttibute == null) {
							if (type instanceof StringType) {
								if (!value.toString().trim().isEmpty()) {
									criteriaAssistent
											.addCriterion(Restrictions.like(nameWithParentAlias, "%" + value + "%"));
								}
							} else if (value instanceof Persistent) {
								criteriaAssistent.addAlias(nameWithParentAlias, name);
								populateCriteriaAssistentToFindByAttributes(criteriaAssistent, (T) value, name);
							} else {
								criteriaAssistent.addCriterion(Restrictions.eq(nameWithParentAlias, value));
							}
						} else {
							if (type instanceof StringType) {
								if (!value.toString().trim().isEmpty()) {
									StringComparation stringComparation = findByAttibute.stringComparation();
									if (stringComparation != null && stringComparation == StringComparation.EXACT) {
										criteriaAssistent.addCriterion(Restrictions.eq(nameWithParentAlias, value));
									} else if (stringComparation != null
											&& stringComparation == StringComparation.END) {
										criteriaAssistent
												.addCriterion(Restrictions.like(nameWithParentAlias, "%" + value));
									} else if (stringComparation != null
											&& stringComparation == StringComparation.START) {
										criteriaAssistent
												.addCriterion(Restrictions.like(nameWithParentAlias, value + "%"));
									} else {
										criteriaAssistent.addCriterion(
												Restrictions.like(nameWithParentAlias, "%" + value + "%"));
									}
								}

							} else if (type instanceof TimestampType) {
								DateComparation dateComparation = findByAttibute.dateComparation();
								if (dateComparation != null && dateComparation == DateComparation.EXACT) {
									criteriaAssistent.addCriterion(Restrictions.eq(nameWithParentAlias, value));
								} else if (dateComparation != null && dateComparation == DateComparation.GREATER_THAN) {
									criteriaAssistent.addCriterion(Restrictions.ge(nameWithParentAlias, value));
								} else if (dateComparation != null && dateComparation == DateComparation.LESS_THAN) {
									criteriaAssistent.addCriterion(Restrictions.le(nameWithParentAlias, value));
								} else if (dateComparation != null && dateComparation == DateComparation.EXACT_DMY) {
									Date dataInicial = DateUtils.truncate(value, Calendar.DAY_OF_MONTH);
									Date dataFinal = DateUtils.addMilliseconds(dataInicial, ONE_DAY_IN_MILLISECONDS);
									criteriaAssistent.addCriterion(
											Restrictions.between(nameWithParentAlias, dataInicial, dataFinal));
								}

							} else if (value instanceof Persistent) {
								criteriaAssistent.addAlias(nameWithParentAlias, name);
								populateCriteriaAssistentToFindByAttributes(criteriaAssistent, (T) value, name);
							} else {
								criteriaAssistent.addCriterion(Restrictions.eq(nameWithParentAlias, value));
							}
						}

					} else {
						log.debug("Atributo não adicionado por ser uma coleção: {} ", nameWithParentAlias);
					}
				} else {
					log.debug("Atributo não adicionado por estar \"null\": {} ", nameWithParentAlias);
				}
			}
		}
	}

	private void populateCriteriaAssistentToSort(CriteriaAssistent criteriaAssistent, String sortField,
			Boolean sortOrder) {
		if (sortField == null || sortField.trim().isEmpty()) {
			DefaultOrderBy defaultOrderBy = getPersistentClass().getAnnotation(DefaultOrderBy.class);

			if (defaultOrderBy != null) {
				log.debug("Adicionando ordenação default definida via anotação {} ", DefaultOrderBy.class.getCanonicalName());
				sortField = defaultOrderBy.sortField();
				sortOrder = defaultOrderBy.sortOrder() == SortOrder.ASCENDING;
			}
		}

		if (sortField != null) {
			String[] fields = sortField.split("\\,");

			for (String field : fields) {
				field = field.trim();
				if (field.split("\\.").length == 1) {
					log.debug("Adicionando resultado da consulta por ordem do campo {} SEM criação de alias", field);
					if (Boolean.TRUE.equals(sortOrder)) {
						log.debug("Ordenação adicionada: {} ASC", field);
						criteriaAssistent.addOrder(Order.asc(field));
					} else {
						log.debug("Ordenação adicionada: {} DESC", field);
						criteriaAssistent.addOrder(Order.desc(field));
					}

				} else {
					String innerClass = field.split("\\.")[0].trim();
					String innerAttribute = field.split("\\.")[1].trim();
					String innerClassWithAttribute = innerClass + "." + innerAttribute;

					log.debug("Adicionando resultado da consulta por ordem do campo {} COM criação de alias", innerClassWithAttribute);

					try {
						criteriaAssistent.addAlias(innerClass, innerClass);
					} catch (HibernateException e) {
						log.debug("Erro ao criar alias \"{}\". Detalhes: {} ", innerClass, e.getMessage());
					}

					if (Boolean.TRUE.equals(sortOrder)) {
						log.debug("Ordenação adicionada: {} ASC", innerClassWithAttribute);
						criteriaAssistent.addOrder(Order.asc(innerClassWithAttribute));
					} else {
						log.debug("Ordenação adicionada: {} DESC", innerClassWithAttribute);
						criteriaAssistent.addOrder(Order.desc(innerClassWithAttribute));
					}
				}
			}

		} else {
			log.debug("Nenhuma ordenação definida.");
		}

	}

	/***
	 * Método que retorna a Anotação FindByAttribute de determinado campo da classe
	 * (caso haja).
	 *
	 * @param fieldName
	 * @return
	 */
	private FindByAttribute getFindByAttributeAnnotation(String fieldName) {
		Field[] fields = getPersistentClass().getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true);
			if (field.getName().equalsIgnoreCase(fieldName)) {
				return field.getAnnotation(FindByAttribute.class);
			}
		}
		return null;
	}

	@Override
	@Transactional()
	public void delete(T entity) {
		try {
			T entityToDelete = ht.merge(entity);
			ht.delete(entityToDelete);
			ht.flush();
		} catch (org.springframework.dao.DataAccessException e) {
			throw DataAcessExceptionTranslator.translate(e);
		} catch (Exception e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	@Transactional
	public void delete(Integer id) {
		delete(findById(id));
	}

	private List<T> findByCriteria(Criterion... criterion) {
		return findByCriteria(0, 0, criterion);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<T> findByCriteria(int firstResult, int maxResult, Criterion... criterion) {
		List result = null;

		if (firstResult < 0) {
			throw new IllegalArgumentException("Para o parâmetro firstResult, forneça um valor maior ou igual a zero");
		}
		if (maxResult < 0) {
			throw new IllegalArgumentException("Para o parâmetro maxResult, forneça um valor maior ou igual a zero");
		}

		try {
			DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
			for (Criterion c : criterion) {
				criteria.add(c);
			}

			CriteriaAssistent ca = new CriteriaAssistent();
			populateCriteriaAssistentToSort(ca, null, null);
			ca.populateCriteria(criteria);

			result = ht.findByCriteria(criteria, firstResult, maxResult);
		} catch (Exception e) {
			throw new DataAccessException(e);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll(int firstResult, int maxResult, String sortField, Boolean sortOrder) {
		List<T> result = null;

		DetachedCriteria d = DetachedCriteria.forClass(getPersistentClass());

		CriteriaAssistent ca = new CriteriaAssistent();
		populateCriteriaAssistentToSort(ca, sortField, sortOrder);
		ca.populateCriteria(d);

		result = ht.findByCriteria(d, firstResult, maxResult);
		return result;

	}

}
