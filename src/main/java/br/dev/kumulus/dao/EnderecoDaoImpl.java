package br.dev.kumulus.dao;

import org.springframework.stereotype.Repository;

import br.dev.kumulus.arq.persistence.hibernate.HibernateTemplateCrudDao;
import br.dev.kumulus.domain.Endereco;

@Repository
public class EnderecoDaoImpl extends HibernateTemplateCrudDao<Endereco> implements EnderecoDao {

	private static final long serialVersionUID = 1L;

}
