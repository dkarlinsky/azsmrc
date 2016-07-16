/**
 * 
 */
package lbms.azsmrc.remote.client.swtgui.sound;

/**
 * @author Damokles
 *
 */
public class Sound {

	public static final Sound ERROR 				= new Sound(0,"Error");
	public static final Sound DOWNLOAD_ADDED		= new Sound(1,"Download Added");
	public static final Sound DOWNLOADING_FINISHED	= new Sound(2,"Download Finished");
	public static final Sound SEEDING_FINISHED		= new Sound(3,"Download Added");

	private int id;
	private String name;
	private Sound (int id, String name) {
		this.id = id;
		this.name = name;
	}

	public static Sound getSoundById (int id) {
		switch (id) {
		case 0:
			return ERROR;
		case 1:
			return DOWNLOAD_ADDED;
		case 2:
			return DOWNLOADING_FINISHED;
		case 3:
			return SEEDING_FINISHED;

		default:
			return null;
		}
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Sound) {
			Sound snd = (Sound) obj;
			return id == snd.id;
		}
		else return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}
}
