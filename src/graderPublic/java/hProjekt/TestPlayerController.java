package hProjekt;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import hProjekt.controller.GameController;
import hProjekt.controller.PlayerController;
import hProjekt.controller.actions.BuildRailAction;
import hProjekt.controller.actions.ChooseCitiesAction;
import hProjekt.controller.actions.ChooseRailsAction;
import hProjekt.controller.actions.ConfirmBuildAction;
import hProjekt.controller.actions.ConfirmDrive;
import hProjekt.controller.actions.DriveAction;
import hProjekt.controller.actions.IllegalActionException;
import hProjekt.controller.actions.PlayerAction;
import hProjekt.controller.actions.RollDiceAction;
import hProjekt.model.Edge;
import hProjekt.model.Player;
import hProjekt.model.Tile;

public class TestPlayerController extends PlayerController {
    private GameController localGameController;

    public TestPlayerController(final GameController gameController, final Player player) {
        super(gameController, player);
        this.localGameController = gameController;
    }

    @Override
    public PlayerAction waitForNextAction() {
        PlayerAction action = null;
        final Set<Class<? extends PlayerAction>> allowedActions = getPlayerObjective()
                .getAllowedActions();

        if (allowedActions.contains(BuildRailAction.class)
                && !getBuildableRails().isEmpty()) {
            getBuildableRails().toArray(Edge[]::new)[0].getRailOwners().add(getPlayer());
            action = new BuildRailAction(List.of());
        }
        if (allowedActions.contains(ConfirmBuildAction.class) && getBuildableRails()
                .isEmpty()) {
            action = new ConfirmBuildAction();
        }
        if (allowedActions.contains(ChooseCitiesAction.class)) {
            action = new ChooseCitiesAction();
        }
        if (allowedActions.contains(ChooseRailsAction.class)) {
            action = new ChooseRailsAction(Set.of(getChooseableEdges().toArray(Edge[]::new)[0]));
        }
        if (allowedActions.contains(ConfirmDrive.class)) {
            action = new ConfirmDrive(true);
        }
        if (allowedActions.contains(DriveAction.class)) {
            action = new DriveAction(getDrivableTiles().keySet().toArray(Tile[]::new)[0]);
        }
        if (allowedActions.contains(RollDiceAction.class)) {
            action = new RollDiceAction();
        }
        if (action != null) {
            try {
                action.execute(this);
            } catch (IllegalActionException e) {
                // We do not care
            }
        }
        return action;
    }

    @Override
    public Set<Edge> getChooseableEdges() {
        return localGameController.getState().getGrid().getEdges().values().stream().collect(Collectors.toSet());
    }

    private void updatePlayerState() {
        // We do not care
    }
}
