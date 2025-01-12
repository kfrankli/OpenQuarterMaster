package tech.ebp.oqm.baseStation.service.notification.item;

import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;

@Traced
public abstract class ItemEventNotificationService<T extends HistoryEvent> extends HistoryEventNotificationService<T> {
	
	public abstract  <S extends Stored, C, W extends StoredWrapper<C, S>, I extends InventoryItem<S, C, W>> void sendEvent(
		I item,
		T event
	);
}
