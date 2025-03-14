package br.dev.kumulus.arq.controls;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.event.PhaseId;

import org.primefaces.component.datatable.DataTable;

import br.dev.kumulus.arq.commons.utils.FacesUtil;
import br.dev.kumulus.arq.commons.utils.ReflectionUtils;
import br.dev.kumulus.arq.exception.ServiceBusinessException;
import br.dev.kumulus.arq.exception.ServiceDataAccessException;
import br.dev.kumulus.arq.persistence.PageData;
import br.dev.kumulus.arq.persistence.Persistent;
import br.dev.kumulus.arq.service.CrudService;

/**
 * Classe abstrata para ser usada como Controller de ações CRUD. Na maioria dos
 * casos o desenvolvedor terá apenas de criar uma classe que herda de
 * <tt>AbstractCrudBean</tt>, fornecer uma implementação da classe de negócio
 * (serviço) e crias as páginas de cadastro e consulta. Feito isto, o mecanismo
 * de busca, inserção, edição e exclusão já estará automaticamente disponível.
 *
 * @param <P> Objeto {@link Persistent} a ser manipulado
 * @param <S> Objeto {@link CrudService} que irá trabalhar com o Objeto de
 *            dominio
 */
public abstract class AbstractCrudBean<P extends Persistent, S extends CrudService<P>> extends AbstractBean {

	private static final long serialVersionUID = 1L;

	private static final String PESQUISAR = "Pesquisar";

	private final Class<P> persistentClass;

	private String filter;
	private String mode;

	private S service;

	private P entity;

	private P entityToSearch;

	private Page page;

	private String personalPage;

	private Action action;

	public enum Page {
		FORM, LIST, PERSONAL;
	}

	public enum Action {
		INSERT, EDIT, VIEW;
	}

	private List<String> searchDataTableIdsToRefresh;

