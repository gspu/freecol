/**
 *  Copyright (C) 2002-2015   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.i18n.Messages;
import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;


/**
 * A named region on the map.
 */
public class Region extends FreeColGameObject implements Nameable, Named {

    private static final Logger logger = Logger.getLogger(Region.class.getName());

    public static final String PACIFIC_NAME_KEY = "model.region.pacific";

    public static enum RegionType implements Named {
        OCEAN,
        COAST,
        LAKE,
        RIVER,
        LAND,
        MOUNTAIN,
        DESERT;

        /**
         * Get a stem key for this region type.
         *
         * @return A stem key.
         */
        public String getKey() {
            return toString().toLowerCase(Locale.US);
        }

        /**
         * Gets a name index key for this region type.
         *
         * @return A name index key.
         */
        public String getNameIndexKey() {
            return "index." + getKey();
        }

        /**
         * Gets a message key for an unknown region of this type.
         *
         * @return A message key.
         */
        public String getUnknownKey() {
            return "model.region." + getKey() + ".unknown";
        }

        // Interface Named

        /**
         * {@inheritDoc}
         */
        public String getNameKey() {
            return Messages.nameKey("model.region." + getKey());
        }
    }

    /** The name of this Region. */
    protected String name;

    /** The type of region. */
    protected RegionType type;

    /** Key used to retrieve description from Messages. */
    protected String nameKey;

    /** The parent Region of this Region. */
    protected Region parent;

    /**
     * Whether this Region is claimable.  Ocean Regions and non-leaf
     * Regions should not be claimable.
     */
    protected boolean claimable = false;

    /**
     * Whether this Region is discoverable.  The Eastern Ocean regions
     * should not be discoverable.  In general, non-leaf regions should
     * not be discoverable.  The Pacific Ocean is an exception however,
     * unless players start there.
     */
    protected boolean discoverable = false;

    /** Which Turn the Region was discovered in. */
    protected Turn discoveredIn;

    /** Which Player the Region was discovered by. */
    protected Player discoveredBy;

    /**
     * Identifier for the unit the region was discovered by.   Using
     * an identifier as units may subsequently die.
     */
    protected String discoverer;

    /** Whether the Region is already discovered when the game starts. */
    protected boolean prediscovered = false;

    /**
     * How much discovering this Region contributes to your score.
     * This should be zero unless the Region is discoverable.
     */
    protected int scoreValue = 0;

    /** The child Regions of this Region. */
    protected List<Region> children = null;


    /**
     * Creates a new <code>Region</code> instance.
     *
     * @param game The enclosing <code>Game</code>.
     */
    public Region(Game game) {
        super(game);
    }

    /**
     * Creates a new <code>Region</code> instance.
     *
     * @param game The enclosing <code>Game</code>.
     * @param id The object identifier.
     */
    public Region(Game game, String id) {
        super(game, id);
    }


    /**
     * Is this region the Pacific Ocean?
     *
     * The Pacific Ocean is special in that it is the only Region that
     * could be discovered in the original game.
     *
     * @return True if this region is the Pacific.
     */
    public boolean isPacific() {
        return PACIFIC_NAME_KEY.equals(this.nameKey)
            || (this.parent != null && this.parent.isPacific());
    }

    /**
     * Gets the name or default name of this Region.
     *
     * @return The i18n-ready name for the region.
     */
    public StringTemplate getLabel() {
        return (prediscovered || isPacific()) ? StringTemplate.key(nameKey)
            : (name == null) ? StringTemplate.key(type.getUnknownKey())
            : StringTemplate.name(name);
    }

    /**
     * Gets the type of the region.
     *
     * @return The region type.
     */
    public final RegionType getType() {
        return this.type;
    }

    /**
     * Gets the enclosing parent region.
     *
     * @return The parent region
     */
    public final Region getParent() {
        return this.parent;
    }

    /**
     * Sets the parent region.
     *
     * @param newParent The new parent region.
     */
    public final void setParent(final Region newParent) {
        this.parent = newParent;
    }

    /**
     * Get a list of the child regions.
     *
     * @return The child regions.
     */
    public final List<Region> getChildren() {
        return (this.children == null) ? Collections.<Region>emptyList()
            : this.children;
    }

    /**
     * Sets the child regions.
     *
     * @param newChildren The new child regions.
     */
    public final void setChildren(final List<Region> newChildren) {
        this.children = newChildren;
    }

