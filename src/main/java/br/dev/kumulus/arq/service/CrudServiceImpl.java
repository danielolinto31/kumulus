package br.dev.kumulus.arq.service;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.dev.kumulus.arq.exception.DataAccessException;
import br.dev.kumulus.arq.exception.PersistenceValidateException;
import br.dev.kumulus.arq.exception.ServiceBusinessException;
import br.dev.kumulus.arq.exception.ServiceDataAccessException;
import br.dev.kumulus.arq.persistence.CrudDao;
import br.dev.kumulus.arq.persistence.PageData;
import br.dev.kumulus.arq.persistence.Persistent;

/**
 * Implementação de {@link CrudService} que utiliza {@link CrudDao} para CRUD.
 *
 * @author <a href="mailto:gewtonarq@gmail.com">Gewton Jhames</a>
 * @author <a href="mailto:misaelbarreto@gmail.com">Misael Barreto</a>
 * @author <a href="mailto:rrafaelpinto@gmail.com">Rafael Pinto</a>
 *
 * @param <T> tipo genérico que será persistido
 * @param <D> tipo de {@link CrudDao} genérico que irá manipular um tipo específico
 *            de {@link Persistent}
 */
public abstract class CrudServiceImpl<T extends Persistent, D extends CrudDao<T>> implements CrudService<T> {

    private static final long serialVersionUID = 1L;

    protected transient Logger log = LoggerFactory.getLogger(getClass());

	protected final D dao;

	protected CrudServiceImpl(D dao) {
		this.dao = dao;
	}

	@Override
    public void delete(T entity) throws ServiceBusinessException {
		try {
            validate(entity, false, false, true);
			dao.delete(entity);
		} catch (DataAccessException e) {
			throw new ServiceDataAccessException(e.getMessage(), e);
		}
	}

	@Override
    public void delete(Integer id) throws ServiceBusinessException{
	    delete(findById(id));
	}
 
	@Override
    public List<T> findAll() {
	    try {
	        return dao.findAll();
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
	}

    @Override
    public List<T> findAll(String sortField, Boolean sortOrder) {
        try {
            return dao.findAll(sortField, sortOrder);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
    }

	@Override
    public List<T> findAll(final int firstResult, final int maxResult) {
        try {
            return dao.findAll(firstResult, maxResult);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
	}

	@Override
	public List<T> findAll(int firstResult, int maxResult, String sortField,
	        Boolean sortOrder) {
        try {
            return dao.findAll(firstResult, maxResult, sortField, sortOrder);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
	}

	@Override
    public Integer countAll() {
        try {
            return dao.countAll();
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }

	}
	
    @Override	
	public T findByAttributesUniqueResult(T entity) {
        try {
            return dao.findByAttributesUniqueResult(entity);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }	    
	}   	

	@Override
    public List<T> findByAttributes(T entity) {
		return findByAttributes(entity, 0, 0);
	}

    @Override
    public PageData<T> findPageDataByAttributes(T entity, int firstResult,
            int pageSize, String sortField, Boolean sortOrder) {
        return dao.findPageDataByAttributes(entity, firstResult, pageSize, sortField, sortOrder);
    }

	@Override
    public List<T> findByAttributes(T entity, final int firstResult, final int maxResult) {
	    try {
	        return dao.findByAttributes(entity, firstResult, maxResult);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
	}

	@Override
	public List<T> findByAttributes(T entity, final String sortField, final Boolean sortOrder) {
	    try {
	        return dao.findByAttributes(entity, sortField, sortOrder);
	    } catch (DataAccessException e) {
	        throw new ServiceDataAccessException(e);
	    }
	}

    @Override
    public List<T> findByAttributes(T entity, final int firstResult,
            final int maxResult, final String sortField, final Boolean sortOrder) {
        try {
            return dao.findByAttributes(entity, firstResult, maxResult, sortField, sortOrder);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
    }

	@Override
    public Integer countByAttributes(T entity) {
	    try {
	        return dao.countByAttributes(entity);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
	}

	@Override
    public T findById(Serializable id) {
	    try {
	        return dao.findById(id);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e);
        }
	}

	@Override
    public Class<T> getPersistentClass() {
		return dao.getPersistentClass();
	}

    @Override
    public void insert(T entity) throws ServiceBusinessException {
        try {
            validate(entity, true, false, false);
            dao.insert(entity);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e.getMessage(), e);
        } catch (PersistenceValidateException e) {
            throw new ServiceBusinessException(e.getMessage(), e);
        }
    }


    @Override
    public void update(T entity) throws ServiceBusinessException {
        try {
            validate(entity, false, true, false);
            dao.update(entity);
        } catch (DataAccessException e) {
            throw new ServiceDataAccessException(e.getMessage(), e);
        } catch (PersistenceValidateException e) {
            throw new ServiceBusinessException(e.getMessage(), e);
        }
    }

	@Override
    public void validate(T entity, boolean isInsert, boolean isUpdate, boolean isDelete) throws ServiceBusinessException {

	}

}
