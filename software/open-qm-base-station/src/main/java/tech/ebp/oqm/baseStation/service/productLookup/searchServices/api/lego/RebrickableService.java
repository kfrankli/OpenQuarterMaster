package tech.ebp.oqm.baseStation.service.productLookup.searchServices.api.lego;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import tech.ebp.oqm.baseStation.rest.restCalls.productLookup.api.RebrickableLookupClient;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class RebrickableService extends LegoLookupService {
	
	@Getter
	ExtItemLookupProviderInfo providerInfo;
	RebrickableLookupClient rebrickableLookupClient;
	private String apiKey;
	
	@Inject
	public RebrickableService(
		@RestClient
		RebrickableLookupClient rebrickableLookupClient,
		@ConfigProperty(name = "productLookup.providers.rebrickable.displayName")
		String displayName,
		@ConfigProperty(name = "productLookup.providers.rebrickable.enabled", defaultValue = "false")
		boolean enabled,
		@ConfigProperty(name = "productLookup.providers.rebrickable.description", defaultValue = "")
		String description,
		@ConfigProperty(name = "productLookup.providers.rebrickable.acceptsContributions", defaultValue = "")
		boolean acceptsContributions,
		@ConfigProperty(name = "productLookup.providers.rebrickable.homepage", defaultValue = "")
		URL homepage,
		@ConfigProperty(name = "productLookup.providers.rebrickable.cost", defaultValue = "")
		String cost,
		@ConfigProperty(name = "productLookup.providers.rebrickable.apiKey", defaultValue = "")
		String apiKey
	) {
		this.rebrickableLookupClient = rebrickableLookupClient;
		this.apiKey = apiKey;
		
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
		return this.providerInfo.isEnabled() && this.apiKey != null && !this.apiKey.isBlank();
	}
	
	@Override
	public List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results) {
		ExtItemLookupResult.Builder<?, ?> resultBuilder = ExtItemLookupResult.builder()
																			 .source(this.getProviderInfo().getDisplayName())
															  .brand("LEGO");
		
		Map<String, String> attributes = new HashMap<>();
		
		for (Iterator<Map.Entry<String, JsonNode>> iter = results.fields(); iter.hasNext(); ) {
			Map.Entry<String, JsonNode> curField = iter.next();
			String curFieldName = curField.getKey();
			String curFieldVal = curField.getValue().asText();
			
			//TODO:: handle images
			
			if (curField.getValue().isNull() || curFieldVal == null || curFieldVal.isBlank()) {
				continue;
			}
			
			switch (curFieldName) {
				case "name":
					resultBuilder.name(curFieldVal);
					resultBuilder.unifiedName(curFieldVal);
					break;
				default:
					attributes.put(curFieldName, curFieldVal);
			}
		}
		
		resultBuilder.attributes(attributes);
		
		return List.of(resultBuilder.build());
	}
	
	@Override
	protected CompletionStage<JsonNode> performPartNumberSearchCall(String partNum) {
		return this.rebrickableLookupClient.getFromPartNum(this.apiKey, partNum);
	}
}
