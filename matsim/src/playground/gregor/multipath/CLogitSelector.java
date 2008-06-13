/* *********************************************************************** *
 * project: org.matsim.*
 * PSLSelector.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.gregor.multipath;

import java.util.ArrayList;

public class CLogitSelector extends LogitSelector {

	private final  static double BETA = 4;
	private final static double THETA = 2;
	

	protected void calcProbabilities(ArrayList<NodeData> toNodes) {
		ArrayList<Double> g_all = new ArrayList<Double>();
		double all = 0;
		for (NodeData toNode : toNodes) {
			NodeData curr = toNode;
			double CF_k = 0;
			do {
				String key = curr.getPrev().getId().toString() + " " + curr.getId().toString();
				LogitLink l = this.pathTree.get(key);
				CF_k += l.numPaths * l.cost / toNode.getCost();
				if (Double.isNaN(CF_k)) {
					int i=0; i++;
				}
				curr = curr.getPrev();
			} while (curr.getPrev() != null);
			CF_k = -BETA * Math.log(CF_k);
			double w = Math.exp(-toNode.getCost()/THETA + CF_k); 
		
			g_all.add(w);
			all += w;
			
		}
		
		for (int i = 0; i < g_all.size(); i++) {
			toNodes.get(i).setProb(g_all.get(i)/all);
		}
		
	}



}

