package br.dev.kumulus.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.br.CPF;

import br.dev.kumulus.arq.commons.utils.DateUtils;
import br.dev.kumulus.arq.persistence.DefaultOrderBy;
import br.dev.kumulus.arq.persistence.Persistent;
import br.dev.kumulus.arq.persistence.SortOrder;
import br.dev.kumulus.arq.persistence.UniqueAttributes;
import br.dev.kumulus.domain.enums.Sexo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "Pessoa")
@UniqueAttributes(value = "cpf")
@DefaultOrderBy(sortField = "nome", sortOrder = SortOrder.ASCENDING)
public class Pessoa implements Persistent {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "id", nullable = false)
	private Integer id;

	@CPF
	@NotNull(message = "O campo \"CPF\" não pode ser vazio.")
	@Length(min = 2, max = 14, message = "O campo \"CPF\" deve conter entre {min} e {max} caracteres.")
	@Column(name = "cpf", nullable = false)
	private String cpf;

	@NotNull(message = "O campo \"nome\" é obrigatório.")
	@Length(min = 2, max = 150, message = "O campo \"nome\" deve conter entre {min} e {max} caracteres.")
	@Column(name = "nome", nullable = false)
	private String nome;

	@Temporal(TemporalType.DATE)
	@NotNull(message = "O campo \"data de nascimento\" é obrigatório.")
	@Column(name = "data_nascimento", nullable = false)
	private Date dataNascimento;

	@NotNull(message = "O campo \"idade\" é obrigatório.")
	@Column(name = "idade", nullable = false)
	private Integer idade;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "O campo \"sexo\" é obrigatório.")
	@Column(name = "sexo", nullable = false, columnDefinition = "char(1)")
	private Sexo sexo;

	@LazyCollection(LazyCollectionOption.FALSE)
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "pessoa", orphanRemoval = true)
	private List<Endereco> enderecoList;

	@Column(name = "ip", nullable = true)
	private String ip;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "data_envio", nullable = true)
	private Date dataEnvio;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	@Transient
	public String getLabel() {
		StringBuilder sb = new StringBuilder();
		sb.append("\"").append(nome).append(", sexo ").append(sexo.getDescricao()).append(", nascido em ")
				.append(DateUtils.dataFormatada(dataNascimento)).append("\"");
		return sb.toString();
	}

	@Override
	@Transient
	public String getEntityLabel() {
		return "Pessoa";
	}

	public Pessoa() {
    	this.enderecoList = new ArrayList<>();
    }

}
