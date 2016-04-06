package bots;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.PlainDocument;

import pirates.game.Direction;
import pirates.game.Location;
import pirates.game.Treasure;
import pirates.game.Pirate;
import pirates.game.PirateBot;
import pirates.game.PirateGame;

public class MyBot implements PirateBot {
	LinkedList<MyPirate> myPirates = null;
	int attakersAndAssasinsNum = 0;
	static int collectors = 0;

	@Override
	public void doTurn(PirateGame game) {
		if (myPirates == null) {
			myPirates = getMyPirates(game);
			game.debug("name " + game.getOpponentName());
		}
		for (int i = 0; i < game.allMyPirates().size(); i++) {
			myPirates.get(i).setPirate(game.allMyPirates().get(i));
			myPirates.get(i).setFree(true);
		}
		collectors = 0;
		attakersAndAssasinsNum = 0;
		setJobs(myPirates, game, game.enemyPiratesWithTreasures());
		setFinalDestenation(myPirates, game);
		shootAndDefened(myPirates, game);
		setMoves(myPirates, game);
		setThisTurnDestination(myPirates, game);
		movePirates(myPirates, game);

	}

	public LinkedList<MyPirate> getMyPirates(PirateGame game) {
		List<Pirate> myPirates = game.allMyPirates();
		LinkedList<MyPirate> pl = new LinkedList<MyPirate>();
		for (int i = 0; i < myPirates.size(); i++)
			pl.add(new MyPirate(myPirates.get(i)));
		return pl;
	}

	public void setJobs(LinkedList<MyPirate> myPirates, PirateGame game, List<Pirate> enemy) {
		sortEnemyWithTreasure(enemy, game);
		for (int i = enemy.size() - 1; i >= 0; i--) {
			boolean targeted = false;
			int j = 0;
			int[] id = getClosestToEnemy(enemy.get(i), game);
			while (!targeted && j < id.length) {
				if (/* myPirates.get(id[j]).isFree()&& */ myPirates.get(id[j]).getPirate().getTurnsToSober() == 0) {
					if (myPirates.get(id[j]).getPirate().getReloadTurns() == 0
							&& enemy.get(i).getDefenseExpirationTurns() == 0
							&& enemy.get(i).getDefenseReloadTurns() != 0) {
						myPirates.get(id[j]).setAttaker(true, enemy.get(i));
						targeted = true;
					} else {
						myPirates.get(id[j]).setAssasin(true, enemy.get(i));
						targeted = true;
					}

				} else {
					myPirates.get(id[j]).setFree(true);
					
				}
				j++;
			}
		}
		for (int i = 0; i < game.myPiratesWithTreasures().size(); i++) {
			myPirates.get(game.myPiratesWithTreasures().get(i).getId()).setFree(true);
		}
	}

	public void setFinalDestenation(LinkedList<MyPirate> myPirates, PirateGame game) {
		for (int i = 0; i < myPirates.size(); i++) {
			if (myPirates.get(i).getPirate().getTurnsToSober() == 0) {
				if (myPirates.get(i).isAssasin()) {
					Location finalDestination = game.getSailOptions(myPirates.get(i).getTarget(),
							myPirates.get(i).getTarget().getInitialLocation(), 1).get(0);
					myPirates.get(i).setFinalDestination(finalDestination);
				} else if (myPirates.get(i).isAttaker()) {
					myPirates.get(i).setFinalDestination(myPirates.get(i).getTarget().getLocation());
				} else if (myPirates.get(i).getPirate().hasTreasure()) {
					myPirates.get(i).setFinalDestination(myPirates.get(i).getPirate().getInitialLocation());
				} else {
					Treasure t = getTreasure(myPirates.get(i).getPirate(), game);
					if (t != null) {
						myPirates.get(i).setFinalDestination(t.getLocation());
						collectors++;
						game.debug("collectors: "+collectors);
					} else {
						myPirates.get(i).setFinalDestination(null);
					}
				}
			} else {
				myPirates.get(i).setFinalDestination(null);
			}
		}
	}

