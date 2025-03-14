package br.dev.kumulus.arq.persistence.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

/**
 * <p>
 * O objetivo desta classe é servir como um repositório dos parâmetros a serem
 * aplicados em um {@link org.hibernate.Criteria Criteria}. No caso são
 * armazenados os {@link CriteriaAlias alias},
 * {@link org.hibernate.criterion.Criterion filtros} e
 * {@link org.hibernate.criterion.Order campos para ordenação}.
 * </p>
 * 
 * <p>
 * Obs: Um dos motivos que estimulou a criação desta entidade foi a necessidade
 * de centralizar a parametrização de um {@link org.hibernate.Criteria
 * Criteria}, evitando alguns problemas, dentre eles a criação de alias
 * duplicados. Para mais detalhes consultar o método
 * {@link CriteriaAssistent#addAlias(String, String)}.
 * </p>
 * 
 * @see CriteriaAssistent
 * 
 * @version 0.6.0.Final
 * @since 0.6.0.Final
 * 
 */
class CriteriaAssistent {

	private final List<CriteriaAlias> aliases;
	private final List<Criterion> criterions;
	private final List<Order> orders;

	public CriteriaAssistent() {
		super();
		this.aliases = new ArrayList<>();
		this.criterions = new ArrayList<>();
		this.orders = new ArrayList<>();
	}

	public List<CriteriaAlias> getAliases() {
		return aliases;
	}

	public List<Criterion> getCriterions() {
		return criterions;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void addCriterion(Criterion criterion) {
		this.criterions.add(criterion);
	}

	public void addAlias(String associationPath, String name) {
		boolean aliasExists = false;

		for (CriteriaAlias ca : aliases) {
			if (ca.getAlias().equals(name)) {
				aliasExists = true;
				if (!ca.getAssociationPath().equals(associationPath)) {
					throw new HibernateException(String.format(
							"Não é possível criar o alias '%s' para a associação '%s' pois ele já está sendo utilizado pela associação %s",
							name, associationPath, ca.getAssociationPath()));
				}
				break;
			}

		}

		if (!aliasExists) {
			this.aliases.add(new CriteriaAlias(associationPath, name));
		}
	}

	public void addOrder(Order order) {
		this.orders.add(order);
	}

	public void populateCriteria(DetachedCriteria criteria) {
		for (CriteriaAlias ca : this.aliases) {
			criteria.createAlias(ca.getAssociationPath(), ca.getAlias());
		}
		for (Criterion c : this.criterions) {
			criteria.add(c);
		}
		for (Order o : this.orders) {
			criteria.addOrder(o);
		}
	}

	public void populateCriteria(Criteria criteria) {
		for (CriteriaAlias ca : this.aliases) {
			criteria.createAlias(ca.getAssociationPath(), ca.getAlias());
		}
		for (Criterion c : this.criterions) {
			criteria.add(c);
		}
		for (Order o : this.orders) {
			criteria.addOrder(o);
		}
	}

}