    /**
     * Add a child region to this region.
     *
     * @param child The child <code>Region</code> to add.
     */
    public void addChild(Region child) {
        if (this.children == null) this.children = new ArrayList<>();
        this.children.add(child);
    }

    /**
     * Is this a leaf region?
     *
     * @return True if the region has no children.
     */
    public boolean isLeaf() {
        return this.children == null || this.children.isEmpty();
    }

    /**
     * Can this region be claimed?
     *
     * @return True if the region can be claimed.
     */
    public final boolean isClaimable() {
        return this.claimable;
    }

    /**
     * Set the claimable value.
     *
     * @param newClaimable The new claimable value.
     */
    public final void setClaimable(final boolean newClaimable) {
        this.claimable = newClaimable;
    }

    /**
     * Can this region be discovered?
     *
     * @return True if the region can be discovered.
     */
    public final boolean isDiscoverable() {
        return this.discoverable;
    }

    /**
     * Set the discoverable value.
     *
     * @param newDiscoverable The new discoverable value.
     */
    public final void setDiscoverable(final boolean newDiscoverable) {
        this.discoverable = newDiscoverable;
        if (this.discoverable) this.prediscovered = false;
    }

    /**
     * Get the identifier for the unit that discovered the region.
     *
     * @return The unit identifier, or null if none yet.
     */
    public String getDiscoverer() {
        return this.discoverer;
    }

    /**
     * Set the identifier for the unit the discovered the region.
     *
     * @param discoverer The unit identifier to set.
     */
    public void setDiscoverer(String discoverer) {
        this.discoverer = discoverer;
    }

    /**
     * Gets a discoverable Region or null.  If this region is
     * discoverable, it is returned.  If not, a discoverable parent is
     * returned, unless there is none.  This is intended for
     * discovering the Pacific Ocean when discovering one of its
     * sub-Regions.
     *
     * @return A discoverable a region, or null if none found.
     */
    public Region getDiscoverableRegion() {
        return (isDiscoverable()) ? this
            : (getParent() != null) ? getParent().getDiscoverableRegion()
            : null;
    }

    /**
     * Gets the turn the region was discovered in.
     *
     * @return The discovery turn.
     */
    public final Turn getDiscoveredIn() {
        return this.discoveredIn;
    }

    /**
     * Sets the discovery turn.
     *
     * @param newDiscoveredIn The new discoveredy turn.
     */
    public final void setDiscoveredIn(final Turn newDiscoveredIn) {
        this.discoveredIn = newDiscoveredIn;
    }

    /**
     * Gets the player that discovered the region.
     *
     * @return The discovering <code>Player</code>.
     */
    public final Player getDiscoveredBy() {
        return this.discoveredBy;
    }

    /**
     * Sets the discovering player.
     *
     * @param newDiscoveredBy The new discovering player.
     */
    public final void setDiscoveredBy(final Player newDiscoveredBy) {
        this.discoveredBy = newDiscoveredBy;
    }

    /**
     * Is the region pre-discovered (e.g. the Atlantic).
     *
     * @return True if the region is prediscovered.
     */
    public final boolean isPrediscovered() {
        return this.prediscovered;
    }

    /**
     * Sets the prediscovered value.
     *
     * @param newPrediscovered The new prediscovered value.
     */
    public final void setPrediscovered(final boolean newPrediscovered) {
        this.prediscovered = newPrediscovered;
    }

    /**
     * Discover this region.
     *
     * @param unit The discovering <code>Unit</code>.
     * @param turn The discovery <code>Turn</code>.
     * @param newName The name of the region.
     * @return A <code>HistoryEvent</code> documenting the discovery.
     */
    public HistoryEvent discover(Unit unit, Turn turn, String newName) {
        final Player player = unit.getOwner();
        if (!isPacific()) this.name = newName;
        this.discoveredBy = player;
        this.discoveredIn = turn;
        this.discoverable = false;
        for (Region r : getChildren()) r.setDiscoverable(false);
        int score = (isDiscoverable()
            && getSpecification().getBoolean(GameOptions.EXPLORATION_POINTS)) 
            ? getScoreValue()
            : 0;
        HistoryEvent h = new HistoryEvent(turn,
            HistoryEvent.EventType.DISCOVER_REGION, player)
                .addStringTemplate("%nation%", player.getNationName())
                .addName("%region%", newName);
        h.setScore(getScoreValue());
        return h;
    }

