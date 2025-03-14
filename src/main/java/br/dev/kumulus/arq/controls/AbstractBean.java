package br.dev.kumulus.arq.controls;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.Flash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.dev.kumulus.arq.commons.utils.FacesUtil;

/**
 * Classe pai para ManagedBeans ou Spring beans com métodos utilitários para JSF
 *
 */
public class AbstractBean implements Serializable {

	private static final long serialVersionUID = 1L;

	protected transient Logger log = LoggerFactory.getLogger(getClass());

	private String operationTitle;

	@PostConstruct
	protected void init() {
		onInit();
	}

	protected void onInit() {
		// method is empty
	}

	public String getOperationTitle() {
		return operationTitle;
	}

	public void setOperationTitle(String operationTitle) {
		this.operationTitle = operationTitle;
	}

	public String addRedirectToPath(String path) {
		return FacesUtil.addRedirectToPath(path);
	}

	public Flash getFlash() {
		return FacesUtil.getFlash();
	}

	public void flash(String key, Object value) {
		FacesUtil.getFlash().put(key, value);
	}

	public void addError(Throwable throwable) {
		log.debug(throwable.getMessage(), throwable);
		FacesUtil.addError(throwable.getMessage());
	}

	public void addError(String message) {
		FacesUtil.addError(message);
	}

	public void addError(String componentId, String message) {
		FacesUtil.addError(componentId, message);
	}

	public void addWarning(String message) {
		FacesUtil.addWarning(message);
	}

	public void addWarning(String componentId, String message) {
		FacesUtil.addWarning(componentId, message);
	}

	public void addInfo(String message) {
		FacesUtil.addInfo(message);
	}

	public void addInfo(String componentId, String message) {
		FacesUtil.addInfo(componentId, message);
	}

	public void addErrors(List<String> messages) {
		FacesUtil.addErrors(messages);
	}

	public void addWarnings(List<String> messages) {
		FacesUtil.addWarnings(messages);
	}

	public void addInfo(List<String> messages) {
		FacesUtil.addInfos(messages);
	}

}
