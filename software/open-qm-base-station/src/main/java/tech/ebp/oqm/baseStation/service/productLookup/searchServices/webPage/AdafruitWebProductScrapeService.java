package tech.ebp.oqm.baseStation.service.productLookup.searchServices.webPage;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import org.jsoup.nodes.Document;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResult;

import javax.enterprise.context.ApplicationScoped;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
@Traced
@NoArgsConstructor
public class AdafruitWebProductScrapeService extends WebPageProductScrapeService {
	private static final List<String> SUPPORTED_HOSTS = List.of("www.adafruit.com");
	
	@SneakyThrows
	@Override
	public ExtItemLookupProviderInfo getProviderInfo() {
		//TODO:: add to application.yaml?
		return ExtItemLookupProviderInfo.builder()
				   .displayName("Adafruit.com")
				   .acceptsContributions(false)
				   .cost("Free")
				   .enabled(this.isEnabled())
				   .homepage(new URL("https://www.adafruit.com/"))
										.build();
	}
	
	@Override
	protected ExtItemLookupResult scrapePageContent(Document webPageContent) {
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.getInitialBuilder(webPageContent);
		
		Map<String, String> atts = new HashMap<>();
		resultBuilder = resultBuilder.unifiedName(webPageContent.getElementsByClass("products_name").text());
		
		resultBuilder = resultBuilder.description(webPageContent.getElementById("tab-description-content").text());
		
		atts.put("price", webPageContent.getElementById("prod-price").text());
		
		resultBuilder.attributes(atts);
		return resultBuilder.build();
	}
	
	
	@Override
	public List<String> supportedHosts() {
		return SUPPORTED_HOSTS;
	}
}
