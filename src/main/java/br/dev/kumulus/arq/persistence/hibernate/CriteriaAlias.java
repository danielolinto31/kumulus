package br.dev.kumulus.arq.persistence.hibernate;

/**
 * O objetivo desta classe é representar um alias a ser criado em um Criteria.
 * 
 * @see CriteriaAssistent
 * 
 * @version 0.6.0.Final
 * @since 0.6.0.Final
 * 
 */
class CriteriaAlias {

	private String associationPath;
	private String alias;

	public CriteriaAlias(String associationPath, String alias) {
		super();
		this.associationPath = associationPath;
		this.alias = alias;
	}

	public String getAssociationPath() {
		return associationPath;
	}

	public String getAlias() {
		return alias;
	}

}
