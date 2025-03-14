package br.dev.kumulus.dao;

import java.util.Comparator;

import org.primefaces.model.SortOrder;

import br.dev.kumulus.arq.commons.utils.Utils;
import br.dev.kumulus.domain.Endereco;

public class LazyEnderecoSorter implements Comparator<Endereco> {

	private String sortField;
	private SortOrder sortOrder;

	public LazyEnderecoSorter(String sortField, SortOrder sortOrder) {
		this.sortField = sortField;
		this.sortOrder = sortOrder;
	}

	@Override
	public int compare(Endereco endereco1, Endereco endereco2) {
		try {
			Object value1 = Utils.getPropertyValueViaReflection(endereco1, sortField);
			Object value2 = Utils.getPropertyValueViaReflection(endereco2, sortField);

			@SuppressWarnings({ "rawtypes", "unchecked" })
			int value = ((Comparable) value1).compareTo(value2);

			return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
