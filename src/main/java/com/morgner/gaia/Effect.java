package com.morgner.gaia;

/**
 *
 * @author Christian Morgner,
 */
public abstract class Effect {
	
	protected Resource affectedResource = null;
	
	public Effect(Resource affectedResource) {
		this.affectedResource = affectedResource;
	}
	
	public abstract Effect effect();
}
