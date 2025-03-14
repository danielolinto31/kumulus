package br.dev.kumulus.dao;

import org.springframework.stereotype.Repository;

import br.dev.kumulus.arq.persistence.hibernate.HibernateTemplateCrudDao;
import br.dev.kumulus.domain.Pessoa;

@Repository
public class PessoaDaoImpl extends HibernateTemplateCrudDao<Pessoa> implements PessoaDao {

	private static final long serialVersionUID = 1L;

}
