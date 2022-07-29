package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.storage.items.InventoryItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.ListAmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.SimpleAmountItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.TrackedItem;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.TrackedStored;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.stream.Stream;

import static com.ebp.openQuarterMaster.baseStation.testResources.TestRestUtils.setupJwtCall;
import static com.ebp.openQuarterMaster.lib.core.Utils.OBJECT_MAPPER;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(value = TestResourceLifecycleManager.class)
@TestHTTPEndpoint(InventoryItemsCrud.class)
class InventoryItemsCrudTest extends RunningServerTest {
	
	@Inject
	InventoryItemTestObjectCreator testObjectCreator;
	
	@Inject
	ObjectMapper objectMapper;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	JwtService jwtService;
	
	@Inject
	TestUserService testUserService;
	
	private ObjectId create(User user, InventoryItem item) throws JsonProcessingException {
		ValidatableResponse response = setupJwtCall(given(), this.jwtService.getUserJwt(user, false).getToken())
										   .contentType(ContentType.JSON)
										   .body(objectMapper.writeValueAsString(item))
										   .when()
										   .post()
										   .then();
		
		response.statusCode(Response.Status.OK.getStatusCode());
		
		log.info("Got response body: {}", response.extract().body().asString());
		
		ObjectId returned = response.extract().body().as(ObjectId.class);
		
		log.info("Got object id back from create request: {}", returned);
		return returned;
	}
	private InventoryItem update(User user, ObjectNode updateData, ObjectId id) throws JsonProcessingException {
		ValidatableResponse response = setupJwtCall(given(), this.jwtService.getUserJwt(user, false).getToken())
										   .contentType(ContentType.JSON)
										   .body(objectMapper.writeValueAsString(updateData))
										   .when()
										   .put(id.toHexString())
										   .then();
		
		response.statusCode(Response.Status.OK.getStatusCode());
		
		InventoryItem returned = response.extract().body().as(InventoryItem.class);
		
		log.info("Got object id back from create request: {}", returned);
		return returned;
	}
	
	public static Stream<Arguments> getSimpleAmountItems(){
		return Stream.of(
			Arguments.of(
				new SimpleAmountItem().setName(FAKER.commerce().productName())
			),
			Arguments.of(
				new SimpleAmountItem(){{
					this.getStorageMap().put(ObjectId.get(), new AmountStored().setAmount(Quantities.getQuantity(0, this.getUnit())));
				}}.setName(FAKER.commerce().productName())
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getSimpleAmountItems")
	public void testCreateSimpleAmountItem(SimpleAmountItem item) throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(returned);
		assertNotNull(stored);
		
		item.setId(returned);
		
		assertEquals(item, stored);
		
		//TODO:: check history
	}
	
	@Test
	public void testCreateListAmountItem() throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		ListAmountItem item = (ListAmountItem) new ListAmountItem().setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(returned);
		assertNotNull(stored);
		
		item.setId(returned);
		
		assertEquals(item, stored);
		
		//TODO:: check history
	}
	
	@Test
	public void testCreateTrackedItem() throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		TrackedItem item = (TrackedItem) new TrackedItem()
			.setTrackedItemIdentifierName("id")
			.setName(FAKER.commerce().productName());
		ObjectId returned = create(user, item);
		
		InventoryItem stored = inventoryItemService.get(returned);
		assertNotNull(stored);
		
		item.setId(returned);
		
		assertEquals(item, stored);
		
		//TODO:: check history
	}
	
	@Test
	public void testUpdateTrackedItem() throws JsonProcessingException {
		User user = this.testUserService.getTestUser(false, true);
		TrackedItem item = (TrackedItem) new TrackedItem()
											 .setTrackedItemIdentifierName("id")
											 .setName(FAKER.commerce().productName());
		item.add(ObjectId.get(), "1", new TrackedStored());
		ObjectId returned = create(user, item);
		
		ObjectNode updateData = OBJECT_MAPPER.createObjectNode();
		updateData.put("name", FAKER.commerce().productName());
		
	}
}