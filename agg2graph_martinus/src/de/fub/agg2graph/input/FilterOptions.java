package de.fub.agg2graph.input;

/**
 * 
 * @author Martinus
 *
 */
public class FilterOptions {

	private int kRequirement = 1;
	private boolean newSegmentAllowed = false;
	
	public int getkRequirement() {
		return kRequirement;
	}

	public void setkRequirement(int kRequirement) {
		this.kRequirement = kRequirement;
		if(kRequirement < 1)
			newSegmentAllowed = true;
		else
			newSegmentAllowed = false;
	}
	
	public boolean getNewSegmentAllowed() {
		return newSegmentAllowed;
	}
}
