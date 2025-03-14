package br.dev.kumulus.arq.service;

import java.io.Serializable;
import java.util.List;

import br.dev.kumulus.arq.exception.ServiceBusinessException;
import br.dev.kumulus.arq.persistence.PageData;
import br.dev.kumulus.arq.persistence.Persistent;

/**
 * <code>CrudService</code> expõe as funcionalidades de persistência de uma
 * entidade do sistema que extenda {@link Persistent} para os clientes. <br />
 * <code>CrudService</code> foi projetada para que a classe que a implemente
 * seja <tt>abstract</tt> e realize essa implementação em cima de uma entidade
 * genérica, para que um terceiro nível de classes concretas possam aplicar essa
 * implementação em uma entidade também concreta, reforçando a coesão e a
 * orientação a objetos em geral ao usá-las.
 *
 * @param <T>
 *            tipo que será persistido
 */
public interface CrudService<T extends Persistent> extends Service {

    public abstract Class<T> getPersistentClass();

    public abstract void validate(T entity, boolean isInsert, boolean isUpdate, boolean isDelete) throws ServiceBusinessException;

    public abstract void insert(T entity) throws ServiceBusinessException;

    public abstract void update(T entity) throws ServiceBusinessException;

    public abstract T findById(Serializable id);

    public abstract List<T> findAll();

    public abstract List<T> findAll(final String sortField, final Boolean sortOrder);

    public abstract List<T> findAll(final int firstResult, final int maxResult);

    public abstract List<T> findAll(final int firstResult, final int maxResult,
            final String sortField, final Boolean sortOrder);

    public abstract Integer countAll();

    public abstract T findByAttributesUniqueResult(T entity);        
    
    public abstract List<T> findByAttributes(T entity);

    public abstract PageData<T> findPageDataByAttributes(T entity, int first,
            int pageSize, String sortField, Boolean sortOrder);

    public abstract List<T> findByAttributes(T entity, int firstResult,int pageSize);

    public abstract List<T> findByAttributes(T entity, final String sortField, final Boolean sortOrder);

    public abstract List<T> findByAttributes(T entity, final int firstResult,
            final int maxResult, final String sortField, final Boolean sortOrder);

    public abstract Integer countByAttributes(T entity);

    public abstract void delete(T entity)throws ServiceBusinessException;

    public abstract void delete(Integer id) throws ServiceBusinessException; 

}