package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class TrackedItemTest extends BasicTest {
	
	public static TrackedItem getLargeTrackedItem() {
		TrackedItem item = new TrackedItem();
		item.setTrackedItemIdentifierName("id");
		
		InventoryItemTest.fillCommon(item);
		
		List<ObjectId> storageIds = InventoryItemTest.getStorageList();
		
		for (ObjectId id : storageIds) {
			item.getStoredWrapperForStorage(id, true);
		}
		
		for (int i = 0; i < InventoryItemTest.NUM_STORED; i++) {
			TrackedStored stored = new TrackedStored();
			InventoryItemTest.fillCommon(stored);
			stored.setIdentifyingDetails(FAKER.lorem().paragraph());
			stored.setValue(BigDecimal.valueOf(RandomUtils.nextDouble(5, 1_000_000)));
			
			String id = UUID.randomUUID().toString();
			stored.setIdentifier(id);
			
			item.getStorageMap().get(storageIds.get(RandomUtils.nextInt(0, storageIds.size()))).put(
				id,
				stored
			);
		}
		item.recalculateDerived();
		
		return item;
	}
	
	public static Stream<Arguments> getTotalArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new TrackedItem(),
				Quantities.getQuantity(0, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new TrackedItem().add(ObjectId.get(), new TrackedStored(FAKER.name().name()), true),
				Quantities.getQuantity(1, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()), true)
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()), true),
				Quantities.getQuantity(2, OqmProvidedUnits.UNIT)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()), true)
					.add(id, new TrackedStored(FAKER.name().name()), true)
					.add(id, new TrackedStored(FAKER.name().name()), true),
				Quantities.getQuantity(3, OqmProvidedUnits.UNIT)
			)
		);
	}
	
	public static Stream<Arguments> getValueArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			Arguments.of(
				new TrackedItem(),
				BigDecimal.valueOf(0.0)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()).setValue(BigDecimal.ONE), true),
				BigDecimal.valueOf(1.0)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()).setValue(BigDecimal.ONE), true)
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()).setValue(BigDecimal.ONE), true),
				BigDecimal.valueOf(2.0)
			),
			Arguments.of(
				new TrackedItem()
					.add(ObjectId.get(), new TrackedStored(FAKER.name().name()).setValue(BigDecimal.ONE), true)
					.add(id, new TrackedStored(FAKER.name().name()).setValue(BigDecimal.ONE), true)
					.add(id, new TrackedStored(FAKER.name().name()).setValue(BigDecimal.ONE), true),
				BigDecimal.valueOf(3.0)
			)
		);
	}
	
	public static Stream<Arguments> getExpiryArguments() {
		ObjectId id = ObjectId.get();
		return Stream.of(
			//TODO
			Arguments.of(
			)
		);
	}
	
	@ParameterizedTest
	@MethodSource("getTotalArguments")
	public void testTotalTest(TrackedItem item, Quantity<?> quantityExpected) {
		assertEquals(
			quantityExpected,
			item.getTotal()
		);
	}
	
	@ParameterizedTest
	@MethodSource("getValueArguments")
	public void testGetValue(TrackedItem item, BigDecimal valueExpected) {
		item.recalcValueOfStored();
		assertEquals(
			valueExpected,
			item.getValueOfStored()
		);
	}
	
	@Test
	public void testLargeItemTotalCalculation() {
		TrackedItem item = getLargeTrackedItem();
		
		StopWatch sw = StopWatch.createStarted();
		item.recalcTotal();
		sw.stop();
		
		log.info("Recalculating totals took {}", sw);
	}
}