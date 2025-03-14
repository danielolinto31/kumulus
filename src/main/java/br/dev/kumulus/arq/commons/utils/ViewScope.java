package br.dev.kumulus.arq.commons.utils;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.web.context.request.FacesRequestAttributes;

/**
 * Essa classe implementa um {@link Scope} que efetua o mesmo comportamento do
 * <tt>ViewScoped</tt> do JSF. <br />
 * O escopo de view funciona da seguinte forma:
 * <ul>
 * <li>Ao carregar uma página pela primeira vez, o seu respectivo bean é
 * instanciado e toda a árvore da componentes da página é criado.</li>
 * <li>Caso a mesma página seja novamente chamada, através de submissão de dados
 * através de requisição AJAX, o bean não é instanciado novamente, ou seja, seu
 * estado é mantido, desta forma não é preciso utilizar artifícios como o
 * componente <i>SaveState</i> do <i>Tomahawk</i> ou colocar o bean em questão
 * no escopo de <b>SESSION</b>.</li>
 * </ul>
 * Se o método tiver um retorno {@link String} e corresponder a um view id
 * válido, então o bean é reconstruído. Caso o método seja <tt>void</tt>, o bean
 * não é instanciado novamente na fase <b>RENDER_RESPONSE</b>.
 * 
 */
public class ViewScope implements Scope {

	public static final String VIEW_SCOPE_CALLBACKS = "view3Scope.callbacks";

	public synchronized Object get(String name, ObjectFactory<?> objectFactory) {
		Object instance = getViewMap().get(name);

		if (instance == null) {
			instance = objectFactory.getObject();
			getViewMap().put(name, instance);
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public Object remove(String name) {
		Object instance = getViewMap().remove(name);
		if (instance != null) {
			Map<String, Runnable> callbacks = (Map<String, Runnable>) getViewMap().get(VIEW_SCOPE_CALLBACKS);
			if (callbacks != null) {
				callbacks.remove(name);
			}
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	public void registerDestructionCallback(String name, Runnable runnable) {
		Map<String, Runnable> callbacks = (Map<String, Runnable>) getViewMap().get(VIEW_SCOPE_CALLBACKS);
		if (callbacks != null) {
			callbacks.put(name, runnable);
		}
	}

	public Object resolveContextualObject(String name) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesRequestAttributes facesRequestAttributes = new FacesRequestAttributes(facesContext);
		return facesRequestAttributes.resolveReference(name);
	}

	public String getConversationId() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		FacesRequestAttributes facesRequestAttributes = new FacesRequestAttributes(facesContext);
		return facesRequestAttributes.getSessionId() + "-" + facesContext.getViewRoot().getViewId();
	}

	private Map<String, Object> getViewMap() {
		return FacesContext.getCurrentInstance().getViewRoot().getViewMap();
	}
}
