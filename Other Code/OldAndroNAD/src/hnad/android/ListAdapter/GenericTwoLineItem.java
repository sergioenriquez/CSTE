package hnad.android.ListAdapter;

/**
 * Convenience class for creating generic items to add to two-line array adapters.
 * 
 * @author Cory Sohrakoff
 *
 */
public class GenericTwoLineItem implements TwoLineArrayAdapterItem {

	private String line1;
	private String line2;
	
	public GenericTwoLineItem(String line1, String line2) {
		this.line1 = line1;
		this.line2 = line2;
	}

	@Override
	public String line1() {
		return line1;
	}

	@Override
	public String line2() {
		return line2;
	}
}
