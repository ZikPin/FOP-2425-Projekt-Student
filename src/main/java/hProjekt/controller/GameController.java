package hProjekt.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Supplier;

import hProjekt.model.*;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import hProjekt.Config;
import hProjekt.controller.actions.ConfirmBuildAction;
import hProjekt.controller.actions.PlayerAction;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Pair;

/**
 * The GameController class represents the controller for the game logic.
 * It manages the game state, player controllers, dice rolling and the overall
 * progression of the game.
 * It tells the players controllers what to do and when to do it.
 */
public class GameController {
    private final GameState state;
    private final Map<Player, PlayerController> playerControllers;
    private final List<AiController> aiControllers = new ArrayList<>();
    private final Supplier<Integer> dice;
    private final IntegerProperty currentDiceRoll = new SimpleIntegerProperty(0);
    private final IntegerProperty roundCounter = new SimpleIntegerProperty(0);
    private final Property<Pair<City, City>> chosenCitiesProperty = new SimpleObjectProperty<>();

    private final Property<PlayerController> activePlayerController = new SimpleObjectProperty<>();

    private boolean stopped = false;

    /**
     * Creates a new GameController with the given game state and dice supplier.
     *
     * @param state the game state
     * @param dice  the dice supplier
     */
    public GameController(GameState state, Supplier<Integer> dice) {
        this.state = state;
        this.playerControllers = new HashMap<>();
        this.dice = dice;
    }

