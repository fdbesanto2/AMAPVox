/*
 * Copyright (C) 2006-2009 Sebastien Griffon
 * 
 * This file is part of Jeeb.
 * 
 * Sketch is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Sketch is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with Sketch. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package fr.ird.jeeb.lib.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.ird.jeeb.lib.defaulttype.Item;
import fr.ird.jeeb.lib.defaulttype.Type;
import fr.ird.jeeb.lib.structure.NodeAttribute.AttributeType;
import fr.ird.jeeb.lib.structure.conditions.ArchiCondition;
import fr.ird.jeeb.lib.structure.conditions.EqualScaleCondition;
import fr.ird.jeeb.lib.structure.geometry.mesh.SimpleMesh;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3d;
import fr.ird.jeeb.lib.structure.iterators.ArchiTreePrefixIterator;
import fr.ird.jeeb.lib.structure.visitors.NodeVisitor;
import fr.ird.jeeb.lib.util.Log;
import fr.ird.jeeb.lib.util.TicketDispenser;
import fr.ird.jeeb.lib.util.Vertex3d;

/**
 * 
 * @author griffon November 08
 * 
 */
public class ArchiNode implements Item, Serializable, Comparable {

	// GEOMETRY ATTRIBUTES

	public static String defaultLengthAttribute = "Length";
	public static String defaultWidthAttribute = "Width";

	public static String defaultTopWidthAttribute = "TopWidth";
	public static String defaultTopHeightAttribute = "TopHeight";

	public static String defaultGeometricalConstraintAttribute = "GeometricalConstraint";

	public static String defaultPlantXAttribute = "PlantX";
	public static String defaultPlantYAttribute = "PlantY";
	public static String defaultPlantZAttribute = "PlantZ";

	public static String defaultPlantInclinationAzimutAttribute = "PlantInclinationAzimut";
	public static String defaultPlantInclinationAngleAttribute = "PlantInclinationAngle";
	public static String defaultPlantStemTwistAttribute = "PlantStemTwist";

	public static String defaultXXAttribute = "XX";
	public static String defaultYYAttribute = "YY";
	public static String defaultZZAttribute = "ZZ";

	public static String defaultRotBearerXAttribute = "XInsertionAngle";
	public static String defaultRotBearerYAttribute = "YInsertionAngle";
	public static String defaultRotBearerZAttribute = "ZInsertionAngle";

	public static String defaultRotLocalXAttribute = "XEuler";
	public static String defaultRotLocalYAttribute = "YEuler";
	public static String defaultRotLocalZAttribute = "ZEuler";

	public static String defaultAzimuthAttribute = "Azimuth";
	public static String defaultElevationAttribute = "Elevation";

	public static String defaultRotHorizontalPlaneAttribute = "HorizontalAngle";

	public static String defaultPhyllotaxyAttribute = "Phyllotaxy";
	public static String defaultPlagiotropyAttribute = "Plagiotropy";
	public static String defaultOrthotropyAttribute = "Orthotropy";

	public static String defaultInsertionAttribute = "Insertion";

	public static String defaultNormalUpAttribute = "NormalUp";
	public static String defaultOrientationResetAttribute = "OrientationReset";
	public static String defaultOffsetAttribute = "Offset";
	public static String defaultBorderOffsetAttribute = "BorderOffset";
	public static String defaultHeightAttribute = "Height";
	public static String defaultStiffnessAttribute = "Stifness";
	public static String defaultTaperingAttribute = "StifnessTapering";

	public static String defaultStiffnessApplyAttribute = "StiffnessApply";
	public static String defaultStiffnessStraighteningAttribute = "StiffnessStraightening";

	// TOPOLOGY ATTRIBUTES
	public static String defaultRelayAttribute = "Relay";
	public static String defaultDeadAttribute = "Dead";

	// OTHER ATTRIBUTES
	public static String defaultNameAttribute = "Name";
	public static String defaultColorAttribute = "Color";
	public static String defaultImageAttribute = "Image";
	public static String defaultIndexAttribute = "Index";
	public static String defaultGroupAttribute = "Group";

	protected enum LinkType {
		RAMIFICATION, SUCCESSION, DECOMPOSITION;
	}

	protected static class GroupAttribute implements Serializable {
		public TicketDispenser idDispenser = new TicketDispenser();

		// public int groupId;
		public String toString() {
			return "" + idDispenser.getCurrentValue();
			// return null;
		}

	}

	public class LinkInfo {

		LinkType link;
		int index;

		public LinkInfo(LinkType link, int index) {
			this.link = link;
			this.index = index;
		}

		/**
		 * @return the link
		 */
		public LinkType getLink() {
			return link;
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}

	}

	static private final long serialVersionUID = 1L;

	/** The ArchiNode geometry (position, orientation, ...) */
	protected SimpleGeometry geometry;

	/** The unique identifiant of this ArchiNode in the ArchiTree */
	protected int id = -1;

	/** The unique identifiant of this ArchiNode in the ArchiModel */
	protected int itemId = -1;

	/** The proxy to add/remove some complex/component */
	protected ComplexProxy compositionProxy;
	protected ComplexProxy complexProxy;

	/** The current predecessor/successor */
	private ArchiNode successor;
	private ArchiNode ancestor;

	/** The proxy to add/remove some ramification/bearer */
	protected RamificationProxy ramificationProxy;
	protected RamificationProxy[] bearerProxy;

	public HashMap<String, NodeAttribute> attributes = null;

	/** The type of this ArchiNode */
	protected ArchiType type;

	public ArchiNode() {

		this.type = ArchiType.DEFAULT;
	}

	public ArchiNode(ArchiType type) {

		this.type = type;
	}

	public ArchiNode(int id) {
		this.id = id;
		this.type = ArchiType.DEFAULT;

	}

