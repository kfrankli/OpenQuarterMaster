package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api.BarcodeLookupClient;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class BarcodeLookupService extends ApiProductSearchService {
	
	BarcodeLookupClient barcodeLookupClient;
	@Getter
	ExtItemLookupProviderInfo providerInfo;
	String apiKey;
	
	@Inject
	public BarcodeLookupService(
		@RestClient
		BarcodeLookupClient barcodeLookupClient,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.displayName")
		String displayName,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.description", defaultValue = "")
		String description,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.acceptsContributions", defaultValue = "")
		boolean acceptsContributions,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.homepage", defaultValue = "")
		URL homepage,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.cost", defaultValue = "")
		String cost,
		@ConfigProperty(name = "productLookup.providers.barcodelookup-com.apiKey", defaultValue = "")
		String apiKey
	) {
		this.barcodeLookupClient = barcodeLookupClient;
		
		ExtItemLookupProviderInfo.Builder infoBuilder = ExtItemLookupProviderInfo
			.builder()
			.displayName(displayName)
			.description(description)
			.acceptsContributions(acceptsContributions)
			.homepage(homepage)
			.cost(cost);
		
		if(apiKey == null || apiKey.isBlank()){
			log.warn("API key for {} was null or blank.", displayName);
			infoBuilder.enabled(false);
			this.apiKey = null;
		} else {
			infoBuilder.enabled(enabled);
			this.apiKey = apiKey;
		}
		
		this.providerInfo = infoBuilder.build();
	}
	
	@Override
	public boolean isEnabled() {
		return this.getProviderInfo().isEnabled();
	}
	
	/**
	 * https://www.barcodelookup.com/api
	 *
	 * @param results
	 * @return
	 */
	@Override
	public List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results) {
		log.debug("Data from BarcodeLookup: {}", results.toPrettyString());
		
		ArrayNode resultsAsArr = (ArrayNode) results.get("products");
		List<ExtItemLookupResult> resultList = new ArrayList<>(resultsAsArr.size());
		
		for (JsonNode result : resultsAsArr) {
			ObjectNode curResultJson = (ObjectNode) result;
			String brandName = "";
			String name = "";
			Map<String, String> attributes = new HashMap<>();
			
			for (Iterator<Map.Entry<String, JsonNode>> iter = curResultJson.fields(); iter.hasNext(); ) {
				Map.Entry<String, JsonNode> curField = iter.next();
				
				if(curField.getValue().isObject() || curField.getValue().isArray()){
					//TODO:: handle?
					continue;
				}
				
				String curFieldName = curField.getKey();
				String curFieldVal = curField.getValue().asText();
				
				if (curField.getValue().isNull() || curFieldVal == null || curFieldVal.isBlank()) {
					continue;
				}
				
				switch (curFieldName) {
					case "brand":
						brandName = curFieldVal;
						break;
					case "title":
						name = curFieldVal;
						break;
					default:
						attributes.put(curFieldName, curFieldVal);
				}
			}
			
			resultList.add(
				ExtItemLookupResult
					.builder()
					.source(this.getProviderInfo().getDisplayName())
					.name(name)
					.brand(brandName)
					.unifiedName(name)
					.attributes(attributes)
					.build()
			);
		}
		
		return resultList;
	}
	
	@Override
	protected CompletionStage<JsonNode> performBarcodeSearchCall(String barcode) {
		return this.barcodeLookupClient.getFromUpcCode(this.apiKey, barcode);
	}
}
