/**
 * 
 */
package gov.noaa.pmel.socat.dashboard.client;

import gov.noaa.pmel.socat.dashboard.client.SocatUploadDashboard.PagesEnum;
import gov.noaa.pmel.socat.dashboard.shared.DashboardCruise;
import gov.noaa.pmel.socat.dashboard.shared.DashboardCruiseWithData;
import gov.noaa.pmel.socat.dashboard.shared.DashboardUtils;
import gov.noaa.pmel.socat.dashboard.shared.DataColumnType;
import gov.noaa.pmel.socat.dashboard.shared.DataSpecsService;
import gov.noaa.pmel.socat.dashboard.shared.DataSpecsServiceAsync;

import java.util.ArrayList;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;

/**
 * Page for specifying the data column types in a DashboardCruiseWithData.
 * 
 * @author Karl Smith
 */
public class DataColumnSpecsPage extends Composite {

	private static final int DATA_COLUMN_WIDTH = 16;

	private static final String TITLE_TEXT = "Identify Data Columns";
	private static final String WELCOME_INTRO = "Logged in as ";
	private static final String LOGOUT_TEXT = "Logout";

	private static final String MESSAGES_TEXT = "Show data-problem messages";

	private static final String SUBMIT_TEXT = "Check Data";

	private static final String CANCEL_TEXT = "Done";

	private static final String INTRO_HTML_PROLOGUE = "Dataset: <ul><li>";
	private static final String INTRO_HTML_EPILOGUE = "</li></ul>";

	private static final String PAGER_LABEL_TEXT = "Rows shown";

	private static final String NOT_CHECKED_MSG = "(data values not checked)";

	private static final String UNKNOWN_COLUMN_TYPE_PROLOGUE = 
			" data columns:<ul>";
	private static final String UNKNOWN_COLUMN_TYPE_EPILOGUE = 
			"</ul> are still <em>(unknown)</em> and need to be specified.";
	private static final String NO_LONGITUDE_ERROR_MSG = 
			"No data column has been identified as the longitude";
	private static final String NO_LATITUDE_ERROR_MSG =
			"No data column has been identified as the latitude";
	private static final String NO_CO2_ERROR_MSG = 
			"No data columns have been identified which provide " +
			"a seawater CO<sub>2</sub> value";
	private static final String NO_TIMESTAMP_ERROR_MSG =
			"No data columns have been identified which provide " +
			"the date and time of each measurement";
	private static final String NO_DATE_ERROR_MSG =
			"No data columns have been identified which provide " +
			"the date of each measurement";
	private static final String NO_TIME_ERROR_MSG =
			"No data columns have been identified which provide " +
			"the time of each measurement";
	private static final String MISSING_DATE_PIECE_ERROR_MSG =
			"The data columns identified do not completely specify " +
			"the date of each measurement";
	private static final String MISSING_TIME_PIECE_ERROR_MSG =
			"The data columns identified do not completely specify " +
			"the time of each measurement";

	private static final String DEFAULT_SECONDS_WARNING_QUESTION = 
			"No data columns have been identified providing the seconds " +
			"for the time of each measurement.  It is strongly recommended " +
			"that seconds be provided; however, a default value of zero " +
			"seconds can be added to the data." +
			"<br />" +
			"Is this okay?";
	private static final String USE_DEFAULT_SECONDS_TEXT = "Yes";
	private static final String NO_DEFAULT_SECONDS_TEXT = "No";

	private static final String GET_COLUMN_SPECS_FAIL_MSG = 
			"Problems obtaining the data column types";
	private static final String SUBMIT_FAIL_MSG = 
			"Problems updating the data column types";
	private static final String MORE_DATA_FAIL_MSG = 
			"Problems obtaining more data from the dataset";

