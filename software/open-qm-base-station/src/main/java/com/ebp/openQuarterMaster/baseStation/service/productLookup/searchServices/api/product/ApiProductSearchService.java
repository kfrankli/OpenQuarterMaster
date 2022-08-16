package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.product;


import com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api.ItemSearchService;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.opentracing.Traced;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Traced
public abstract class ApiProductSearchService extends ItemSearchService {
	
	protected abstract CompletionStage<JsonNode> performBarcodeSearchCall(String barcode);
	
	public  Optional<CompletableFuture<List<ProductLookupResult>>> searchBarcode(String barcode){
		if(!this.isEnabled()){
			return Optional.empty();
		}
		CompletionStage<JsonNode> stage = this.performBarcodeSearchCall(barcode);
		
		return Optional.of(stage.thenApply(this::jsonNodeToSearchResults).toCompletableFuture());
	}
}