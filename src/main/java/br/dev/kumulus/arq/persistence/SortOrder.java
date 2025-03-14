package br.dev.kumulus.arq.persistence;

/**
 * Enum responsável por definir a ordenação de determinados métodos da arquitetura.
 *
 */
public enum SortOrder {

    ASCENDING("ASC"),
    DESCENDING("DESC"),
    UNSORTED("");

    private String value;

    private SortOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
