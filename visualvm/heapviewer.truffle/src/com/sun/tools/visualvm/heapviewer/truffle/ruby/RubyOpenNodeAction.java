/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.tools.visualvm.heapviewer.truffle.ruby;

import com.sun.tools.visualvm.heapviewer.HeapContext;
import com.sun.tools.visualvm.heapviewer.model.DataType;
import com.sun.tools.visualvm.heapviewer.model.HeapViewerNode;
import com.sun.tools.visualvm.heapviewer.ui.HeapViewerActions;
import com.sun.tools.visualvm.heapviewer.ui.HeapViewerNodeAction;
import com.sun.tools.visualvm.heapviewer.ui.NodeObjectsView;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.profiler.heap.Instance;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@ServiceProvider(service=HeapViewerNodeAction.Provider.class)
@NbBundle.Messages({
    "RubyOpenNodeAction_OpenTypeTab=Open Type in New Tab"
})
public class RubyOpenNodeAction extends HeapViewerNodeAction.Provider {
    
    public boolean supportsView(HeapContext context, String viewID) {
        return viewID.startsWith("ruby_") && RubyHeapFragment.getRubyContext(context) != null;
    }

    public HeapViewerNodeAction[] getActions(HeapViewerNode node, HeapContext context, HeapViewerActions actions) {
        HeapContext rbContext = RubyHeapFragment.getRubyContext(context);
        
        List<HeapViewerNodeAction> actionsList = new ArrayList(2);
        
        HeapViewerNode copy = node instanceof RubyNodes.RubyNode ? node.createCopy() : null;
        actionsList.add(new OpenNodeAction(copy, rbContext, actions));
        
        Instance instance = HeapViewerNode.getValue(node, DataType.INSTANCE, context.getFragment().getHeap());
        if (instance != null && RubyDynamicObject.isDynamicObject(instance)) {
            RubyDynamicObject object = new RubyDynamicObject(instance);
            if (object.isRubyObject()) {
                String typeName = object.getType(rbContext.getFragment().getHeap());
                RubyType type = ((RubyHeapFragment)rbContext.getFragment()).getType(typeName, null); // should already be computed
                if (type != null) {
                    RubyNodes.RubyTypeNode typeNode = new RubyNodes.RubyTypeNode(type);
                    actionsList.add(new OpenClassAction(typeNode, rbContext, actions));
                }
            }
        }
        
        return actionsList.toArray(new HeapViewerNodeAction[0]);
    }
    
    
    private static class OpenNodeAction extends NodeObjectsView.DefaultOpenAction {
        
        private OpenNodeAction(HeapViewerNode node, HeapContext context, HeapViewerActions actions) {
            super(node, context, actions);
        }

        public NodeObjectsView createView(HeapViewerNode node, HeapContext context, HeapViewerActions actions) {
            return new RubyObjectView(node, context, actions);
        }
        
    }
    
    private static class OpenClassAction extends NodeObjectsView.OpenAction {
        
        private OpenClassAction(HeapViewerNode node, HeapContext context, HeapViewerActions actions) {
            super(Bundle.RubyOpenNodeAction_OpenTypeTab(), 1, node, context, actions);
        }

        public NodeObjectsView createView(HeapViewerNode node, HeapContext context, HeapViewerActions actions) {
            return new RubyObjectView(node, context, actions);
        }
        
    }
    
}
