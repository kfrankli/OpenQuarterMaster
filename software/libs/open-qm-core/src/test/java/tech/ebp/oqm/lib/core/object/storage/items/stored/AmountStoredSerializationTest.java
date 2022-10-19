package tech.ebp.oqm.lib.core.object.storage.items.stored;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.quantity.Quantities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AmountStoredSerializationTest extends ObjectSerializationTest<AmountStored> {
	
	protected AmountStoredSerializationTest() {
		super(AmountStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new AmountStored(UnitUtils.UNIT)),
			Arguments.of(
				new AmountStored(5, UnitUtils.UNIT)
					.setCondition(50)
					.setConditionNotes(FAKER.lorem().paragraph())
					.setAttributes(Map.of("hello", "world"))
					.setExpires(LocalDateTime.now())
					.setKeywords(List.of("hello", "world"))
					.setImageIds(List.of(ObjectId.get()))
			
			)
		);
	}
}