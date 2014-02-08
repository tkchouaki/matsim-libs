package org.matsim.contrib.accessibility.interfaces;

import java.util.Map;

import org.matsim.contrib.accessibility.AccessibilityControlerListenerImpl.Modes4Accessibility;
import org.matsim.core.api.experimental.facilities.ActivityFacility;

public interface ZoneDataExchangeInterface {
	
	public void getZoneAccessibilities(ActivityFacility measurePoint, Map<Modes4Accessibility,Double> accessibilities ) ;
	
	public boolean endReached();
	
}
