package br.dev.kumulus.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import br.dev.kumulus.arq.exception.ServiceBusinessException;
import br.dev.kumulus.arq.service.CrudServiceImpl;
import br.dev.kumulus.dao.PessoaDao;
import br.dev.kumulus.domain.Pessoa;

@Service
public class PessoaServiceImpl extends CrudServiceImpl<Pessoa, PessoaDao> implements PessoaService {

	private static final long serialVersionUID = 1L;

	@Inject
	public PessoaServiceImpl(PessoaDao dao) {
		super(dao);
	}
	
	@Override
	public void validate(Pessoa entity, boolean isInsert, boolean isUpdate, boolean isDelete)
			throws ServiceBusinessException {
		
		if ((isInsert || isUpdate) && entity.getEnderecoList().isEmpty()) {
			throw new ServiceBusinessException("É obrigatório cadastrar um endereço para salvar a pessoa");
		}

		super.validate(entity, isInsert, isUpdate, isDelete);
	}

}
