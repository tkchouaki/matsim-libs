/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2020 by the members listed in the COPYING,        *
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
 * *********************************************************************** *
 */

package org.matsim.contrib.zone.skims;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.common.zones.ZoneSystem;
import org.matsim.contrib.common.zones.systems.grid.square.SquareGridZoneSystem;
import org.matsim.core.network.NetworkUtils;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author Michal Maciejewski (michalm)
 */
public class FreeSpeedTravelTimeMatrixTest {

	@RegisterExtension
	MatsimTestUtils utils = new MatsimTestUtils();

	private final Network network = NetworkUtils.createNetwork();
	private final Node nodeA = NetworkUtils.createAndAddNode(network, Id.createNodeId("A"), new Coord(0, 0));
	private final Node nodeB = NetworkUtils.createAndAddNode(network, Id.createNodeId("B"), new Coord(150, 150));
	private final Node nodeC = NetworkUtils.createAndAddNode(network, Id.createNodeId("C"), new Coord(-10, -10));

	public FreeSpeedTravelTimeMatrixTest() {
		NetworkUtils.createAndAddLink(network, Id.createLinkId("AB"), nodeA, nodeB, 150, 15, 20, 1);
		NetworkUtils.createAndAddLink(network, Id.createLinkId("BA"), nodeB, nodeA, 300, 15, 40, 1);
		NetworkUtils.createAndAddLink(network, Id.createLinkId("AC"), nodeA, nodeC, 165, 15, 20, 1);
		NetworkUtils.createAndAddLink(network, Id.createLinkId("CA"), nodeC, nodeA, 135, 15, 20, 1);
	}

	@Test
	void matrix() throws MalformedURLException {
		DvrpTravelTimeMatrixParams params = new DvrpTravelTimeMatrixParams();
		params.maxNeighborDistance = 0;
		ZoneSystem zoneSystem = new SquareGridZoneSystem(network, 100.);
		var matrix = FreeSpeedTravelTimeMatrix.createFreeSpeedMatrix(network, zoneSystem, params, 1, 1);

		// distances between central nodes: A and B
		assertThat(matrix.getTravelTime(nodeA, nodeA, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeA, nodeB, 0)).isEqualTo(10 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeA, 0)).isEqualTo(20 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeB, 0)).isEqualTo(0);

		// non-central node: C and A are in the same zone; A is the central node
		assertThat(matrix.getTravelTime(nodeA, nodeC, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeC, nodeA, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeB, nodeC, 0)).isEqualTo(20 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeC, nodeB, 0)).isEqualTo(10 + 1); // 1 s for moving over nodes
	
		// write and read cache
		URL cachePath = new File(utils.getOutputDirectory(), "cache.bin").toURI().toURL();
		matrix.write(cachePath, network);
		matrix = FreeSpeedTravelTimeMatrix.createFreeSpeedMatrixFromCache(network, zoneSystem, null, 1, 1, cachePath);

		// distances between central nodes: A and B
		assertThat(matrix.getTravelTime(nodeA, nodeA, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeA, nodeB, 0)).isEqualTo(10 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeA, 0)).isEqualTo(20 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeB, 0)).isEqualTo(0);

		// non-central node: C and A are in the same zone; A is the central node
		assertThat(matrix.getTravelTime(nodeA, nodeC, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeC, nodeA, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeB, nodeC, 0)).isEqualTo(20 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeC, nodeB, 0)).isEqualTo(10 + 1); // 1 s for moving over nodes
	}

	@Test
	void sparseMatrix() throws MalformedURLException {
		DvrpTravelTimeMatrixParams params = new DvrpTravelTimeMatrixParams();
		params.maxNeighborDistance = 9999;

		ZoneSystem zoneSystem = new SquareGridZoneSystem(network, 100.);
		var matrix = FreeSpeedTravelTimeMatrix.createFreeSpeedMatrix(network, zoneSystem, params, 1, 1);

		// distances between central nodes: A and B
		assertThat(matrix.getTravelTime(nodeA, nodeA, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeA, nodeB, 0)).isEqualTo(10 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeA, 0)).isEqualTo(20 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeB, 0)).isEqualTo(0);

		// non-central node: C and A are in the same zone; A is the central node
		assertThat(matrix.getTravelTime(nodeA, nodeC, 0)).isEqualTo(11 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeC, nodeA, 0)).isEqualTo(9 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeC, 0)).isEqualTo(20 + 11 + 2); // 2 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeC, nodeB, 0)).isEqualTo(10 + 9 + 2); // 2 s for moving over nodes

		// write and read cache
		URL cachePath = new File(utils.getOutputDirectory(), "cache.bin").toURI().toURL();
		matrix.write(cachePath, network);
		matrix = FreeSpeedTravelTimeMatrix.createFreeSpeedMatrixFromCache(network, zoneSystem, null, 1, 1, cachePath);

		// distances between central nodes: A and B
		assertThat(matrix.getTravelTime(nodeA, nodeA, 0)).isEqualTo(0);
		assertThat(matrix.getTravelTime(nodeA, nodeB, 0)).isEqualTo(10 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeA, 0)).isEqualTo(20 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeB, 0)).isEqualTo(0);

		// non-central node: C and A are in the same zone; A is the central node
		assertThat(matrix.getTravelTime(nodeA, nodeC, 0)).isEqualTo(11 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeC, nodeA, 0)).isEqualTo(9 + 1); // 1 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeB, nodeC, 0)).isEqualTo(20 + 11 + 2); // 2 s for moving over nodes
		assertThat(matrix.getTravelTime(nodeC, nodeB, 0)).isEqualTo(10 + 9 + 2); // 2 s for moving over nodes
	}
}
