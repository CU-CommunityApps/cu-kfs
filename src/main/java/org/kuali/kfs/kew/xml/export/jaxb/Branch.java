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
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.engine.node.NodeType;
import org.kuali.kfs.kew.engine.node.RouteNode;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

/**
 * <p>JAXB-annotated class for {@link org.kuali.kfs.kew.engine.node.Branch } type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{}requests"/&gt;
 *         &lt;element ref="{}simple"/&gt;
 *         &lt;element ref="{}role" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}NCName" /&gt;
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
@XmlType(name = "", propOrder = {"name", "requestsList", "simpleList", "roleList", "splitList"})
@XmlRootElement(namespace = "", name = XmlConstants.BRANCH)
public final class Branch extends ExportableRouteNode {

    @XmlAttribute(name = XmlConstants.NAME, required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    private String name;

    @XmlElement(namespace = "", name = XmlConstants.REQUESTS)
    private Queue<Requests> requestsList;

    @XmlElement(namespace = "", name = XmlConstants.ROLE)
    private Queue<Role> roleList;

    @XmlElement(namespace = "", name = XmlConstants.SIMPLE)
    private Queue<Simple> simpleList;

    @XmlElement(namespace = "", name = XmlConstants.SPLIT)
    private Queue<Split> splitList;

    @XmlTransient
    private Split splitParent;

    public Branch() {
    }

    public Branch(final String name, final Split split) {
        this.name = name;
        splitParent = split;
    }

    void addBranchChild(final RouteNode routeNode) {

        final NodeType nodeType = NodeType.getNodeTypeFromRouteNode(routeNode);

        if (nodeType == NodeType.JOIN) {
            // every branch is within a split. if not, we have a big problem
            final Join join = new Join(routeNode, Objects.requireNonNull(splitParent));
            join.setRouteNodeData(routeNode, NodeType.JOIN);
            splitParent.setJoin(join);
            join.processNextNodes(routeNode);

        } else if (nodeType == NodeType.ROLE) {
            final Role role = new Role(routeNode, this);
            addRole(role);
            role.processNextNodes(routeNode);

        } else if (nodeType == NodeType.REQUESTS) {
            final Requests requests = new Requests(routeNode, this);
            addRequests(requests);
            requests.processNextNodes(routeNode);

        } else if (nodeType == NodeType.SIMPLE) {
            final Simple simple = new Simple(routeNode, this);
            addSimple(simple);
            simple.processNextNodes(routeNode);

        } /*CU customization to backport FINP-9392*/else if (nodeType == NodeType.SPLIT) {
            final Split split = new Split(routeNode, this);
            addSplit(split);
            split.processNextNodes(routeNode);
        } else {
            final String errMsg =
                    String.format("Attempt to add Unsupported child type %s to XML node %s", nodeType, getClass());
            throw new WorkflowRuntimeException(errMsg);
        }
    }

    private void addRequests(final Requests requests) {
        if (requestsList == null) {
            requestsList = new ArrayDeque<>();
        }
        requestsList.add(requests);
    }

    private void addRole(final Role role) {
        if (roleList == null) {
            roleList = new ArrayDeque<>();
        }
        roleList.add(role);
    }

    private void addSimple(final Simple simple) {
        if (simpleList == null) {
            simpleList = new ArrayDeque<>();
        }
        simpleList.add(simple);
    }

    // CU customization to backport FINP-9392
    private void addSplit(final Split split) {
        if (splitList == null) {
            splitList = new ArrayDeque<>();
        }
        splitList.add(split);
    }
}
