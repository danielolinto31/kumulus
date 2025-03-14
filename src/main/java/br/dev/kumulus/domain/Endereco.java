package br.dev.kumulus.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import br.dev.kumulus.arq.persistence.DefaultOrderBy;
import br.dev.kumulus.arq.persistence.Persistent;
import br.dev.kumulus.arq.persistence.SortOrder;
import lombok.Data;

@Data
@Entity
@Table(name = "Endereco")
@DefaultOrderBy(sortField = "cidade", sortOrder = SortOrder.ASCENDING)
public class Endereco implements Persistent {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false)
	private Integer id;

	@NotNull(message = "O campo \"cep\" é obrigatório.")
	@Length(min = 2, max = 8, message = "O campo \"cep\" deve conter entre {min} e {max} caracteres.")
	@Column(name = "cep", nullable = false)
	private String cep;

	@NotNull(message = "O campo \"uf\" é obrigatório.")
	@Length(min = 2, max = 2, message = "O campo \"uf\" deve conter {max} caracteres.")
	@Column(name = "uf", nullable = false)
	private String uf;

	@NotNull(message = "O campo \"cidade\" é obrigatório.")
	@Length(min = 2, max = 100, message = "O campo \"cidade\" deve conter entre {min} e {max} caracteres.")
	@Column(name = "cidade", nullable = false)
	private String cidade;

	@NotNull(message = "O campo \"bairro\" é obrigatório.")
	@Length(min = 2, max = 100, message = "O campo \"bairro\" deve conter entre {min} e {max} caracteres.")
	@Column(name = "bairro", nullable = false)
	private String bairro;

	@NotNull(message = "O campo \"logradouro\" é obrigatório.")
	@Length(min = 2, max = 100, message = "O campo \"logradouro\" deve conter entre {min} e {max} caracteres.")
	@Column(name = "logradouro", nullable = false)
	private String logradouro;

	@NotNull(message = "O campo \"número\" é obrigatório.")
	@Column(name = "numero", nullable = false)
	private Integer numero;

	@Column(name = "complemento", length = 100)
	private String complemento;

	@ManyToOne
	@JoinColumn(name = "id_pessoa")
	private Pessoa pessoa;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	@Transient
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append(logradouro).append(", ").append(numero).append(", ").append(bairro).append(", ").append(cidade)
				.append("/").append(uf).append(", ").append(cep);
		return sb.toString();
	}

	@Override
	@Transient
	public String getEntityLabel() {
		return "Endereco";
	}

	public String getCidadeAndUf() {
		StringBuilder sb = new StringBuilder();
		sb.append(cidade).append("/").append(uf);
		return sb.toString();
	}

}
