/* *********************************************************************** *
 * project: org.matsim.*												   *
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
package org.matsim.contrib.emissions.example;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author nagel
 *
 */
public class RunDetailedEmissionToolOnlineExampleIT {
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;

	/**
	 * Test method for {@link RunDetailedEmissionToolOnlineExample#main(java.lang.String[])}.
	 */

	/*
	 *
	 * Abort if values are not found in detailled table
	 * This is by now (feb'20) the default. Setting it here for the tests explicitly
	 *
	 * */

	@SuppressWarnings("static-method")
	@Test
	public final void testDetailed_vehTypeV1() {
		try {
			Config config = RunDetailedEmissionToolOnlineExample.prepareConfig( new String[]{"./scenarios/sampleScenario/testv2_Vehv1/config_detailed.xml"} ) ;
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			config.controler().setLastIteration( 1 );
			EmissionsConfigGroup emissionsConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );
			emissionsConfig.setHbefaVehicleDescriptionSource( EmissionsConfigGroup.HbefaVehicleDescriptionSource.fromVehicleTypeDescription );
			emissionsConfig.setDetailedFallbackBehaviour(EmissionsConfigGroup.DetailedFallbackBehaviour.abort);
			Scenario scenario = RunDetailedEmissionToolOnlineExample.prepareScenario( config ) ;
			RunDetailedEmissionToolOnlineExample.run( scenario ) ;
		} catch ( Exception ee ) {
			ee.printStackTrace();
			fail("something did not work" ) ;
		}
	}

	/**
	 * Test method for {@link RunDetailedEmissionToolOnlineExample#main(java.lang.String[])}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testDetailed_vehTypeV2() {
		try {
			Config config = RunDetailedEmissionToolOnlineExample.prepareConfig( new String[]{"./scenarios/sampleScenario/testv2_Vehv2/config_detailed.xml"} ) ;
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			config.controler().setLastIteration( 1 );
			EmissionsConfigGroup emissionsConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );
			emissionsConfig.setDetailedFallbackBehaviour(EmissionsConfigGroup.DetailedFallbackBehaviour.abort);
			Scenario scenario = RunDetailedEmissionToolOnlineExample.prepareScenario( config ) ;
			RunDetailedEmissionToolOnlineExample.run( scenario ) ;
		} catch ( Exception ee ) {
			ee.printStackTrace();
			fail("something did not work" ) ;
		}
	}


	/*
	 *
	 * Fallback to Average
	 * this was the previous behaviour.
	 *
	 * */

	@SuppressWarnings("static-method")
	@Test
	public final void testDetailed_vehTypeV1_FallbackToAverage() {
		try {
			Config config = RunDetailedEmissionToolOnlineExample.prepareConfig( new String[]{"./scenarios/sampleScenario/testv2_Vehv1/config_detailed.xml"} ) ;
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			config.controler().setLastIteration( 1 );
			EmissionsConfigGroup emissionsConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );
			emissionsConfig.setHbefaVehicleDescriptionSource( EmissionsConfigGroup.HbefaVehicleDescriptionSource.fromVehicleTypeDescription );
			emissionsConfig.setDetailedFallbackBehaviour(EmissionsConfigGroup.DetailedFallbackBehaviour.tryTechnologyAverageOrVehicleTypeAverage); //This is the previous behaviour -> Test only pass, if falling back to average table :(
			Scenario scenario = RunDetailedEmissionToolOnlineExample.prepareScenario( config ) ;
			RunDetailedEmissionToolOnlineExample.run( scenario ) ;
		} catch ( Exception ee ) {
			ee.printStackTrace();
			fail("something did not work" ) ;
		}
	}

	/**
	 * Test method for {@link RunDetailedEmissionToolOnlineExample#main(java.lang.String[])}.
	 */
	@SuppressWarnings("static-method")
	@Test
	public final void testDetailed_vehTypeV2_FallbackToAverage() {
		try {
			Config config = RunDetailedEmissionToolOnlineExample.prepareConfig( new String[]{"./scenarios/sampleScenario/testv2_Vehv2/config_detailed.xml"} ) ;
			config.controler().setOutputDirectory( utils.getOutputDirectory() );
			config.controler().setLastIteration( 1 );
			EmissionsConfigGroup emissionsConfig = ConfigUtils.addOrGetModule( config, EmissionsConfigGroup.class );
			emissionsConfig.setDetailedFallbackBehaviour(EmissionsConfigGroup.DetailedFallbackBehaviour.tryTechnologyAverageOrVehicleTypeAverage); //This is the previous behaviour -> Test only pass, if falling back to average table :(
			Scenario scenario = RunDetailedEmissionToolOnlineExample.prepareScenario( config ) ;
			RunDetailedEmissionToolOnlineExample.run( scenario ) ;
		} catch ( Exception ee ) {
			ee.printStackTrace();
			fail("something did not work" ) ;
		}
	}

}
