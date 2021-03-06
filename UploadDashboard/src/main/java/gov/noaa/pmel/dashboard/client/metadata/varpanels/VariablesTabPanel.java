package gov.noaa.pmel.dashboard.client.metadata.varpanels;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import gov.noaa.pmel.dashboard.client.UploadDashboard;
import gov.noaa.pmel.dashboard.client.metadata.EditSocatMetadataPage;
import gov.noaa.pmel.dashboard.client.metadata.LabeledListBox;
import gov.noaa.pmel.dashboard.shared.DashboardDataset;
import gov.noaa.pmel.socatmetadata.shared.variable.AirPressure;
import gov.noaa.pmel.socatmetadata.shared.variable.AquGasConc;
import gov.noaa.pmel.socatmetadata.shared.variable.BioData;
import gov.noaa.pmel.socatmetadata.shared.variable.GasConc;
import gov.noaa.pmel.socatmetadata.shared.variable.GenData;
import gov.noaa.pmel.socatmetadata.shared.variable.InstData;
import gov.noaa.pmel.socatmetadata.shared.variable.Temperature;
import gov.noaa.pmel.socatmetadata.shared.variable.Variable;

import java.util.ArrayList;
import java.util.Arrays;

public class VariablesTabPanel extends Composite {

    private static final ArrayList<String> varTypeListNames = new ArrayList<String>(Arrays.asList(
            "Air Pressure",
            "Aqueous Gas Conc.",
            "Atmospheric Gas Conc.",
            // "Biological",
            "Temperature",
            "Other measured",
            "Other generic",
            "Flag"
    ));
    private static final ArrayList<String> varTypeSimpleNames = new ArrayList<String>(Arrays.asList(
            new AirPressure().getSimpleName(),
            new AquGasConc().getSimpleName(),
            new GasConc().getSimpleName(),
            // new BioData().getSimpleName(),
            new Temperature().getSimpleName(),
            new InstData().getSimpleName(),
            new GenData().getSimpleName(),
            new Variable().getSimpleName()
    ));

    interface VariablesTabPanelUiBinder extends UiBinder<FlowPanel,VariablesTabPanel> {
    }

    private static final VariablesTabPanelUiBinder uiBinder = GWT.create(VariablesTabPanelUiBinder.class);

    @UiField
    Label headerLabel;
    @UiField
    TabLayoutPanel mainPanel;
    @UiField
    Button addButton;
    @UiField
    Button removeButton;

    ArrayList<VariablePanel> variablePanels;

    /**
     * Creates a {@link TabLayoutPanel} with add and remove buttons underneath.
     * The tab panel contains {@link VariablePanel} panels for each variable.
     * The add button will add an new VariablePanel (and thus, a new Variable) and display it.
     * The remove button will remove the currently selected VariablePanel (and its associated Variable)
     * and show the next VariablePanel, or the last VariablePanel if there is no next VariablePanel.
     * <p>
     * A call to {@link #showPanel(int)} will need to be made to show a panel.
     *
     * @param dataset
     *         the dataset associated with this metadata
     * @param variables
     *         the initial list of variables to show
     */
    public VariablesTabPanel(DashboardDataset dataset, ArrayList<Variable> variables) {
        initWidget(uiBinder.createAndBindUi(this));

        headerLabel.setText(EditSocatMetadataPage.VARIABLES_TAB_TEXT + " (variables) for " + dataset.getDatasetId());
        variablePanels = new ArrayList<VariablePanel>(variables.size());
        // Add a panel for each variable
        for (int k = 0; k < variables.size(); k++) {
            addPanel(k, variables.get(k));
        }
        addButton.setText("Add another");
        addButton.setTitle("Adds a new variable description after the currently displayed one");
        removeButton.setText("Remove current");
        removeButton.setTitle("Removes the currently displayed variable description");
    }

