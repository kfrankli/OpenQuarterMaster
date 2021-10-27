package com.ebp.openQuarterMaster.lib.core.storage;

import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Describes an area for storage.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class StorageBlock extends MainObject {
    /**
     * The label for this storage block
     */
    @NonNull
    @NotNull
    private String label;
    /**
     * The location of this storage block. If a sub-block, just the location within that sub-block.
     */
    @NonNull
    @NotNull
    private String location;
    /**
     * The parent of this storage block, if any
     */
    private UUID parent;
    /**
     * The capacities of this storage block. Intended to describe different units of capacity for the block.
     */
    private List<Capacity> capacityMeasures;

}