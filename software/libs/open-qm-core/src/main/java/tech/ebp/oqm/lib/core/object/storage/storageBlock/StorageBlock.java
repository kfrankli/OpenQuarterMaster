package tech.ebp.oqm.lib.core.object.storage.storageBlock;

import lombok.ToString;
import tech.ebp.oqm.lib.core.object.ImagedMainObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;

import javax.measure.Quantity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an area for storage.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class StorageBlock extends ImagedMainObject {
	
	/**
	 * The label for this storage block
	 */
	@NonNull
	@NotNull
	@NotBlank
	private String label;
	
	/**
	 * The nickname for this storage block
	 */
	@NonNull
	@NotNull
	private String nickname = "";
	
	/**
	 * Text that describes the storage block
	 */
	@NonNull
	@NotNull
	private String description = "";
	
	/**
	 * The location of this storage block. If a sub-block, just the location within that sub-block.
	 */
	@NonNull
	@NotNull
	private String location = "";
	
	
	/**
	 * The parent of this storage block, if any
	 */
	private ObjectId parent;
	
	/**
	 * The capacities of this storage block. Intended to describe different units of capacity for the block.
	 */
	@NonNull
	@NotNull
	private List<@NotNull Quantity<?>> capacityMeasures = new ArrayList<>();
	
	public boolean hasParent() {
		return this.getParent() != null;
	}
}