    /**
     * Creates a new GameController with the given game state.
     *
     * @param state the game state
     */
    public GameController(GameState state) {
        this(state, () -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1));
    }

    /**
     * Creates a new GameController with a new game state and a random dice
     * supplier.
     */
    public GameController() {
        this(new GameState(new HexGridImpl(Config.TOWN_NAMES), new ArrayList<>()),
                () -> Config.RANDOM.nextInt(1, Config.DICE_SIDES + 1));
    }

    /**
     * Returns the game state.
     *
     * @return the game state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Returns a map from players to player controllers.
     *
     * @return a map from players to player controllers
     */
    public Map<Player, PlayerController> getPlayerControllers() {
        return playerControllers;
    }

    /**
     * Returns a property that contains the active player controller.
     *
     * @return a property that contains the active player controller
     */
    public Property<PlayerController> activePlayerControllerProperty() {
        return activePlayerController;
    }

    /**
     * Returns the active player controller.
     *
     * @return the active player controller
     */
    public PlayerController getActivePlayerController() {
        return activePlayerController.getValue();
    }

    /**
     * Returns the current dice roll property.
     *
     * @return the current dice roll property
     */
    public IntegerProperty currentDiceRollProperty() {
        return currentDiceRoll;
    }

    /**
     * Returns the current dice roll.
     *
     * @return the current dice roll
     */
    public int getCurrentDiceRoll() {
        return currentDiceRoll.get();
    }

    /**
     * Returns the round counter property.
     *
     * @return the round counter property
     */
    public IntegerProperty roundCounterProperty() {
        return roundCounter;
    }

    /**
     * Returns the chosen cities property that contains the starting and target city
     * as a javaFX Pair.
     *
     * @return the chosen cities property
     */
    public ReadOnlyProperty<Pair<City, City>> chosenCitiesProperty() {
        return chosenCitiesProperty;
    }

    /**
     * Returns the starting city.
     *
     * @return the starting city
     */
    public City getStartingCity() {
        return chosenCitiesProperty.getValue().getKey();
    }

    /**
     * Returns the target city.
     *
     * @return the target city
     */
    public City getTargetCity() {
        return chosenCitiesProperty.getValue().getValue();
    }

    /**
     * Casts the dice and returns the result.
     *
     * @return the result of the dice roll
     */
    public int castDice() {
        currentDiceRoll.set(dice.get());
        return currentDiceRoll.get();
    }

    /**
     * Stops the game and the Thread.
     */
    public void stop() {
        stopped = true;
    }

    /**
     * Initializes the player controllers for each player in the game state.
     * If a player is an AI, it creates an AI controller for the player.
     */
    private void initPlayerControllers() {
        for (Player player : state.getPlayers()) {
            playerControllers.put(player, new PlayerController(this, player));
            if (player.isAi()) {
                try {
                    aiControllers.add(player.getAiController()
                            .getConstructor(PlayerController.class, HexGrid.class, GameState.class, Property.class,
                                    IntegerProperty.class, IntegerProperty.class, ReadOnlyProperty.class)
                            .newInstance(playerControllers.get(player), state.getGrid(), state,
                                    activePlayerController, currentDiceRoll, roundCounter, chosenCitiesProperty));
                } catch (NoSuchMethodException e) {
                    System.err.println("Could not create ai controller for player " + player.getName());
                    System.err.println("You probably forgot to implement the constructor in your ai controller.");
                    System.err.println("The full error message: " + e.getMessage());
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    System.err.println("Could not create ai controller for player " + player.getName());
                    System.err.println("You probably do not have all necessary parameters in your constructor.");
                    System.err.println("The full error message: " + e.getMessage());
                    e.printStackTrace();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    System.err.println("Could not create ai controller for player " + player.getName());
                    System.err.println("An error occurred while trying to create the ai controller.");
                    System.err.println("The full error message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Starts the game and handles the game loop.
     *
     * The game consists of two phases: the building phase and the driving phase.
     *
     * @throws IllegalStateException if there are not enough playerss
     */
    public void startGame() {
        if (this.state.getPlayers().size() < Config.MIN_PLAYERS) {
            throw new IllegalStateException("Not enough players");
        }
        if (playerControllers.isEmpty()) {
            initPlayerControllers();
        }

        // Bauphase
        getState().getGamePhaseProperty().setValue(GamePhase.BUILDING_PHASE);
        executeBuildingPhase();

        // Fahrphase
        getState().getGamePhaseProperty().setValue(GamePhase.DRIVING_PHASE);
        roundCounter.set(0);
        executeDrivingPhase();

        getState().getWinnerProperty().setValue(getState().getPlayers().stream()
                .max((p1, p2) -> Integer.compare(p1.getCredits(), p2.getCredits())).get());
    }

    /**
     * Executes the building phase of the game.
     * The building phase consists of the following steps:
     * - While there are unconnected cities, let a player roll the dice
     * - Starting with the player that rolled the dice, let the players build until
     * all players have built
     * - The players are given a building budget according to the dice roll
     * - Repeat until there are only
     * {@link Config#UNCONNECTED_CITIES_START_THRESHOLD} unconnected cities left
     */
    @StudentImplementationRequired("P2.3")
    private void executeBuildingPhase() {
        // TODO: P2.3
        int indexActivePlayer = 0;
        // Schleife
        while (getState().getGrid().getUnconnectedCities().size() > Config.UNCONNECTED_CITIES_START_THRESHOLD) {
            // Erhöhen von der Runde
            roundCounter.set(roundCounter.get() + 1);

            // Spielerindex erhöhen und Würfeln ausführen
            indexActivePlayer = (roundCounter.get() - 1) % playerControllers.size();
            activePlayerController.setValue(getPlayerControllers().get(getState().getPlayers().get(indexActivePlayer)));
            activePlayerController.getValue().setPlayerObjective(PlayerObjective.ROLL_DICE);

            // Setzen des neuen Baubudgets
            playerControllers.values()
                .forEach(
                    playerController -> {
                        playerController.setBuildingBudget(getCurrentDiceRoll());
                        waitForBuild(playerController);
                    }
                );
        }
    }

    /**
     * Chooses two random cities from the grid and sets them as starting and target
     * city.
     * The chosen cities are stored in the chosen cities property.
     */
    @StudentImplementationRequired("P2.4")
    public void chooseCities() {
        // TODO: P2.4
        // Wählen einer Stadt
        List<City> cities = getState().getGrid().getCities().values()
            .stream()
            .filter(getState().getChosenCities()::contains)
            .toList();

        City cityStart = cities.get(Config.RANDOM.nextInt(cities.size()));
        getState().addChosenCity(cityStart);

        // Wählen anderer Stadt
        cities = getState().getGrid().getCities().values()
            .stream()
            .filter(getState().getChosenCities()::contains)
            .toList();

        City cityEnd = cities.get(Config.RANDOM.nextInt(cities.size()));
        getState().addChosenCity(cityStart);

        chosenCitiesProperty.setValue(new Pair<City, City>(cityStart, cityEnd));
    }

    /**
     * Let the players build during the driving phase.
     * The players are sorted by their credits in ascending order ensuring that the
     * player with the least credits builds first.
     * Players are given a fixed building budget of
     * {@link Config#MAX_BUILDINGBUDGET_DRIVING_PHASE}.
     */
    private void buildingDuringDrivingPhase() {
        getState().getPlayers().stream().sorted((p1, p2) -> Integer.compare(p1.getCredits(), p2.getCredits()))
                .forEachOrdered((player) -> {
                    final PlayerController pc = playerControllers.get(player);
                    pc.setBuildingBudget(Config.MAX_BUILDINGBUDGET_DRIVING_PHASE);
                    waitForBuild(pc);
                });
    }

    /**
     * Let the players choose the rails they want to rent and confirm the calculated
     * path.
     */
    @StudentImplementationRequired("P2.6")
    private void letPlayersChoosePath() {
        // TODO: P2.6
        for (PlayerController pc : playerControllers.values()) {
            // Zurücksetzen und Position ändern
            pc.resetDrivingPhase();
            getState().setPlayerPositon(pc.getPlayer(), getStartingCity().getPosition());

            // Einen Pfad wählen
            while (!pc.hasConfirmedPath()) {
                pc.setPlayerObjective(PlayerObjective.CHOOSE_PATH); // Nicht sicher
                pc.waitForNextAction(PlayerObjective.CHOOSE_PATH);
                pc.setPlayerObjective(PlayerObjective.CONFIRM_PATH);
                pc.waitForNextAction(PlayerObjective.CONFIRM_PATH);
            }
        }
    }

    /**
     * Handles the driving.
     * If only one player is driving, the player automatically reaches the target
     * city.
     * While there are players that have not reached the target city and there are
     * still credits to win, let the players roll the dice and drive.
     * If a player reaches the target city, all other players surplus will be
     * reduced by {@link Config#DICE_SIDES} before each round.
     * The players are sorted by their credits in descending order ensuring that the
     * player with the most credits drives first.
     */
    @StudentImplementationRequired("P2.7")
    private void handleDriving() {
        // TODO: P2.7
        if (!getState().getDrivingPlayers().isEmpty()) {
            if (getState().getDrivingPlayers().size() == 1) {
                Player player = getState().getDrivingPlayers().getFirst();
                getState().setPlayerPositon(player, getTargetCity().getPosition());
            } else {
                // Spieler, die Zielstadt erreicht haben
                List<Player> reachedTargetCity = getState().getPlayerPositions().keySet()
                    .stream()
                    .filter(player -> {return getState().getPlayerPositions().get(player).equals(getTargetCity().getPosition());})
                    .toList();

                // Subtrahieren den Wert von DICE_SIDES vom Surplus
                getState().getDrivingPlayers()
                    .stream()
                    .filter(player -> !reachedTargetCity.contains(player))
                    .forEach(player -> getState().addPlayerPointSurplus(player, getState().getPlayerPointSurplus().get(player) - Config.DICE_SIDES));

                // Sortieren nach Credits
                List<Player> drivingPlayers = getState().getDrivingPlayers()
                    .stream()
                    .sorted(Comparator.comparing(Player::getCredits))
                    .toList();

                // Updating the list of driving players
                getState().resetDrivingPlayers();
                drivingPlayers.forEach(getState()::addDrivingPlayer);

                // Schleife bis Ende der Fahrphase
                while(reachedTargetCity.size() < Config.WINNING_CREDITS.size() || reachedTargetCity.size() != getState().getDrivingPlayers().size()) {
                    // Schleife für jeden Spieler, der die Zielstadt noch nicht erreicht hat
                    for (Player drivingPlayer: getState().getDrivingPlayers()) {
                        if (!reachedTargetCity.contains(drivingPlayer)) {
                            // Würfeln und fahren
                            PlayerController pc = playerControllers.get(drivingPlayer);
                            pc.setPlayerObjective(PlayerObjective.ROLL_DICE);
                            pc.waitForNextAction(PlayerObjective.ROLL_DICE);
                            pc.setPlayerObjective(PlayerObjective.DRIVE);
                            pc.waitForNextAction(PlayerObjective.DRIVE);

                            // Update die Liste mit den die Stadt erreichten Spielern
                            if (getState().getPlayerPositions().get(drivingPlayer).equals(getTargetCity().getPosition())) {
                                reachedTargetCity.add(drivingPlayer);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the winners of a round.
     * The winners are the players that have reached the target city. If multiple
     * players have reached the target city, the player with the biggest point
     * surplus wins.
     *
     * @return the winners of a round
     */
    @StudentImplementationRequired("P2.8")
    private List<Player> getWinners() {
        // TODO: P2.8
        List<Player> playersSorted = getState().getPlayerPointSurplus().keySet()
            .stream()
            .sorted(Comparator.comparing(getState().getPlayerPointSurplus()::get))
            .toList();

        List<Player> winners = new ArrayList<>(playersSorted);

        for (int i = 0; i < Config.WINNING_CREDITS.size(); i++) {
            winners.add(playersSorted.get(i));
        }

        return winners;
    }

    /**
     * Executes the driving phase of the game.
     * The driving phase consists of the following steps:
     * - If the round counter is a multiple of 3, let the players build during the
     * driving phase
     * - Let a player choose the cities to drive to
     * - Let the players choose their path
     * - Let the players that are driving roll the dice and drive
     * - Check if a player has reached the target city and if so, add credits to the
     * player
     * - Repeat until all cities were chosen
     */
    @StudentImplementationRequired("P2.9")
    private void executeDrivingPhase() {
        // TODO: P2.9
        // Die Liste mit nicht gewählten Städten
        List<TilePosition> cities = getState().getGrid().getCities()
            .keySet()
            .stream()
            .filter(tilePosition -> getState().getChosenCities().contains(getState().getGrid().getCityAt(tilePosition)))
            .toList();

        while (!cities.isEmpty()) {
            // 1. Schritt
            roundCounter.set(roundCounter.get() + 1);

            // 2. Schritt
            getState().resetDrivingPlayers();
            getState().resetPlayerSurplus();
            getState().resetPlayerPositions();

            // 3. Schritt
            if (roundCounter.get() % 3 == 0) {
                buildingDuringDrivingPhase();
            }

            // 4. Schritt
            PlayerController pcCityChooser = playerControllers.values().stream().findFirst().get();

            for (Player players: playerControllers.keySet()) {
                if (players.getID() == (roundCounter.get() - 1) % state.getPlayers().size()) {
                    pcCityChooser = playerControllers.get(players);
                }
            }

            pcCityChooser.setPlayerObjective(PlayerObjective.CHOOSE_CITIES);
            pcCityChooser.waitForNextAction(PlayerObjective.CHOOSE_CITIES);

            // 5. Schritt
            letPlayersChoosePath();

            // 6. Schritt
            handleDriving();

            // 7. Schritt
            List<Player> winners = getWinners();
            for (int i = 0; i < Config.WINNING_CREDITS.size(); i++) {
                winners.get(i).addCredits(Config.WINNING_CREDITS.get(i));
            }
        }
    }

    /**
     * Waits for the player to build.
     *
     * @param pc The {@link PlayerController} to wait for.
     */
    private void waitForBuild(final PlayerController pc) {
        withActivePlayer(pc, () -> {
            PlayerAction action = pc.waitForNextAction(PlayerObjective.PLACE_RAIL);
            while (!(action instanceof ConfirmBuildAction)) {
                action = pc.waitForNextAction();
            }
        });
    }

    /**
     * Executes the given {@link Runnable} and set the active player to the given
     * {@link PlayerController}.
     * After the {@link Runnable} is executed, the active player is set to
     * {@code null} and the objective is set to {@link PlayerObjective#IDLE}.
     *
     * @param pc The {@link PlayerController} to set as active player.
     * @param r  The {@link Runnable} to execute.
     */
    @DoNotTouch
    public void withActivePlayer(final PlayerController pc, final Runnable r) {
        if (stopped) {
            throw new RuntimeException("Game was stopped");
        }
        activePlayerController.setValue(pc);
        r.run();
        pc.setPlayerObjective(PlayerObjective.IDLE);
        activePlayerController.setValue(null);
    }
}
