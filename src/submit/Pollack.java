package submit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import game.FindState;
import game.Finder;
import game.Node;
import game.NodeStatus;
import game.ScramState;
import game.Tile;

/** Student solution for two methods. */
public class Pollack extends Finder {

    /** Method to DFS Walk Pollack from current block to neighbor blocks. This method takes in
     * arguments state of type FindState that corresponds to the current state which will be used to
     * get data about the whereabouts and other useful info about Pollack and also visits of type
     * HashSet which will be used to populate and store data about the visited blocks. **/
    public void walkPollack(FindState state, HashSet<Long> visits) {
        // save current position
        long here= state.currentLoc();

        // Return if we are at the orb
        if (state.distanceToOrb() == 0) return;

        // Add current state to visited states
        visits.add(here);

        // Get neighbors and sort
        ArrayList<NodeStatus> neighbors= new ArrayList<>(state.neighbors());
        Collections.sort(neighbors);

        // Loop for each block in the neighbors close by.
        for (NodeStatus block : neighbors) {
            long neigh_block= block.getId();
            // If the block had not yet been visited, visit it.
            if (!visits.contains(neigh_block)) {
                state.moveTo(neigh_block);
                // Recursive call on block, and then move to it.
                walkPollack(state, visits);
                // Return if we are at the orb
                if (state.distanceToOrb() == 0) return;
                state.moveTo(here);
            }
        }
    }

    /** This method is a way to get out after having found the orb in a path that optimizes the
     * amount of gold picked up on the way back. It looks for gold until it is runnning low on steps
     * and returns after that. **/
    public void ightImmaDip(ScramState state) {

        // get gold until I caint no mo
        for (Node block : state.allNodes()) {
            Node whereimat= state.currentNode();

            // Calculate the shortest path to exit and remove the block we're on
            List<Node> path_seq= Path.shortest(whereimat, state.getExit());
            path_seq.remove(0);

            // if we have enough steps, continue gold hunt
            if (path_seq.size() > state.stepsLeft()) {

                // if there's gold on this block, get shortest path to it
                if (block.getTile().gold() != 0) {
                    // shortest path to gold
                    List<Node> path_gold= Path.shortest(whereimat, block);
                    path_gold.remove(0);
                    // shortest path from gold to exit
                    List<Node> path_seqGold= Path.shortest(block, state.getExit());
                    path_seqGold.remove(0);

                    // if path from gold to exit is within steps left, do it.
                    if (path_gold.size() + path_seqGold.size() < state.stepsLeft() + 3) {
                        // execute path to gold
                        for (Node step : path_gold) {
                            state.moveTo(step);
                        }
                        // execute path to exit
                        for (Node next : path_seqGold) {
                            state.moveTo(next);
                        }
                    }
                }
                // if we dont have enough steps just leave
            } else {
                for (Node reg : path_seq) {
                    state.moveTo(reg);
                }
            }
        }
    }

    /** Method to make Pollack leave to the exit using the refined shortest path algorithm. This
     * method will take in a single paramater state, of type ScramState which will be used to move
     * Pollack, get the exit location, etc **/
    public void ightImmaHeadOut(ScramState state) {
        // Get Nodes we are on, and want to travel to
        Node here= state.currentNode();
        Node destination= state.getExit();

        // Calculate the shortest path algorithm and remove the block we're on
        List<Node> path_seq= Path.shortest(here, destination);
        path_seq.remove(0);
        int size= path_seq.size();

        // Move to the block in the shortest path algo
        for (Node block : path_seq) {
            if (!state.currentNode().equals(block)) {
                state.moveTo(block);
                size-= 1;
                // if gold on any neighbors grab it if have enough steps left
                if (state.stepsLeft() > size + 10) {
                    getTheBag1(block, state, path_seq);
                }
            }
        }
    }

    /** A method to deviate along the shortest path algorithm to get gold on the neighbor blocks if
     * its there. **/
    public void getTheBag1(Node block, ScramState state, List<Node> path_seq) {
        // Get neighbors of block
        Set<Node> neigh= block.getNeighbors();

        // for each neighbor, get tile...
        for (Node surrounds : neigh) {
            Tile til= surrounds.getTile();
            // if tile has gold then we can move to its node
            if (til.gold() != 0) {
                state.moveTo(surrounds);
                // move back to original node after
                int num= path_seq.indexOf(block);
                // If next block was one we are at..
                if (path_seq.get(num + 1) != null &&
                    path_seq.get(num + 1).equals(surrounds)) {
                    break;
                } else {
                    state.moveTo(block);
                }
            }
        }
    }

    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as <br>
     * a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * state.currentLoc(), state.neighbors(), and state.distanceToOrb() in FindState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function state.moveTo(long id) in FindState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void findOrb(FindState state) {
        // TODO 1: Get the orb
        HashSet<Long> visits= new HashSet<>();
        walkPollack(state, visits);
    }

    /** Pres Pollack is standing at a node given by parameter state.<br>
     *
     * Get out of the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before time runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through parameter state. <br>
     * state.currentNode() and state.getExit() will return Node objects of interest, and <br>
     * state.allNodes() will return a collection of all nodes on the graph.
     *
     * The cavern will collapse in the number of steps given by <br>
     * state.stepsLeft(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use state.stepsLeft() to get the time still remaining, <br>
     * Use state.moveTo() to move to a destination node adjacent to your current node.<br>
     * Do not call state.grabGold(). Gold on a node is automatically picked up <br>
     * when the node is reached.<br>
     *
     * The method must return from this function while standing at the exit. <br>
     * Failing to do so before time runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough time to scram using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using the shortest path method to calculate the shortest <br>
     * path to the exit is a good starting solution */
    @Override
    public void scram(ScramState state) {
        // TODO 2: scram
        ightImmaDip(state);
        if (state.currentNode().equals(state.getExit())) { return; }
    }
}
