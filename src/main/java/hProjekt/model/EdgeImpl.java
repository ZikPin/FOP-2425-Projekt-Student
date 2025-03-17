package hProjekt.model;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tudalgo.algoutils.student.annotation.StudentImplementationRequired;

import hProjekt.Config;
import javafx.beans.property.Property;
import javafx.util.Pair;

/**
 * Default implementation of {@link Edge}.
 *
 * @param grid       the HexGrid instance this edge is placed in
 * @param position1  the first position
 * @param position2  the second position
 * @param railOwners the road's owner, if a road has been built on this edge
 */
public record EdgeImpl(HexGrid grid, TilePosition position1, TilePosition position2, Property<List<Player>> railOwners)
        implements Edge {
    @Override
    public HexGrid getHexGrid() {
        return grid;
    }

    @Override
    public TilePosition getPosition1() {
        return position1;
    }

    @Override
    public TilePosition getPosition2() {
        return position2;
    }

    @Override
    public Property<List<Player>> getRailOwnersProperty() {
        return railOwners;
    }

    /**
     * checks all the edges adjacent to two tiles connected by this edge
     * and saves only the ones that have a rail owned by the given player
     * @param player the player to check for.
     * @return a set of adjacent {@link Edge} on which the given player has rails
     */
    @Override
    @StudentImplementationRequired("P1.3")
    public Set<Edge> getConnectedRails(final Player player) {
        // TODO: P1.3
        Set<Edge> edges=this.getConnectedEdges();
        Set<Edge> res = new HashSet<>();
        for (Edge edge : edges){
            if(edge.hasRail()){
                if(edge.getRailOwners().contains(player)) res.add(edge);
            }
        }

        return res;
    }

    @Override
    public Map<Player, Integer> getRentingCost(Player player) {
        if (getRailOwners().contains(player)) {
            return Map.of();
        }
        return getRailOwners().stream().collect(Collectors.toMap(p -> p, p -> 1));
    }

    @Override
    public int getDrivingCost(TilePosition from) {
        if (!getAdjacentTilePositions().contains(from)) {
            throw new IllegalArgumentException("The given position is not adjacent to this edge.");
        }
        return Config.TILE_TYPE_TO_DRIVING_COST.get(new Pair<Tile.Type, Tile.Type>(
                getHexGrid().getTileAt(from).getType(),
                getHexGrid().getTileAt(getPosition1().equals(from) ? getPosition2() : getPosition1()).getType()));
    }

    @Override
    public int getTotalBuildingCost(Player player) {
        return getBaseBuildingCost() + getTotalParallelCost(player);
    }

    @Override
    public int getTotalParallelCost(Player player) {
        return getParallelCostPerPlayer(player).values().stream().reduce(0, Integer::sum);
    }

    @Override
    public Map<Player, Integer> getParallelCostPerPlayer(Player player) {
        final Map<Player, Integer> result = new HashMap<>();
        if ((!getRailOwners().isEmpty()) && (!((getRailOwners().size() == 1) && getRailOwners().contains(player)))) {
            if (Collections.disjoint(getHexGrid().getCities().keySet(), getAdjacentTilePositions())) {
                getRailOwners().stream().forEach(p -> result.put(p, 5));
            } else {
                getRailOwners().stream().forEach(p -> result.put(p, 3));
            }
        }
        getAdjacentTilePositions().stream().flatMap(position -> {
            if (getHexGrid().getCityAt(position) != null) {
                return Stream.empty();
            }
            Set<Player> owners = getHexGrid().getTileAt(position).getEdges().stream()
                    .filter(Predicate.not(this::equals)).flatMap(edge -> edge.getRailOwners().stream())
                    .collect(Collectors.toUnmodifiableSet());
            if (owners.contains(player)) {
                return Stream.empty();
            }
            return owners.stream();
        }).forEach(p -> result.put(p, Math.max(result.getOrDefault(p, 0), 1)));
        return result;
    }

    @Override
    public int getBaseBuildingCost() {
        return Config.TILE_TYPE_TO_BUILDING_COST.get(getAdjacentTilePositions().stream()
                .map(position -> getHexGrid().getTileAt(position).getType()).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public List<Player> getRailOwners() {
        return getRailOwnersProperty().getValue();
    }

    @Override
    public boolean removeRail(Player player) {
        return getRailOwnersProperty().getValue().remove(player);
    }

    /**
     * (tries to) Adds a new rail for the given player
     * @param player the player to add the rail for
     * @return {@code true} if the rail was added successfully
     *         {@code false} if there was a problem
     *                  (e.g. player already has a rail on this edge, it's player's first rail but not adjacent to a StartCity etc.)
     */
    @Override
    @StudentImplementationRequired("P1.3")
    public boolean addRail(Player player) {
        // TODO: P1.3
            if (getRailOwners().contains(player)) return false;

            Map<Set<TilePosition>, Edge> rails = grid.getRails(player);
            if (rails.isEmpty()) { //player has no other rails
                // Zik: changed because getCity returns null
                if (!(grid.getStartingCities().containsKey(position1)
                    || grid.getStartingCities().containsKey(position2)))
                    return false;
            } else { //player already has rail(-s)
                // Zik: Just experimenting to solve the error
                if (rails
                    .values()
                    .stream()
                    .filter(this::connectsTo)
                    .toList().isEmpty())
                    return false; //the "new" rails is not connected to the Rail net of the player

            }

        //"all tests succeeded" so we can go further with building the rail
        railOwners.getValue().add(player);
        return true;
    }

    @Override
    public boolean hasRail() {
        return (getRailOwnersProperty().getValue() != null) && (!getRailOwnersProperty().getValue().isEmpty());
    }

    /**
     * Checks the 1st and 2nd positions of this and given Edges in pairs on equality
     * @param other the other edge
     * @return {@code true} if the given and this edge has a common {@link TilePosition} as pos1 or pos2
     *          {@code false} if otherwise
     */
    @Override
    @StudentImplementationRequired("P1.3")
    public boolean connectsTo(Edge other) {
        // TODO: P1.3
        return this.getPosition1().equals(other.getPosition1()) ||
            this.getPosition1().equals(other.getPosition2()) ||
            this.getPosition2().equals(other.getPosition1()) ||
            this.getPosition2().equals(other.getPosition2());

    }

    @Override
    public Set<TilePosition> getAdjacentTilePositions() {
        return Set.of(getPosition1(), getPosition2());
    }

    /**
     * "Adds together" all edges that are connected to Pos1 of this edge and to Pos2 of this edge
     * @return a set of all the edges that are adjacent to two tiles that this edge connects
     */
    @Override
    @StudentImplementationRequired("P1.3")
    public Set<Edge> getConnectedEdges() {
        // TODO: P1.3
        //useful methods: Tile::getEdges; HexGridImpl::getTileAt

        Set<Edge> edges = new HashSet<>();
        edges.addAll(getHexGrid().getTileAt(position1).getEdges()); //add edges at Pos1
        edges.addAll(getHexGrid().getTileAt(position2).getEdges()); //add the edges at Pos2
        return edges;
    }
}
