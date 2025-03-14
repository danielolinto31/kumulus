package br.dev.kumulus.service;

import br.dev.kumulus.arq.service.CrudService;
import br.dev.kumulus.domain.Endereco;
import br.dev.kumulus.dto.EnderecoDTO;
import br.dev.kumulus.viacep.ws.ViaCepDTO;

public interface EnderecoService extends CrudService<Endereco> {

	EnderecoDTO createAnEnderecoFromViaCepDTO(ViaCepDTO viaCepDTO);

}