	@SuppressWarnings("unchecked")
	protected AbstractCrudBean(S service) {
		if (log.isDebugEnabled()) {
			log.debug("Instanciando {}", this);
		}

		this.persistentClass = (Class<P>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		this.service = service;

		entityNewInstance();
		entityToSearchNewInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void init() {
		try {
			super.init();

			String f = FacesUtil.getRequest().getParameter("filter");
			String m = FacesUtil.getRequest().getParameter("mode");
			P entityFromFilter = null;

			boolean executeDefineInitialMode = (f == null && m == null);

			if (f != null) {
				filter = f.trim();

				try {
					entityFromFilter = (P) ReflectionUtils.createFrom(persistentClass, filter);
					executeDefineInitialMode = false;
				} catch (NoSuchFieldException e) {
					this.addError(
							"Parâmetro \"entity\" inválido, por isso a página será iniciada no modo padrão. Detalhes: Atributo não encontrado >>> "
									+ e);
					executeDefineInitialMode = true;
				} catch (Exception e) {
					this.addError(
							"Parâmetro \"entity\" inválido, por isso a página será iniciada no modo padrão. Detalhes: "
									+ e);
					executeDefineInitialMode = true;
				}
			}

			if (m != null && !executeDefineInitialMode) {
				mode = m.trim().toLowerCase();

				try {
					if (mode.equals("insert")) {
						loadInsertMode();
					} else if (mode.equals("edit")) {
						P entity = service.findById(entityFromFilter.getId());
						entityFromFilter = null;
						loadEditMode(entity);
					} else if (mode.equals("view")) {
						P entity = service.findById(entityFromFilter.getId());
						entityFromFilter = null;
						loadViewMode(entity);
					} else if (mode.equals("search")) {
						loadSearchMode();
						if (entityFromFilter != null) {
							setEntityToSearch(entityFromFilter);
							search();
						}
					} else {
						throw new Exception(
								"Valor desconhecido. Seu valor deve ser \"insert\", \"edit\" ou \"search\"");
					}
					executeDefineInitialMode = false;
				} catch (Exception e) {
					this.addError(
							"Parâmetro \"mode\" inválido, por isso a página será iniciada no modo padrão. Detalhes: "
									+ e.getMessage());
					executeDefineInitialMode = true;
				}
			}

			if (executeDefineInitialMode) {
				defineInitialMode();
			}
		} catch (Exception e) {
			addError(e);
		}
	}

	protected List<String> getSearchDataTableIdsToRefresh() {
		if (searchDataTableIdsToRefresh == null) {
			searchDataTableIdsToRefresh = new ArrayList<>();
			searchDataTableIdsToRefresh.add("frmSearchList:dtTable");
		}

		return searchDataTableIdsToRefresh;
	}

	protected void refreshSeachDataTables() {
		if (FacesUtil.getFacesContext().getCurrentPhaseId() == PhaseId.INVOKE_APPLICATION) {
			for (String dataTableId : getSearchDataTableIdsToRefresh()) {
				try {
					DataTable dataTable = (DataTable) FacesUtil.getFacesContext().getViewRoot()
							.findComponent(dataTableId);
					if (dataTable.getRowCount() > 0) {
						dataTable.setFirst(0);
					}
				} catch (Exception e) {
					log.warn(String.format(
							"Não foi possível atualizar as tabs do datable \"%s\". Forneça o id correto através do método getSearchDataTableIdsToRefresh().",
							dataTableId), e);
				}
			}
		}
	}

	protected void defineInitialMode() {
		loadInsertMode();
	}

	protected void entityNewInstance() {
		try {
			entity = persistentClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			addError(e);
		}
	}

	protected void entityToSearchNewInstance() {
		try {
			entityToSearch = persistentClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			addError(e);
		}
	}

	protected PageData<P> loadSearchData(int first, int pageSize, String sortField, boolean sortOrder) {
		return service.findPageDataByAttributes(entityToSearch, first, pageSize, sortField, sortOrder);
	}

	public P getEntity() {
		return entity;
	}

	public void setEntity(P entity) {
		this.entity = entity;
	}

	public P getEntityToSearch() {
		return entityToSearch;
	}

	public void setEntityToSearch(P entityToSearch) {
		this.entityToSearch = entityToSearch;
	}

	public S getService() {
		return service;
	}

	public void setService(S service) {
		this.service = service;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page, String operationTitle) {
		this.page = page;
		setOperationTitle(operationTitle);
	}

	public String getPersonalPage() {
		return personalPage;
	}

	public void setPersonalPage(String personalPage) {
		this.personalPage = personalPage;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public final String getFilter() {
		return filter;
	}

	private void setFilter(String filter) {
		this.filter = filter;
	}

	public final String getMode() {
		return mode;
	}

	private void setMode(String mode) {
		this.mode = mode;
	}

	@SuppressWarnings("rawtypes")
	private String mountFilter(P entityToMount, boolean mountUsingOnlyEntityId) {

		StringBuilder sb = new StringBuilder();

		if (mountUsingOnlyEntityId) {
			sb.append("id");
			sb.append(":");
			sb.append(entityToMount.getId());
		} else {
			List<String> attributesToIgnore = new ArrayList<>();
			attributesToIgnore.add("serialVersionUID");

			List<Class> classesToIgnore = new ArrayList<>();
			classesToIgnore.add(Collection.class);
			classesToIgnore.add(List.class);

			Map<String, Object> entityAttributes = ReflectionUtils.getFieldsValues(entityToMount, null, null,
					attributesToIgnore, classesToIgnore, true);
			boolean adicionarVirgula = false;
			for (Entry<String, Object> entry : entityAttributes.entrySet()) {

				if (entry.getValue() != null) {
					if (adicionarVirgula) {
						sb.append(",");
					}

					if (entry.getValue() instanceof Persistent) {
						sb.append(entry.getKey() + ".id");
						sb.append(":");
						sb.append(((Persistent) entry.getValue()).getId());
					} else {
						sb.append(entry.getKey());
						sb.append(":");
						sb.append(entry.getValue());
					}

					adicionarVirgula = true;
				}
			}
		}

		/*
		 * Os parênteses estão sendo acrescentados no filtro apenas para facilitar a
		 * visualização do filtro dentro da URL. No caso poderia ser qualquer
		 * delimitador. Obs: Uma vantagem do parênteses é que um link não fica
		 * "quebrado" ao ser incorporado no corpo de um e-mail.
		 */
		if (!sb.toString().isEmpty()) {
			return "(" + sb.toString() + ")";
		} else {
			return "";
		}
	}

	public String getEntityLabel() {
		return (entity != null ? entity.getEntityLabel() : "");
	}

	public String getPageTitle() {
		return getEntityLabel() + ": " + getOperationTitle();
	}

	public boolean isPageForm() {
		return page == Page.FORM;
	}

	public boolean isPageList() {
		return page == Page.LIST;
	}

	public boolean isPagePersonal() {
		return page == Page.PERSONAL;
	}

	public boolean isActionEdit() {
		return action == Action.EDIT;
	}

	public boolean isActionInsert() {
		return action == Action.INSERT;
	}

	public boolean isActionView() {
		return action == Action.VIEW;
	}

	protected void onBeforeLoadInsertMode() {
	}

	protected void onAfterLoadInsertMode() {
	}

	protected void onBeforeSearch() {
	}

	protected void onAfterSearch() {
	}

	protected void onBeforeLoadEditMode(P entityToEdit) {
	}

	protected void onAfterLoadEditMode(P entityToEdit) {
	}

	protected void onBeforeLoadViewMode(P entityToView) {
	}

	protected void onAfterLoadViewMode(P entityToView) {
	}

	protected void onBeforeDelete() {
	}

	protected void onAfterDelete() {
	}

	protected void onBeforeSave(P entityToSave) throws ServiceBusinessException {
	}

	protected void onAfterSave(P entitySaved) throws ServiceBusinessException {
	}

	public void loadInsertMode() {
		try {
			onBeforeLoadInsertMode();

			setPage(Page.FORM, "Inserir");
			setAction(Action.INSERT);
			entityNewInstance();

			onAfterLoadInsertMode();

			setMode("insert");
			setFilter("");
		} catch (Exception e) {
			addError(e);
		}
	}

	public void search() {
		try {
			refreshSeachDataTables();
			onBeforeSearch();
			setPage(Page.LIST, PESQUISAR);

			if (log.isDebugEnabled()) {
				log.debug("Search invocado {}", this);
			}

			setMode("search");
			setFilter(mountFilter(getEntityToSearch(), false));
		} catch (Exception e) {
			addError(e);
		}
	}

	public void loadSearchMode() {
		try {
			onBeforeLoadSearchMode();
			entityToSearchNewInstance();
			setPage(Page.LIST, PESQUISAR);

			if (log.isDebugEnabled()) {
				log.debug("Load Search Mode Invocado {}", this);
			}

			onAfterLoadSearchMode();

			/*
			 * Executa a pseudo-busca. Mesmo sem esta chamada, a busca seria executada, pois
			 * ela ocorre a cada renderização do componente <code>p:dataTable</code>. Esta
			 * chamada aqui ao metodo "search" é coerente para manter uma ordem natural de
			 * execução dos métodos hooks envolvidos.
			 */
			search();
		} catch (Exception e) {
			addError(e);
		}
	}

	protected void onBeforeLoadSearchMode() {
	}

	protected void onAfterLoadSearchMode() {
	}

	public void loadEditMode(P entityToEdit) {
		try {
			onBeforeLoadEditMode(entityToEdit);

			setEntity(entityToEdit);
			setPage(Page.FORM, "Alterar");
			setAction(Action.EDIT);

			onAfterLoadEditMode(entityToEdit);
			if (log.isDebugEnabled()) {
				log.debug("edit invocado {}", this);
			}

			setMode("edit");
			setFilter(mountFilter(entityToEdit, true));
		} catch (Exception e) {
			addError(e);
		}
	}

	public void loadViewMode(P entityToView) {
		try {
			onBeforeLoadViewMode(entityToView);

			setEntity(entityToView);
			setPage(Page.FORM, "Visualizar");
			setAction(Action.VIEW);

			onAfterLoadViewMode(entityToView);
			if (log.isDebugEnabled()) {
				log.debug("Modo de visualização invocado {}", this);
			}

			setMode("view");
			setFilter(mountFilter(entityToView, true));
		} catch (Exception e) {
			addError(e);
		}
	}

	/**
	 * @deprecated Método temporariamente habilitado por conta de problemas ainda
	 *             não resolvidos ao passar a instância "objRow" do componente
	 *             "p:dataTable" como parâmetro do método, assim como foi feito no
	 *             método de edição.
	 */
	@Deprecated
	public void delete() {
		delete(this.entity);
	}

	public void delete(P entityToDelete) {
		setPage(Page.LIST, PESQUISAR);
		if (log.isDebugEnabled()) {
			log.debug("delete() invocado {}", this);
		}
		try {
			onBeforeDelete();

			executeServiceDelete(entityToDelete);
			addInfo("Exclusão realizada com sucesso");

			onAfterDelete();
		} catch (ServiceBusinessException | ServiceDataAccessException e) {
			addError(e);
		}
	}

	public void save(P entityToSave, Action actionAfterSave) {
		if (log.isDebugEnabled()) {
			log.debug("save() invocado {}", this);
		}

		try {
			onBeforeSave(entityToSave);
			if (action == Action.INSERT) {
				executeServiceInsert(entityToSave);
				addInfo("Registro " + entityToSave.getLabel() + " inserido com sucesso");
			} else if (action == Action.EDIT) {
				executeServiceUpdate(entityToSave);
				addInfo("Registro " + entityToSave.getLabel() + " editado com sucesso");
			}
			/*
			 * O método onAfterSave() tem que ficar neste ponto pois pode ser invocado para
			 * salvar outras entidades ligadas a entidade principal. Além disso pode ser
			 * utilizado para gerar exceção caso alguma regra do negócio não seja atendida
			 * (o mesmo pode ocorrer com o onBeforeSave()).Ex: Cadastrar uma pessoa e em
			 * seguida cadastrar os contatos desta pessoa, e exigir que pelo menos um
			 * contato seja informado. Caso nenhum contato seja informado, uma exceção é
			 * gerada.
			 */
			onAfterSave(entityToSave);

			if (actionAfterSave == Action.INSERT) {
				loadInsertMode();
			} else {
				setAction(Action.EDIT);
			}

		} catch (ServiceDataAccessException | ServiceBusinessException e) {
			addError(e);
		}

	}

	public void executeServiceInsert(P entityToInsert) throws ServiceBusinessException {
		service.insert(entityToInsert);
	}

	public void executeServiceUpdate(P entityToUpdate) throws ServiceBusinessException {
		service.update(entityToUpdate);
	}

	public void executeServiceDelete(P entityToDelete) throws ServiceBusinessException {
		service.delete(entityToDelete);
	}

	public void saveAndContinue() {
		save(entity, Action.EDIT);
	}

	public void saveAndClean() {
		save(entity, Action.INSERT);
	}

}
