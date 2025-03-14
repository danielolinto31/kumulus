package br.dev.kumulus.viacep.ws;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Service;

import br.dev.kumulus.arq.exception.ViaCepException;

@Service
public class ViaCepServiceImpl implements ViaCep {

	private static final String BASE_URL = "https://viacep.com.br/ws/";

	@Override
	public ViaCepDTO find(String cep) {
		try {
			URL url = new URL(BASE_URL + cep + "/xml/");
			SAXReader xmlReader = new SAXReader();
			xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			Document documento = xmlReader.read(url);
			Element root = documento.getRootElement();

			Map<String, BiConsumer<ViaCepDTO, String>> setters = new HashMap<>();
			setters.put(ViaCepAttributesEnum.CEP.getDescricao(), ViaCepDTO::setCep);
			setters.put(ViaCepAttributesEnum.LOGRADOURO.getDescricao(), ViaCepDTO::setLogradouro);
			setters.put(ViaCepAttributesEnum.COMPLEMENTO.getDescricao(), ViaCepDTO::setComplemento);
			setters.put(ViaCepAttributesEnum.BAIRRO.getDescricao(), ViaCepDTO::setBairro);
			setters.put(ViaCepAttributesEnum.CIDADE.getDescricao(), ViaCepDTO::setCidade);
			setters.put(ViaCepAttributesEnum.UF.getDescricao(), ViaCepDTO::setUf);
			setters.put(ViaCepAttributesEnum.IBGE.getDescricao(), ViaCepDTO::setIbge);
			setters.put(ViaCepAttributesEnum.GIA.getDescricao(), ViaCepDTO::setGia);
			setters.put(ViaCepAttributesEnum.DDD.getDescricao(), ViaCepDTO::setDdd);
			setters.put(ViaCepAttributesEnum.SIAFI.getDescricao(), ViaCepDTO::setSiafi);

			ViaCepDTO result = new ViaCepDTO();
			
			List<?> rawElements = root.elements();
	        List<Element> elements = rawElements.stream()
	            .filter(Element.class::isInstance)
	            .map(Element.class::cast)
	            .collect(Collectors.toList());
	        
	        elements.stream()
	            .filter(element -> setters.containsKey(element.getName()))
	            .forEach(element -> setters.get(element.getName()).accept(result, element.getStringValue()));
	        
			return result;
		} catch (Exception e) {
			throw new ViaCepException(e.getMessage());
		}
	}
}