	public Treasure getTreasure(Pirate p, PirateGame game) {

		if (game.treasures().size() > 0) {
			if (game.getOpponentName().equals("25")) {
				int maxValue = game.treasures().get(0).getValue();
				int index = 0;
				for (int i = 1; i < game.treasures().size(); i++) {
					if (game.treasures().get(i).getValue() > maxValue) {
						maxValue = game.treasures().get(i).getValue();
						index = i;
					}
				}

				if (maxValue > 1) {
					game.debug("value: " + maxValue);
					return game.treasures().get(index);
				}
			}
			int min = game.distance(p.getLocation(), game.treasures().get(0)
					.getLocation()) /*
									 * +game.distance(game.treasures().get(0).
									 * getLocation(),p.getInitialLocation()
									 */;
			int n = 0;
			for (int i = 1; i < game.treasures().size(); i++) {
				if (game.distance(p.getLocation(), game.treasures().get(i).getLocation())
				/*
				 * +game.distance(game.treasures ().get(i) .getLocation(),p.
				 * getInitialLocation() )
				 */
				< min) {
					min = game.distance(p.getLocation(), game.treasures().get(i).getLocation())
					/*
					 * +game.distance(game.treasures(). get(i).getLocation(),p.
					 * getInitialLocation())
					 */
					;
					n = i;
				}
			}
			Treasure treasure = game.treasures().get(n);
			return treasure;
		}
		return null;
	}

	public void shootAndDefened(LinkedList<MyPirate> myPirates, PirateGame game) {
		for (int i = 0; i < myPirates.size(); i++) {
			Pirate enemy;
			if (!myPirates.get(i).getPirate().hasTreasure()) {

				if (myPirates.get(i).isAttaker()) {

					enemy = myPirates.get(i).getTarget();
					if (game.inRange(enemy, myPirates.get(i).getPirate())) {
						game.debug("vfvffvfvfv");

						game.attack(myPirates.get(i).getPirate(), enemy);
						myPirates.get(i).setFinalDestination(null);

					}
//					 else{
//						 enemy=closestEnemyPirateWithoutTreasure(myPirates.get(i).getPirate(),
//						 game);
//						 if(enemy!=null&&enemy.getReloadTurns()==0&&myPirates.get(i).getPirate().getDefenseReloadTurns()==0&&game.inRange(enemy, myPirates.get(i).getPirate())){
//						 game.defend(myPirates.get(i).getPirate());
//						 game.debug("id: "+myPirates.get(i).getPirate().getId()+"in 1 ");
//
//						 myPirates.get(i).setFinalDestination(null);
//						 }
//					 }
				} else {
					enemy = closestEnemyPirateWithTreasure(myPirates.get(i).getPirate(), game);
					if (enemy == null) {
						enemy = closestEnemyPirateWithoutTreasure(myPirates.get(i).getPirate(), game);
					}
					if (myPirates.get(i).getPirate().getTurnsToSober() == 0 && enemy != null) {
						if (myPirates.get(i).getPirate().getReloadTurns() == 0) {
							if (enemy.getDefenseExpirationTurns() == 0 && enemy.getDefenseReloadTurns() != 0&& game.inRange(enemy, myPirates.get(i).getPirate())) {
								game.attack(myPirates.get(i).getPirate(), enemy);
								myPirates.get(i).setFinalDestination(null);
							}
						}
						 else {
						 enemy =closestEnemyPirateWithoutTreasure(myPirates.get(i).getPirate(),game);
						 if(enemy!=null&&myPirates.get(i).getPirate().getDefenseReloadTurns()
							 == 0&& game.inRange(enemy,myPirates.get(i).getPirate())
							 &&enemy.getReloadTurns()==0) {
							 game.debug("id: "+myPirates.get(i).getPirate().getId()+"in 2");
							 game.defend(myPirates.get(i).getPirate());
							 myPirates.get(i).setFinalDestination(null);
							 }
						 }
					}
				}
			} else {
				enemy = closestEnemyPirateWithoutTreasure(myPirates.get(i).getPirate(), game);
				if (enemy != null && game.inRange(enemy, myPirates.get(i).getPirate()) && enemy.getReloadTurns() == 0
						&& enemy.getTurnsToSober() == 0 && myPirates.get(i).getPirate().getDefenseReloadTurns() == 0) {
					game.defend(myPirates.get(i).getPirate());
					 game.debug("id: "+myPirates.get(i).getPirate().getId()+" in 3");

					myPirates.get(i).setFinalDestination(null);
				}
			}
		}
	}

