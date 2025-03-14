package br.dev.kumulus.viacep.ws;

public enum ViaCepAttributesEnum {
	CEP("cep"),
    LOGRADOURO("logradouro"),
    COMPLEMENTO("complemento"),
    BAIRRO("bairro"),
    CIDADE("localidade"),
    UF("uf"),
    IBGE("ibge"),
    GIA("gia"),
    DDD("ddd"),
    SIAFI("siafi");

	private final String descricao;

	private ViaCepAttributesEnum(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}