	public ArchiNode(int id, ArchiType type) {
		this.id = id;
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setItemId(int id) {
		this.itemId = id;
	}

	@Override
	public int getItemId() {
		return itemId;
	}

	// /**
	// * Return true if this ArchiNode is a group (eg. a plant, a scene)
	// */
	// public boolean isGroupNode() {
	// if (attributes != null) {
	// return attributes.containsKey(ArchiNode.defaultGroupAttribute);
	// }
	// return false;
	// }
	//
	// /**
	// * Set this node as a group. Create a Group attribute.
	// */
	// public void setGroupNode() {
	// GroupAttribute groupAtt = new GroupAttribute();
	// set(ArchiNode.defaultGroupAttribute, groupAtt);
	// }
	//
	/**
	 * Insert a successor between this node and the current successor
	 */
	public void insertSuccessor(ArchiNode successor) throws ScaleException {

		if (successor != null) {
			ArchiNode currentSuccessor = this.getLinkedSuccessor();
			this.addSuccessor(successor);
			if (currentSuccessor != null)
				successor.addSuccessor(currentSuccessor);
		}

	}

	/**
	 * Insert a predecessor between this node and the current predecessor
	 */
	public void insertPredecessor(ArchiNode predecessor) throws ScaleException {
		if (hasLinkedComplex()) {
			getLinkedComplex().insertComponent(predecessor);
		} else if (hasLinkedBearer()) {

			getLinkedBearer().insertRamification(predecessor, 0);
			getLinkedBearer().unlinkRamification(this);
			predecessor.insertSuccessor(this);
		} else {
			ancestor.insertSuccessor(predecessor);
		}

	}

	/**
	 * Insert a component
	 */
	public void insertComponent(ArchiNode component) throws ScaleException {

		List<ArchiNode> currentComponents = this.getLinkedComponents();
		if (component != null) {

			if (currentComponents == null) {
				currentComponents = new ArrayList<ArchiNode>();
				currentComponents.add(component);
			} else
				currentComponents.add(0, component);

			unlinkComponents();
			this.addComponents(currentComponents);
		}

	}

	/**
	 * Insert a component
	 */
	public void insertRamification(ArchiNode ramification, int index) throws ScaleException {

		List<ArchiNode> currentRamifications = this.getLinkedRamifications();
		if (ramification != null) {
			currentRamifications.add(index, ramification);
			this.addRamifications(currentRamifications);
		}

	}

	/**************************************************************/
	/**************** SETTER AND LINKER ***************************/
	/**************************************************************/

	/**
	 * Add a new component node at the end of the current components list
	 * 
	 * @param node
	 * @throws Exception
	 */
	public void addComponent(ArchiNode node) throws ScaleException {

		if (node == null)
			return;

		if (node.getScale() <= this.getScale())
			throw new ScaleException(" Composition scale conflict : " + this.getType().toString() + " >= "
					+ node.getType().toString());

		if (compositionProxy == null) {
			createDefaultComplexProxy();
		}

		compositionProxy.linkComponent(this, node);

	}

	/**
	 * Add a new component node at the end of the current components list
	 * 
	 * @param node
	 * @throws Exception
	 */
	public void addComponent(ArchiNode node, boolean componentSuccession) throws ScaleException {

		if (componentSuccession)
			addComponent(node);
		else {
			if (node == null)
				return;

			if (node.getScale() <= this.getScale())
				throw new ScaleException(" Composition scale conflict : " + this.getType().toString() + " >= "
						+ node.getType().toString());

			if (compositionProxy == null) {
				createNoSuccessionComplexProxy();
			} else if (compositionProxy instanceof DefaultComplexProxyImpl) {
				ArchiNode comp = compositionProxy.getFirstComponent();
				createNoSuccessionComplexProxy();
				compositionProxy.linkComponent(this, comp);
			}

			compositionProxy.linkComponent(this, node);

		}
	}

	/**
	 * Add a new component node at the end of the current components list
	 * 
	 * @param node
	 * @throws Exception
	 */
	public void addComponents(List<ArchiNode> nodes) throws ScaleException {

		if (nodes == null)
			return;

		if (nodes.iterator().next().getScale() <= this.getScale())
			throw new ScaleException(" Composition scale conflict : " + this.getType().toString() + " >= "
					+ nodes.iterator().next().getType().toString());

		if (compositionProxy == null) {
			createDefaultComplexProxy();
		}

		compositionProxy.linkComponents(this, nodes);

	}

	/**
	 * Add a new ramification node to the bearer node
	 * 
	 * @param son
	 *            the node to add
	 */
	public void addRamification(ArchiNode son) {

		if (son == null)
			return;

		if (ramificationProxy == null) {
			createDefaultRamificationProxy();
		}
		ramificationProxy.linkRamification(this, son);

	}

	/**
	 * Add a new ramification node to the bearer node
	 * 
	 * @param son
	 *            the node to add
	 */
	public void addRamifications(List<ArchiNode> sons) {

		if (sons == null)
			return;

		if (ramificationProxy == null) {
			createDefaultRamificationProxy();
		}
		ramificationProxy.linkRamifications(this, sons);

	}

	public void addSuccessor(ArchiNode successor) throws ScaleException {

		this.successor = successor;

		if (successor == null)
			return;

		if (successor.getScale() != this.getScale())
			throw new ScaleException(" Succession scale conflict : " + this.getType().toString() + " != "
					+ successor.getType().toString());

		this.successor.ancestor = this;
	}

	public void unlinkComponents() {
		if (hasLinkedComponents()) {
			compositionProxy.unlinkComponents(this);
		}

	}

	public void unlinkComponent(ArchiNode component) {
		if (hasLinkedComponents()) {
			compositionProxy.unlinkComponent(this, component);
		}
	}

	public void unlinkRamifications() {
		if (hasLinkedRamifications()) {
			ramificationProxy.unlinkRamifications(this);
		}
	}

	public void unlinkRamification(ArchiNode ramification) {
		if (hasLinkedRamifications()) {
			ramificationProxy.unlinkRamification(this, ramification);
		}
	}

	public void unlinkSuccessor() {
		if (successor == null)
			return;
		successor.ancestor = null;
		successor = null;
	}

	/**
	 * Remove the previous node whatever his relation
	 */
	public void unlinkPrevious() throws ScaleException {

		ArchiNode previousNode = null;
		previousNode = this.getLinkedPredecessor();
		if (previousNode != null) {
			previousNode.unlinkSuccessor();
		} else {
			previousNode = this.getLinkedComplex();
			if (previousNode != null) {
				previousNode.unlinkComponent(this);
			} else {
				previousNode = this.getLinkedBearer();
				if (previousNode != null) {
					previousNode.unlinkRamification(this);
				}
			}
		}

	}

	/**
	 * @param
	 * @return the actually removed nodes
	 * @throws Exception
	 */
	public Collection<ArchiNode> selfPrune() throws ScaleException {

		Collection<ArchiNode> descendants = getDescendantsComplex();
		unlinkPrevious();
		return descendants;
	}

	// return the actually removed nodes
	public void selfRemove() {

		try {

			ArchiNode predecessor = getLinkedPredecessor();
			ArchiNode successor = getLinkedSuccessor();
			ArchiNode bearer = getLinkedBearer();
			ArchiNode complex = getLinkedComplex();

			this.unlinkSuccessor();

			if (predecessor != null) {
				predecessor.unlinkSuccessor();
				predecessor.addSuccessor(successor);
			}

			if (complex != null) {
				complex.unlinkComponent(this);
				complex.addComponent(successor);
			}

			if (bearer != null)
				bearer.unlinkRamification(this);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private void createDefaultRamificationProxy() {
		this.ramificationProxy = new DefaultRamificationProxyImpl();
	}

	private void createDefaultComplexProxy() {
		this.compositionProxy = new DefaultComplexProxyImpl();
	}

	private void createNoSuccessionComplexProxy() {
		this.compositionProxy = new NoSuccesionComplexProxyImpl();
	}

	public RamificationProxy getRamificationProxy() {

		return ramificationProxy;
	}

	public ComplexProxy getCompositionProxy() {

		return compositionProxy;
	}

	public RamificationProxy[] getBearerProxy() {

		return bearerProxy;
	}

	public ComplexProxy getComplexProxy() {

		return complexProxy;
	}

	protected void setCompositionProxy(ComplexProxy multiScaler) {
		this.compositionProxy = multiScaler;
	}

	protected void setRamificationProxy(RamificationProxy ramificator) {
		this.ramificationProxy = ramificator;
	}

	protected void setComplexProxy(ComplexProxy multiScaler) {
		this.complexProxy = multiScaler;
	}

	protected void addBearerProxy(RamificationProxy ramificator) {
		if (bearerProxy == null)
			bearerProxy = new RamificationProxy[1];
		else {
			bearerProxy = Arrays.copyOf(bearerProxy, bearerProxy.length + 1);
		}
		this.bearerProxy[bearerProxy.length - 1] = ramificator;
	}

	protected void removeBearerProxy(RamificationProxy ramificator) {
		if (bearerProxy != null && bearerProxy.length == 1)
			bearerProxy = null;
		else {

			List<RamificationProxy> bearersList = Arrays.asList(bearerProxy);
			bearersList.remove(ramificator);
			bearersList.toArray(bearerProxy);
		}

	}

	/**************************************************************/
	/**************** GETTER AND SEEKER ***************************/
	/**************************************************************/

	public boolean hasLinkedBearer() {
		return bearerProxy != null;
	}

	public boolean hasLinkedRamifications() {
		return ramificationProxy != null;
	}

	public boolean hasLinkedComplex() {
		return complexProxy != null;
	}

	public boolean hasLinkedComponents() {
		return compositionProxy != null;
	}

	public ArchiNode getLinkedBearer() {
		return hasLinkedBearer() ? bearerProxy[0].getBearer() : null;
	}

	public List<ArchiNode> getLinkedBearers() {
		if (!hasLinkedBearer())
			return null;
		ArrayList<ArchiNode> bearers = new ArrayList<ArchiNode>();
		for (int i = 0; i < bearerProxy.length; i++) {
			bearers.add(bearerProxy[i].getBearer());
		}
		return bearers;
	}

	public List<ArchiNode> getLinkedRamifications() {
		return hasLinkedRamifications() ? ramificationProxy.getRamifications() : null;
	}

	public ArchiNode getLinkedSuccessor() {
		return successor;
	}

	public ArchiNode getLinkedPredecessor() {
		return ancestor;
	}

	public ArchiNode getLinkedComplex() {
		return hasLinkedComplex() ? complexProxy.getComplex() : null;
	}

	public List<ArchiNode> getLinkedComponents() {
		return hasLinkedComponents() ? compositionProxy.getComponents() : null;
	}

	public ArchiNode getLinkedComponent(int index) throws ScaleException {
		return hasLinkedComponents() ? compositionProxy.getComponentAt(index) : null;
	}

	public int getLinkedComponentsNumber() {
		Collection<ArchiNode> components = getLinkedComponents();
		if (components != null)
			return components.size();
		return 0;
	}

	public ArchiNode getFirstComponent() {
		return hasLinkedComponents() ? compositionProxy.getFirstComponent() : null;
	}

	public ArchiNode getLastComponent() {
		return hasLinkedComponents() ? compositionProxy.getLastComponent() : null;
	}

	// Return components that are formally linked with the complex
	public List<ArchiNode> getRootComponents() {
		if (compositionProxy != null) {
			if (compositionProxy instanceof NoSuccesionComplexProxyImpl) {
				return compositionProxy.getComponents();
			} else
				return Arrays.asList(compositionProxy.getFirstComponent());
		}
		return null;
	}

	public ArchiNode getFirstComponent(ArchiCondition condition) {
		ArchiNode lDec = getFirstComponent();
		while (lDec != null && !condition.isCorrect(lDec)) {
			lDec = lDec.getFirstComponent(condition);
		}
		return lDec;
	}

	public boolean isRoot() {
		return getLinkedBearer() == null && getLinkedPredecessor() == null && getLinkedComplex() == null;
	}

	/**
	 * @return true if this node or his components are linked to ramifications
	 */
	public boolean hasRamifications() {

		if (hasLinkedRamifications())
			return true;
		Collection<ArchiNode> childrenNode;
		try {
			childrenNode = getLinkedComponents();
			if (childrenNode != null) {
				for (ArchiNode node : childrenNode) {
					if (node.hasRamifications())
						return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Find the first the component elements for every scale that validate the
	 * condition
	 * 
	 * @return the first components for the given condition
	 * @throws Exception
	 */
	public ArchiNode findComponents(ArchiCondition condition) throws ScaleException {

		ArrayList<ArchiNode> currentDecompositions = (ArrayList<ArchiNode>) getLinkedComponents();

		if (currentDecompositions != null && !currentDecompositions.isEmpty()) {

			for (ArchiNode node : currentDecompositions) {

				if (condition.isCorrect(node)) {
					return node;
				}

				ArchiNode tmpDecomposition = node.findComponents(condition);
				if (tmpDecomposition != null) {
					return tmpDecomposition;
				}

			}
		}

		return null;

	}

	/**
	 * Get bearer ArchiNode of the current node for the given order *
	 * 
	 * @return the bearer ArchiNode of the current node, null otherwise
	 * @throws Exception
	 */
	public ArchiNode getBearerAtOrder(int order) {

		if (order >= this.getOrder())
			return null;

		ArchiNode bearer = getBearer();

		while (bearer != null && bearer.getOrder() != order) {
			bearer = bearer.getBearer();
		}

		return bearer;

	}

	/**
	 * Get bearer ArchiNode of the current node for the given order *
	 * 
	 * @return the bearer ArchiNode of the current node, null otherwise
	 * @throws Exception
	 */
	public ArchiNode getBearerAtScale(int scale) {

		ArchiNode bearer = getBearer();
		return bearer.getLinkedNode(scale);

	}

	/**
	 * Get scale ArchiNode of the current node for the given scale *
	 * 
	 * @return the complex or component ArchiNode of the current node, null
	 *         otherwise
	 * @throws Exception
	 */
	public ArchiNode getLinkedNode(int scale) {

		short tmpScale = this.getScale();

		if (tmpScale == scale)
			return this;

		if (tmpScale > scale) {
			return getComplex(new EqualScaleCondition(scale));
		} else {
			return getLastComponent(new EqualScaleCondition(scale));
		}

	}

	/**
	 * Get the first found bearer ArchiNode of the current node
	 * 
	 * @warning the bearer can be in any scale
	 * @return the bearer ArchiNode of the current node, null otherwise
	 * @throws Exception
	 */
	public ArchiNode getBearer() {

		if (getLinkedBearer() != null) {
			return getLinkedBearer();
		} else {
			ArchiNode container = getComplex();
			if (container != null) {
				return container.getBearer();
			} else {
				if (ancestor != null)
					return ancestor.getBearer();
				else
					return null;
			}
		}

	}

	/**
	 * Get the firsts found bearers ArchiNode of the current node
	 * 
	 * @warning the bearers can be in any scale
	 * @return the bearers list of the current node, null otherwise
	 * @throws Exception
	 */
	public List<ArchiNode> getBearers() {
		if (getLinkedBearers() != null) {
			return getLinkedBearers();
		} else {
			ArchiNode container = getComplex();
			if (container != null) {
				return container.getBearers();
			} else {
				return null;
			}
		}

	}

	/**
	 * Get the complex of this node
	 */
	public ArchiNode getComplex() {

		if (getLinkedComplex() != null) {
			return getLinkedComplex();
		}
		// seek predecessor to check if he has a complexProxy
		if (getLinkedPredecessor() != null) {
			return getLinkedPredecessor().getComplex();
		}

		return null;

	}

	/**
	 * Get the top complex of this node
	 */
	public ArchiNode getComplex(int scale) {

		if (scale >= this.getScale())
			return null;

		ArchiNode container = this.getComplex();
		while (container != null && container.getScale() != scale) {
			// scale of the container is the asked one
			container = container.getComplex();
		}

		return container;

	}

	/**
	 * Get the top complex of this node
	 */

	public ArchiNode getAxis() {

		ArchiNode container = this.getComplex();

		while (container != null && container.getScale() > ArchiType.AXIS.getScale()) {
			// scale of the container is the asked one
			container = container.getComplex();
		}

		return container;

	}

	/**
	 * Find a complex for this node for a given condition
	 * 
	 * @param condition
	 *            the condition {@link ArchiCondition}
	 * @return the complex of this node for the given condition
	 * @throws Exception
	 */
	public ArchiNode getComplex(ArchiCondition condition) {

		ArchiNode container = getComplex();
		while (container != null && !condition.isCorrect(container)) {
			// scale of the container is the asked one
			container = container.getComplex();
		}
		return container;

	}

	/**
	 * Get all the nodes in the subtree rooting from this node
	 * 
	 * @return all the nodes after the current node
	 * @throws Exception
	 */
	public Collection<ArchiNode> getDescendants() throws ScaleException {

		ArrayList<ArchiNode> descendants = new ArrayList<ArchiNode>();
		ArchiTreePrefixIterator it = new ArchiTreePrefixIterator(this);
		while (it.hasNext()) {
			descendants.add(it.next());
		}
		return descendants;
	}

	/**
	 * Get all the nodes in the subtree rooting from this node
	 * 
	 * @param condition
	 *            The condition {@link ArchiCondition}
	 * @return all the nodes after the current node for the given condition
	 * @throws Exception
	 */
	public Collection<ArchiNode> getDescendants(ArchiCondition condition) throws ScaleException {

		ArrayList<ArchiNode> descendants = new ArrayList<ArchiNode>();
		ArchiTreePrefixIterator it = new ArchiTreePrefixIterator(this);
		ArchiNode tmpNode = null;
		while (it.hasNext()) {
			tmpNode = it.next();
			if (condition.isCorrect(tmpNode)) {
				descendants.add(tmpNode);
			}
		}
		return descendants;
	}

	/**
	 * Get all the descendants from this node and his complex/ Set of nodes born
	 * by a node and all his complex
	 * 
	 * @throws Exception
	 */
	public Collection<ArchiNode> getDescendantsComplex() throws ScaleException {
		Collection<ArchiNode> descendants = getDescendants();

		ArchiNode complex = getComplex();
		while (complex != null && complex.getScale() > ArchiType.PLANT.getScale()) {
			ArchiNode successor = complex.getLinkedSuccessor();
			if (successor != null) {
				descendants.addAll(successor.getDescendants());
			}
			complex = complex.getComplex();
		}

		return descendants;
	}

	/**
	 * Get all the composition and ramifications of the given parameter node
	 * 
	 * @param node
	 * @return A collection of nodes
	 */
	public Collection<ArchiNode> getCompositionsAndRamifications() {

		try {
			Collection<ArchiNode> actuallyLinked = new ArrayList<ArchiNode>();
			// actuallyLinked.add(this);
			ArchiNode component = this.getFirstComponent();
			if (component != null)
				actuallyLinked.addAll(component.getDescendants());

			Collection<ArchiNode> ramifications = this.getLinkedRamifications();
			if (ramifications != null && !ramifications.isEmpty()) {
				for (ArchiNode r : ramifications) {
					actuallyLinked.addAll(r.getDescendants());
				}
			}
			return actuallyLinked;
		} catch (Exception e) {
			System.err.println("ArchiTree.getCompositionsAndRamificationsOf error : " + e.getMessage());
			return null;
		}
	}

	public ArchiNode getLastComponent(ArchiCondition condition) {
		ArchiNode lDec = getLastComponent();
		while (lDec != null && !condition.isCorrect(lDec)) {
			lDec = lDec.getLastComponent(condition);
		}
		return lDec;
	}

	public ArchiNode getLastBaseComponent() {
		ArchiNode lDec = getLastComponent();
		if (lDec != null) {
			while (lDec.getLastComponent() != null) {
				lDec = lDec.getLastComponent();
			}
			return lDec;
		}

		return this;
	}

	/**
	 * Get all the component elements for every scale
	 * 
	 * @return All the components of this node
	 * @throws Exception
	 */
	public Collection<ArchiNode> getAllComponents() throws ScaleException {

		return getAllComponents(null);

	}

	/**
	 * Get all the component elements for a given condition
	 * 
	 * @param condition
	 *            The condition {@link ArchiCondition} to accept a node in the
	 *            returned collection
	 * @return The components of this node for the condition
	 * @throws Exception
	 */
	public Collection<ArchiNode> getAllComponents(ArchiCondition condition) throws ScaleException {

		ArrayList<ArchiNode> totalDecompositions = new ArrayList<ArchiNode>();
		ArrayList<ArchiNode> currentDecompositions = (ArrayList<ArchiNode>) getLinkedComponents();

		if (currentDecompositions != null && !currentDecompositions.isEmpty()) {

			for (ArchiNode node : currentDecompositions) {

				if (condition == null || condition.isCorrect(node))
					totalDecompositions.add(node);

				ArrayList<ArchiNode> tmpDecompositions = (ArrayList<ArchiNode>) node.getAllComponents(condition);
				if (tmpDecompositions != null) {
					totalDecompositions.addAll(tmpDecompositions);
				}
			}
			return totalDecompositions;

		} else {
			return totalDecompositions;
		}

	}

	/**
	 * Get the component elements for the finest scale level
	 * 
	 * @return All the components of this node for the finest scale level
	 * @throws Exception
	 */
	public Collection<ArchiNode> getBaseComponents() throws ScaleException {

		ArrayList<ArchiNode> totalDecompositions = new ArrayList<ArchiNode>();
		ArrayList<ArchiNode> currentDecompositions = (ArrayList<ArchiNode>) getLinkedComponents();

		if (currentDecompositions != null && !currentDecompositions.isEmpty()) {
			for (ArchiNode node : currentDecompositions) {

				ArrayList<ArchiNode> tmpDecompositions = (ArrayList<ArchiNode>) node.getBaseComponents();
				if (tmpDecompositions != null) {
					totalDecompositions.addAll(tmpDecompositions);
				} else {
					totalDecompositions.add(node);
				}
			}
			return totalDecompositions;

		} else {
			return null;
		}

	}

	/**
	 * Get all the node directly linked to the current node
	 * 
	 * @return the next directly linked nodes.
	 * @throws Exception
	 */
	public Collection<ArchiNode> getBackwardLinkedNodes() throws ScaleException {
		return getBackwardLinkedNodes(true, true, true);
	}

	/**
	 * Get all the node directly linked to the current node
	 * 
	 * @return the next directly linked nodes.
	 * @throws Exception
	 */
	public Collection<ArchiNode> getBackwardLinkedNodes(boolean lookPredecessor, boolean lookBearer, boolean lookComplex)
			throws ScaleException {

		Collection<ArchiNode> descendants = new ArrayList<ArchiNode>();

		if (lookBearer) {
			ArchiNode bearer = this.getLinkedBearer();
			if (bearer != null) {
				descendants.add(bearer);
			}
		}

		if (lookPredecessor) {
			ArchiNode prede = this.getLinkedPredecessor();
			if (prede != null) {
				descendants.add(prede);
			}
		}

		if (lookComplex) {
			ArchiNode complex = this.getLinkedComplex();
			if (complex != null) {
				descendants.add(complex);
			}
		}

		return descendants;
	}

	/**
	 * Get all the node directly linked to the current node
	 * 
	 * @return the next directly linked nodes.
	 * @throws Exception
	 */
	public Collection<ArchiNode> getForwardLinkedNodes() throws ScaleException {
		return getForwardLinkedNodes(true, true, true);
	}

	/**
	 * Get all the node directly linked to the current node
	 * 
	 * @return the next directly linked nodes.
	 * @throws Exception
	 */
	public Collection<ArchiNode> getForwardLinkedNodes(boolean lookSuccessor, boolean lookRamification,
			boolean lookDecomposition) throws ScaleException {

		Collection<ArchiNode> descendants = new ArrayList<ArchiNode>();

		if (lookDecomposition) {

			List<ArchiNode> components = this.getRootComponents();
			if (components != null) {
				for (ArchiNode c : components) {
					descendants.add(c);
				}
			}
		}

		if (lookRamification) {
			Collection<ArchiNode> ramifications = this.getLinkedRamifications();
			if (ramifications != null && !ramifications.isEmpty()) {
				for (ArchiNode r : ramifications) {
					descendants.add(r);
				}
			}
		}

		if (lookSuccessor) {
			ArchiNode successor = this.getLinkedSuccessor();
			if (successor != null) {
				descendants.add(successor);
			}
		}

		return descendants;
	}

	/**
	 * Get all the node path from the root to the current node
	 */
	public Collection<ArchiNode> getPathFromRoot(ArchiCondition condition) throws ScaleException {
		ArrayList<ArchiNode> path = (ArrayList<ArchiNode>) getPathToRoot(condition);
		Collections.reverse(path);
		return path;

	}

	/**
	 * Get all the node path from the current node to the root
	 */
	public Collection<ArchiNode> getPathToRoot(ArchiCondition condition) throws ScaleException {
		Collection<ArchiNode> path = new ArrayList<ArchiNode>();
		ArchiNode currentNode = this;
		while (currentNode != null) {

			if (condition != null) {
				if (condition.isCorrect(currentNode)) {
					path.add(currentNode);
				}
			} else {

				path.add(currentNode);
			}

			// ArchiNode previousNode = null;
			// previousNode = currentNode.getPredecessorInAxis();
			//
			// if (previousNode == null) {
			// previousNode = currentNode.getBearer();
			// }

			currentNode = currentNode.getPrevious();
			// currentNode = previousNode;
		}
		return path;
	}

	/**
	 * Get all the topological links path from the root to the current node
	 */
	public Collection<LinkInfo> getLinkPath() throws ScaleException {
		ArrayList<LinkInfo> path = new ArrayList<LinkInfo>();
		ArchiNode currentNode = this;
		while (currentNode != null) {

			ArchiNode previousNode = null;
			previousNode = currentNode.getLinkedPredecessor();
			if (previousNode != null) {
				path.add(new LinkInfo(LinkType.SUCCESSION, 0));
			} else {
				previousNode = currentNode.getComplex();
				if (previousNode != null) {
					path.add(new LinkInfo(LinkType.DECOMPOSITION, 0));
				} else {
					previousNode = currentNode.getBearer();

					if (previousNode != null) {
						int index = previousNode.getLinkedRamifications().indexOf(currentNode);
						path.add(new LinkInfo(LinkType.RAMIFICATION, index));

					}
				}

			}
			currentNode = previousNode;
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * Get the current node position in his complex
	 * 
	 * @return the position (start from 0)
	 */
	public int getPositionInComplex() {
		int position = 0;
		try {
			ArchiNode predecessor = this.getLinkedPredecessor();
			while (predecessor != null) {
				position++;
				predecessor = predecessor.getLinkedPredecessor();
			}
			return position;
		} catch (Exception e) {
			System.err.println("getPositionInComplex error : " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Get the current node position in his complex
	 * 
	 * @return the position (start from 0)
	 */
	public int getPositionInComplex(boolean fromBottom) {
		int position = 0;
		try {

			if (fromBottom) {
				ArchiNode predecessor = this.getLinkedPredecessor();
				while (predecessor != null) {
					position++;
					predecessor = predecessor.getLinkedPredecessor();
				}
				return position;
			} else {
				ArchiNode successor = this.getLinkedSuccessor();
				while (successor != null) {
					position++;
					successor = successor.getLinkedSuccessor();
				}
				return position;
			}

		} catch (Exception e) {
			System.err.println("getPositionInComplex error : " + e.getMessage());
			return -1;
		}
	}

	public int getIndex() {

		ArchiNode complex = this.getComplex();
		if (complex != null) {
			return getPositionInComplex() + 1;
		} else {
			ArchiNode bearer = this.getBearer();
			if (bearer != null)
				return bearer.getRamifications().indexOf(this) + 1;
		}
		return 1;
	}

	/**
	 * Get the current node position in his axis
	 * 
	 * @return the position (start from 0)
	 */
	public int getPositionInComplex(int scale) {
		int position = 0;
		try {
			ArchiNode predecessor = this.getPredecessorInComplex(scale);
			while (predecessor != null) {
				position++;
				predecessor = predecessor.getPredecessorInComplex(scale);
			}
			return position;
		} catch (Exception e) {
			System.err.println("getPositionInComplex error : " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Get the current node position in his axis
	 * 
	 * @return the position (start from 0)
	 */
	public int getPositionInComplex(int scale, boolean fromBottom) {
		try {
			int position = 0;
			if (fromBottom) {

				ArchiNode predecessor = this.getPredecessorInComplex(scale);
				while (predecessor != null) {
					position++;
					predecessor = predecessor.getPredecessorInComplex(scale);
				}
				return position;
			} else {
				ArchiNode successor = this.getSuccessorInComplex(scale);
				while (successor != null) {
					position++;
					successor = successor.getSuccessorInComplex(scale);
				}
				return position;
			}
		} catch (Exception e) {
			System.err.println("getPositionInComplex error : " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Get the current node position in his axis
	 * 
	 * @return the position (start from 0)
	 */
	public int getPositionInAxis() {
		return getPositionInComplex(2);
	}

	public int getPositionOnBearer(final String typeName) {

		ArchiNode complexNode = this;
		ArchiNode bearerNode = this.getBearer();

		while (bearerNode != null) {

			if (bearerNode.getType().getTranslatedName().equals(typeName)) {
				return 1;
			} else {
				complexNode = bearerNode.getComplex();
				while (complexNode != null && !complexNode.getType().getTranslatedName().equals(typeName)) {
					complexNode = complexNode.getComplex();
				}

				if (complexNode != null) {
					return bearerNode.getPositionInComplex(complexNode.getScale());
				} else {
					bearerNode = bearerNode.getBearer();
				}
			}
		}

		return -1;

	}

	public int getPositionOnBearerAxis(int order) {

		ArchiNode bearerNode = this.getBearerAtOrder(order);

		if (bearerNode != null) {

			return bearerNode.getPositionInAxis();

		}

		return -1;

	}

	/**
	 * Get the current node position in his axis
	 * 
	 * @return the position (start from 0)
	 */
	public int getPositionInAxis(boolean fromBottom) {
		return getPositionInComplex(2, fromBottom);
	}

	/**
	 * Get all the previous ArchiNodes of the current node in the axe. *
	 * 
	 * @return the previous ArchiNodes of the current node, null otherwise
	 * 
	 */
	public Collection<ArchiNode> getSuccessorsInAxis() {
		ArrayList<ArchiNode> successors = new ArrayList<ArchiNode>();
		ArchiNode succ = this;
		while ((succ = succ.getSuccessorInAxis()) != null) {
			successors.add(succ);
		}
		return successors;

	}

	/**
	 * Get the next ArchiNode of the current node in the axe. *
	 * 
	 * @return the next ArchiNode of the current node, null otherwise
	 * 
	 */
	public ArchiNode getSuccessorInAxis() {
		return getSuccessorInComplex(2);
	}

	/**
	 * Get the next ArchiNode of the current node in the axe. *
	 * 
	 * @return the next ArchiNode of the current node, null otherwise
	 * 
	 */
	public ArchiNode getSuccessorInComplex(int scale) {
		ArchiNode successor = getLinkedSuccessor();
		if (successor != null)
			return successor;
		else {
			ArchiNode container = getComplex();
			EqualScaleCondition condition = new EqualScaleCondition(this.getScale());
			while (container != null && container.getScale() >= scale) {
				ArchiNode successorComplex = container.getLinkedSuccessor();
				if (successorComplex != null) {
					successor = successorComplex.getFirstComponent(condition);
					if (successor != null)
						break;
				}
				container = container.getComplex();
			}

			return successor;
		}
	}

	/**
	 * Get the next ArchiNode of the current node in the axe. *
	 * 
	 * @param distance
	 *            Number of nodes from the current node to the successor
	 * @return the next ArchiNode of the current node, null otherwise
	 * 
	 */
	public ArchiNode getSuccessorInAxis(int distance) {

		ArchiNode successor = this;
		for (int i = 0; i < distance; i++) {
			if (successor == null)
				break;
			successor = successor.getSuccessorInAxis();
		}

		return successor;
	}

	/**
	 * Get the previous ArchiNode of the current node in the axe. *
	 * 
	 * @return the previous ArchiNode of the current node, null otherwise
	 * 
	 */
	public ArchiNode getPredecessorInComplex(int scale) {
		ArchiNode predecessor = getLinkedPredecessor();
		if (predecessor != null)
			return predecessor;
		else {
			ArchiNode container = getComplex();
			EqualScaleCondition condition = new EqualScaleCondition(this.getScale());
			while (container != null && container.getScale() >= scale) {
				ArchiNode predecessorComplex = container.getLinkedPredecessor();
				if (predecessorComplex != null) {
					predecessor = predecessorComplex.getLastComponent(condition);
					if (predecessor != null)
						break;
				}
				container = container.getComplex();
			}

			return predecessor;
		}

	}

	/**
	 * Get all the previous ArchiNodes of the current node in the axe. *
	 * 
	 * @return the previous ArchiNodes of the current node, null otherwise
	 * 
	 */
	public Collection<ArchiNode> getPredecessorsInAxis() {
		ArrayList<ArchiNode> predecessors = new ArrayList<ArchiNode>();
		ArchiNode prec = this;
		while ((prec = prec.getPredecessorInAxis()) != null) {
			predecessors.add(prec);
		}
		return predecessors;

	}

	/**
	 * Get the previous ArchiNode of the current node in the axe. *
	 * 
	 * @return the previous ArchiNode of the current node, null otherwise
	 * 
	 */
	public ArchiNode getPredecessorInAxis() {
		return getPredecessorInComplex(2);

	}

	/**
	 * Get the previous ArchiNode of the current node in the axe. *
	 * 
	 * @param distance
	 *            Number of nodes from the current node to the predecessor
	 * @return the previous ArchiNode of the current node, null otherwise
	 * 
	 */
	public ArchiNode getPredecessorInAxis(int distance) {

		ArchiNode predecessor = this;
		for (int i = 0; i < distance; i++) {
			if (predecessor == null)
				break;
			predecessor = predecessor.getPredecessorInAxis();
		}

		return predecessor;
	}

	/**
	 * Get the previous node whatever his relation
	 */
	public ArchiNode getPrevious() {

		ArchiNode previousNode = getLinkedPredecessor();

		if (previousNode == null) {
			previousNode = this.getComplex();
		}
		if (previousNode == null) {
			previousNode = this.getBearer();
		}

		return previousNode;
	}

	/**
	 * Get the linked previous node whatever his relation
	 */
	public ArchiNode getLinkedPrevious() {

		ArchiNode previousNode = getLinkedPredecessor();

		if (previousNode == null) {
			previousNode = this.getLinkedComplex();
		}
		if (previousNode == null) {
			previousNode = this.getLinkedBearer();
		}

		return previousNode;
	}

	/**
	 * Get the previous node whatever his relation
	 */
	public ArchiNode getPrevious(ArchiCondition condition) {
		ArchiNode previous = getPrevious();
		while (previous != null && !condition.isCorrect(previous)) {
			previous = previous.getPrevious();
		}

		return previous;
	}

	/**
	 * Get all the ramifications of this node AND his decomposition
	 * 
	 * @return the ramification node collection
	 */
	public Collection<ArchiNode> getRamifications(ArchiCondition condition) {

		Collection<ArchiNode> allRamification = getRamifications();
		Collection<ArchiNode> allRamificationCondition = new ArrayList<ArchiNode>();

		for (ArchiNode r : allRamification) {
			if (condition.isCorrect(r))
				allRamificationCondition.add(r);

			r = r.getFirstComponent();
		}

		return allRamificationCondition;
	}

	/**
	 * Get all the ramifications of this node AND his decomposition
	 * 
	 * @return the ramification node collection
	 */
	public ArrayList<ArchiNode> getRamifications() {

		ArrayList<ArchiNode> allRamification = new ArrayList<ArchiNode>();

		try {

			Collection<ArchiNode> ramifications = this.getLinkedRamifications();
			if (ramifications != null && !ramifications.isEmpty()) {
				allRamification.addAll(ramifications);
			}

			Collection<ArchiNode> decomposition = this.getLinkedComponents();

			if (decomposition != null) {
				for (ArchiNode d : decomposition) {
					allRamification.addAll(d.getRamifications());
				}

			}
		} catch (Exception e) {
			System.err.println("getRamification error : " + e.getMessage());
		}

		return allRamification;
	}

	/**
	 * 
	 */
	public ArchiNode getRoot() {

		ArchiNode currentNode = this;
		ArchiNode previousNode = this;
		while (currentNode != null) {

			previousNode = currentNode;

			currentNode = previousNode.getComplex();

			if (currentNode == null) {
				currentNode = previousNode.getBearer();
			}
		}

		return previousNode;

	}

	public ArchiNode getRoot(int scale) {

		ArchiNode currentNode = this;
		ArchiNode previousNode = this;
		while (currentNode != null && currentNode.getScale() >= scale) {
			previousNode = currentNode;
			currentNode = previousNode.getComplex();
			if (currentNode == null) {
				currentNode = previousNode.getBearer();
			}
		}

		return previousNode;

	}

	public int getRamificationsNumber() {

		Collection<ArchiNode> ramifications = getRamifications();
		if (ramifications != null)
			return ramifications.size();
		return 0;

	}

	/**************************************************************/
	/**************** GEOMETRY AND ATTRIBUTES **********************/
	/**************************************************************/

	public void clearGeometry() {
		geometry = null;
	}

	@Override
	public int compareTo(Object o) {
		if (!(o instanceof ArchiNode)) {
			return -1;
		} // do not crash
		ArchiNode t = (ArchiNode) o;

		if (t.getScale() != this.getScale())
			return t.getScale() - getScale();

		return getItemId() - t.getItemId();
	}

	/**
	 * Copy the nodes to the current one on copyNode
	 * 
	 * @param dataManager
	 * 
	 * @return the next directly linked nodes.
	 * @throws Exception
	 */
	public ArchiNode copy(boolean ramification, boolean decomposition, boolean succession, boolean data)
			throws Exception {

		ArchiNode copyNode = new ArchiNode(type);

		// copyNode.setCache(dataManager);

		if (data) {
			Collection<NodeAttribute> attributes = this.getAllAttributeValues();
			if (attributes != null) {
				for (NodeAttribute a : attributes) {
					copyNode.setAttribute((NodeAttribute) a.clone());
				}
			}
		}

		if (ramification) {
			Collection<ArchiNode> ramifications = this.getLinkedRamifications();
			if (ramifications != null && !ramifications.isEmpty()) {
				for (ArchiNode r : ramifications) {
					copyNode.addRamification(r.copy(true, true, true, data));
				}
			}
		}

		if (succession) {
			ArchiNode successor = this.getLinkedSuccessor();
			if (successor != null) {
				copyNode.addSuccessor(successor.copy(true, true, true, data));
			}
		}

		if (decomposition) {
			ArchiNode decompositions = this.getFirstComponent();
			if (decompositions != null) {

				if (!(decompositions.complexProxy instanceof NoSuccesionComplexProxyImpl))
					copyNode.addComponent(decompositions.copy(true, true, true, data));
				else
					copyNode.addComponent(decompositions.copy(true, true, true, data), false);
			}
		}

		return copyNode;

	}

	public void copyAttributeFrom(ArchiNode copy) {

		if (copy.attributes != null) {
			this.attributes = new HashMap<String, NodeAttribute>();
			for (String key : copy.attributes.keySet()) {
				try {
					this.attributes.put(key, (NodeAttribute) copy.attributes.get(key).clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public Collection<NodeAttribute> getAllAttributeValues() {
		if (attributes != null) {
			return attributes.values();
		} else
			return null;
	}

	/**
	 * Get the attribute by his key name
	 * 
	 * @param key
	 *            The key name of the attribute
	 * @return The NodeAttribute attribute
	 */
	public NodeAttribute getAttribute(String key) {
		if (attributes == null)
			return null;
		NodeAttribute current_attribute = attributes.get(key);
		return current_attribute;
	}

	/**
	 * Get all the attribute keys of this node
	 * 
	 * 
	 * @return all the attribute keys of this node
	 */
	public Map<String, AttributeType> getAttributeKeys() {
		if (attributes == null)
			return null;
		else {
			HashMap<String, AttributeType> attributeKeys = new HashMap<String, AttributeType>();
			for (Entry<String, NodeAttribute> entry : attributes.entrySet()) {
				attributeKeys.put(entry.getKey(), entry.getValue().getType());
			}
			return attributeKeys;
		}
	}

	/**
	 * Get the attribute value by his key name
	 * 
	 * @param <T>
	 * @param key
	 *            The key name of the attribute
	 * @return The object attribute
	 */
	public <T> T get(String key) {

		NodeAttribute current_attribute = getAttribute(key);
		if (current_attribute == null)
			return null;
		return (T) current_attribute.getValue();
	}

	/**
	 * Get the attribute string by his key name
	 * 
	 * @param <T>
	 * @param key
	 *            The key name of the attribute
	 * @return The object attribute
	 */
	public String getString(String key) {

		NodeAttribute current_attribute = getAttribute(key);
		if (current_attribute == null)
			return "";
		return current_attribute.getValue().toString();
	}

	public double getLength() {
		NodeAttribute lengthAttribute = getAttribute(defaultLengthAttribute);
		if (lengthAttribute != null) {

			// fc-17.10.2014 added 5 lines to replace the line under (got a
			// ClassCastException Float cannot be cast to Double)
			Object la = lengthAttribute.getValue();
			if (la instanceof Double)
				return (Double) lengthAttribute.getValue();
			else if (la instanceof Double)
				return (Float) lengthAttribute.getValue();

			// return (Double) lengthAttribute.getValue(); // fc-17.10.2014

		}
		return 1;
	}

	public BoundingBox3d computeBBox(boolean recursive) {
		if (recursive && hasLinkedComponents()) {

			BoundingBox3d bbox = null;
			Collection<ArchiNode> desc = new ArrayList<ArchiNode>();
			try {

				Collection<ArchiNode> lnodes = getForwardLinkedNodes(false, true, true);
				for (ArchiNode n : lnodes) {
					desc.addAll(n.getDescendants());
				}

				for (ArchiNode c : desc) {
					if (c.geometry != null) {
						BoundingBox3d bc = c.geometry.computeBBox();
						if (bc != null) {
							if (bbox == null)
								bbox = new BoundingBox3d();
							bbox.update(bc.getMin());
							bbox.update(bc.getMax());
						}
					}

				}
			} catch (ScaleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return bbox;

		} else if (geometry != null) {
			return geometry.computeBBox();
		}
		return null;
	}

	@Override
	public Vertex3d getMax() {
		BoundingBox3d box = computeBBox(false);
		if (box != null)
			return new Vertex3d(box.max.x, box.max.y, box.max.z);

		return new Vertex3d(0, 0, 0);
	}

	@Override
	public Vertex3d getMin() {
		BoundingBox3d box = computeBBox(false);
		if (box != null)
			return new Vertex3d(box.min.x, box.min.y, box.min.z);

		return new Vertex3d(0, 0, 0);
	}

	@Override
	public String getName() {
		if (getAttribute(defaultNameAttribute) != null) {
			return getAttribute(defaultNameAttribute).toString();
		} else {
			return type.getTranslatedName();
		}
	}

	/**
	 * @return the ramification depth of this node in the tree
	 */
	public int getOrder() {
		int order = 0;
		ArchiNode currentNode = this;
		while (currentNode != null) {
			order++;
			try {
				currentNode = currentNode.getBearer();
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}
		return order;
	}

	/**
	 * Get the scale of this node
	 * 
	 * @return the scale
	 */
	public short getScale() {
		return type.getScale();
	}

	@Override
	public Type getType() {
		return type;
	}

	public double getWidth() {
		NodeAttribute witdhAttribute = getAttribute(defaultWidthAttribute);
		if (witdhAttribute != null) {
			return (Double) witdhAttribute.getValue();
		}
		return 1;
	}

	@Override
	public double getX() {
		if (geometry != null && geometry instanceof SimpleGeometry)
			return (geometry).getPosition().x;
		else
			return 0;
	}

	@Override
	public double getY() {
		if (geometry != null && geometry instanceof SimpleGeometry)
			return (geometry).getPosition().y;
		else
			return 0;
	}

	@Override
	public double getZ() {
		if (geometry != null && geometry instanceof SimpleGeometry)
			return (geometry).getPosition().z;
		else
			return 0;
	}

	public String printNodes(String tab) throws ScaleException {

		String print = "";
		print = this.toString() + " scale =" + this.getScale() + "\n";

		if (this.getFirstComponent() != null) {

			print += tab + "/";
			print += this.getFirstComponent().printNodes(tab);

		}

		if (this.getLinkedRamifications() != null) {

			for (ArchiNode currentNode : this.getLinkedRamifications()) {
				print += "\t" + tab + "+";
				print += currentNode.printNodes("\t" + tab);
			}

		}

		if (this.getLinkedSuccessor() != null) {
			print += tab + "<";
			print += this.getLinkedSuccessor().printNodes(tab);
		}

		return print;

	}

	public void removeAttribute(String key) {
		if (this.attributes != null) {
			attributes.remove(key);
			if (attributes.isEmpty()) {
				attributes = null;
			}
		}
	}

	public void setAttribute(NodeAttribute value) {

		if (this.attributes == null) {
			attributes = new HashMap<String, NodeAttribute>();
		}

		attributes.put(value.getKey(), value);

	}

	public <T> void set(String key, T attribute) {

		if (this.attributes == null) {
			attributes = new HashMap<String, NodeAttribute>();
		}

		if (attribute != null) {
			NodeAttribute a = new NodeAttribute(key, attribute);
			attributes.put(key, a);

		} else { // if attribute == null then remove the attribute
			attributes.remove(key);
		}

	}

	public void setGeometry(SimpleGeometry geometry) {
		this.geometry = geometry;
	}

	// Set length and width by adding RealNodeAttribute
	public void setLength(Double length) {
		set(defaultLengthAttribute, length);
	}

	@Override
	public void setName(String name) {
		set(defaultNameAttribute, name);
	}

	@Override
	public void setType(Type type) {
		if (type instanceof ArchiType) {
			this.type = (ArchiType) type;
		} else {
			Log.println("ArchiNode : setType. Type must be a ArchiScaleType");
		}
	}

	public void setWidth(Double width) {
		set(defaultWidthAttribute, width);
	}

	public void setHeight(Double height) {
		set(defaultHeightAttribute, height);
	}

	/** set x y z must change the geometry not the attribute */
	@Override
	public void setX(double v) {
		set(defaultXXAttribute, v);

	}

	@Override
	public void setXYZ(double x, double y, double z) {
		set(defaultXXAttribute, x);
		set(defaultYYAttribute, y);
		set(defaultZZAttribute, z);

	}

	@Override
	public void setY(double v) {
		set(defaultYYAttribute, v);

	}

	@Override
	public void setZ(double v) {
		set(defaultZZAttribute, v);

	}

	@Override
	public String toString() {
		return getName() + " : id = " + this.id;
	}

	public SimpleGeometry getGeometry() {
		return geometry;
	}

	public boolean has3DMesh() { // sg+fc-15.4.2013
		return this.getGeometry() != null && (this.getGeometry() instanceof MeshGeometry)
				&& ((MeshGeometry) this.getGeometry()).getMesh() != null;
	}

	/**
	 * Returns the SimpleMesh of this node if any, else returns null. This mesh
	 * is translated, rotated and scaled (the transformed mesh).
	 */
	public SimpleMesh getMesh() {
		if (has3DMesh()) {
			return ((MeshGeometry) this.getGeometry()).getTransformedMesh();
		} else {
			return null;
		}
	}

	public void visit(NodeVisitor visitor) {

		Collection<ArchiNode> descendants;
		try {
			descendants = getDescendants();
			for (ArchiNode desc : descendants) {
				visitor.visit(desc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class ScaleException extends Exception {

		public ScaleException(String string) {
			// TODO Auto-generated constructor stub
		}

	}

}
