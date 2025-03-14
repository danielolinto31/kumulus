package br.dev.kumulus.arq.commons.utils;

import java.util.HashMap;
import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructViewMapEvent;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.ViewMapListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewScopeCallbackRegister implements ViewMapListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("unchecked")
	public void processEvent(SystemEvent event) throws AbortProcessingException {

		if (event instanceof PostConstructViewMapEvent) {
			PostConstructViewMapEvent viewMapEvent = (PostConstructViewMapEvent) event;
			UIViewRoot viewRoot = (UIViewRoot) viewMapEvent.getComponent();
			Map<String, Object> viewMap = viewRoot.getViewMap();
			viewMap.put(ViewScope.VIEW_SCOPE_CALLBACKS, new HashMap<String, Runnable>());
			logger.debug(" <<< PostConstructViewMapEvent >>>");
		} else if (event instanceof PreDestroyViewMapEvent) {
			PreDestroyViewMapEvent viewMapEvent = (PreDestroyViewMapEvent) event;
			UIViewRoot viewRoot = (UIViewRoot) viewMapEvent.getComponent();
			Map<String, Object> viewMap = viewRoot.getViewMap();

			Map<String, Runnable> callbacks = (Map<String, Runnable>) viewMap.get(ViewScope.VIEW_SCOPE_CALLBACKS);
			if (callbacks != null) {
				for (Runnable c : callbacks.values()) {
					logger.debug(c.getClass().getName());
					c.run();
				}
				callbacks.clear();
			}
			logger.debug("<<< PreDestroyViewMapEvent >>>");
		}
	}

	@SuppressWarnings("unused")
	private Map<String, Object> getRequestMap() {
		return FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
	}

	public boolean isListenerForSource(Object source) {
		return source instanceof UIViewRoot;
	}
}
