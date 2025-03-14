package br.dev.kumulus.domain.enums;

import lombok.Getter;

@Getter
public enum Sexo {

	F("Feminino"),
	M("Masculino");

	private String descricao;

	private Sexo(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public String toString() {
		return this.name();
	}

}
