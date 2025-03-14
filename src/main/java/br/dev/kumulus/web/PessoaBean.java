package br.dev.kumulus.web;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.primefaces.component.export.ExcelOptions;
import org.primefaces.component.export.PDFOptions;
import org.primefaces.component.export.PDFOrientationType;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;

import br.dev.kumulus.arq.commons.utils.DateUtils;
import br.dev.kumulus.arq.commons.utils.Utils;
import br.dev.kumulus.arq.controls.AbstractCrudBean;
import br.dev.kumulus.arq.exception.ServiceBusinessException;
import br.dev.kumulus.dao.LazyEnderecoDataModel;
import br.dev.kumulus.dao.LazyPessoaDataModel;
import br.dev.kumulus.domain.Endereco;
import br.dev.kumulus.domain.Pessoa;
import br.dev.kumulus.domain.enums.Sexo;
import br.dev.kumulus.dto.EnderecoDTO;
import br.dev.kumulus.service.EnderecoService;
import br.dev.kumulus.service.PessoaService;
import br.dev.kumulus.viacep.ws.ViaCep;
import br.dev.kumulus.viacep.ws.ViaCepDTO;
import br.dev.kumulus.viacep.ws.ViaCepServiceImpl;
import lombok.Getter;
import lombok.Setter;

@Controller
@Getter
@Setter
@Scope("view")
public class PessoaBean extends AbstractCrudBean<Pessoa, PessoaService> {

	private static final long serialVersionUID = 1L;

	private static final String ERRO = "Erro: ";
	private static final String MESSAGE_VIACEP_CEP_NAO_PREENCHIDO = "O campo CEP não foi preenchido. Digite um CEP válido";
	private static final String MESSAGE_VIACEP_CEP_NAO_ENCONTRADO = "CEP não encontrado. Verifique se foi digitado corretamente";
	private static final String MESSAGE_SUCESSO_ENDERECO_EXCLUIR = "Endereço removido com sucesso";
	private static final String MESSAGE_ERRO_VIACEP = "Ocorreu um erro ao realizar requisição para o webservice: ";
	private static final String MESSAGE_ERRO_IP = "Erro ao capturar o IP do usuário";
	private static final String MESSAGE_ERRO_ENDERECO_EXCLUIR = "Não foi possível excluir o endereço. A lista de endereços não pode estar vazia";
	private static final String MESSAGE_VALIDACAO_DATA_NASCIMENTO = "A data de nascimento não pode ser maior que a data atual";

	private final EnderecoService enderecoService;
	private transient ViaCep viaCep = new ViaCepServiceImpl();

	private LazyDataModel<Pessoa> pessoaModel;
	private LazyDataModel<Endereco> enderecoModel;
	private List<Pessoa> pessoaList;
	private List<Sexo> sexoList;
	private List<Endereco> enderecoList;
	private List<FilterMeta> filterBy;
	private Endereco endereco;
	private Date dataAtual = DateUtils.getCurrentDate();
	private transient ExcelOptions excelOpt;
	private transient PDFOptions pdfOpt;

	private transient HttpServletRequest request;

	@Inject
	public PessoaBean(PessoaService service, EnderecoService enderecoService) {
		super(service);
		this.enderecoService = enderecoService;
		personalizacaoExportacao();
	}

	@Override
	public void onInit() {
		this.pessoaList = new ArrayList<>();
		this.sexoList = Arrays.asList(Sexo.values());
		this.enderecoList = new ArrayList<>();

		pessoaModel = new LazyPessoaDataModel(getService().findByAttributes(getEntity()));
		//enderecoModel = new LazyEnderecoDataModel(enderecoService.findByAttributes(endereco));

		personalizacaoExportacao();
	}

	@Override
	public void delete(Pessoa entityToDelete) {
		super.delete(entityToDelete);
		loadInsertMode();
	}

	@Override
	public void saveAndClean() {
		getEntity().setIdade(inserirIdade());
		getEntity().setIp(getClientIp(request));
		getEntity().setDataEnvio(new Date());

		persistirPessoa();
	}

	private void persistirPessoa() {
		if (Action.INSERT.equals(getAction())) {
			super.saveAndClean();
		} else {
			super.saveAndContinue();
		}
	}

	public void carregarEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

	public void excluirEndereco(Endereco endereco) {
		try {
			getEntity().getEnderecoList().remove(endereco);
			executeServiceUpdate(getEntity());
			enderecoService.delete(endereco);
			addInfo(MESSAGE_SUCESSO_ENDERECO_EXCLUIR);
		} catch (ServiceBusinessException e) {
			getEntity().getEnderecoList().add(endereco);
			log.error(MESSAGE_ERRO_ENDERECO_EXCLUIR, e);
			addError(MESSAGE_ERRO_ENDERECO_EXCLUIR);
		}
	}

	public void prepararNovoEndereco() {
		this.endereco = new Endereco();
	}

	public void salvarEndereco() {
		if (endereco.getCep() != null && !endereco.getCep().isEmpty()) {
			if (endereco.getId() == null) {
				endereco.setPessoa(getEntity());
				getEntity().getEnderecoList().add(endereco);
			} else {
				getEntity().getEnderecoList().stream().filter(e -> e.getId().equals(endereco.getId())).findFirst()
						.ifPresent(e -> {
							e.setPessoa(getEntity());
							e.setCep(endereco.getCep());
							e.setUf(endereco.getUf());
							e.setCidade(endereco.getCidade());
							e.setBairro(endereco.getBairro());
							e.setLogradouro(endereco.getLogradouro());
							e.setNumero(endereco.getNumero());
							e.setComplemento(endereco.getComplemento());
						});
			}
			this.endereco = new Endereco();
			PrimeFaces.current().executeScript("PF('dlgEndereco').hide()");
			PrimeFaces.current().ajax().update("messages");
		} else {
			addError("É necessário preencher um CEP válido");
		}
	}

