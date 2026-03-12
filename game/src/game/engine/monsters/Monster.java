package game.engine.monsters;

import game.engine.*;

public abstract class Monster implements Comparable<Monster> {
	private String name;
	private String description;
	private Role role;
	private Role originalRole;
	private int energy;
	private int position;
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;

	public Monster(String name, String description, Role originalRole,
			int energy) {

		this.name = name;
		this.description = description;
		this.originalRole = originalRole;
		this.role = originalRole;
		this.energy = energy;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Role getOriginalRole() {
		return originalRole;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		this.energy = Math.max(0, energy);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		if (position < 0) {

			this.position = 0;
		} else {

			this.position = position % Constants.BOARD_SIZE;
		}
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public boolean isShielded() {
		return shielded;
	}

	public void setShielded(boolean shielded) {
		this.shielded = shielded;
	}

	public int getConfusionTurns() {
		return confusionTurns;
	}

	public void setConfusionTurns(int confusionTurns) {
		this.confusionTurns = confusionTurns;
	}

	public int compareTo(Monster o) {
		if (this.position > o.position)
			return 1;
		if (this.position < o.position)
			return -1;
		return 0;

	}

}
