package br.dev.kumulus.arq.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Esta anotação tem por objetivo oferecer ao usuário desenvolvedor um meio de
 * definir uma ordenação default para determinada entidade. Ela será levada em
 * em conta nos métodos de busca definidos na interface {@link CrudDao}.
 * </p>
 * 
 * @see CrudDao#findByAttributes(Persistent)
 * @see CrudDao#findByAttributes(Persistent, int, int)
 * @see CrudDao#findByAttributes(Persistent, String, Boolean)
 * @see CrudDao#findByAttributes(Persistent, int, int, String, Boolean)
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultOrderBy {

    String sortField() default "";

    SortOrder sortOrder() default SortOrder.ASCENDING;
}
