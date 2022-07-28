package com.ebp.openQuarterMaster.baseStation.rest.search;

import com.ebp.openQuarterMaster.lib.core.history.ObjectHistory;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class HistorySearch extends SearchObject<ObjectHistory> {
	//TODO:: object specific fields, add to bson filter list
}