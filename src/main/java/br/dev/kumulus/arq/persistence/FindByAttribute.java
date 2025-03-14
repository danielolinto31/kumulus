package br.dev.kumulus.arq.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FindByAttribute {

	public enum DateComparation {
		LESS_THAN, GREATER_THAN, EXACT, EXACT_DMY;
	}

	public enum StringComparation {
		ANYWHERE, END, START, EXACT;
	}

	public DateComparation dateComparation() default DateComparation.EXACT_DMY;

	public StringComparation stringComparation() default StringComparation.ANYWHERE;

}
