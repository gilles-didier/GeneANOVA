public interface PreferencesAsker {

    /**
     * Returns image from <code>x</code>.
     */
	public void set(String name, Preferences pref);

	public void init(String name, Preferences pref);

}
