/* *********************************************************************** *
 * project: matsim
 * VehicleUtils.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.vehicles;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.gbl.Gbl;
import org.matsim.utils.objectattributes.attributable.AttributesUtils;


/**
 * @author nagel
 *
 */
public final class VehicleUtils {
	private static final Logger log = LogManager.getLogger( VehicleUtils.class ) ;

	private static final String DEFAULT_VEHICLE_TYPE_ID = "defaultVehicleType";
	private static final String VEHICLE_ATTRIBUTE_KEY = "vehicles";
	private static final String VEHICLE_TYPES_ATTRIBUTE_KEY = "vehicleTypes";

	// should remain under the hood --> should remain private
	private static final String DOOR_OPERATION_MODE = "doorOperationMode" ;
	private static final String EGRESSTIME = "egressTimeInSecondsPerPerson";
	private static final String ACCESSTIME = "accessTimeInSecondsPerPerson";
	private static final String FUELCONSUMPTION = "fuelConsumptionLitersPerMeter";
	private static final String ENERGYCONSUMPTION = "energyConsumptionKWhPerMeter";
	private static final String ENERGYCAPACITY = "energyCapacityInKWhOrLiters";
	private static final String HBEFA_VEHICLE_CATEGORY_= "HbefaVehicleCategory";
	private static final String HBEFA_TECHNOLOGY = "HbefaTechnology";
	private static final String HBEFA_SIZE_CLASS = "HbefaSizeClass";
	private static final String HBEFA_EMISSIONS_CONCEPT = "HbefaEmissionsConcept";
	private static final String COST_PER_SECOND_WAITING = "costsPerSecondWaiting";
	private static final String COST_PER_SECOND_INSERVICE = "costsPerSecondInService";
	private static final String FUEL_TYPE = "fuelType";
	private static final String INITIAL_LINK_ID = "initialLinkId";

	public static VehicleType createVehicleType( Id<VehicleType> typeId ){
		return new VehicleType( typeId );
	}

	public static VehicleType createVehicleType( Id<VehicleType> typeId, String networkMode){
		return new VehicleType( typeId, networkMode );
	}

	public static VehiclesFactory getFactory() {
		return new VehiclesFactoryImpl();
	}

	public static Vehicles createVehiclesContainer() {
		return new VehiclesImpl();
	}

	public static VehicleType createDefaultVehicleType() {
		VehicleType defaultVehicleType = VehicleUtils.getFactory()
				.createVehicleType(Id.create(DEFAULT_VEHICLE_TYPE_ID, VehicleType.class));

		defaultVehicleType.getCapacity().setSeats(4);
		defaultVehicleType.setNetworkMode(TransportMode.car);
		return defaultVehicleType;
	}

	/**
	 * Creates a vehicle id based on the person and the mode
	 * <p>
	 * If config.qsim().getVehicleSource() is "modeVehicleTypesFromVehiclesData", the returned id is a combination of
	 * the person's id and the supplied mode. E.g. "person1_car
	 *
	 * @param person The person which owns the vehicle
	 * @param mode   The mode this vehicle is for
	 * @return a VehicleId
	 */
	public static Id<Vehicle> createVehicleId(Person person, String mode) {
		return Id.createVehicleId(person.getId().toString() + "_" + mode);
	}

	public static void copyFromTo( VehicleType in, VehicleType out ) {
		out.setMaximumVelocity(in.getMaximumVelocity());
		out.setDescription(in.getDescription());
		out.setPcuEquivalents(in.getPcuEquivalents());
		out.setLength(in.getLength());
		out.setWidth(in.getLength());
		out.setFlowEfficiencyFactor(in.getFlowEfficiencyFactor());
		out.setNetworkMode(in.getNetworkMode());
		// (all the deprecated setters are copied via the attributes!)
		AttributesUtils.copyAttributesFromTo(in, out);

		CostInformation cost = in.getCostInformation();
		out.getCostInformation().setCostsPerSecond(cost.getCostsPerSecond()).setCostsPerMeter(cost.getCostsPerMeter()).setFixedCost(cost.getFixedCosts());
		AttributesUtils.copyAttributesFromTo(cost, out.getCostInformation());

		VehicleCapacity cap = in.getCapacity();
		out.getCapacity().setWeightInTons(cap.getWeightInTons()).setSeats(cap.getSeats()).setSeats(cap.getStandingRoom()).setVolumeInCubicMeters(cap.getVolumeInCubicMeters());
		AttributesUtils.copyAttributesFromTo(cap, out.getCapacity());

		AttributesUtils.copyAttributesFromTo(in.getEngineInformation(), out.getEngineInformation());

	}