	public boolean ifToDefend(Pirate p, PirateGame game) {
		for (int i = 0; i < game.enemyPiratesWithoutTreasures().size(); i++) {
			if (game.inRange(p, game.enemyPiratesWithoutTreasures().get(i))
					&& game.enemyPiratesWithoutTreasures().get(i).getReloadTurns() == 0
					&& !game.enemyDrunkPirates().contains(game.enemyPiratesWithoutTreasures().get(i))) {
				return true;
			}
		}
		return false;
	}

	public Pirate closestEnemyPirateWithTreasure(Pirate pirate, PirateGame game) {
		List<Pirate> enemyPirate = game.enemyPiratesWithTreasures();
		if (enemyPirate.size() != 0) {
			int min = game.distance(pirate, enemyPirate.get(0));
			int minIndex = 0;
			for (int i = 1; i < enemyPirate.size(); i++) {
				if (game.distance(pirate, enemyPirate.get(i)) < min) {
					min = game.distance(pirate, enemyPirate.get(i));
					minIndex = i;
				}
			}
			return enemyPirate.get(minIndex);
		}
		return null;

	}

	public Pirate closestEnemyPirateWithoutTreasure(Pirate pirate, PirateGame game) {
		List<Pirate> enemyPirate = game.enemyPiratesWithoutTreasures();
		if (enemyPirate.size() != 0) {
			int min = game.distance(pirate, enemyPirate.get(0));
			int minIndex = 0;
			for (int i = 1; i < enemyPirate.size(); i++) {
				if (game.distance(pirate, enemyPirate.get(i)) < min) {
					min = game.distance(pirate, enemyPirate.get(i));
					minIndex = i;
				}
			}
			return enemyPirate.get(minIndex);
		}
		return null;

	}

	public void setThisTurnDestination(LinkedList<MyPirate> myPirates, PirateGame game) {
		for (int i = 0; i < myPirates.size(); i++) {
			if (myPirates.get(i).getFinalDestination() != null && !myPirates.get(i).getPirate().isLost()) {
				List<Location> sailOp = game.getSailOptions(myPirates.get(i).getPirate(),
						myPirates.get(i).getFinalDestination(), myPirates.get(i).getMoves());
				if (sailOp.size() > 1 && !myPirates.get(i).isAssasin()) {

					int index = 1;
					if (game.getOpponentName().equals("22")) {
						index = game.getTurn() % 2;
					}
					if (isEmpty(sailOp.get(index), game, myPirates.get(i).getPirate().hasTreasure())) {
						myPirates.get(i).setThisTurnDestination(sailOp.get(index));
					} else {
						myPirates.get(i).setThisTurnDestination(sailOp.get(Math.abs(index - 1)));
					}
				} else {
					myPirates.get(i).setThisTurnDestination(sailOp.get(0));
				}

			} else {
				myPirates.get(i).setThisTurnDestination(null);
			}
		}
	}

	public boolean isEmpty(Location nextLocation, PirateGame game, boolean hasTreasure) {
		boolean clean = true;
		for (int i = 0; i < game.myPirates().size(); i++) {
			if (game.myPirates().get(i).getLocation().equals(nextLocation)) {
				clean = false;
			}
		}
		if (hasTreasure) {
			for (int i = 0; i < game.enemyPirates().size(); i++) {
				if (game.enemyPirates().get(i).getLocation().equals(nextLocation)) {
					clean = false;
				}
			}
		}
		return clean;

	}

