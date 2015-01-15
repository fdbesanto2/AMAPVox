package fr.ird.jeeb.workspace.archimedes.raytracing;

import fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel.InterceptionModel;
import fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel.ScatteringModel;

/**
 * Radiative Model Class
 * @author Cresson, Nov. 2012
 *
 */
public class RadiativeModel {
	public ScatteringModel 		scatteringModel;
	public InterceptionModel	interceptionModel;
	public RadiativeModel() {}
	
	/**
	 * Constructor with a scatteringModel and a interceptionModel.
	 * Various combination and use of the scattering/interception Models:
	 * 	-	1 ScatteringModel (to model a simple scattering process)
	 * 	-	1 InterceptionModel (sufacic interception model)
	 * 	-	1 InterceptionModel and 1 ScatteringModel (for volumic interception model, a scattering model is needed)
	 * @param scatteringModel	scattering model
	 * @param interceptionModel	interception model
	 */
	public RadiativeModel(ScatteringModel scatteringModel, InterceptionModel interceptionModel) {
		this.scatteringModel	= scatteringModel;
		this.interceptionModel	= interceptionModel;
	}
}