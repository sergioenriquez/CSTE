package hnad.android.ListAdapter;

/**
 * This class contains the two methods needed by a custom adapter to display two lines of text.
 * See {@link TwoLineArrayAdapter}
 * @author Cory Sohrakoff
 *
 */
public interface TwoLineArrayAdapterItem {

	/**
	 * @return The text to display for line one.
	 */
	public String line1();
	
	/**
	 * @return The text to display for line two.
	 */
	public String line2();
}