	private static final String SANITY_CHECK_FAIL_MSG = 
			"Automatic data checking failed, " +
			"indicating very serious problems with the data.";
	private static final String SANITY_CHECK_ERROR_MSG = 
			"Automatic data checking found serious problems with the data.";
	private static final String SANITY_CHECK_WARNING_MSG = 
			"Automatic data checking found possible problems with the data";
	private static final String SANITY_CHECK_SUCCESS_MSG =
			"Automatic data checking did not find any problems with the data";

	interface CruiseDataColumnSpecsPageUiBinder extends UiBinder<Widget, DataColumnSpecsPage> {
	}

	private static CruiseDataColumnSpecsPageUiBinder uiBinder = 
			GWT.create(CruiseDataColumnSpecsPageUiBinder.class);

	private static DataSpecsServiceAsync service = 
			GWT.create(DataSpecsService.class);

	@UiField InlineLabel titleLabel;
	@UiField InlineLabel userInfoLabel;
	@UiField Button logoutButton;
	@UiField HTML introHtml;
	@UiField DataGrid<ArrayList<String>> dataGrid;
	@UiField Label pagerLabel;
	@UiField Label messagesLabel;
	@UiField SimplePager gridPager;
	@UiField Button messagesButton;
	@UiField Button submitButton;
	@UiField Button cancelButton;

	// Username associated with this page
	private String username;
	// Popup to confirm continue with default zero seconds
	private DashboardAskPopup defaultSecondsPopup;
	// Cruise associated with and updated by this page
	private DashboardCruise cruise;
	// List of CruiseDataColumn objects associated with the column Headers
	private ArrayList<CruiseDataColumn> cruiseDataCols;
	// Asynchronous data provider for the data grid 
	private AsyncDataProvider<ArrayList<String>> dataProvider;

	// Singleton instance of this page
	private static DataColumnSpecsPage singleton = null;

	/**
	 * Creates an empty cruise data column specification page.  
	 * Allows the user to update the data column types for a
	 * cruise when populated.
	 */
	DataColumnSpecsPage() {
		initWidget(uiBinder.createAndBindUi(this));
		singleton = this;

		username = "";
		defaultSecondsPopup = null;

		titleLabel.setText(TITLE_TEXT);
		logoutButton.setText(LOGOUT_TEXT);
		messagesButton.setText(MESSAGES_TEXT);
		pagerLabel.setText(PAGER_LABEL_TEXT);
		submitButton.setText(SUBMIT_TEXT);
		cancelButton.setText(CANCEL_TEXT);

		cruise = new DashboardCruise();
		cruiseDataCols = new ArrayList<CruiseDataColumn>();

		// Create the asynchronous data provider for the data grid
		dataProvider = new AsyncDataProvider<ArrayList<String>>() {
			@Override
			protected void onRangeChanged(HasData<ArrayList<String>> display) {
				// Ignore the call if there is no expocode assigned
				if ( cruise.getExpocode().isEmpty() )
					return;
				// Get the data for the cruise from the server
				final Range range = display.getVisibleRange();
				service.getCruiseData(DashboardLoginPage.getUsername(), 
						DashboardLoginPage.getPasshash(), 
						cruise.getExpocode(), range.getStart(), range.getLength(), 
						new AsyncCallback<ArrayList<ArrayList<String>>>() {
					@Override
					public void onSuccess(ArrayList<ArrayList<String>> newData) {
						updateRowData(range.getStart(), newData);
					}
					@Override
					public void onFailure(Throwable ex) {
						SocatUploadDashboard.showFailureMessage(MORE_DATA_FAIL_MSG, ex);
					}
				});
			}
		};
		dataProvider.addDataDisplay(dataGrid);
		// Assign the pager controlling which rows of the the data grid are shown
		gridPager.setDisplay(dataGrid);
	}

