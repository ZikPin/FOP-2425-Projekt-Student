package hProjekt.model;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.tudalgo.algoutils.student.annotation.DoNotTouch;
import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import hProjekt.Config;
import hProjekt.controller.AiController;
import javafx.scene.paint.Color;

/**
 * Default implementation of {@link Player}.
 */
public class PlayerImpl implements Player {
    private final HexGrid hexGrid;
    private final String name;
    private final int id;
    private final Color color;
    private final Class<? extends AiController> aiController;
    private int credits;

    @DoNotTouch("Please don't create a public Contructor, use the Builder instead.")
    private PlayerImpl(final HexGrid hexGrid, final Color color, final int id, final String name,
            final Class<? extends AiController> ai) {
        this.hexGrid = hexGrid;
        this.color = color;
        this.id = id;
        this.name = name;
        this.aiController = ai;
        this.credits = Config.STARTING_CREDITS;
    }

    /**
     * Getter-Method for the attribute hexGrid
     * @return {@link HexGrid} on which the Player plays
     */
    @Override
    @StudentImplementationRequired("P1.1")
    public HexGrid getHexGrid() {
        // TODO: P1.1
        return hexGrid;
    }

    /**
     * Getter-Method for the name of the player
     * @return name of the player
     */
    @Override
    @StudentImplementationRequired("P1.1")
    public String getName() {
        // TODO: P1.1
        return name;
    }

    /**
     * Getter-Method for the id of the player
     * @return player's id
     */
    @Override
    @StudentImplementationRequired("P1.1")
    public int getID() {
        // TODO: P1.1
        return id;
    }

    /**
     * Getter-Method for the color of the player
     * @return player's color
     */
    @Override
    @StudentImplementationRequired("P1.1")
    public Color getColor() {
        // TODO: P1.1
        return color;
    }

    /**
     * Checks if the PLayer is controlled by AI
     * @return {@code true} if player is AI, {@code false} if otherwise
     */
    @Override
    @StudentImplementationRequired("P1.1")
    public boolean isAi() {
        // TODO: P1.1
        return aiController!=null;
    }

    /**
     * Getter-Method for the credits of the player
     * @return amount of Credits that the player has
     */
    @Override
    @StudentImplementationRequired("P1.2")
    public int getCredits() {
        // TODO: P1.2
        return credits;
    }

    /**
     * Adds the given amount of Credits to the current balance of the player
     * @param amount the amount of credits to add
     */
    @Override
    @StudentImplementationRequired("P1.2")
    public void addCredits(int amount) {
        // TODO: P1.2
        credits+=amount;
    }

    /**
     * Removes given amount of credits from the current balance of the player
     * @param amount the amount of credits to remove
     * @return {@code true} if the credits were successfully removed,
     *         {@code false} if player does not have enough credits to be removed
     */
    @Override
    @StudentImplementationRequired("P1.2")
    public boolean removeCredits(int amount) {
        // TODO: P1.2
        if (amount>credits || amount<0) return false;
        credits-=amount;
        return true;
    }

    @Override
    public String toString() {
        return String.format("Player %d %s (%s)", getID(), getName(), getColor());
    }

    // Trying the equal Override solution, to solve the Problem with getRails
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            System.out.println("The objects are same");
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            System.out.println("The objects are not equal");
            return false;
        }

        Player player = (Player) obj;

        String currentName = this.toString().trim();
        String otherName = player.toString().trim();

        if (currentName.equals(otherName)) return true;
        else {
            for (int i = 0; i < Math.min(currentName.length(), otherName.length()); i++) {
                if(otherName.charAt(i) != currentName.charAt(i)) System.out.print(i);
            }
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString().trim());
    }

    @Override
    public Class<? extends AiController> getAiController() {
        return aiController;
    }

    @Override
    public Map<Set<TilePosition>, Edge> getRails() {
        return getHexGrid().getRails(this);
    }

    /**
     * Builder for {@link PlayerImpl}.
     * Allows to create a new player and modify its properties before building it.
     */
    @DoNotTouch
    public static class Builder {
        private int id;
        private Color color;
        private @Nullable String name;
        private @Nullable Class<? extends AiController> aiController;

        /**
         * Creates a new builder for a player with the given id.
         *
         * @param id the id of the player to create
         */
        public Builder(final int id) {
            this.id = id;
            color(null);
        }

        /**
         * Returns the color of the player.
         *
         * @return the color of the player
         */
        public Color getColor() {
            return this.color;
        }

        /**
         * Sets the color of the player.
         *
         * @param playerColor the color of the player
         * @return this builder
         */
        public Builder color(final Color playerColor) {
            this.color = playerColor == null
                    ? new Color(
                            Config.RANDOM.nextDouble(),
                            Config.RANDOM.nextDouble(),
                            Config.RANDOM.nextDouble(),
                            1)
                    : playerColor;
            return this;
        }

        /**
         * Returns the name of the player.
         *
         * @return the name of the player
         */
        public @Nullable String getName() {
            return this.name;
        }

        /**
         * Sets the name of the player.
         *
         * @param playerName the name of the player
         * @return this builder
         */
        public Builder name(final @Nullable String playerName) {
            this.name = playerName;
            return this;
        }

        /**
         * Returns the name of the player or a default name if no name was set.
         * The default name is "Player" followed by the id of the player.
         *
         * @return the name of the player or a default name if no name was set
         */
        public String nameOrDefault() {
            return this.name == null ? String.format("Player%d", this.id) : this.name;
        }

        /**
         * Sets the id of the player.
         *
         * @param newId the id of the player
         * @return this builder
         */
        public Builder id(final int newId) {
            this.id = newId;
            return this;
        }

        /**
         * Returns the id of the player.
         *
         * @return the id of the player
         */
        public int getId() {
            return this.id;
        }

        /**
         * Returns whether the player is an AI.
         *
         * @return whether the player is an AI
         */
        public boolean isAi() {
            return this.aiController != null;
        }

        /**
         * Sets whether the player is an AI.
         *
         * @param ai the ai controller of the player
         * @return this builder
         */
        public Builder ai(final Class<? extends AiController> ai) {
            this.aiController = ai;
            return this;
        }

        /**
         * Builds the player with the properties set in this builder.
         *
         * @param grid the grid the player is on
         * @return the player with the properties set in this builder
         */
        public Player build(final HexGrid grid) {
            return new PlayerImpl(grid, this.color, this.id, nameOrDefault(), this.aiController);
        }
    }
}
