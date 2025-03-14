package br.dev.kumulus.viacep.ws;

import lombok.Data;

@Data
public class ViaCepDTO {

	private String cep;
	private String logradouro;
	private String complemento;
	private String bairro;
	private String cidade;
	private String uf;
	private String ibge;
	private String gia;
	private String ddd;
	private String siafi;

}