	public void setMoves(LinkedList<MyPirate> myPirates, PirateGame game) {
		int moves = 6;
		for (int i = 0; i < game.myPiratesWithTreasures().size(); i++) {
			myPirates.get(game.myPiratesWithTreasures().get(i).getId()).setMoves(1);
			{
				moves -= 1;
			}
		}
		List<Pirate> piratesWitoutTreasure = game.myPiratesWithoutTreasures();
		for (int i = 0; i < piratesWitoutTreasure.size(); i++) {
			if (myPirates.get(piratesWitoutTreasure.get(i).getId()).getFinalDestination() != null) {
				if (myPirates.get(piratesWitoutTreasure.get(i).getId()).isAssasin()
						|| myPirates.get(piratesWitoutTreasure.get(i).getId()).isAttaker()) {
					int distance = game.distance(myPirates.get(piratesWitoutTreasure.get(i).getId()).getPirate(),
							myPirates.get(piratesWitoutTreasure.get(i).getId()).getFinalDestination());
					if (distance >= moves) {
						myPirates.get(piratesWitoutTreasure.get(i).getId()).setMoves(moves);
						moves = 0;
					} else {
						myPirates.get(piratesWitoutTreasure.get(i).getId()).setMoves(distance);
						moves -= distance;
					}
				}
			}
		}
		for (int i = 0; i < piratesWitoutTreasure.size(); i++) {
			if (myPirates.get(piratesWitoutTreasure.get(i).getId()).getFinalDestination() != null) {
				if (myPirates.get(piratesWitoutTreasure.get(i).getId()).isFree()) {
					int distance = game.distance(myPirates.get(piratesWitoutTreasure.get(i).getId()).getPirate(),
							myPirates.get(piratesWitoutTreasure.get(i).getId()).getFinalDestination());
					if (distance >= moves) {
						int n=moves/collectors+moves%collectors;
						myPirates.get(piratesWitoutTreasure.get(i).getId()).setMoves(n);
						moves -=n;
						collectors--;
					} else {
						myPirates.get(piratesWitoutTreasure.get(i).getId()).setMoves(distance);
						moves -= distance;
					}
				}
			}
		}
	}

	public void movePirates(LinkedList<MyPirate> myPirates, PirateGame game) {
		for (int i = 0; i < myPirates.size(); i++) {
			game.debug(myPirates.get(i).toString());
			if (myPirates.get(i).getThisTurnDestination() != null && myPirates.get(i).getMoves() > 0
					&& myPirates.get(i).getFinalDestination() != null) {
				game.setSail(myPirates.get(i).getPirate(), myPirates.get(i).getThisTurnDestination());
			}
		}
	}

	public int[] getClosestToEnemy(Pirate enemy, PirateGame game) {
		int[] minIndex = new int[game.myPiratesWithoutTreasures().size()];
		for (int i = 0; i < minIndex.length; i++) {
			minIndex[i] = game.myPiratesWithoutTreasures().get(i).getId();
		}
		int temp;
		for (int i = 1; i < minIndex.length; i++) {
			if (game.distance(enemy, game.myPiratesWithoutTreasures().get(i - 1)) > game.distance(enemy,
					game.myPiratesWithoutTreasures().get(i))) {
				temp = minIndex[i];
				for (int j = i; j > 0; j--) {
					minIndex[j] = minIndex[j - 1];
				}
				minIndex[0] = temp;
			}
		}
		return minIndex;
	}

	public void sortEnemyWithTreasure(List<Pirate> enemy, PirateGame game) {
		Pirate temp;

		for (int i = enemy.size() - 1; i > 0; i--) {
			int distance = game.distance(enemy.get(i), enemy.get(i).getInitialLocation());

			if (distance < game.distance(enemy.get(i - 1), enemy.get(i - 1).getInitialLocation())) {
				temp = enemy.get(i);
				for (int j = i; j > 0; j--) {
					enemy.set(j, enemy.get(j - 1));
				}
				enemy.set(0, temp);
			}
		}
		for (int i = enemy.size() - 1; i > 0; i--) {

			if (enemy.get(i).getTreasureValue() > enemy.get(i - 1).getTreasureValue()) {
				temp = enemy.get(i);
				for (int j = i; j > 0; j--) {
					enemy.set(j, enemy.get(j - 1));
				}
				enemy.set(0, temp);
			}
		}
	}

}