    /**
     * Initialized the type list for a VariablePanel instance.  Assumes the type list has not yet been
     * initialized.  Appropriate values are assigned to the variable type list and selects the appropriate
     * value for the type of variable given.  Also adds the callback to the type list to change to the
     * appropriate panel for a newly selected variable type.
     */
    public void assignVariableTypeList(LabeledListBox typeList, Variable vari, VariablePanel panel) {
        for (String name : varTypeListNames) {
            typeList.addItem(name);
        }
        int k = varTypeSimpleNames.indexOf(vari.getSimpleName());
        if ( k < 0 ) {
            UploadDashboard.showMessage("Unexpected variable type of " +
                    SafeHtmlUtils.htmlEscape(vari.getSimpleName()));
        }
        typeList.setSelectedIndex(k);
        typeList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                changeVariableType(variablePanels.indexOf(panel), typeList.getSelectedIndex());
            }
        });
    }

    /**
     * @param index
     *         show the VariablePanel at this index; does nothing if invalid
     */
    public void showPanel(int index) {
        if ( (index < 0) || (index >= variablePanels.size()) )
            return;
        mainPanel.selectTab(index, true);
    }

    /**
     * @return the list of updated variables
     */
    public ArrayList<Variable> getUpdatedVariables() {
        ArrayList<Variable> variables = new ArrayList<Variable>(variablePanels.size());
        for (VariablePanel panel : variablePanels) {
            variables.add(panel.getUpdatedVariable());
        }
        return variables;
    }

    @UiHandler("addButton")
    void addButtonOnClick(ClickEvent event) {
        Variable vari;
        int index = mainPanel.getSelectedIndex();
        if ( (index >= 0) && (index < variablePanels.size()) ) {
            // make a copy of the currently selected Variable
            vari = variablePanels.get(index).getUpdatedVariable();
            vari = (Variable) (vari.duplicate(null));
            // erase data that must be specific for this variable
            vari.setColName(null);
            vari.setFullName(null);
        }
        else {
            // Should never happen
            vari = new Variable();
        }
        index++;
        addPanel(index, vari);
        mainPanel.selectTab(index, true);
    }

    @UiHandler("removeButton")
    void removeButtonOnClick(ClickEvent event) {
        removePanel(mainPanel.getSelectedIndex());
    }

    /**
     * Removes the indicated panel from the tab panel.  On return, the selected panel will be
     * the next panel in the list, or the last panel if the panel removed was the last panel.
     * This will not remove the panel if there is no other panel remaining; instead, an error
     * message is presented to the user.
     *
     * @param index
     *         remove the panel at this index; if invalid, does nothing
     */
    private void removePanel(int index) {
        int numPanels = variablePanels.size();
        if ( numPanels < 2 ) {
            UploadDashboard.showMessage("There must be at least one variable");
            return;
        }
        if ( (index < 0) || (index >= numPanels) )
            return;
        variablePanels.remove(index);
        mainPanel.remove(index);
        numPanels--;
        if ( index == numPanels )
            index--;
        mainPanel.selectTab(index, true);
    }

    /**
     * Adds a panel appropriate for the given variable at the given index in the tab panel.
     *
     * @param index
     *         insert panel at this index; if invalid, an error message is presented to the user
     * @param vari
     *         variable to be associated with this panel
     */
    private void addPanel(int index, Variable vari) {
        if ( (index < 0) || (index > variablePanels.size()) ) {
            UploadDashboard.showMessage("Unexpected invalid replacement panel index of " + index);
            return;
        }
        VariablePanel panel;
        String simpleName = vari.getSimpleName();
        HTML header = new HTML();
        switch ( simpleName ) {
            case "AirPressure":
                panel = new AirPressureVarPanel((AirPressure) vari, header, this);
                break;
            case "AquGasConc":
                panel = new AquGasConcVarPanel((AquGasConc) vari, header, this);
                break;
            case "GasConc":
                panel = new GasConcVarPanel((GasConc) vari, header, this);
                break;
            case "BioData":
                panel = new BioDataVarPanel((BioData) vari, header, this);
                break;
            case "Temperature":
                panel = new TemperatureVarPanel((Temperature) vari, header, this);
                break;
            case "InstData":
                panel = new InstDataVarPanel((InstData) vari, header, this);
                break;
            case "GenData":
                panel = new GenDataVarPanel((GenData) vari, header, this);
                break;
            case "Variable":
                panel = new FlagVarPanel(vari, header, this);
                break;
            default:
                UploadDashboard.showMessage("Unexpect variable type of " + SafeHtmlUtils.htmlEscape(simpleName));
                return;
        }
        panel.initialize();
        variablePanels.add(index, panel);
        mainPanel.insert(panel, header, index);
    }

    /**
     * Change the variable and associated panel to the selected type.
     *
     * @param varIdx
     *         index of the variable and associate panel to change
     * @param typeIdx
     *         index (in the varType lists) of the variable type to change to
     */
    private void changeVariableType(int varIdx, int typeIdx) {
        if ( varIdx < 0 ) {
            UploadDashboard.showMessage("Unexpected unknown variable panel to replace");
            return;
        }
        if ( typeIdx < 0 ) {
            UploadDashboard.showMessage("No variable type selected");
            return;
        }
        Variable oldVar = variablePanels.get(varIdx).getUpdatedVariable();
        Variable vari;
        String simpleName = varTypeSimpleNames.get(typeIdx);
        switch ( simpleName ) {
            case "AirPressure":
                vari = new AirPressure(oldVar);
                break;
            case "AquGasConc":
                vari = new AquGasConc(oldVar);
                break;
            case "GasConc":
                vari = new GasConc(oldVar);
                break;
            case "BioData":
                vari = new BioData(oldVar);
                break;
            case "Temperature":
                vari = new Temperature(oldVar);
                break;
            case "InstData":
                vari = new InstData(oldVar);
                break;
            case "GenData":
                vari = new GenData(oldVar);
                break;
            case "Variable":
                vari = new Variable(oldVar);
                break;
            default:
                UploadDashboard.showMessage("Unexpect variable type of " + SafeHtmlUtils.htmlEscape(simpleName));
                return;
        }
        // Add the new panel first and then remove the old panel to avoid problems if there is only one panel
        addPanel(varIdx + 1, vari);
        // Since the new panel was positioned after the one to be removed,
        // then new panel will then be the one selected after removal
        removePanel(varIdx);
    }

}
