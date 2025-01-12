package tech.ebp.oqm.lib.core.object.history.events;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemAddEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemSubEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemTransferEvent;
import tech.ebp.oqm.lib.core.object.history.events.user.UserDisabledEvent;
import tech.ebp.oqm.lib.core.object.history.events.user.UserEnabledEvent;
import tech.ebp.oqm.lib.core.object.history.events.user.UserLoginEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

/**
 * Describes an event in an object's history.
 * <p>
 * TODO:: validator to ensure type
 */
@Data
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CreateEvent.class, name = "CREATE"),
	@JsonSubTypes.Type(value = UpdateEvent.class, name = "UPDATE"),
	@JsonSubTypes.Type(value = DeleteEvent.class, name = "DELETE"),
	@JsonSubTypes.Type(value = UserLoginEvent.class, name = "USER_LOGIN"),
	@JsonSubTypes.Type(value = UserEnabledEvent.class, name = "USER_ENABLED"),
	@JsonSubTypes.Type(value = UserDisabledEvent.class, name = "USER_DISABLED"),
	@JsonSubTypes.Type(value = ItemExpiryWarningEvent.class, name = "ITEM_EXPIRY_WARNING"),
	@JsonSubTypes.Type(value = ItemExpiredEvent.class, name = "ITEM_EXPIRED"),
	@JsonSubTypes.Type(value = ItemAddEvent.class, name = "ITEM_ADD"),
	@JsonSubTypes.Type(value = ItemSubEvent.class, name = "ITEM_SUBTRACT"),
	@JsonSubTypes.Type(value = ItemTransferEvent.class, name = "ITEM_TRANSFER")
})
@BsonDiscriminator
public abstract class HistoryEvent {
	
	/**
	 * The user that performed the event Not required to be anything, as in some niche cases there wouldn't be one (adding user)
	 */
	private ObjectId userId;
	
	/**
	 * When the event occurred
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime timestamp = ZonedDateTime.now();
	
	public abstract EventType getType();
	
}
