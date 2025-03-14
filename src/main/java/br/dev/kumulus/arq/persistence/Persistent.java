package br.dev.kumulus.arq.persistence;

import java.io.Serializable;

/**
 * <code>Persistent</code> define uma entidade persistível. Uma classe de
 * domínio.<br />
 * As classes que a implementarem deverão fornecer um identificador padrão e um
 * label que representa o objeto que foi instanciado e um nome amigável para a
 * entidade em si.
 * 
 */
public interface Persistent extends Serializable {

	public Integer getId();

	public String getLabel();

	public String getEntityLabel();

}
