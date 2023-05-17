package challenge.game.rest;

/**
 * Tárolja a játékok 3 típusát.
 */
public enum GameType {

	/**
	 * Gyakorló játék, mely leváltja a SINGLE_PLAYER játéktípust, továbbá
	 * lehetőséget ad hogy ugyanazon csapat egyszerre több bot-ját is csatlakoztassa,
	 * megmérje őket egymás ellen. 
	 */
	PRACTISE_GAME,
	
	/**
     * Az egyjátékos módhoz tartozó enum.
     * @Depricated : use PRACTISE_GAME instead.
     */
	@Deprecated
	SINGLE_PLAYER,
    
	/**
     * A többjátékos módhoz tartozó enum.
     */
	MULTI_PLAYER,
    
	/**
     * Kvalifikációhoz használt játékmódhoz tartozó enum.
     */
    QUALIFYING;
	
	public boolean isPractise() {
		return this == PRACTISE_GAME || this == SINGLE_PLAYER;
	}
}