	/**
	 * Checks whether a person has a vehicle id for mode - without throwing an
	 * exception if not.
	 *
	 * @param person the person one wants to check for a vehicle id
	 * @param mode   the mode for the vehicle id to check
	 * @return whether person has a vehicle id for that mode
	 *
	 * @see {@link VehicleUtils#getVehicleId(Person, String)}
	 */
	public static boolean hasVehicleId(Person person, String mode) {
		PersonVehicles personVehicles = (PersonVehicles) person.getAttributes().getAttribute(VehicleUtils.VEHICLE_ATTRIBUTE_KEY);
		return personVehicles != null && personVehicles.getVehicle(mode) != null;
	}

	/**
	 * Retrieves vehicleIds of all vehicles that are assigned to the person.
	 *
	 * @param person the person one wants to retrieve vehicles for
	 * @return vehicle ids mapped to the mode the respective vehicle may be used for
	 * @throws RuntimeException In case no vehicleIds were set
	 */
	public static Map<String, Id<Vehicle>> getVehicleIds(Person person) {
		var personVehicles = (PersonVehicles) person.getAttributes().getAttribute(VehicleUtils.VEHICLE_ATTRIBUTE_KEY);
		if (personVehicles == null) {
			throw new RuntimeException("Could not retrieve vehicle id from person: " + person.getId().toString() +
								   ". \nIf you are not using config.qsim().getVehicleSource() with 'defaultVehicle' or 'modeVehicleTypesFromVehiclesData' you have to provide " +
								   "a vehicle for each mode for each person. Attach a PersonVehicles instance (containing a map of mode:String -> id:Id<Vehicle>) with key 'vehicles' as person attribute to each person." +
								   "\n VehicleUtils.insertVehicleIdIntoAttributes does this for you.");
		}
		return personVehicles.getModeVehicles();
	}

	/**
	 * Retrieve the optional vehicle types per mode that might be assigned to a person.
	 * Returns null if this is not defined.
	 */
	public static Map<String, Id<VehicleType>> getVehicleTypes(Person person) {
		var personVehicles = (PersonVehicleTypes) person.getAttributes().getAttribute(VehicleUtils.VEHICLE_TYPES_ATTRIBUTE_KEY);
		return personVehicles != null ? personVehicles.getModeVehicleTypes() : null;
	}

	/**
	 * Retrieves a vehicleId from the person's attributes.
	 *
	 * @return the vehicleId of the person's vehicle for the specified mode
	 * @throws RuntimeException In case no vehicleIds were set or in case no vehicleId was set for the specified mode
	 */
	public static Id<Vehicle> getVehicleId(Person person, String mode) {
		Map<String, Id<Vehicle>> vehicleIds = getVehicleIds(person);
		if (!vehicleIds.containsKey(mode)) {
			throw new RuntimeException("Could not retrieve vehicle id from person: " + person.getId().toString() + " for mode: " + mode +
								   ". \nIf you are not using config.qsim().getVehicleSource() with 'defaultVehicle' or 'modeVehicleTypesFromVehiclesData' you have to provide " +
								   "a vehicle for each mode for each person. Attach a PersonVehicles instance (containing a map of mode:String -> id:Id<Vehicle>) with key 'vehicles' as person attribute to each person." +
								   "\n VehicleUtils.insertVehicleIdIntoAttributes does this for you."
			);
		}
		return vehicleIds.get(mode);
	}

