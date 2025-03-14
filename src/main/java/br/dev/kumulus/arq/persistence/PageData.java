package br.dev.kumulus.arq.persistence;

import java.io.Serializable;
import java.util.List;

/**
 * <code>PageData</code> define uma entidate para auxílio a paginação.<br />
 * A idéia é que métodos que retornam <code>PageData</code> já 'entreguem' os
 * dados ajustados para paginação, de forma que seja possível saber qual a
 * quantidade total de registros e quais são os registros que estão sendo
 * exibidos no momento. A quantidade de registros retornada, pode ser menor que
 * a quantidade total de registros.
 *
 */
public interface PageData<T> extends Serializable {

	public List<T> getData();

	public Integer getCountAll();

}
