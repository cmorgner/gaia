/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.morgner.gaia.entity;

import com.morgner.gaia.Resource;
import com.morgner.gaia.util.IntColor;

/**
 *
 * @author Christian Morgner
 */
public class WaterPump extends PowerConsumer {

	private static final IntColor color = new IntColor(190, 180, 180);
	
	public WaterPump(Resource resource) {
		super(resource, Resource.WATER, 1, 100, 20.0);
	}
	
	@Override
	public IntColor getColor(IntColor color, double stepNum, double stepWidth, double stepHeight, int i, int j) {
		return this.color;
	}
}
