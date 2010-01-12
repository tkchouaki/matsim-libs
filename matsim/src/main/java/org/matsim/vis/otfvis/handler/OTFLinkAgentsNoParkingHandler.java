/* *********************************************************************** *
 * project: org.matsim.*
 * OTFLinkAgentsNoParkingHandler.java
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

package org.matsim.vis.otfvis.handler;

import java.nio.ByteBuffer;

import org.matsim.ptproject.qsim.QueueLink;
import org.matsim.ptproject.qsim.SimulationTimer;
import org.matsim.vis.otfvis.data.OTFDataWriter;
import org.matsim.vis.snapshots.writers.PositionInfo;
import org.matsim.vis.snapshots.writers.PositionInfo.VehicleState;


/**
 * OTFLinkAgentsNoParkingHandler enables the user (per preferences) to select whether she/he wants to
 * see parked vehicles or not.
 * 
 * @author david
 *
 */
public class OTFLinkAgentsNoParkingHandler extends OTFLinkAgentsHandler {
	
	static public class Writer extends  OTFLinkAgentsHandler.Writer {
		private static final long serialVersionUID = 6541770536927233851L;

		@Override
		protected void writeAllAgents(ByteBuffer out) {
			// Write additional agent data
			positions.clear();
			src.getVisData().getVehiclePositions(SimulationTimer.getTime(), positions);
			int valid = 0;
			for (PositionInfo pos : positions) {
				if (pos.getVehicleState() != VehicleState.Parking) valid++;
			}
			out.putInt(valid);

			for (PositionInfo pos : positions) {
				if (pos.getVehicleState() != VehicleState.Parking) writeAgent(pos, out);
			}
		}

		@Override
		public OTFDataWriter<QueueLink> getWriter() {
			return new Writer();
		}

	}
	
}
