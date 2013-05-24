package gov.noaa.pmel.tmap.las.client.laswidget;

import gov.noaa.pmel.tmap.las.client.ClientFactory;
import gov.noaa.pmel.tmap.las.client.event.AddSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.AddVariableConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.GridChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.MapChangeEvent;
import gov.noaa.pmel.tmap.las.client.event.RemoveSelectionConstraintEvent;
import gov.noaa.pmel.tmap.las.client.event.WidgetSelectionChangeEvent;
import gov.noaa.pmel.tmap.las.client.map.OLMapWidget;
import gov.noaa.pmel.tmap.las.client.serializable.CategorySerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ConstraintSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.ERDDAPConstraintGroup;
import gov.noaa.pmel.tmap.las.client.serializable.GridSerializable;
import gov.noaa.pmel.tmap.las.client.serializable.VariableSerializable;
import gov.noaa.pmel.tmap.las.client.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ConstraintWidgetGroup extends Composite {

    boolean active = false;
    VerticalPanel mainPanel = new VerticalPanel();
    HorizontalPanel interiorPanel = new HorizontalPanel();
    ScrollPanel scrollPanel = new ScrollPanel();
    FlowPanel displayPanel = new FlowPanel();
    StackLayoutPanel constraintPanel = new StackLayoutPanel(Style.Unit.PX);
    HTML topLabel = new HTML("<strong>Select:</strong>");
    HTML constraintLabel = new HTML("<strong>My selections:</strong>");
    ClientFactory clientFactory = GWT.create(ClientFactory.class);
    EventBus eventBus = clientFactory.getEventBus();
    private static double STACK_HEIGHT = Constants.CONTROLS_WIDTH + 60;
    ERDDAPVariableConstraintPanel variableConstraints;
//    SelectionConstraintPanel selectionConstraintPanel = new SelectionConstraintPanel();
//    SubsetConstraintPanel subsetConstraintPanel = new SubsetConstraintPanel();
//    SeasonConstraintPanel seasonConstraintPanel = new SeasonConstraintPanel();
    
    // Keep track of the dsid for this panel.
    String dsid;
    
    // Keep track of the stack panel index.
    int panel = 0;

    public ConstraintWidgetGroup() {
        constraintPanel.setSize(Constants.CONTROLS_WIDTH+"px", STACK_HEIGHT+"px");
        mainPanel.add(topLabel);
        interiorPanel.add(constraintPanel);
        mainPanel.add(interiorPanel);
        scrollPanel.setSize(Constants.CONTROLS_WIDTH-10+"px", "100px");
        scrollPanel.add(displayPanel);
        scrollPanel.addStyleName("allBorderGray");
        displayPanel.setSize(Constants.CONTROLS_WIDTH-25+"px", "125px");
        mainPanel.add(constraintLabel);
        mainPanel.add(scrollPanel);
//        constraintPanel.add(selectionConstraintPanel, "text", 30);
//        constraintPanel.add(subsetConstraintPanel, "text", 30);
//        constraintPanel.add(seasonConstraintPanel, "text", 30);

        variableConstraints = new ERDDAPVariableConstraintPanel();
//        subsetConstraintPanel.addVariableConstraint(variableConstraints);
        initWidget(mainPanel);
    }

    public void init(String dsid) {
        this.dsid = dsid;
        Util.getRPCService().getERDDAPConstraintGroups(dsid, initConstraintsCallback);
    }
    protected AsyncCallback<List<ERDDAPConstraintGroup>> initConstraintsCallback = new AsyncCallback<List<ERDDAPConstraintGroup>>() {

        @Override
        public void onFailure(Throwable caught) {
            Window.alert("Unable to initialize the constraints panel.");
        }

        @Override
        public void onSuccess(List<ERDDAPConstraintGroup> constraintGroups) {
            init(constraintGroups);
            Util.getRPCService().getCategories(dsid, dsid, categoryCallback);
        }
    };
    protected AsyncCallback<CategorySerializable[]> categoryCallback = new AsyncCallback<CategorySerializable[]>() {

        @Override
        public void onFailure(Throwable caught) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onSuccess(CategorySerializable[] cats) {
            
            for (int i = 0; i < cats.length; i++) {
                if ( cats[i].isVariableChildren() ) {
                    VariableSerializable[] variables = cats[i].getDatasetSerializable().getVariablesSerializable();
                    variableConstraints.setVariables(variables);
                }
            }
        }
        
    };
    public void init(List<ERDDAPConstraintGroup> constraintGroups) {
        panel = 0;
        if ( constraintPanel.getWidgetCount() > 0 ) constraintPanel.clear();
        for (Iterator iterator = constraintGroups.iterator(); iterator.hasNext();) {
            ERDDAPConstraintGroup erddapConstraintGroup = (ERDDAPConstraintGroup) iterator.next();
            if ( erddapConstraintGroup.getType().equals("selection") ) {
                SelectionConstraintPanel selectionConstraintPanel = new SelectionConstraintPanel();
                selectionConstraintPanel.init(erddapConstraintGroup);
                constraintPanel.add(selectionConstraintPanel, erddapConstraintGroup.getName(), 22);       
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals("subset")) {
                SubsetConstraintPanel subsetConstraintPanel = new SubsetConstraintPanel();
                subsetConstraintPanel.init(erddapConstraintGroup);
                constraintPanel.add(subsetConstraintPanel, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals("season") ) {
                SeasonConstraintPanel seasonConstraintPanel = new SeasonConstraintPanel();
                seasonConstraintPanel.init(erddapConstraintGroup);
                constraintPanel.add(seasonConstraintPanel, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            } else if ( erddapConstraintGroup.getType().equals("variable") ) {
                variableConstraints = new ERDDAPVariableConstraintPanel();
                constraintPanel.add(variableConstraints, erddapConstraintGroup.getName(), 22);
                constraintPanel.setHeaderHTML(panel, "<div style='font-size:.7em'>"+erddapConstraintGroup.getName()+"</div>");
                panel++;
            }
        }
 
        if ( constraintPanel.getWidgetCount() > 0 ) {
            constraintPanel.showWidget(0);
        }
        eventBus.addHandler(AddVariableConstraintEvent.TYPE, new AddVariableConstraintEvent.Handler() {

            @Override
            public void onAdd(AddVariableConstraintEvent event) {
                String variable = event.getVariable();
                String op1 = event.getOp1();
                String op2 = event.getOp2();
                String lhs = event.getLhs();
                String rhs = event.getRhs();
                String varid = event.getVarid();
                String dsid = event.getDsid();
                boolean apply = event.isApply();
                ConstraintTextAnchor anchor1 = new ConstraintTextAnchor("variable", dsid, varid, variable, lhs, variable, lhs, op1);
                ConstraintTextAnchor anchor2 = new ConstraintTextAnchor("variable", dsid, varid, variable, rhs, variable, rhs, op2);
                ConstraintTextAnchor a = findMatchingAnchor(anchor1);
                ConstraintTextAnchor b = findMatchingAnchor(anchor2);
                if ( apply ) {
                    if ( lhs != null && !lhs.equals("") ) {
                        if ( a != null ) {
                            displayPanel.remove(a);
                        }
                        displayPanel.add(anchor1);
                    } else if ( lhs != null ) {
                        // it's blank, applies been pressed, remove the anchor if it exists
                        if ( a != null ) {
                            displayPanel.remove(a);
                        }
                    }
                    if ( rhs != null && !rhs.equals("") ) {
                        if ( b != null ) {
                            displayPanel.remove(b);
                        }
                        displayPanel.add(anchor2);
                    } else if ( rhs != null ) {
                        // It's the same as above...
                        if ( b != null ) {
                            displayPanel.remove(b);
                        }
                    }

                } else {
                    // Apply button not pressed, newly created variable constraint, if there are active constraints, fill them in...
                    if ( lhs != null && rhs != null && lhs.equals("") && rhs.equals("") ) {                        
                        if ( a != null ) {
                            // There is a matching active constraint for the lhs.
                            variableConstraints.setLhs(a.getKeyValue());
                        }
                        if ( b != null ) {
                            variableConstraints.setRhs(b.getKeyValue());
                        }
                    }
                }
            }
        });
        eventBus.addHandler(AddSelectionConstraintEvent.TYPE, new AddSelectionConstraintEvent.Handler() {

            @Override
            public void onAdd(AddSelectionConstraintEvent event) {

                String variable = event.getVariable();
                String value = event.getValue();
                String key = event.getKey();
                String keyValue = event.getKeyValue();

                ConstraintTextAnchor anchor = new ConstraintTextAnchor("text", null, null, variable, value, key, keyValue, "eq");

                if ( !contains(anchor) ) {
                    displayPanel.add(anchor);
                }

                eventBus.fireEvent(new WidgetSelectionChangeEvent(false, true, true));

            }
        });
        // Event handlers for date time and lat/lon (map) constraint events.
        eventBus.addHandler(RemoveSelectionConstraintEvent.TYPE, new RemoveSelectionConstraintEvent.Handler() {

            @Override
            public void onRemove(RemoveSelectionConstraintEvent event) {
                Object source = event.getSource();
                if ( source instanceof ConstraintTextAnchor ) {
                    ConstraintTextAnchor anchor = (ConstraintTextAnchor) source;
                    displayPanel.remove(anchor);
                    if ( anchor.getType().equals("variable") ) {
                        variableConstraints.clearTextField(anchor);
                    }
                } else if ( source instanceof SeasonConstraintPanel ) {
                    String variable = event.getVariable();
                    String value = event.getValue();
                    String key = event.getKey();
                    String keyValue = event.getKeyValue();

                    ConstraintTextAnchor anchor = new ConstraintTextAnchor("text", null, null, variable, value, key, keyValue, "eq");
                    for (int i = 0; i < displayPanel.getWidgetCount(); i++ ) {
                        ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
                        if ( a.equals(anchor) ) {
                            displayPanel.remove(a);
                        }
                    }
                }
                eventBus.fireEvent(new WidgetSelectionChangeEvent(false, true, true));
            }

        });
    }
//    public void init(List<ERDDAPConstraintGroup> constraintGroups, List<VariableSerializable> variables) {
//        VariableConstraintLayout variableConstraints = new VariableConstraintLayout("", true);
//        init(constraintGroups);
//        constraintPanel.add(variableConstraints, "Select Data by Variable Value", 30);
//        for (Iterator varIt = variables.iterator(); varIt.hasNext();) {
//            VariableSerializable variableSerializable = (VariableSerializable) varIt.next();
//            variableConstraints.addItem(variableSerializable);
//        }
//    }
    public List<ConstraintSerializable> getConstraints() {
        List<ConstraintSerializable> constraints = new ArrayList<ConstraintSerializable>();
        Map<String, ConstraintSerializable> cons = new HashMap<String, ConstraintSerializable>();
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor anchor = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( anchor.getType().equals("text") ) {
                String key = anchor.getKey();
                String op = anchor.getOp();
                String value = anchor.getKeyValue();
                ConstraintSerializable keyConstraint = cons.get(key);
                if ( keyConstraint == null ) {
                    keyConstraint = new ConstraintSerializable("text", null, null, key, op, "\""+value+"\"", key+"_"+value);
                    cons.put(key, keyConstraint);
                } else {
                    String v = keyConstraint.getRhs();
                    v = v.substring(0, v.length()-1);
                    v = v + "|" + value+"\"";
                    keyConstraint.setRhs(v);
                    keyConstraint.setOp("like");
                }
            } else {
                String dsid = anchor.getDsid();
                String varid = anchor.getVarid();
                String op = anchor.getOp();
                String lhs = anchor.getValue();
                ConstraintSerializable con = new ConstraintSerializable("variable", dsid, varid, varid, op, lhs, dsid+"_"+varid);
                constraints.add(con);
            }
        }
        for (Iterator keysIt = cons.keySet().iterator(); keysIt.hasNext();) {
            String key = (String) keysIt.next();
            constraints.add(cons.get(key));
        }
        return constraints;
    }
    private ConstraintTextAnchor findMatchingAnchor(ConstraintTextAnchor anchor) {
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( a.getKey().equals(anchor.getKey()) && a.getOp().equals(anchor.getOp()) ) {
                return a;
            }
        }
        return null;
    }
    private boolean contains(ConstraintTextAnchor anchor) {
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( anchor.equals(a) ) {
                return true;
            }
        }
        return false;
    }
    private void remove(ConstraintTextAnchor anchor) {
        ConstraintTextAnchor remove = null;
        for (int i = 0; i < displayPanel.getWidgetCount(); i++) {
            ConstraintTextAnchor a = (ConstraintTextAnchor) displayPanel.getWidget(i);
            if ( anchor.equals(a) ) {
                remove = a;
            }
        }
        if ( remove != null ) {
            displayPanel.remove(remove);
        }
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isActive() {
        return active;
    }

    public void setSelectedPanelIndex(int panelIndex) {
        constraintPanel.showWidget(panelIndex);
    }
    
    public List<ConstraintTextAnchor> getAnchors() {
        List<ConstraintTextAnchor> anchors = new ArrayList<ConstraintTextAnchor>();
        for (int i=0; i < displayPanel.getWidgetCount(); i++ ) {
            ConstraintTextAnchor cta = (ConstraintTextAnchor) displayPanel.getWidget(i);
            anchors.add(cta);
        }
        return anchors;
    }

    public void setConstraints(List<ConstraintTextAnchor> cons) {
        for (Iterator conIt = cons.iterator(); conIt.hasNext();) {
            ConstraintTextAnchor cta = (ConstraintTextAnchor) conIt.next();
            displayPanel.add(cta);
        }
    }

    public int getConstraintPanelIndex() {
       return constraintPanel.getVisibleIndex();
    }

    public void clearConstraints() {
       displayPanel.clear();
        
    }
}
