package br.dev.kumulus.arq.commons.utils;

import java.io.IOException;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import br.dev.kumulus.arq.exception.MimeTypeException;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

public class FacesUtil {

	private FacesUtil() {
		// constructor not implement
	}

	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	public static ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	public static HttpServletRequest getRequest() {
		return (HttpServletRequest) getExternalContext().getRequest();
	}

	public static String getRequestContextPath() {
		return getExternalContext().getRequestContextPath();
	}

	public static String getRequestURI() {
		HttpServletRequest servletRequest = (HttpServletRequest) getExternalContext().getRequest();
		return servletRequest.getRequestURI();
	}

	public static Object getRequestAttribute(String attribute) {
		return getRequest().getAttribute(attribute);
	}

	public static void setRequestAttribute(String attribute, Object object) {
		getRequest().setAttribute(attribute, object);
	}

	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	public static void setSessionAttribute(String attribute, Object object) {
		getSession().setAttribute(attribute, object);
	}

	public static Object getSessionAttribute(String attribute) {
		return getSession().getAttribute(attribute);
	}

	public static String getFullContextRealPath() {
		return getSession().getServletContext().getRealPath("");
	}

	public static String addRedirectToPath(String path) {
		if (!path.contains("?")) {
			return path + "?faces-redirect=true";
		} else {
			return path + "&faces-redirect=true";
		}
	}

	public static String addIncludeViewParamsToPath(String path) {
		if (!path.contains("?")) {
			return path + "?includeViewParams=true";
		} else {
			return path + "&includeViewParams=true";
		}
	}

	public static Flash getFlash() {
		return getExternalContext().getFlash();
	}

	public static void putInFlash(String key, Object value) {
		getFlash().put(key, value);
	}

	private static void addMessage(String componentId, FacesMessage.Severity severity, String message) {
		getFacesContext().addMessage(componentId, new FacesMessage(severity, message + ".", null));
	}

	public static void addMessages(Severity severity, List<String> messages) {
		for (String message : messages) {
			addMessage(null, severity, message);
		}
	}

	public static void addError(String message) {
		addError(null, message);
	}

	public static void addError(String componentId, String message) {
		addMessage(componentId, FacesMessage.SEVERITY_ERROR, message);
	}

	public static void addErrors(List<String> messages) {
		addMessages(FacesMessage.SEVERITY_ERROR, messages);
	}

	public static void addWarning(String message) {
		addWarning(null, message);
	}

	public static void addWarning(String componentId, String message) {
		addMessage(componentId, FacesMessage.SEVERITY_WARN, message);
	}

	public static void addWarnings(List<String> messages) {
		addMessages(FacesMessage.SEVERITY_WARN, messages);
	}

	public static void addInfo(String message) {
		addInfo(null, message);
	}

	public static void addInfo(String componentId, String message) {
		addMessage(componentId, FacesMessage.SEVERITY_INFO, message);
	}

	public static void addInfos(List<String> messages) {
		addMessages(FacesMessage.SEVERITY_INFO, messages);
	}

	public static String getMimeType(byte[] dataFile) {
		String mimeType = null;
		MagicMatch magic;
		try {
			magic = Magic.getMagicMatch(dataFile);
			mimeType = magic.getMimeType();
		} catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
			throw new MimeTypeException("Ocorreu um erro ao tentar obter o tipo do arquivo", e);
		}  

		return mimeType;
	}

	public static void provideFileDownload(String fileName, byte[] dataFile) throws IOException {
		provideFile(true, fileName, dataFile);
	}

	public static void provideFileView(String fileName, byte[] dataFile) throws IOException {
		provideFile(false, fileName, dataFile);
	}

	private static synchronized void provideFile(boolean forceDownload, String fileName, byte[] dataFile)
			throws IOException {

		HttpServletResponse response = (HttpServletResponse) getExternalContext().getResponse();

		ServletOutputStream servletOutputStream = response.getOutputStream();

		String contentDisposition = (forceDownload ? "attachment;filename=\"" + fileName + "\""
				: "filename=\"" + fileName + "\"");

		String contentType = getMimeType(dataFile);
		response.setContentType(contentType);
		response.setHeader("Content-Disposition", contentDisposition);
		response.setContentLength(dataFile.length);

		servletOutputStream.write(dataFile, 0, dataFile.length);
		servletOutputStream.flush();
		servletOutputStream.close();
		FacesContext.getCurrentInstance().renderResponse();
		FacesContext.getCurrentInstance().responseComplete();
	}

}
