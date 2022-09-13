package tech.ebp.oqm.lib.core.object.storage.items.stored;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.units.indriya.quantity.Quantities;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class AmountStoredSerializationTest extends ObjectSerializationTest<AmountStored> {
	
	protected AmountStoredSerializationTest() {
		super(AmountStored.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new AmountStored()),
			Arguments.of(
				new AmountStored()
					.setAmount(Quantities.getQuantity(5, UnitUtils.UNIT))
					.setCondition(50)
					.setConditionNotes(FAKER.lorem().paragraph())
					.setAttributes(Map.of("hello", "world"))
					.setExpires(LocalDate.now())
					.setKeywords(List.of("hello", "world"))
					.setImageIds(List.of(ObjectId.get()))
				
			)
		);
	}
}