	/**
	 * Display the cruise data column specifications page for a cruise
	 * with the latest cruise data column specifications from the server.
	 * Adds this page to the page history.
	 * 
	 * @param expocode
	 * 		show the specifications for this cruise
	 */
	static void showPage(String expocode) {
		service.getCruiseDataColumnSpecs(DashboardLoginPage.getUsername(), 
								DashboardLoginPage.getPasshash(), expocode, 
								new AsyncCallback<DashboardCruiseWithData>() {
			@Override
			public void onSuccess(DashboardCruiseWithData cruiseSpecs) {
				if ( cruiseSpecs != null ) {
					if ( singleton == null )
						singleton = new DataColumnSpecsPage();
					SocatUploadDashboard.updateCurrentPage(singleton);
					singleton.updateCruiseColumnSpecs(cruiseSpecs);
					History.newItem(PagesEnum.IDENTIFY_COLUMNS.name(), false);
				}
				else {
					SocatUploadDashboard.showMessage(GET_COLUMN_SPECS_FAIL_MSG + 
						" (unexpected null cruise column specificiations)");
				}
			}
			@Override
			public void onFailure(Throwable ex) {
				SocatUploadDashboard.showFailureMessage(GET_COLUMN_SPECS_FAIL_MSG, ex);
			}
		});
	}

	/**
	 * Redisplays the last version of this page if the username
	 * associated with this page matches the current login username.
	 * 
	 * @param addToHistory 
	 * 		if true, adds this page to the page history 
	 */
	static void redisplayPage(boolean addToHistory) {
		// If never show before, or if the username does not match the 
		// current login username, show the login page instead
		if ( (singleton == null) || 
			 ! singleton.username.equals(DashboardLoginPage.getUsername()) ) {
			DashboardLoginPage.showPage(true);
		}
		else {
			SocatUploadDashboard.updateCurrentPage(singleton);
			if ( addToHistory )
				History.newItem(PagesEnum.IDENTIFY_COLUMNS.name(), false);
		}
	}

	/**
	 * Updates the data column specification page with the given
	 * column types and data.
	 * 
	 * @param cruiseSpecs
	 * 		current cruise data column type specifications and
	 * 		initial cruise data for display
	 */
	private void updateCruiseColumnSpecs(DashboardCruiseWithData cruiseSpecs) {
		username = DashboardLoginPage.getUsername();
		userInfoLabel.setText(WELCOME_INTRO + username);

		String status = cruiseSpecs.getDataCheckStatus();
		if ( status.equals(DashboardUtils.CHECK_STATUS_NOT_CHECKED) ||
			 status.equals(DashboardUtils.CHECK_STATUS_UNACCEPTABLE) ) {
			messagesLabel.setText(NOT_CHECKED_MSG);
			messagesButton.setEnabled(false);
		}
		else {
			String msgText;
			int numErrors = cruiseSpecs.getNumErrorMsgs();
			if ( numErrors == 0 )
				msgText = "no";
			else
				msgText = Integer.toString(numErrors);
			msgText += " errors; ";
			int numWarns = cruiseSpecs.getNumWarnMsgs();
			if ( numWarns == 0 )
				msgText += "no";
			else
				msgText += Integer.toString(numWarns);
			msgText += " warnings";
			messagesLabel.setText(msgText);
			if ( (numErrors == 0) && (numWarns == 0) )
				messagesButton.setEnabled(false);
			else
				messagesButton.setEnabled(true);
		}

		// Clear the expocode in case the data provider gets called while clearing
		cruise.setExpocode(null);

		// Delete any existing columns and headers
		int k = dataGrid.getColumnCount();
		while ( k > 0 ) {
			k--;
			dataGrid.removeColumn(k);
		}
		// Clear the list of CruiseDataColumns
		cruiseDataCols.clear();

		// Assign the new cruise information needed by this page
		cruise.setNumDataRows(cruiseSpecs.getNumDataRows());
		cruise.setDataColTypes(cruiseSpecs.getDataColTypes());
		cruise.setUserColNames(cruiseSpecs.getUserColNames());
		cruise.setDataColUnits(cruiseSpecs.getDataColUnits());
		cruise.setMissingValues(cruiseSpecs.getMissingValues());
		cruise.setWoceThreeRowIndices(cruiseSpecs.getWoceThreeRowIndices());
		cruise.setWoceFourRowIndices(cruiseSpecs.getWoceFourRowIndices());

		cruise.setExpocode(cruiseSpecs.getExpocode());

		introHtml.setHTML(INTRO_HTML_PROLOGUE +  
				SafeHtmlUtils.htmlEscape(cruise.getExpocode()) + 
				INTRO_HTML_EPILOGUE);

		// Rebuild the data grid using the provided CruiseDataColumnSpecs
		if ( cruise.getDataColTypes().size() < 4 )
			throw new IllegalArgumentException(
					"Unexpected small number of data columns: " + 
					cruise.getDataColTypes().size());
		int minTableWidth = 2;
		for (k = 0; k < cruise.getDataColTypes().size(); k++) {
			// TextColumn for displaying the data strings for this column
			ArrayListTextColumn dataColumn = new ArrayListTextColumn(k);
			// CruiseDataColumn for creating the Header cell for this column
			CruiseDataColumn cruiseColumn = new CruiseDataColumn(cruise, k);
			// Maintain a reference to the CruiseDataColumn object
			cruiseDataCols.add(cruiseColumn);
			// Add this data column and the header to the grid
			dataGrid.addColumn(dataColumn, cruiseColumn.getHeader());
			// Set the width of this column - all the same width
			dataGrid.setColumnWidth(dataColumn, DATA_COLUMN_WIDTH, Style.Unit.EM);
			// Add this width to the minimum table width
			minTableWidth += DATA_COLUMN_WIDTH;
		}
		// Set the minimum table width
		dataGrid.setMinimumTableWidth(minTableWidth, Style.Unit.EM);
		// Update the data provider with the data in the CruiseDataColumnSpecs
		dataProvider.updateRowCount(cruise.getNumDataRows(), true);
		dataProvider.updateRowData(0, cruiseSpecs.getDataValues());
		// Set the number of data rows to display in the grid.
		// This will refresh the view.
		dataGrid.setPageSize(DashboardUtils.MAX_ROWS_PER_GRID_PAGE);
	}

