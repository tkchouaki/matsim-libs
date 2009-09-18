/* *********************************************************************** *
 * project: org.matsim.*
 * PersonDeleteNonKtiCompatibleRoutes.java
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

package playground.meisterk.kti.population.algorithms;

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.basic.v01.population.BasicPlanElement;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;

import playground.meisterk.kti.router.KtiPtRoute;

/**
 * Deletes all routes from a plans file which are not compatible with the routes used in the kti runs.
 * 
 * @author meisterk
 *
 */
public class PersonDeleteNonKtiCompatibleRoutes extends AbstractPersonAlgorithm {

	@Override
	public void run(PersonImpl person) {

		/*
		 * All "pt" routes which were not generated by PlansCalcRouteKti are deleted.
		 * All car routes are deleted because their distance is not calculated as actually simulated in the traffic simulation.
		 * 
		 * Routes have to be deleted because otherwise they might survive in the evolutionary process
		 * when they are shorter/cheaper than the correct routes. They are not "automagically" replaced by the
		 * strategic modules that only generate correct routes.
		 */
		for (PlanImpl plan : person.getPlans()) {
			for (BasicPlanElement pe : plan.getPlanElements()) {
				if (pe instanceof LegImpl) {
					LegImpl leg = (LegImpl) pe;
					if (leg.getMode().equals(TransportMode.pt)) {
						if (((KtiPtRoute) leg.getRoute()).getFromStop() == null) {
							leg.setRoute(null);
						}
					} else {
						leg.setRoute(null);
					}
				}
			}
		}
		
	}

}