	/**
	 * Attaches vehicle ids to a person, so that the router knows which vehicle to use for which mode and person.
	 *
	 * @param modeToVehicle mode string mapped to vehicle ids. The provided map is copied and stored as unmodifiable map.
	 *                      If a mode key already exists in the persons's attributes it is overridden. Otherwise, existing
	 *                      and provided values are merged into one map
	 *                      We use PersonVehicle Class in order to have a dedicated PersonVehicleAttributeConverter to/from XML
	 *
	 * @deprecated inline to more expressive method
	 */
	@Deprecated
	public static void insertVehicleIdsIntoAttributes(Person person, Map<String, Id<Vehicle>> modeToVehicle){
		insertVehicleIdsIntoPersonAttributes( person, modeToVehicle );
	}
	/**
	 * Attaches vehicle ids to a person, so that the router knows which vehicle to use for which mode and person.
	 *
	 * @param modeToVehicle mode string mapped to vehicle ids. The provided map is copied and stored as unmodifiable map.
	 *                      If a mode key already exists in the persons's attributes it is overridden. Otherwise, existing
	 *                      and provided values are merged into one map
	 *                      We use PersonVehicle Class in order to have a dedicated PersonVehicleAttributeConverter to/from XML
	 */
	public static void insertVehicleIdsIntoPersonAttributes(Person person, Map<String, Id<Vehicle>> modeToVehicle) {
		Object attr = person.getAttributes().getAttribute(VEHICLE_ATTRIBUTE_KEY);
		// copy in case it's a UnmodifiableMap
		Map<String, Id<Vehicle>> modeToVehicleCopy = new HashMap<>(modeToVehicle);
		PersonVehicles personVehicles;
		if (attr == null) {
			personVehicles = new PersonVehicles();
		} else {
			personVehicles = (PersonVehicles) attr;
		}
		personVehicles.addModeVehicleList(modeToVehicleCopy);
		person.getAttributes().putAttribute(VEHICLE_ATTRIBUTE_KEY, personVehicles);
	}

	/**
	 * Attaches vehicle types to a person, so that the router knows which vehicle to use for which mode and person.
	 * @param modeToVehicleType mode string mapped to vehicle type ids. The provided map is copied and stored as unmodifiable map.
	 * @deprecated please inline to more expressive method name
	 */
	@Deprecated
	public static void insertVehicleTypesIntoAttributes(Person person, Map<String, Id<VehicleType>> modeToVehicleType) {
		insertVehicleTypesIntoPersonAttributes( person, modeToVehicleType );
	}
	/**
	 * Attaches vehicle types to a person, so that the router knows which vehicle to use for which mode and person.
	 * @param modeToVehicleType mode string mapped to vehicle type ids. The provided map is copied and stored as unmodifiable map.
	 */
	public static void insertVehicleTypesIntoPersonAttributes(Person person, Map<String, Id<VehicleType>> modeToVehicleType) {
		Object attr = person.getAttributes().getAttribute(VEHICLE_TYPES_ATTRIBUTE_KEY);

		Map<String, Id<VehicleType>> modeToTypesCopy = new HashMap<>(modeToVehicleType);
		PersonVehicleTypes personVehiclesTypes;
		if (attr == null) {
			personVehiclesTypes = new PersonVehicleTypes();
		} else {
			personVehiclesTypes = (PersonVehicleTypes) attr;
		}
		personVehiclesTypes.putModeVehicleTypes(modeToTypesCopy);
		person.getAttributes().putAttribute(VEHICLE_TYPES_ATTRIBUTE_KEY, personVehiclesTypes);
	}

	//******** general VehicleType attributes ************

	public static VehicleType.DoorOperationMode getDoorOperationMode( VehicleType vehicleType ){
		final Object attribute = vehicleType.getAttributes().getAttribute( DOOR_OPERATION_MODE );
		if ( attribute==null ) {
			return VehicleType.DoorOperationMode.serial; // this was the default value in V1; could also return null instead.
		} else if (attribute instanceof VehicleType.DoorOperationMode ){
			return (VehicleType.DoorOperationMode) attribute;
		} else if (attribute instanceof String) {
			String modeString = (String) attribute;
			if ( VehicleType.DoorOperationMode.serial.toString().equalsIgnoreCase(modeString )) {
				return VehicleType.DoorOperationMode.serial;
			} else if ( VehicleType.DoorOperationMode.parallel.toString().equalsIgnoreCase(modeString )) {
				return VehicleType.DoorOperationMode.parallel;
			} else {
				throw new IllegalArgumentException("VehicleType " + vehicleType.getId().toString() + " : Door operation mode " + modeString + "is not supported");
			}
		}
		else {
			throw new RuntimeException("Type of " + attribute + "is not supported here");
		}
	}

	public static void setDoorOperationMode( VehicleType vehicleType, VehicleType.DoorOperationMode mode ){
		vehicleType.getAttributes().putAttribute( DOOR_OPERATION_MODE, mode ) ;
	}

	public static double getEgressTime(VehicleType vehicleType) {
		final Object attribute = vehicleType.getAttributes().getAttribute( EGRESSTIME );
		if ( attribute==null ) {
			return 1.0 ; // this was the default value in V1; could also return Double-null instead.
		} else {
			return (double) attribute;
		}
	}