	public int inserirIdade() {
		LocalDate birthday = getEntity().getDataNascimento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return Period.between(birthday, LocalDate.now()).getYears();
	}

	public void validateDate() {
		if (getEntity().getDataNascimento() != null && Utils
				.verifyEndDateGreaterThanInitialDate(getEntity().getDataNascimento(), DateUtils.getCurrentDate())) {
			addError(MESSAGE_VALIDACAO_DATA_NASCIMENTO);
			getEntity().setDataNascimento(null);
		}
	}

	public String getClientIp(HttpServletRequest request) {
		String ipAddress = "";

		try {
			InetAddress addr = InetAddress.getLocalHost();

			if (request == null) {
				ipAddress = addr.getHostAddress();
			} else {
				ipAddress = request.getHeader("X-FORWARDED-FOR");
				if (ipAddress == null) {
					ipAddress = request.getRemoteAddr();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
			log.error(ERRO, e);
			addError(MESSAGE_ERRO_IP);
		}

		return ipAddress;
	}

	public void carregarEnderecoPeloCep() {
		try {
			if (endereco.getCep() == null || endereco.getCep().isEmpty()) {
				addWarning(MESSAGE_VIACEP_CEP_NAO_PREENCHIDO);
				return;
			}

			ViaCepDTO cepResult = viaCep.find(endereco.getCep());
			if (cepResult != null && cepResult.getCep() != null) {
				EnderecoDTO enderecoResult = enderecoService.createAnEnderecoFromViaCepDTO(cepResult);

				if (Boolean.TRUE.equals(enderecoResult.getSucesso())) {
					endereco.setUf(enderecoResult.getUf());
					endereco.setCidade(enderecoResult.getCidade());
					endereco.setBairro(enderecoResult.getBairro());
					endereco.setLogradouro(enderecoResult.getLogradouro());
				}
			} else {
				addWarning(MESSAGE_VIACEP_CEP_NAO_ENCONTRADO);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ERRO, e);
			addError(MESSAGE_ERRO_VIACEP + e.getMessage());
		}
	}

	public void limparCampos() {
		entityNewInstance();
		entityToSearchNewInstance();
		loadInsertMode();
	}

	public void personalizacaoExportacao() {
		excelOpt = new ExcelOptions();
		excelOpt.setFacetBgColor("#F88017");
		excelOpt.setFacetFontSize("10");
		excelOpt.setFacetFontColor("#0000FF");
		excelOpt.setFacetFontStyle("BOLD");
		excelOpt.setCellFontColor("#145C14");
		excelOpt.setCellFontSize("8");
		excelOpt.setFontName("Verdana");

		pdfOpt = new PDFOptions();
		pdfOpt.setFacetBgColor("#F4F4F4");
		pdfOpt.setFacetFontColor("#000000");
		pdfOpt.setFacetFontStyle("BOLD");
		pdfOpt.setFacetFontSize("10");
		pdfOpt.setCellFontSize("10");
		pdfOpt.setFontName("Arial");
		pdfOpt.setOrientation(PDFOrientationType.LANDSCAPE);
	}

	public void preProcessEnderecosPDF(Object document) throws IOException, BadElementException, DocumentException {
		Document pdf = (Document) document;
		pdf.setMargins(0, 0, 20, 20);
		pdf.open();
		pdf.setPageSize(PageSize.A4);

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		String separator = File.separator;
		String logo = externalContext.getRealPath("") + separator + "resources" + separator + "images" + separator
				+ "icon-kumulus.png";

		Image imagem = Image.getInstance(logo);
		imagem.scaleAbsolute(50, 50);
		imagem.setAlt("Logo do sistema");
		imagem.setAlignment(Element.ALIGN_CENTER);

		Paragraph cabecalho = new Paragraph("Kumulus - Cadastro de Pessoas");
		cabecalho.setAlignment(Element.ALIGN_CENTER);
		cabecalho.setSpacingAfter(20);

		Paragraph titulo = new Paragraph("Lista de Endereços");
		titulo.setAlignment(Element.ALIGN_LEFT);
		titulo.setIndentationLeft(84);
		titulo.setSpacingAfter(5);

		pdf.add(imagem);
		pdf.add(cabecalho);
		pdf.add(titulo);
	}

	public void preProcessPessoasPDF(Object document) throws IOException, BadElementException, DocumentException {
		Document pdf = (Document) document;
		pdf.setMargins(0, 0, 20, 20);
		pdf.open();
		pdf.setPageSize(PageSize.A4);

		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();

		String separator = File.separator;
		String logo = externalContext.getRealPath("") + separator + "resources" + separator + "images" + separator
				+ "icon-kumulus.png";

		Image imagem = Image.getInstance(logo);
		imagem.scaleAbsolute(50, 50);
		imagem.setAlt("Logo do sistema");
		imagem.setAlignment(Element.ALIGN_CENTER);

		Paragraph cabecalho = new Paragraph("Kumulus - Cadastro de Pessoas");
		cabecalho.setAlignment(Element.ALIGN_CENTER);
		cabecalho.setSpacingAfter(20);

		Paragraph titulo = new Paragraph("Lista de Pessoas");
		titulo.setAlignment(Element.ALIGN_LEFT);
		titulo.setIndentationLeft(84);
		titulo.setSpacingAfter(5);

		pdf.add(imagem);
		pdf.add(cabecalho);
		pdf.add(titulo);
	}
}
