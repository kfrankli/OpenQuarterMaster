package tech.ebp.oqm.lib.core.testUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public abstract class ObjectSerializationTest<T> extends BasicTest {
	
	/** The max amount of time in seconds we want de/serialization to take (triggers warning if over this) */
	private static final int SERIALIZATION_TIME_THRESHOLD = 2;
	/** The threshold for logging out the json string representation of the data */
	private static final int SERIALIZED_SIZE_LOG_THRESHOLD = 1_000;
	
	
	/** The speed of the theoretical connection. Used to calculate {@link #SERIALIZED_SIZE_THRESHOLD}. In bytes per second. */
	private static final int TRANSFER_SPEED = 100_000_000;
	/** The max time in seconds we would want a transfer to take. Used to calculate {@link #SERIALIZED_SIZE_THRESHOLD}. */
	private static final int MAX_TRANSFER_TIME = 1;
	/**
	 * The max size of a json document we want to make (triggers warning if over this). Calculated based on how long it would take to
	 * transfer
	 */
	private static final int SERIALIZED_SIZE_THRESHOLD = TRANSFER_SPEED * MAX_TRANSFER_TIME;
	
	/**
	 * The class of the object we are de/serializing.
	 */
	private final Class<T> clazz;
	
	protected ObjectSerializationTest(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	@BeforeEach
	public void stateThresholds(){
		log.info("Serialization time threshold: {}s", SERIALIZATION_TIME_THRESHOLD);
		log.info("Serialized size threshold: {}", FileUtils.byteCountToDisplaySize(SERIALIZED_SIZE_THRESHOLD));
	}
	
	@ParameterizedTest
	@MethodSource("getObjects")
	public void testSerialization(T object) throws JsonProcessingException {
		StopWatch sw = StopWatch.createStarted();
		String json = OBJECT_MAPPER.writeValueAsString(object);
		sw.stop();
		log.info("Serialized object in {}", sw);
		
		boolean failedSerializeTime = false,
			failedSerializeSize = false,
			failedDeserializeTime = false;
		
		if (sw.getTime(TimeUnit.SECONDS) >= SERIALIZATION_TIME_THRESHOLD) {
			log.warn("Serialization took longer than threshold {} seconds to complete.", SERIALIZATION_TIME_THRESHOLD);
			failedSerializeTime = true;
		}
		
		log.info("Length of json string: {} bytes ({})", json.length(), FileUtils.byteCountToDisplaySize(json.length()));
		log.info("Would take {}s to send over {}bps connection.", (double) json.length() / (double) TRANSFER_SPEED, TRANSFER_SPEED);
		
		if (json.length() > SERIALIZED_SIZE_THRESHOLD) {
			log.warn("Length of JSON string very, very long.");
			failedSerializeSize = true;
		}
		
		if (json.length() < SERIALIZED_SIZE_LOG_THRESHOLD) {
			log.info("json: {}", json);
		}
		
		sw = StopWatch.createStarted();
		T objectBack = OBJECT_MAPPER.readValue(json, clazz);
		sw.stop();
		log.info("Deserialized object in {}", sw);
		
		if (sw.getTime(TimeUnit.SECONDS) >= SERIALIZATION_TIME_THRESHOLD) {
			log.warn("Deserialization took longer than the threshold {} seconds to complete.", SERIALIZATION_TIME_THRESHOLD);
			failedDeserializeTime = true;
		} else {
			log.info("Deserialization did not take too long.");
		}
		
		try {
			assertEquals(object, objectBack, "Deserialized object was not equal to original.");
		} catch(AssertionError e) {
			throw e;
		} catch(Throwable e) {
			throw new IllegalStateException("Failed to determine if original and deserialized were equal.", e);
		}
		
		log.info("Original and deserialized objects were equal.");
		
		assertFalse(
			failedSerializeSize || failedSerializeTime || failedDeserializeTime,
			"Failed one or more of size/time related checks; failedSerialSize=" +
			failedSerializeSize +
			", failedSerializeTime=" +
			failedSerializeTime +
			", failedDeserializeTime=" +
			failedDeserializeTime
		);
	}
	
}
