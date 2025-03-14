package br.dev.kumulus.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EnderecoDTO {

    private String cep;
    private String uf;
    private String cidade;
    private String bairro;
    private String logradouro;
    private Integer numero;
    private Boolean sucesso = Boolean.FALSE;
}
