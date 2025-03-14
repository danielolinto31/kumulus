package br.dev.kumulus.arq.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Esta anotação tem por objetivo oferecer ao usuário desenvolvedor um meio de
 * garantir a unicidade de registros, ou seja, impedir a existência de registros
 * duplicados. Para isso o usuário pode especificar um campo, "n" campos e/ou
 * uma sequência de campos.
 * </p>
 * <p>
 * Obs: Caso o atributo em questão não seja primitivo, a comparação se dará via
 * método <code>equals()</code>.
 * </p>
 * 
 * <p>
 * Exemplo em que não será permitido o cadastro de um produto com o mesmo
 * <b>nome e fornecedor</b>:
 * <pre>{@code
 * UniqueAttributes(["nome,fornecedor"]) [
 * class Produto  
 *     private String nome;
 *     private Fornecedor fornecedor;
 *     ...
 * ]
 * }</pre>
 * </p>
 * 
 * <p>
 * Exemplo em que não será permitido o cadastro de um produto com o mesmo
 * <b>nome e fornecedor</b> ou com a mesma <b>data de cadastro</b>
 * <pre>{@code
 * UniqueAttributes(["nome,fornecedor", "dataCadastro"]) [
 * class Produto  
 *     private String nome;
 *     private Fornecedor fornecedor;
 *     ...
 * ]
 * }</pre>
 * </p>
 * 
 * <p>
 * Obs: Por questões de limitação do javadoc não é posssível utilizar "@" e "{}"
 * nos exemplos de código.
 * </p>
 * </p>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UniqueAttributes {

    public String[] value() default {};

}
