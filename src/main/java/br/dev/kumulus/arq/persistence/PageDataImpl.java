package br.dev.kumulus.arq.persistence;

import java.util.List;

/**
 * Implementação default de {@link PageData}. É obrigatória a instanciação da
 * classe com dois atributos: <tt>data</tt> e <tt>countAll</tt>, onde
 * data é a lista de objetos encontrada e countAll quantidade total de objetos,
 * independente da lista retornada.
 * 
 * @param <T> tipo genérico que a classe representa.
 */
public class PageDataImpl<T> implements PageData<T> {

	private static final long serialVersionUID = 1L;

	private transient List<T> data;
	private final Integer countAll;

	public PageDataImpl(Integer countAll, List<T> data) {
		super();
		this.data = data;
		this.countAll = countAll;
	}

	@Override
	public List<T> getData() {
		return data;
	}

	@Override
	public Integer getCountAll() {
		return countAll;
	}

}
