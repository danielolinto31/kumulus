package br.dev.kumulus.service;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import br.dev.kumulus.arq.service.CrudServiceImpl;
import br.dev.kumulus.dao.EnderecoDao;
import br.dev.kumulus.domain.Endereco;
import br.dev.kumulus.dto.EnderecoDTO;
import br.dev.kumulus.viacep.ws.ViaCepDTO;

@Service
public class EnderecoServiceImpl extends CrudServiceImpl<Endereco, EnderecoDao> implements EnderecoService {

	private static final long serialVersionUID = 1L;

	@Inject
	public EnderecoServiceImpl(EnderecoDao dao) {
		super(dao);
	}

	@Override
	public EnderecoDTO createAnEnderecoFromViaCepDTO(ViaCepDTO viaCepDTO) {
		EnderecoDTO enderecoDTO = new EnderecoDTO();
		if (viaCepDTO != null && viaCepDTO.getCep() != null) {
			enderecoDTO.setCep(viaCepDTO.getCep());
			enderecoDTO.setUf(viaCepDTO.getUf());
			enderecoDTO.setCidade(viaCepDTO.getCidade());
			enderecoDTO.setBairro(viaCepDTO.getBairro());
			enderecoDTO.setLogradouro(viaCepDTO.getLogradouro());
			enderecoDTO.setSucesso(Boolean.TRUE);
		}

		return enderecoDTO;
	}

}