	@UiHandler("logoutButton")
	void logoutOnClick(ClickEvent event) {
		DashboardLogoutPage.showPage();
	}

	@UiHandler("cancelButton")
	void cancelOnClick(ClickEvent event) {
		// Return to the latest cruise listing page, which may  
		// have been updated from previous actions on this page.
		CruiseListPage.showPage(false);
	}

	@UiHandler("messagesButton") 
	void showMessagesOnClick(ClickEvent event) {
		DataMessagesPage.showPage(cruise.getExpocode());
	}

	@UiHandler("submitButton")
	void submitOnClick(ClickEvent event) {
		// longitude given?
		boolean hasLongitude = false;
		// latitude given?
		boolean hasLatitude = false;
		// sea water CO2 value given?
		boolean hasco2 = false;
		// time stamp given?
		boolean hasYear = false;
		boolean hasMonth = false;
		boolean hasDay = false;
		boolean hasHour = false;
		boolean hasMinute = false;
		boolean hasSecond = false;
		// data still given as unknown
		ArrayList<Integer> unknownIndices = new ArrayList<Integer>();

		// Check the column types 
		int k = 0;
		for ( DataColumnType colType : cruise.getDataColTypes() ) {
			if ( colType == DataColumnType.UNKNOWN ) {
				unknownIndices.add(k);
			}
			else if ( colType == DataColumnType.TIMESTAMP ) {
				hasYear = true;
				hasMonth = true;
				hasDay = true;
				hasHour = true;
				hasMinute = true;
				hasSecond = true;
			}
			else if ( colType == DataColumnType.DATE ) {
				hasYear = true;
				hasMonth = true;
				hasDay = true;
			}
			else if ( colType == DataColumnType.YEAR ) {
				hasYear = true;
			}
			else if ( colType == DataColumnType.MONTH ) {
				hasMonth = true;
			}
			else if ( colType == DataColumnType.DAY ) {
				hasDay = true;
			}
			else if ( colType == DataColumnType.TIME ) {
				hasHour = true;
				hasMinute = true;
				hasSecond = true;
			}
			else if ( colType == DataColumnType.HOUR ) {
				hasHour = true;
			}
			else if ( colType == DataColumnType.MINUTE ) {
				hasMinute = true;
			}
			else if ( colType == DataColumnType.SECOND ) {
				hasSecond = true;
			}
			else if ( colType == DataColumnType.LONGITUDE ) {
				hasLongitude = true;
			}
			else if ( colType == DataColumnType.LATITUDE ) {
				hasLatitude = true;
			}
			else if ( (colType == DataColumnType.XCO2WATER_EQU) ||
					  (colType == DataColumnType.XCO2WATER_SST) ||
					  (colType == DataColumnType.PCO2WATER_EQU) ||
					  (colType == DataColumnType.PCO2WATER_SST) ||
					  (colType == DataColumnType.FCO2WATER_EQU) ||
					  (colType == DataColumnType.FCO2WATER_SST) ) {
				hasco2 = true;
			}
			k++;
		}
		if ( unknownIndices.size() > 0 ) {
			// Unknown column data types found; put up error message and return
			ArrayList<String> colNames = cruise.getUserColNames();
			String errMsg = Integer.toString(unknownIndices.size()) + 
					UNKNOWN_COLUMN_TYPE_PROLOGUE;
			int cnt = 0;
			for ( int idx : unknownIndices ) {
				cnt++;
				if ( (cnt == 5) && (unknownIndices.size() > 5) ) {
					errMsg += "<li> ... </li>";
					break;
				}
				errMsg += "<li>" + SafeHtmlUtils.htmlEscape(colNames.get(idx)) + "</li>";
			}
			errMsg += UNKNOWN_COLUMN_TYPE_EPILOGUE;
			SocatUploadDashboard.showMessage(errMsg);
			return;
		}
		if ( ! hasLongitude ) {
			// no longitude - error
			SocatUploadDashboard.showMessage(NO_LONGITUDE_ERROR_MSG);
			return;
		}
		if ( ! hasLatitude ) {
			// no latitude - error
			SocatUploadDashboard.showMessage(NO_LATITUDE_ERROR_MSG);
			return;
		}
		if ( ! hasco2 ) {
			// no sea water CO2 - error
			SocatUploadDashboard.showMessage(NO_CO2_ERROR_MSG);
			return;
		}
		if ( ! (hasYear || hasMonth || hasDay || hasHour || hasMinute) ) {
			// timestamp completely missing - error
			SocatUploadDashboard.showMessage(NO_TIMESTAMP_ERROR_MSG);
			return;
		}
		if ( ! (hasYear || hasMonth || hasDay) ) {
			// date completely missing - error
			SocatUploadDashboard.showMessage(NO_DATE_ERROR_MSG);
			return;
		}
		if ( ! (hasHour || hasMinute) ) {
			// time completely missing - error
			SocatUploadDashboard.showMessage(NO_TIME_ERROR_MSG);
			return;
		}
		if ( ! (hasYear && hasMonth && hasDay) ) {
			// incomplete date given - error
			SocatUploadDashboard.showMessage(MISSING_DATE_PIECE_ERROR_MSG);
			return;
		}
		if ( ! (hasHour && hasMinute) ) {
			// incomplete time given - error
			SocatUploadDashboard.showMessage(MISSING_TIME_PIECE_ERROR_MSG);
			return;
		}

		if ( ! hasSecond ) {
			// Warning about missing seconds, asking whether to continue
			if ( defaultSecondsPopup == null ) {
				defaultSecondsPopup = new DashboardAskPopup(USE_DEFAULT_SECONDS_TEXT,
						NO_DEFAULT_SECONDS_TEXT, new AsyncCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean okay) {
						// Only continue if okay to use default zero for seconds
						if ( okay )
							doSubmit();
					}
					@Override
					public void onFailure(Throwable caught) {
						// never called
						;
					}
				});
			}
			defaultSecondsPopup.askQuestion(DEFAULT_SECONDS_WARNING_QUESTION);
			return;
		}

		// longitude, latitude, sea water co2, and some form of a timestamp 
		// is present so continue on  
		doSubmit();
	}

	private void doSubmit() {
		// Submit the updated data column types to the server.
		// This update invokes the SanityChecker on the data and
		// the results are then reported back to this page.
		service.updateCruiseDataColumnSpecs(DashboardLoginPage.getUsername(), 
								DashboardLoginPage.getPasshash(), cruise, 
								new AsyncCallback<DashboardCruiseWithData>() {
			@Override
			public void onSuccess(DashboardCruiseWithData specs) {
				if ( specs == null ) {
					SocatUploadDashboard.showMessage(SUBMIT_FAIL_MSG + 
							" (unexpected null cruise information returned)");
					return;
				}
				updateCruiseColumnSpecs(specs);
				String status = specs.getDataCheckStatus();
				if ( status.equals(DashboardUtils.CHECK_STATUS_NOT_CHECKED) ||
					 status.equals(DashboardUtils.CHECK_STATUS_UNACCEPTABLE) ) {
					// the sanity checker had serious problems
					SocatUploadDashboard.showMessage(SANITY_CHECK_FAIL_MSG);
				}
				else if ( status.startsWith(DashboardUtils.CHECK_STATUS_ERRORS_PREFIX) ) {
					// errors issued
					SocatUploadDashboard.showMessage(SANITY_CHECK_ERROR_MSG);
				}
				else if ( status.startsWith(DashboardUtils.CHECK_STATUS_WARNINGS_PREFIX) ) {
					// warnings issued
					SocatUploadDashboard.showMessage(SANITY_CHECK_WARNING_MSG);
				}
				else {
					// no problems
					SocatUploadDashboard.showMessage(SANITY_CHECK_SUCCESS_MSG);
				}
			}
			@Override
			public void onFailure(Throwable ex) {
				SocatUploadDashboard.showFailureMessage(SUBMIT_FAIL_MSG, ex);
			}
		});
	}

	/**
	 * TextColumn for displaying the value at a given index 
	 * of an ArrayList of Strings 
	 */
	private class ArrayListTextColumn extends TextColumn<ArrayList<String>> {
		private int colNum;
		/**
		 * Creates a TextColumn for an ArrayList of Strings that 
		 * displays the value at the given index in the ArrayList.
		 * @param colNum
		 * 		display data at this index of the ArrayList
		 */
		ArrayListTextColumn(int colNum) {
			super();
			this.colNum = colNum;
		}
		@Override
		public String getValue(ArrayList<String> dataRow) {
			if ( (dataRow != null) && (dataRow.size() > colNum) )
				return dataRow.get(colNum);
			else
				return "";
		}
		@Override
		public void render(Cell.Context ctx, ArrayList<String> obj, SafeHtmlBuilder sb) {
			Integer rowIdx = ctx.getIndex();
			if ( cruise.getWoceFourRowIndices().get(colNum).contains(rowIdx) ) {
				sb.appendHtmlConstant("<div style=\"background-color:" + 
						SocatUploadDashboard.ERROR_COLOR + ";\">");
				sb.appendEscaped(getValue(obj));
				sb.appendHtmlConstant("</div>");
			}
			else if ( cruise.getWoceThreeRowIndices().get(colNum).contains(rowIdx) ) {
				sb.appendHtmlConstant("<div style=\"background-color:" + 
						SocatUploadDashboard.WARNING_COLOR + ";\">");
				sb.appendEscaped(getValue(obj));
				sb.appendHtmlConstant("</div>");
			}
			else {
				super.render(ctx, obj, sb);
			}
		}
	}

}