package br.dev.kumulus.dao;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.apache.commons.collections4.ComparatorUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.filter.FilterConstraint;
import org.primefaces.util.LocaleUtils;

import br.dev.kumulus.arq.commons.utils.Utils;
import br.dev.kumulus.domain.Endereco;

public class LazyEnderecoDataModel extends LazyDataModel<Endereco> {

    private static final long serialVersionUID = 1L;

    private List<Endereco> datasource;

    public LazyEnderecoDataModel(List<Endereco> datasource) {
        this.datasource = datasource;
    }

    @Override
    public Endereco getRowData(String rowKey) {
        for (Endereco endereco : datasource) {
            if (endereco.getId() == Integer.parseInt(rowKey)) {
                return endereco;
            }
        }

        return null;
    }

    @Override
    public String getRowKey(Endereco endereco) {
        return String.valueOf(endereco.getId());
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        return (int) datasource.stream()
                .filter(o -> filter(FacesContext.getCurrentInstance(), filterBy.values(), o))
                .count();
    }

	@Override
    public List<Endereco> load(int offset, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        // apply offset & filters
        List<Endereco> enderecos = datasource.stream()
                .filter(o -> filter(FacesContext.getCurrentInstance(), filterBy.values(), o))
                .collect(Collectors.toList());

        // sort
        if (!sortBy.isEmpty()) {
            List<Comparator<Endereco>> comparators = sortBy.values().stream()
                  .map(o -> new LazyEnderecoSorter(o.getField(), o.getOrder()))
                  .collect(Collectors.toList());
            Comparator<Endereco> cp = ComparatorUtils.chainedComparator(comparators); // from apache
            enderecos.sort(cp);
        }

        return enderecos.subList(offset, Math.min(offset + pageSize, enderecos.size()));
    }

    private boolean filter(FacesContext context, Collection<FilterMeta> filterBy, Object o) {
        boolean matching = true;

        for (FilterMeta filter : filterBy) {
            FilterConstraint constraint = filter.getConstraint();
            Object filterValue = filter.getFilterValue();

            try {
                Object columnValue = String.valueOf(Utils.getPropertyValueViaReflection(o, filter.getField()));
                matching = constraint.isMatching(context, columnValue, filterValue, LocaleUtils.getCurrentLocale());
            }
            catch (ReflectiveOperationException | IntrospectionException e) {
                matching = false;
            }

            if (!matching) {
                break;
            }
        }

        return matching;
    }

}