	public static void setEgressTime(VehicleType vehicleType, double egressTime) {
		vehicleType.getAttributes().putAttribute(EGRESSTIME, egressTime);
	}

	public static double getAccessTime(VehicleType vehicleType) {
		final Object attribute = vehicleType.getAttributes().getAttribute( ACCESSTIME );
		if ( attribute==null ) {
			return 1.0 ; // this was the default value in V1; could also return Double-null instead.
		} else {
			return (double) attribute ;
		}
	}

	public static void setAccessTime(VehicleType vehicleType, double accessTime) {
		vehicleType.getAttributes().putAttribute(ACCESSTIME, accessTime);
	}

	/**
	 * @deprecated use getFuelConsumptionPerMeter instead
	 */
	@Deprecated
	public static Double getFuelConsumption(VehicleType vehicleType) {
		return getFuelConsumptionLitersPerMeter(vehicleType.getEngineInformation());
	}

	/**
	 * @deprecated use setFuelConsumptionPerMeter instead
	 */
	@Deprecated
	public static void setFuelConsumption(VehicleType vehicleType, double literPerMeter) {
		setFuelConsumptionLitersPerMeter(vehicleType.getEngineInformation(), literPerMeter);
	}

	//******** EngineInformation attributes ************
	//TODO create enum for fuel type
	public static String getHbefaTechnology( EngineInformation ei ){
		return (String) ei.getAttributes().getAttribute( HBEFA_TECHNOLOGY ) ;
	}
	public static void setHbefaTechnology( EngineInformation engineInformation, String hbefaTechnology ) {
		engineInformation.getAttributes().putAttribute( HBEFA_TECHNOLOGY, hbefaTechnology ) ;
	}

	public static String getHbefaVehicleCategory( EngineInformation ei ){
		return (String) ei.getAttributes().getAttribute( HBEFA_VEHICLE_CATEGORY_ ) ;
	}
	public static void setHbefaVehicleCategory( EngineInformation engineInformation, String hbefaVehicleCategory ) {
		engineInformation.getAttributes().putAttribute( HBEFA_VEHICLE_CATEGORY_, hbefaVehicleCategory ) ;
	}

	public static String getHbefaSizeClass( EngineInformation ei ) {
		return (String) ei.getAttributes().getAttribute(HBEFA_SIZE_CLASS);
	}
	public static void setHbefaSizeClass( EngineInformation engineInformation, String hbefaSizeClass ) {
		engineInformation.getAttributes().putAttribute( HBEFA_SIZE_CLASS, hbefaSizeClass ) ;
	}

	public static String getHbefaEmissionsConcept( EngineInformation ei ) {
		return (String) ei.getAttributes().getAttribute(HBEFA_EMISSIONS_CONCEPT);
	}
	public static void setHbefaEmissionsConcept( EngineInformation engineInformation, String emissionsConcept ) {
		engineInformation.getAttributes().putAttribute( HBEFA_EMISSIONS_CONCEPT, emissionsConcept ) ;
	}

	public static Double getEnergyConsumptionKWhPerMeter(EngineInformation engineInformation) {
		return (Double) engineInformation.getAttributes().getAttribute(ENERGYCONSUMPTION);
	}

	public static void setEnergyConsumptionKWhPerMeter(EngineInformation engineInformation, double energyConsumptionKWhPerMeter) {
		engineInformation.getAttributes().putAttribute(ENERGYCONSUMPTION, energyConsumptionKWhPerMeter);
	}

	public static Double getFuelConsumptionLitersPerMeter(EngineInformation engineInformation) {
		return (Double) engineInformation.getAttributes().getAttribute(FUELCONSUMPTION);
	}

	public static void setFuelConsumptionLitersPerMeter(EngineInformation engineInformation, double fuelConsumptionLitersPerMeter) {
		engineInformation.getAttributes().putAttribute(FUELCONSUMPTION, fuelConsumptionLitersPerMeter);
	}

	public static Double getEnergyCapacity(EngineInformation engineInformation) {
		return (Double) engineInformation.getAttributes().getAttribute(ENERGYCAPACITY);
	}

	public static void setEnergyCapacity(EngineInformation engineInformation, double energyCapacityInKWhOrLiters) {
		engineInformation.getAttributes().putAttribute(ENERGYCAPACITY, energyCapacityInKWhOrLiters);
	}
	//******** CostInformation attributes ************

