package br.dev.kumulus.arq.persistence;

import java.io.Serializable;
import java.util.List;

import br.dev.kumulus.arq.exception.DataAccessException;
import br.dev.kumulus.arq.exception.PersistenceValidateException;


public interface CrudDao<T extends Persistent> extends Dao {

    public abstract Class<T> getPersistentClass();

    public abstract void validate(T entity) throws PersistenceValidateException;

    public abstract void insert(T entity) throws PersistenceValidateException;

    public abstract void update(T entity) throws PersistenceValidateException;

    public abstract T findById(Serializable id);

    public abstract List<T> findAll();

    public abstract List<T> findAll(String sortField, Boolean sortOrder);

    public abstract List<T> findAll(final int firstResult, final int maxResult);

    public abstract List<T> findAll(int firstResult, int maxResult, String sortField, Boolean sortOrder);

    public abstract Integer countAll();

    public abstract T findByAttributesUniqueResult(T entity);    
    
    public abstract List<T> findByAttributes(T entity);

    public abstract PageData<T> findPageDataByAttributes(T entity, int firstResult,
            int maxResult, String sortField, Boolean sortOrder);

    public abstract List<T> findByAttributes(T entity, final int firstResult,
            final int maxResult);

    public abstract List<T> findByAttributes(T entity, final int firstResult,
            final int maxResult, final String sortField, final Boolean sortOrder);

    public List<T> findByAttributes(T entity, final String sortField, final Boolean sortOrder);

    public abstract Integer countByAttributes(T entity);

    public abstract void delete(T entity);

    public abstract void delete(Integer id) throws DataAccessException;

}
