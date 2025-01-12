package tech.ebp.oqm.lib.core.object.history.events;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;

/**
 * Describes an event with a description of the event.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public abstract class DescriptiveEvent extends HistoryEvent {
	
	/** Description of the event */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String description = "";
	
	protected DescriptiveEvent(String description) {
		this.description = description;
	}
}
