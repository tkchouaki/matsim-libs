/* *********************************************************************** *
 * project: org.matsim.*
 * FacilitiesLoadCalculator.java.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.anhorni.PLOC.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;

import playground.anhorni.PLOC.MultiplerunsControler;


public class ShoppingCalculator implements ShutdownListener {
	
	private double totalExpenditurePerFacilityPerHour[][];
	private ObjectAttributes personAttributes;
	
	public void notifyShutdown(ShutdownEvent event) {
		this.evaluate(event);
		this.printStatistics(event);
	}
	
	public ShoppingCalculator(ObjectAttributes personAttributes) {
		this.totalExpenditurePerFacilityPerHour = new double[MultiplerunsControler.shoppingFacilities.length][24];
		this.personAttributes = personAttributes;
	}
	
	private void evaluate(ShutdownEvent event) {
		for (Person p : event.getControler().getPopulation().getPersons().values()) {			
			int shopLocIndex = -1;								
			final List<? extends PlanElement> actslegs = p.getSelectedPlan().getPlanElements();
			for (int j = 0; j < actslegs.size(); j=j+2) {
				ActivityImpl act = (ActivityImpl)actslegs.get(j);
				if (act.getType().equals("s")) {
					shopLocIndex = ArrayUtils.indexOf(MultiplerunsControler.shoppingFacilities, Integer.parseInt(act.getFacilityId().toString()));
					double expenditure = (Double) this.personAttributes.getAttribute(p.getId().toString(), "expenditure");
					double arrivalTime = ((LegImpl)actslegs.get(j-1)).getArrivalTime();
					int startTime = (int) (((arrivalTime) / 3600.0) % 24);
					this.totalExpenditurePerFacilityPerHour[shopLocIndex][startTime] += expenditure;
				}	
			}
		}
	}

	private void printStatistics(ShutdownEvent event) {		
		String runId = event.getControler().getConfig().findParam("controler", "runId");
				
		try {
			String parts[] = runId.split("D");
			String run = parts[0].substring(1);
			String day = parts[1];
			
			String outputPath = "src/main/java/playground/anhorni/output/PLOC/3towns/run";
			final BufferedWriter out =
				IOUtils.getBufferedWriter(outputPath + run + "/day" + day + "/totalExpendituresPerRunDay.txt");
			out.write("Hour\t");
			for (int i = 0; i < MultiplerunsControler.shoppingFacilities.length; i++) {
				out.append("facility_" + MultiplerunsControler.shoppingFacilities[i] + "\t");
			}
			out.write("sum\n");
			double sumPerFacility[] = new double[MultiplerunsControler.shoppingFacilities.length];
			
			for (int h = 0; h < 24; h++) {
				out.write(h + "\t");
				double sumPerHour = 0.0;
				for (int i = 0; i < MultiplerunsControler.shoppingFacilities.length; i++) {
					out.write(String.valueOf(totalExpenditurePerFacilityPerHour[i][h]) + "\t");
					sumPerHour += totalExpenditurePerFacilityPerHour[i][h];
					sumPerFacility[i] += totalExpenditurePerFacilityPerHour[i][h];
				}
				out.write(sumPerHour + "\n");
			}
			out.write("sum\t");
			for (int i = 0; i < MultiplerunsControler.shoppingFacilities.length; i++) {
				out.write(sumPerFacility[i] + "\t");
			}
			out.flush();
			out.close();
		} catch (final IOException e) {
			Gbl.errorMsg(e);
		}
	}
}