    /**
     * Gets the score for discovering this region.
     *
     * @return The score.
     */
    public final int getScoreValue() {
        return this.scoreValue;
    }

    /**
     * Sets the score for discovering this region.
     *
     * @param newScoreValue The new score.
     */
    public final void setScoreValue(final int newScoreValue) {
        this.scoreValue = newScoreValue;
    }


    // Implement Nameable

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(final String newName) {
        this.name = newName;
    }


    // Implement Named
    
    /**
     * {@inheritDoc}
     */
    public final String getNameKey() {
        return this.nameKey;
    }


    // Serialization

    private static final String CHILD_TAG = "child";
    private static final String CLAIMABLE_TAG = "claimable";
    private static final String DISCOVERABLE_TAG = "discoverable";
    private static final String DISCOVERED_BY_TAG = "discoveredBy";
    private static final String DISCOVERED_IN_TAG = "discoveredIn";
    private static final String NAME_TAG = "name";
    private static final String NAME_KEY_TAG = "nameKey";
    private static final String PARENT_TAG = "parent";
    private static final String PREDISCOVERED_TAG = "prediscovered";
    private static final String SCORE_VALUE_TAG = "scoreValue";
    private static final String TYPE_TAG = "type";
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        if (name != null) {
            xw.writeAttribute(NAME_TAG, name);
        }

        xw.writeAttribute(NAME_KEY_TAG, nameKey);

        xw.writeAttribute(TYPE_TAG, type);

        xw.writeAttribute(PREDISCOVERED_TAG, prediscovered);

        xw.writeAttribute(CLAIMABLE_TAG, claimable);

        xw.writeAttribute(DISCOVERABLE_TAG, discoverable);

        if (parent != null) {
            xw.writeAttribute(PARENT_TAG, parent);
        }

        if (discoveredIn != null) {
            xw.writeAttribute(DISCOVERED_IN_TAG, discoveredIn.getNumber());
        }

        if (discoveredBy != null) {
            xw.writeAttribute(DISCOVERED_BY_TAG, discoveredBy);
        }

        if (scoreValue > 0) {
            xw.writeAttribute(SCORE_VALUE_TAG, scoreValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeChildren(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeChildren(xw);

        for (Region child : getChildren()) {

            xw.writeStartElement(CHILD_TAG);

            xw.writeAttribute(ID_ATTRIBUTE_TAG, child);

            xw.writeEndElement();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        name = xr.getAttribute(NAME_TAG, (String)null);

        nameKey = xr.getAttribute(NAME_KEY_TAG, (String)null);

        type = xr.getAttribute(TYPE_TAG, RegionType.class, (RegionType)null);

        claimable = xr.getAttribute(CLAIMABLE_TAG, false);

        discoverable = xr.getAttribute(DISCOVERABLE_TAG, false);

        prediscovered = xr.getAttribute(PREDISCOVERED_TAG, false);

        scoreValue = xr.getAttribute(SCORE_VALUE_TAG, 0);

        int turn = xr.getAttribute(DISCOVERED_IN_TAG, UNDEFINED);
        discoveredIn = (turn == UNDEFINED) ? null : new Turn(turn);

        discoveredBy = xr.findFreeColGameObject(getGame(), DISCOVERED_BY_TAG,
            Player.class, (Player)null, false);

        parent = xr.makeFreeColGameObject(getGame(), PARENT_TAG,
                                          Region.class, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readChildren(FreeColXMLReader xr) throws XMLStreamException {
        // Clear containers.
        children = null;

        super.readChildren(xr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readChild(FreeColXMLReader xr) throws XMLStreamException {
        final String tag = xr.getLocalName();

        if (CHILD_TAG.equals(tag)) {
            addChild(xr.makeFreeColGameObject(getGame(), ID_ATTRIBUTE_TAG,
                                              Region.class, true));
            xr.closeTag(CHILD_TAG);
        
        } else {
            super.readChild(xr);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append("[").append(getId())
            .append(" ").append((name == null) ? "(null)" : name)
            .append(" ").append(nameKey).append(" ").append(type)
            .append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getXMLTagName() { return getXMLElementTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "region".
     */
    public static String getXMLElementTagName() {
        return "region";
    }
}
