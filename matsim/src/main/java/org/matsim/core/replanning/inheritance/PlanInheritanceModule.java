package org.matsim.core.replanning.inheritance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.groups.ControlerConfigGroup.CompressionType;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.replanning.GenericPlanStrategy;
import org.matsim.core.replanning.StrategyManager;
import org.matsim.core.utils.io.IOUtils;

import com.google.inject.Singleton;

@Singleton
public class PlanInheritanceModule extends AbstractModule implements StartupListener, BeforeMobsimListener, ShutdownListener  {
	

	public static final String PLAN_ID = "planId";
	public static final String ITERATION_CREATED = "iterationCreated";
	public static final String PLAN_MUTATOR = "planMutator";
	
	public static final String INITIAL_PLAN = "initialPlan";
	
	public static final String FILENAME_PLAN_INHERITANCE_RECORDS = "planInheritanceRecords";
	
	long numberOfPlanInheritanceRecordsCreated = 0;
	Map<String, PlanInheritanceRecord> planId2planInheritanceRecords = new ConcurrentHashMap<>();
	
	PlanInheritanceRecordWriter planInheritanceRecordWriter;
	private ArrayList<String> strategies;
	
	private final Character DELIMITER = '\t';
	private BufferedWriter selectedPlanStrategyShareWriter;
	private BufferedWriter planStrategyShareWriter;
	
	@Override
	public void notifyStartup(StartupEvent event) {
		CompressionType compressionType = event.getServices().getConfig().controler().getCompressionType();
		this.planInheritanceRecordWriter = new PlanInheritanceRecordWriter(event.getServices().getControlerIO().getOutputFilename(FILENAME_PLAN_INHERITANCE_RECORDS + ".csv", compressionType));
		this.strategies = this.getActiveStrategies(event.getServices().getConfig().strategy().getStrategySettings(), event.getServices().getStrategyManager());
		this.selectedPlanStrategyShareWriter = this.initializeDistributionWriter(this.strategies, event.getServices().getControlerIO().getOutputFilename(FILENAME_PLAN_INHERITANCE_RECORDS + "_shares_selected.csv"));
		this.planStrategyShareWriter = this.initializeDistributionWriter(this.strategies, event.getServices().getControlerIO().getOutputFilename(FILENAME_PLAN_INHERITANCE_RECORDS + "_shares.csv"));
	}

	/**
	 * Retrieve all active plan strategies with their correct name in alphabetical order
	 */
	private ArrayList<String> getActiveStrategies(Collection<StrategySettings> strategySettings, StrategyManager strategyManager) {
		Set<String> activeSubpopulations = new HashSet<>();
		for (StrategySettings strategySetting : strategySettings) {
			activeSubpopulations.add(strategySetting.getSubpopulation());
		}
		
		Set<String> planStrategiesNames = new HashSet<>();
		for (String subpopulation : activeSubpopulations) {
			for (GenericPlanStrategy<Plan, Person> planStrategy : strategyManager.getStrategies(subpopulation)) {
				planStrategiesNames.add(planStrategy.toString());
			}
		}
		
		ArrayList<String> strategies = new ArrayList<>(planStrategiesNames.size() + 1);
		strategies.addAll(planStrategiesNames);
		Collections.sort(strategies);
		strategies.add(0, INITIAL_PLAN);
		
		return strategies;
	}

