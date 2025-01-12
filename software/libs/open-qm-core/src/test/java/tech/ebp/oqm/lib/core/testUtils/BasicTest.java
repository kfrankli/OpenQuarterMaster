package tech.ebp.oqm.lib.core.testUtils;

import tech.ebp.oqm.lib.core.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;

public abstract class BasicTest {
	
	public static final Faker FAKER = Faker.instance();
	public static final ObjectMapper OBJECT_MAPPER = Utils.OBJECT_MAPPER;
}
