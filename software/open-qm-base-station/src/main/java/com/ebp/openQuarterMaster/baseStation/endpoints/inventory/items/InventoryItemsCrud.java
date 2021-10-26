package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import lombok.extern.java.Log;
import lombok.extern.jbosslog.JBossLog;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Traced
@Slf4j
@Path("/inventory/item")
public class InventoryItemsCrud {

    @Inject
    InventoryItemService service;

    private static final String markerBase = "InvItm_";
    private static final Marker createMarker = MarkerFactory.getMarker(markerBase + "C");
    private static final Marker readMarker = MarkerFactory.getMarker(markerBase + "R");
    private static final Marker updateMarker = MarkerFactory.getMarker(markerBase + "U");
    private static final Marker deleteMarker = MarkerFactory.getMarker(markerBase + "D");

    @POST
    @Operation(
            summary = "Adds a new inventory item."
    )
    @Tags({@Tag(name = "Inventory Items")})
    @APIResponse(
            responseCode = "201",
            description = "Got the user's info.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                            implementation = InventoryItem.class
                    )
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Bad request given. Data given could not pass validation. (no user at given id, etc.)",
            content = @Content(mediaType = "text/plain")
    )
    @Produces(MediaType.APPLICATION_JSON)
    public Response createInventoryItem(@Valid InventoryItem item) {
//        log.info(createMarker, "Creating new item.");
        log.info("Creating new item.");
        ObjectId output = service.add(item);
//        log.info(createMarker, "Item created: {}", output);
        log.info("Item created: " + output);
        return Response.status(Response.Status.CREATED).entity(output).build();
        //TODO:: request tracing
    }
}