	/**
	 * Initialize the writer with the active strategies
	 */
	private BufferedWriter initializeDistributionWriter(ArrayList<String> strategies, String filename) {
		
		BufferedWriter planStrategyShareWriter = IOUtils.getBufferedWriter(filename);
		
		StringBuffer header = new StringBuffer();
		header.append("iteration"); header.append(DELIMITER);
		for (int i = 0; i < strategies.size(); i++) {
			if (i > 0) {
				header.append(DELIMITER);
			} 
			header.append(strategies.get(i));
		}
		
		try {
			planStrategyShareWriter.write(header.toString());
			planStrategyShareWriter.newLine();
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize the plan strategy share writer!", e);
		}
		
		return planStrategyShareWriter;
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		
		Set<String> activePlanIds = new HashSet<>();
		Set<String> selectedPlanIds = new HashSet<>();
		
		for (Person person : event.getServices().getScenario().getPopulation().getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				
				if (plan.getPlanMutator() == null) {
					// initial plan - set initial plan defaults
					plan.setPlanMutator(INITIAL_PLAN);
					plan.setIterationCreated(event.getIteration());
				}
				
				if (plan.getIterationCreated() == event.getIteration()) {
					// it's a new plan created in this iteration - create a new record
					
					PlanInheritanceRecord planInheritanceRecord = new PlanInheritanceRecord();
					planInheritanceRecord.agentId = person.getId().toString();
					planInheritanceRecord.planId = Long.toString(++this.numberOfPlanInheritanceRecordsCreated, 36);
					planInheritanceRecord.ancestorId = plan.getPlanId();
					plan.setPlanId(planInheritanceRecord.planId);
					planInheritanceRecord.iterationCreated = plan.getIterationCreated();
					planInheritanceRecord.mutatedBy = plan.getPlanMutator();
					
					this.planId2planInheritanceRecords.put(planInheritanceRecord.planId, planInheritanceRecord);
				}
				
				if (PersonUtils.isSelected(plan)) {
					this.planId2planInheritanceRecords.get(plan.getPlanId()).iterationsSelected.add(event.getIteration());
					selectedPlanIds.add(plan.getPlanId());
				}
				
				activePlanIds.add(plan.getPlanId());
			}
		}
		
		List<String> deletedPlans = new ArrayList<>();
		for (String planId : this.planId2planInheritanceRecords.keySet()) {
			if (!activePlanIds.contains(planId)) {
				deletedPlans.add(planId);
			}
		}
		
		for (String deletedPlanId : deletedPlans) {
			PlanInheritanceRecord deletedPlanInheritanceRecord = this.planId2planInheritanceRecords.remove(deletedPlanId);
			deletedPlanInheritanceRecord.iterationRemoved = event.getIteration();
			this.planInheritanceRecordWriter.write(deletedPlanInheritanceRecord);
		}
		
		this.planInheritanceRecordWriter.flush();
		
		this.calculateAndWriteDistribution(event.getIteration(), this.strategies, this.planId2planInheritanceRecords, selectedPlanIds, this.selectedPlanStrategyShareWriter);
		this.calculateAndWriteDistribution(event.getIteration(), this.strategies, this.planId2planInheritanceRecords, this.planId2planInheritanceRecords.keySet(), this.planStrategyShareWriter);
	}

	private void calculateAndWriteDistribution(int currentIteration, ArrayList<String> strategies, Map<String, PlanInheritanceRecord> planId2planInheritanceRecords, Set<String> planIds, BufferedWriter writer) {
		Map<String, AtomicLong> strategy2count = new HashMap<>();
		for (String strategyName : strategies) {
			strategy2count.put(strategyName, new AtomicLong(0));
		}
		for (String planId : planIds) {
			String mutatedBy = planId2planInheritanceRecords.get(planId).mutatedBy;
			strategy2count.get(mutatedBy).incrementAndGet();
		}
		long sum = strategy2count.values().stream().mapToLong(count -> count.get()).sum();
		StringBuffer line = new StringBuffer();
		line.append(currentIteration);
		line.append(DELIMITER);
		for (int i = 0; i < strategies.size(); i++) {
			if (i > 0) {
				line.append(DELIMITER);
			} 
			line.append(String.valueOf(strategy2count.get(strategies.get(i)).doubleValue() / sum));
		}
		try {
			writer.write(line.toString());
			writer.newLine();
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize the plan strategy share writer!", e);
		}
	}
	
	@Override
	public void notifyShutdown(ShutdownEvent event) {
		for (PlanInheritanceRecord planInheritanceRecord : this.planId2planInheritanceRecords.values()) {
			this.planInheritanceRecordWriter.write(planInheritanceRecord);
		}
		
		this.planInheritanceRecordWriter.flush();
		this.planInheritanceRecordWriter.close();
		
		try {
			this.selectedPlanStrategyShareWriter.flush();
			this.selectedPlanStrategyShareWriter.close();
			this.planStrategyShareWriter.flush();
			this.planStrategyShareWriter.close();
		} catch (IOException e) {
			new RuntimeException(e);
		}
		
		this.planId2planInheritanceRecords.clear();
	}

	@Override
	public void install() {
		addControlerListenerBinding().to(PlanInheritanceModule.class);
	}
}
