package br.dev.kumulus.dao;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import br.dev.kumulus.arq.commons.utils.Utils;
import br.dev.kumulus.domain.Pessoa;

public class LazyPessoaSorter implements Comparator<Pessoa> {

	private String sortField;
	private SortOrder sortOrder;

	public LazyPessoaSorter(String sortField, SortOrder sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	@Override
	public int compare(Pessoa pessoa1, Pessoa pessoa2) {
		try {
			Object value1 = Utils.getPropertyValueViaReflection(pessoa1, sortField);
			Object value2 = Utils.getPropertyValueViaReflection(pessoa2, sortField);

			@SuppressWarnings({ "rawtypes", "unchecked" })
			int value = ((Comparable) value1).compareTo(value2);

			return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
