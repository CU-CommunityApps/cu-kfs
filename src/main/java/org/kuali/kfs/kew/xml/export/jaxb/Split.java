/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kew.xml.export.jaxb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.engine.node.BranchPrototype;
import org.kuali.kfs.kew.engine.node.NodeType;
import org.kuali.kfs.kew.engine.node.RouteNode;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>JAXB-annotated class for &lt;split&gt; xml element.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{}type"/&gt;
 *         &lt;sequence&gt;
 *           &lt;element ref="{}branch" maxOccurs="unbounded"/&gt;
 *           &lt;element ref="{}join"/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *       &lt;attribute name="nextNode" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
/*
 * CU customization to backport FINP-9392 on top of 7/13/22 version of this file. 
 * This can be removed when we upgrade to the 03/15/2023 version of financials.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"name", "nextAppDocStatus", "nextNode", "branches", "join"})
@XmlRootElement(namespace = "", name = XmlConstants.SPLIT)
public final class Split extends ExportableRouteNode implements RouteNodesChild, RoutePathChild {

    //CU customization to backport FINP-9392
    @XmlTransient
    private static final Logger LOG = LogManager.getLogger();

    @XmlElement(namespace = "", name = XmlConstants.BRANCH)
    private List<Branch> branches;

    @XmlElement(namespace = "", name = XmlConstants.JOIN)
    private Join join;

    @XmlAttribute(name = XmlConstants.NAME, required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    private String name;

    @XmlAttribute(name = XmlConstants.NEXT_APP_DOC_STATUS)
    @XmlSchemaType(name = "anySimpleType")
    private String nextAppDocStatus;

    @XmlAttribute(name = XmlConstants.NEXT_NODE)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    private String nextNode;

    @XmlTransient
    private RoutePath routePathParent;

    // CU customization to backport FINP-9392
    @XmlTransient
    private Branch branchParent;

    public Split() {
    }

    public Split(final RouteNode routeNode) {
        name = routeNode.getRouteNodeName();
    }

    public Split(final RouteNode routeNode, final RoutePath routePathParent) {
        name = routeNode.getRouteNodeName();
        this.routePathParent = routePathParent;
    }

    // CU customization to backport FINP-9392
    public Split(final RouteNode routeNode, final Branch branchParent) {
        this.branchParent = branchParent;
        name = routeNode.getRouteNodeName();
    }

    private void addBranchNode(final RouteNode routeNode) {
        final BranchPrototype branchPrototype = routeNode.getBranch();
        if (branchPrototype == null) {
            throw new WorkflowRuntimeException(String.format(
                    "Found a split next node with no associated branch " + "prototype: %s",
                    routeNode.getRouteNodeName()
            ));
        }
        final Branch branch = new Branch(branchPrototype.getName(), this);
        branch.addBranchChild(routeNode);
        if (branches == null) {
            branches = new ArrayList<>();
        }
        branches.add(branch);
    }

    // CU customization to backport FINP-9392
    void addNextNodeToParent(final RouteNode routeNode) {
        if (routePathParent != null) {
            routePathParent.addRoutePathChild(routeNode);
        } else if (branchParent != null) {
            branchParent.addBranchChild(routeNode);
        } else {
            LOG.error("addNextNodeToParent(...): no parent set; routeNode: {}", routeNode);
            throw new WorkflowRuntimeException("addNextNodeToParent(...): no parent set");
        }
    }

    @Override
    public void processNextNodes(final RouteNode routeNode) {
        routeNode.getNextNodes().forEach(this::addBranchNode);
    }

    public void setJoin(final Join join) {
        this.join = join;
    }

    public void setNextAppDocStatus(final String nextAppDocStatus) {
        this.nextAppDocStatus = nextAppDocStatus;
    }

    public void setNextNode(final String nextNode) {
        this.nextNode = nextNode;
    }

    @Override
    public void setRouteNodeData(final RouteNode routeNode, final NodeType nodeType) {
        if (nodeType.isCustomNode(routeNode.getNodeType())) {
            setType(routeNode.getNodeType());
        }
    }
}
