package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints;

import com.ebp.openQuarterMaster.baseStation.service.productLookup.ProductLookupService;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupProviderInfo;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResult;
import com.ebp.openQuarterMaster.lib.core.rest.productLookup.ProductLookupResults;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

@Traced
@Slf4j
@Path("/api/productLookup")
@Tags({@Tag(name = "Product Lookup", description = "Endpoints for searching for products.")})
@RequestScoped
public class ProductLookup extends EndpointProvider {
	
	@Inject
	JsonWebToken jwt;
	
	@Inject
	ProductLookupService productLookupService;
	
	@GET
	@Path("providers")
	@Operation(
		summary = "Gets information on supported product search providers."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = ProductLookupProviderInfo.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response providerInfo(
		@Context SecurityContext securityContext
	) {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.getProviderInfo()).build();
	}
	
	@GET
	@Path("providers/enabled")
	@Operation(
		summary = "Gets information on supported and enabled product search providers."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON,
			schema = @Schema(
				type = SchemaType.ARRAY,
				implementation = ProductLookupProviderInfo.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response enabledProviderInfo(
		@Context SecurityContext securityContext
	) {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(
			this.productLookupService.getProviderInfo().stream().filter(ProductLookupProviderInfo::isEnabled)
		).build();
	}
	
	@GET
	@Path("barcode/{barcode}")
	@Operation(
		summary = "Searches enabled providers for the barcode given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ProductLookupResults.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchBarcode(
		@Context SecurityContext securityContext,
		@PathParam("barcode") String barcode
	) {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.searchBarcode(barcode)).build();
	}
	
	@GET
	@Path("webpage/{webpage}")
	@Operation(
		summary = "Scans the given webpage for product details."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ProductLookupResult.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response scanWebpage(
		@Context SecurityContext securityContext,
		@PathParam("webpage") String page
	) throws MalformedURLException, ExecutionException, InterruptedException {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.scanPage(new URL(page))).build();
	}
	
	
	@GET
	@Path("lego/{partNo}")
	@Operation(
		summary = "Searches enabled providers for the barcode given."
	)
	@APIResponse(
		responseCode = "200",
		description = "Image retrieved.",
		content = @Content(
			mediaType = "application/json",
			schema = @Schema(
				implementation = ProductLookupResults.class
			)
		)
	)
	@PermitAll
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchLegoPart(
		@Context SecurityContext securityContext,
		@PathParam("partNo") String partNo
	) {
		logRequestContext(this.jwt, securityContext);
		
		return Response.ok(this.productLookupService.searchLegoPart(partNo)).build();
	}
}