class MyPirate {
	private Pirate pirate;
	private Location finalDestination;
	private Location thisTurnDestination;
	private boolean destroy;
	private int moves = 0;
	private boolean free = true;
	private boolean attaker = false;
	private boolean assasin = false;
	private Pirate target;

	public MyPirate(Pirate pirate) {
		this.pirate = pirate;
	}

	public Pirate getPirate() {
		return pirate;
	}

	public void setPirate(Pirate pirate) {
		this.pirate = pirate;
	}

	public Location getFinalDestination() {
		return finalDestination;
	}

	public void setFinalDestination(Location finalDestination) {
		this.finalDestination = finalDestination;
		this.moves = 0;

	}

	public Location getThisTurnDestination() {
		return thisTurnDestination;
	}

	public void setThisTurnDestination(Location thisTurnDestination) {
		this.thisTurnDestination = thisTurnDestination;
	}

	public boolean isDestroy() {
		return destroy;
	}

	public void setDestroy(boolean destroy) {
		this.destroy = destroy;
	}

	public int getMoves() {
		return moves;
	}

	public void setMoves(int moves) {
		this.moves = moves;
	}

	public boolean isFree() {
		return free;
	}

	public void setFree(boolean free) {
		this.free = free;
		if (free) {
			this.assasin = false;
			this.attaker = false;
		}
	}

	public boolean isAttaker() {
		return attaker;
	}

	public void setAttaker(boolean attaker, Pirate target) {
		this.attaker = attaker;
		this.target = target;
		if (attaker) {
			this.assasin = false;
			this.free = false;
		}
	}

	public boolean isAssasin() {
		return assasin;
	}

	public void setAssasin(boolean assasin, Pirate target) {
		this.assasin = assasin;
		this.target = target;

		if (assasin) {
			this.free = false;
			this.attaker = false;
		}
	}

	public Pirate getTarget() {
		return target;
	}

	public void setTarget(Pirate target) {
		this.target = target;
	}

	public String toString() {
		String status = "regular";
		if (this.assasin) {
			status = "assasin";
		} else if (this.attaker) {
			status = "attaker";
		}
		return ("ID: "+this.pirate.getId()+" | Moves: " + this.moves + " | Status: " + status);
	}
}

class MyEnemies {
	private PirateGame game;
	private Pirate Pirate;
	private LinkedList<Pirate> targetPirates;
	private int[][] piratesId;
	private Pirate[] enemyPirates;
	public boolean threatnd = false;

	public MyEnemies(PirateGame game, Pirate pirate) {
		enemyPirates = (Pirate[]) game.allEnemyPirates().toArray();
		Pirate = pirate;
		targetPirates = new LinkedList<Pirate>();
		piratesId = new int[enemyPirates.length][2];
		for (int i = 0; i < enemyPirates.length; i++)
			piratesId[i][1] = game.distance(Pirate, enemyPirates[i]);
	}

	public void oneCycle() {
		targetPirates.clear();
		for (int i = 0; i < enemyPirates.length; i++) {
			piratesId[i][0] = piratesId[i][1];
			piratesId[i][1] = game.distance(Pirate, enemyPirates[i]);
			if (piratesId[i][1] < 12 && piratesId[i][1] < piratesId[i][0] && !enemyPirates[i].hasTreasure()
					&& enemyPirates[i].getTurnsToSober() == 0)
				targetPirates.add(enemyPirates[i]);
		}
		if (!Pirate.hasTreasure())
			threatnd = false;
		else if (targetPirates.isEmpty())
			threatnd = false;
		else
			threatnd = true;

	}
}