	public static Double getCostsPerSecondWaiting(CostInformation costInformation) {
		return (Double) costInformation.getAttributes().getAttribute(COST_PER_SECOND_WAITING);
	}


	public static void setCostsPerSecondWaiting(CostInformation costInformation, double costsPerSecond) {
		costInformation.getAttributes().putAttribute(COST_PER_SECOND_WAITING, costsPerSecond);
	}


	public static Double getCostsPerSecondInService(CostInformation costInformation) {
		return (Double) costInformation.getAttributes().getAttribute(COST_PER_SECOND_INSERVICE);
	}


	public static void setCostsPerSecondInService(CostInformation costInformation, double costsPerSecond) {
		costInformation.getAttributes().putAttribute(COST_PER_SECOND_INSERVICE, costsPerSecond);
	}

	public static Vehicle createVehicle( Id<Vehicle> id , VehicleType type ){
		return new VehicleImpl( id , type );
	}

	/**
	 * @deprecated use getHbefaTechnology instead
	 */
	@Deprecated
	static EngineInformation.FuelType getFuelType(EngineInformation engineInformation ){
		return (EngineInformation.FuelType) engineInformation.getAttributes().getAttribute( FUEL_TYPE );
	}

	/**
	 * @deprecated use setHbefaTechnology instead
	 */
	@Deprecated
	static void setFuelType(EngineInformation engineInformation, EngineInformation.FuelType fuelType ){
		engineInformation.getAttributes().putAttribute( FUEL_TYPE,  fuelType);
	}

	/**
	 * @Deprecated use getFuelConsumptionPerMeter instead
	 */
	@Deprecated
	static Double getFuelConsumption(EngineInformation engineInformation ){
		return (Double) engineInformation.getAttributes().getAttribute( FUELCONSUMPTION );
	}

	/**
	 * @Deprecated use setFuelConsumptionPerMeter instead
	 */
	@Deprecated
	static void setFuelConsumption(EngineInformation engineInformation, double fuelConsumption ){
		engineInformation.getAttributes().putAttribute( FUELCONSUMPTION,  fuelConsumption);
	}

	public static Vehicles getOrCreateAllvehicles( Scenario  scenario ) {
		Vehicles map = (Vehicles) scenario.getScenarioElement( "allvehicles" );
		if ( map==null ) {
			log.info( "adding scenario element for allvehicles container" );
			map = new VehiclesImpl();
			scenario.addScenarioElement("allvehicles" , map);
		}
		return map;
	}
	private static int tryStdCnt = 5;
	private static int tryTrnCnt = 5;
	public static Vehicle findVehicle( Id<Vehicle> vehicleId, Scenario scenario) {
		Vehicle vehicle = getOrCreateAllvehicles( scenario ).getVehicles().get( vehicleId );
		if ( vehicle==null ) {
			if ( tryStdCnt>0){
				tryStdCnt--;
				log.info("vehicleId={} not in allVehicles; trying standard vehicles container ...", vehicleId);
				if ( tryStdCnt==0 ) {
					log.info( Gbl.FUTURE_SUPPRESSED );
				}
			}
			vehicle = scenario.getVehicles().getVehicles().get(  vehicleId );
		}
		if ( vehicle==null ) {
			if ( tryTrnCnt>0 ) {
				tryTrnCnt--;
				log.info("vehicleId={} not in allVehicles; trying transit vehicles container ...", vehicleId);
				if ( tryTrnCnt==0 ) {
					log.info(  Gbl.FUTURE_SUPPRESSED );
				}
			}
			vehicle = scenario.getTransitVehicles().getVehicles().get(  vehicleId );
		}
		if ( vehicle==null ) {
			log.info("unable to find vehicle for vehicleId={}; will return null", vehicleId);
		}
		return vehicle ;
	}
	public static void writeVehicles( Vehicles vehicles, String filename ) {
		new MatsimVehicleWriter( vehicles ).writeFile( filename );

	}

	public static Id<Link> getInitialLinkId(Vehicle vehicle) {
		String attribute = (String) vehicle.getAttributes().getAttribute(INITIAL_LINK_ID);
		return attribute == null ? null : Id.createLinkId(attribute);
	}

	public static void setInitialLinkId(Vehicle vehicle, Id<Link> initialLinkId) {
		vehicle.getAttributes().putAttribute(INITIAL_LINK_ID, initialLinkId.toString());
	}
}
