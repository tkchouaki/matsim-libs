package playground.david.otfivs.executables;

import org.matsim.core.gbl.Gbl;
import org.matsim.vis.otfvis.OTFClient;





public class OnTheFlyClientFileTveh {

	public static void main(String[] args) {
		String netFileName = "../studies/schweiz/2network/ch.xml"; 
//		String vehFileName = "../runs/run168/run168.it210.T.veh.gz"; 
		String vehFileName = "";//"../runs/run301/output/100.T.veh.gz"; 

		if (Gbl.getConfig() == null) Gbl.createConfig(null);

		String localDtdBase = "../matsimJ/dtd/";
		Gbl.getConfig().global().setLocalDtdBase(localDtdBase);
		
		OTFClient client = new OTFClient("tveh:"+vehFileName + "@" + netFileName);
		client.run();
	}

 
}
