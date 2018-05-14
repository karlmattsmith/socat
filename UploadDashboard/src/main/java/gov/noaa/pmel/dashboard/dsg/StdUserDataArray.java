/**
 *
 */
package gov.noaa.pmel.dashboard.dsg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeSet;

import gov.noaa.pmel.dashboard.datatype.CharDashDataType;
import gov.noaa.pmel.dashboard.datatype.DashDataType;
import gov.noaa.pmel.dashboard.datatype.DoubleDashDataType;
import gov.noaa.pmel.dashboard.datatype.IntDashDataType;
import gov.noaa.pmel.dashboard.datatype.KnownDataTypes;
import gov.noaa.pmel.dashboard.datatype.StringDashDataType;
import gov.noaa.pmel.dashboard.datatype.ValueConverter;
import gov.noaa.pmel.dashboard.server.DashboardServerUtils;
import gov.noaa.pmel.dashboard.shared.ADCMessage;
import gov.noaa.pmel.dashboard.shared.DashboardDatasetData;
import gov.noaa.pmel.dashboard.shared.DashboardUtils;
import gov.noaa.pmel.dashboard.shared.DataColumnType;
import gov.noaa.pmel.dashboard.shared.DataLocation;
import gov.noaa.pmel.dashboard.shared.QCFlag;
import gov.noaa.pmel.dashboard.shared.QCFlag.Severity;

/**
 * A 2-D array of objects corresponding to the standardized values of string values
 * provided by the user.  Also contains 1-D arrays of information describing each
 * data column.
 *
 * @author Karl Smith
 */
public class StdUserDataArray extends StdDataArray {

    public static final String INCONSISTENT_NUMBER_OF_DATA_VALUES_MSG =
            "inconstistent number of data values";

    private String[] userColNames;
    private String[] userUnits;
    private String[] userMissVals;
    private Boolean[] standardized;
    private ArrayList<ADCMessage> stdMsgList;
    int woceAutocheckIndex;

    /**
     * Create from the user's data column descriptions, data strings,
     * data row numbers, and data check flags given for this dataset.  Any
     * data columns types matching {@link DashboardServerUtils#UNKNOWN}
     * or {@link DashboardServerUtils#OTHER} are ignored;
     * {@link #isUsableIndex(int)} will return false, and
     * {@link #getStdVal(int, int)} will throw an exception
     * for data columns of these types.
     * <br /><br />
     * The list of automated data check messages describing problems
     * (critical errors) encountered when standardizing the data can
     * be retrieved using {@link #getStandardizationMessages()}.
     * <br /><br />
     * No bounds checking of standardized data values is performed.
     *
     * @param dataset
     *         dataset, with user's strings data, to use
     * @param knownTypes
     *         all known user data types
     * @throws IllegalArgumentException
     *         if there are no data values,
     *         if a data column description is not a known user data type,
     *         if a required unit conversion is not supported, or
     *         if a standardizer for a given data type is not known
     */
    public StdUserDataArray(DashboardDatasetData dataset,
            KnownDataTypes knownTypes) throws IllegalArgumentException {
        super(dataset.getDataColTypes(), knownTypes);

        // Add the user's units, missing values, and user column names
        userUnits = new String[numDataCols];
        userMissVals = new String[numDataCols];
        userColNames = new String[numDataCols];
        ArrayList<DataColumnType> dataColumnTypes = dataset.getDataColTypes();
        int numUserDataCols = dataColumnTypes.size();
        ArrayList<String> names = dataset.getUserColNames();
        if ( names.size() != numUserDataCols )
            throw new IllegalArgumentException("number of user column names (" +
                    names.size() + ") does not match the number of user column types (" +
                    numUserDataCols + ")");
        for (int k = 0; k < numUserDataCols; k++) {
            DataColumnType dataColType = dataColumnTypes.get(k);
            userUnits[k] = dataColType.getUnits().get(dataColType.getSelectedUnitIndex());
            if ( DashboardUtils.STRING_MISSING_VALUE.equals(userUnits[k]) )
                userUnits[k] = null;
            userMissVals[k] = dataColType.getSelectedMissingValue();
            if ( DashboardUtils.STRING_MISSING_VALUE.equals(userMissVals[k]) )
                userMissVals[k] = null;
            userColNames[k] = names.get(k);
        }
        // the StdDataArray constructor used adds SAMPLE_NUMBER and WOCE_AUTOCHECK
        woceAutocheckIndex = -1;
        for (int k = numUserDataCols; k < numDataCols; k++) {
            if ( DashboardServerUtils.WOCE_AUTOCHECK.typeNameEquals(dataTypes[k]) )
                woceAutocheckIndex = k;
            // use the standard unit, a default missing value string,
            // and the type display name for these added types
            userUnits[k] = dataTypes[k].getUnits().get(0);
            if ( DashboardUtils.STRING_MISSING_VALUE.equals(userUnits[k]) )
                userUnits[k] = null;
            userMissVals[k] = null;
            userColNames[k] = dataTypes[k].getDisplayName();
        }

        standardized = new Boolean[numDataCols];
        for (int k = 0; k < numDataCols; k++)
            standardized[k] = null;

        ArrayList<ArrayList<String>> dataVals = dataset.getDataValues();
        if ( dataVals.isEmpty() )
            throw new IllegalArgumentException("no data values given");
        numSamples = dataVals.size();

        ArrayList<Integer> rowNums = dataset.getRowNums();
        if ( rowNums.size() != numSamples )
            throw new IllegalArgumentException("number of row numbers (" +
                    rowNums.size() + ") does not match the number of samples (" +
                    numSamples + ")");

        stdObjects = new Object[numSamples][numDataCols];
        stdMsgList = new ArrayList<ADCMessage>();

        // Create a 2-D array of these Strings for efficiency
        String[][] strDataVals = new String[numSamples][numDataCols];
        int woceIdx = -1;
        for (int j = 0; j < numSamples; j++) {
            ArrayList<String> rowVals = dataVals.get(j);
            if ( rowVals.size() != numUserDataCols ) {
                // Generate a general message for this row - in case too long
                ADCMessage msg = new ADCMessage();
                msg.setSeverity(Severity.CRITICAL);
                msg.setRowNumber(j+1);
                msg.setGeneralComment(INCONSISTENT_NUMBER_OF_DATA_VALUES_MSG);
                msg.setDetailedComment(INCONSISTENT_NUMBER_OF_DATA_VALUES_MSG + "; " +
                        numUserDataCols + " expected but " + rowVals.size() + " found");
                stdMsgList.add(msg);
                // Continue on, assuming the missing values are at the end
            }
            for (int k = 0; k < numUserDataCols; k++) {
                try {
                    strDataVals[j][k] = rowVals.get(k);
                } catch ( IndexOutOfBoundsException ex ) {
                    // Setting it to null will generate a "no value given" message
                    strDataVals[j][k] = null;
                }
            }
            for (int k = numUserDataCols; k < numDataCols; k++) {
                if ( DashboardServerUtils.SAMPLE_NUMBER.typeNameEquals(dataTypes[k]) ) {
                    strDataVals[j][k] = rowNums.get(j).toString();
                }
                else if ( DashboardServerUtils.WOCE_AUTOCHECK.typeNameEquals(dataTypes[k]) ) {
                    // Default to acceptable; update afterwards
                    strDataVals[j][k] = DashboardServerUtils.FLAG_ACCEPTABLE.toString();
                    woceIdx = k;
                }
                else {
                    throw new IllegalArgumentException("unexpected unknown added data types");
                }
            }
        }
        // Add the automatic data checker WOCE flags
        if ( woceIdx >= 0 ) {
            for ( QCFlag flag : dataset.getCheckerFlags() ) {
                if ( DashboardServerUtils.WOCE_AUTOCHECK.getVarName().equals(flag.getFlagName()) ) {
                    Integer j = flag.getRowIndex();
                    if ( ! DashboardUtils.INT_MISSING_VALUE.equals(j) ) {
                        strDataVals[j][woceIdx] = flag.getFlagValue().toString();
                    }
                }
            }
        }

        // Standardize data columns
        boolean needsAnotherPass;
        do {
            needsAnotherPass = false;
            for (int k = 0; k < numDataCols; k++) {
                DashDataType<?> colType = dataTypes[k];
                if ( DashboardServerUtils.UNKNOWN.typeNameEquals(colType) ||
                        DashboardServerUtils.OTHER.typeNameEquals(colType) ) {
                    for (int j = 0; j < numSamples; j++) {
                        stdObjects[j][k] = null;
                    }
                    standardized[k] = null;
                }
                else {
                    try {
                        ValueConverter<?> stdizer = colType.getStandardizer(userUnits[k], userMissVals[k], this);
                        for (int j = 0; j < numSamples; j++) {
                            try {
                                stdObjects[j][k] = stdizer.convertValueOf(strDataVals[j][k]);
                            } catch ( IllegalArgumentException ex ) {
                                stdObjects[j][k] = null;
                                ADCMessage msg = new ADCMessage();
                                msg.setSeverity(Severity.CRITICAL);
                                msg.setRowNumber(j+1);
                                msg.setColNumber(k+1);
                                msg.setColName(userColNames[k]);
                                msg.setGeneralComment(ex.getMessage());
                                if ( strDataVals[j][k] == null )
                                    msg.setDetailedComment(ex.getMessage());
                                else
                                    msg.setDetailedComment(ex.getMessage() + ": \"" + strDataVals[j][k] + "\"");
                                stdMsgList.add(msg);
                            }
                        }
                        standardized[k] = true;
                    } catch ( IllegalStateException ex ) {
                        standardized[k] = false;
                        needsAnotherPass = true;
                    }
                }
            }
        } while ( needsAnotherPass );
    }

    /**
     * Check for missing longitude, latitude, sample depth, and time columns
     * or data values.  Any problems found generate messages that are added
     * to the internal list of messages.
     *
     * @return the sample times for the data;  may be null if there was incomplete
     *         specification of sample time, or may contain null values if there
     *         were problems computing the sample time
     */
    public Double[] checkMissingLonLatDepthTime() {
        try {
            Double[] longitudes = getSampleLongitudes();
            for (int j = 0; j < numSamples; j++) {
                if ( longitudes[j] == null ) {
                    ADCMessage msg = new ADCMessage();
                    msg.setSeverity(Severity.CRITICAL);
                    msg.setRowNumber(j+1);
                    msg.setColNumber(longitudeIndex+1);
                    msg.setColName(userColNames[longitudeIndex]);
                    String comment = "missing longitude";
                    msg.setGeneralComment(comment);
                    msg.setDetailedComment(comment);
                    stdMsgList.add(msg);
                }
            }
        } catch ( Exception ex ) {
            ADCMessage msg = new ADCMessage();
            msg.setSeverity(Severity.CRITICAL);
            String comment = "no longitude column";
            msg.setGeneralComment(comment);
            msg.setDetailedComment(comment);
            stdMsgList.add(msg);
        }

        try {
            Double[] latitudes = getSampleLatitudes();
            for (int j = 0; j < numSamples; j++) {
                if ( latitudes[j] == null ) {
                    ADCMessage msg = new ADCMessage();
                    msg.setSeverity(Severity.CRITICAL);
                    msg.setRowNumber(j+1);
                    msg.setColNumber(latitudeIndex+1);
                    msg.setColName(userColNames[latitudeIndex]);
                    String comment = "missing latitude";
                    msg.setGeneralComment(comment);
                    msg.setDetailedComment(comment);
                    stdMsgList.add(msg);
                }
            }
        } catch ( Exception ex ) {
            ADCMessage msg = new ADCMessage();
            msg.setSeverity(Severity.CRITICAL);
            String comment = "no latitude column";
            msg.setGeneralComment(comment);
            msg.setDetailedComment(comment);
            stdMsgList.add(msg);
        }

        try {
            Double[] depths = getSampleDepths();
            for (int j = 0; j < numSamples; j++) {
                if ( depths[j] == null ) {
                    ADCMessage msg = new ADCMessage();
                    msg.setSeverity(Severity.CRITICAL);
                    msg.setRowNumber(j+1);
                    msg.setColNumber(sampleDepthIndex+1);
                    msg.setColName(userColNames[sampleDepthIndex]);
                    String comment = "missing sample depth";
                    msg.setGeneralComment(comment);
                    msg.setDetailedComment(comment);
                    stdMsgList.add(msg);
                }
            }
        } catch ( Exception ex ) {
            ADCMessage msg = new ADCMessage();
            msg.setSeverity(Severity.CRITICAL);
            String comment = "no sample depth column";
            msg.setGeneralComment(comment);
            msg.setDetailedComment(comment);
            stdMsgList.add(msg);
        }

        Double[] times = null;
        try {
            times = getSampleTimes();
            for (int j = 0; j < numSamples; j++) {
                if ( times[j] == null ) {
                    ADCMessage msg = new ADCMessage();
                    msg.setSeverity(Severity.CRITICAL);
                    msg.setRowNumber(j+1);
                    String comment = "incomplete sample date/time specification";
                    msg.setGeneralComment(comment);
                    msg.setDetailedComment(comment);
                    stdMsgList.add(msg);
                }
            }
        } catch ( Exception ex ) {
            ADCMessage msg = new ADCMessage();
            msg.setSeverity(Severity.CRITICAL);
            String comment = "incomplete columns specifying sample date/time";
            msg.setGeneralComment(comment);
            msg.setDetailedComment(comment);
            stdMsgList.add(msg);
        }

        return times;
    }

    /**
     * Reorders the data rows as best possible so that the data is
     *         (1) ascending in time (old to new),
     *         (2) ascending in longitude
     *         (3) ascending in latitude
     *         (4) ascending in depth (shallow to deep)
     *         (5) original row number
     * Missing data columns (lon/lat/depth/time) will be treated
     * as an array of missing values.  Missing values in an array
     * will be ordered such that they appear before valid values.
     *
     * @param times
     *         sample times to be used for this data array;
     *         can be null to indicate sample times are not fully specified
     */
    public void reorderData(Double[] times) {
        Double[] longitudes;
        try {
            longitudes = getSampleLongitudes();
        } catch ( Exception ex ) {
            longitudes = null;
        }
        Double[] latitudes;
        try {
            latitudes = getSampleLatitudes();
        } catch ( Exception ex ) {
            latitudes = null;
        }
        Double[] depths;
        try {
            depths = getSampleDepths();
        } catch ( Exception ex ) {
            depths = null;
        }

        TreeSet<DataLocation> orderedSet = new TreeSet<DataLocation>();
        for (int rowIdx = 0; rowIdx < numSamples; rowIdx++) {
            DataLocation dataLoc = new DataLocation();
            // Assign the row index instead of the number
            dataLoc.setRowNumber(rowIdx);
            if ( longitudes != null )
                dataLoc.setLongitude(longitudes[rowIdx]);
            if ( latitudes != null )
                dataLoc.setLatitude(latitudes[rowIdx]);
            if ( depths != null )
                dataLoc.setDepth(depths[rowIdx]);
            if ( times != null ) {
                Double timeValSecs = times[rowIdx];
                if ( timeValSecs != null )
                    dataLoc.setDataDate( new Date(Math.round(timeValSecs * 1000.0)) );
            }
            // Leave dataValue as the missing value and add to the ordered set
            if ( ! orderedSet.add(dataLoc) )
                throw new RuntimeException("Unexpected duplicate data location with row number");
        }

        // Reorder the rows according to the ordering in orderedSet
        // Just assign the new order of the object arrays; no need to duplicate the objects themselves
        Object[][] orderedRows = new Object[numSamples][];
        int rowIdx = 0;
        for ( DataLocation dataLoc : orderedSet ) {
            // getRowNumber returns the row index assigned above
            orderedRows[rowIdx] = stdObjects[dataLoc.getRowNumber()];
            rowIdx++;
        }
        // Update the array of array of objects to the new ordering
        stdObjects = orderedRows;
    }

    /**
     * Checks that all values given (not missing values) are within the
     * acceptable range for that data type.  Any problems found generate
     * (error or warning) messages that are added to the internal list of
     * messages.
     */
    public void checkBounds() {
        for (int k = 0; k < numDataCols; k++) {
            DashDataType<?> dtype = dataTypes[k];

            if ( dtype instanceof StringDashDataType ) {
                StringDashDataType strtype = (StringDashDataType) dtype;
                for (int j = 0; j < numSamples; j++) {
                    ADCMessage msg = strtype.boundsCheckStandardValue(
                                            (String) stdObjects[j][k]);
                    if ( msg != null ) {
                        msg.setRowNumber(j+1);
                        msg.setColNumber(k+1);
                        msg.setColName(userColNames[k]);
                        stdMsgList.add(msg);
                    }
                }
            }
            else if ( dtype instanceof CharDashDataType ) {
                CharDashDataType chartype = (CharDashDataType) dtype;
                for (int j = 0; j < numSamples; j++) {
                    ADCMessage msg = chartype.boundsCheckStandardValue(
                                            (Character) stdObjects[j][k]);
                    if ( msg != null ) {
                        msg.setRowNumber(j+1);
                        msg.setColNumber(k+1);
                        msg.setColName(userColNames[k]);
                        stdMsgList.add(msg);
                    }
                }
            }
            else if ( dtype instanceof IntDashDataType ) {
                IntDashDataType inttype = (IntDashDataType) dtype;
                for (int j = 0; j < numSamples; j++) {
                    ADCMessage msg = inttype.boundsCheckStandardValue(
                                            (Integer) stdObjects[j][k]);
                    if ( msg != null ) {
                        msg.setRowNumber(j+1);
                        msg.setColNumber(k+1);
                        msg.setColName(userColNames[k]);
                        stdMsgList.add(msg);
                    }
                }
            }
            else if ( dtype instanceof DoubleDashDataType ) {
                DoubleDashDataType dbltype = (DoubleDashDataType) dtype;
                for (int j = 0; j < numSamples; j++) {
                    ADCMessage msg = dbltype.boundsCheckStandardValue(
                                            (Double) stdObjects[j][k]);
                    if ( msg != null ) {
                        msg.setRowNumber(j+1);
                        msg.setColNumber(k+1);
                        msg.setColName(userColNames[k]);
                        stdMsgList.add(msg);
                    }
                }
            }
            else {
                throw new IllegalArgumentException("unexpected data type encountered " +
                        "in bounds checking: " + dtype);
            }
        }
    }

    /**
     * @return the list of automated data check messages describing
     *         problems detected in the data.  The messages that are
     *         in this list comes from the constructor as well as any
     *         check methods that were called.
     */
    public ArrayList<ADCMessage> getStandardizationMessages() {
        return stdMsgList;
    }

    /**
     * Determines is this data column is an appropriate index.
     * Checks that the value is in the appropriate range and
     * that the column with this index has been standardized.
     *
     * @param idx
     *         index to test
     * @return if the index is valid
     */
    @Override
    public boolean isUsableIndex(int idx) {
        if ( idx < 0 )
            return false;
        if ( idx >= numDataCols )
            return false;
        return Boolean.TRUE.equals(standardized[idx]);
    }

    /**
     * Get the standard value object for the specified value (column index)
     * of the specified sample (row index).
     *
     * @param sampleIdx
     *         index of the sample (row)
     * @param columnIdx
     *         index of the data column
     * @return standard value object; null is returned for "missing value" or
     *         values that could not be interpreted
     * @throws IndexOutOfBoundsException
     *         if either the sample index or the column index is invalid
     * @throws IllegalArgumentException
     *         if the value cannot be standardized
     * @throws IllegalStateException
     *         if the value has not been standardized
     */
    @Override
    public Object getStdVal(int sampleIdx, int columnIdx)
            throws IndexOutOfBoundsException, IllegalArgumentException, IllegalStateException {
        if ( (sampleIdx < 0) || (sampleIdx >= numSamples) )
            throw new IndexOutOfBoundsException("sample index is invalid: " + sampleIdx);
        if ( (columnIdx < 0) || (columnIdx >= numDataCols) )
            throw new IndexOutOfBoundsException("data column index is invalid: " + columnIdx);
        if ( standardized[columnIdx] == null )
            throw new IllegalArgumentException("value cannot be standardized");
        if ( ! standardized[columnIdx] )
            throw new IllegalStateException("value has not been standardized");
        return stdObjects[sampleIdx][columnIdx];
    }

    /**
     * @return if this standardized user data array has a WOCE_AUTOCHECK column
     */
    public boolean hasWoceAutocheck() {
        if ( (woceAutocheckIndex < 0) || (woceAutocheckIndex >= numDataCols) )
            return false;
        return true;
    }

    /**
     * Reset all values in the WOCE_AUTOCHECK column, if it exists,
     * to {@link DashboardServerUtils#FLAG_ACCEPTABLE}.
     */
    public void resetWoceAutocheck() {
        if ( (woceAutocheckIndex < 0) || (woceAutocheckIndex >= numDataCols) )
            return;
        for (int j = 0; j < numSamples; j++)
            stdObjects[j][woceAutocheckIndex] = DashboardServerUtils.FLAG_ACCEPTABLE;
    }

    /**
     * Get the current WOCE_AUTOCHECK flag for a data sample (row).
     *
     * @param sampleIdx
     *         index of the data sample (row) to check
     * @return the current WOCE_AUTOCHECK flag for the data sample
     * @throws IllegalArgumentException
     *         if the sample index is invalid, or
     *         if there is not WOCE_AUTOCHECK column
     */
    public Character getWoceAutocheckFlag(int sampleIdx) throws IllegalArgumentException {
        if ( (sampleIdx < 0) || (sampleIdx >= numSamples) )
            throw new IndexOutOfBoundsException("sample index is invalid: " + sampleIdx);
        if ( (woceAutocheckIndex < 0) || (woceAutocheckIndex >= numDataCols) )
            throw new IllegalArgumentException("no WOCE autocheck column");
        return (Character) stdObjects[sampleIdx][woceAutocheckIndex];
    }

    /**
     * Set the WOCE_AUTOCHECK flag for a data sample (row).
     *
     * @param sampleIdx
     *         index of the data sample (row) to set
     * @param newFlag
     *         WOCE flag to assign
     * @throws IllegalArgumentException
     *         if the sample index is invalid,
     *         if there is not WOCE_AUTOCHECK column, or
     *         if the flag given is not a valid WOCE flag
     */
    public void setWoceAutocheck(int sampleIdx, Character newFlag) throws IllegalArgumentException {
        if ( (sampleIdx < 0) || (sampleIdx >= numSamples) )
            throw new IndexOutOfBoundsException("sample index is invalid: " + sampleIdx);
        if ( (woceAutocheckIndex < 0) || (woceAutocheckIndex >= numDataCols) )
            throw new IllegalArgumentException("no WOCE autocheck column");
        if ( ! ( DashboardServerUtils.FLAG_ACCEPTABLE.equals(newFlag) ||
                 DashboardServerUtils.WOCE_QUESTIONABLE.equals(newFlag) ||
                 DashboardServerUtils.WOCE_BAD.equals(newFlag) ) )
            throw new IllegalArgumentException("invalid WOCE flag value");
        stdObjects[sampleIdx][woceAutocheckIndex] = newFlag;
    }

    @Override
    public int hashCode() {
        final int prime = 37;
        int result = super.hashCode();
        result = prime * result + stdMsgList.hashCode();
        result = prime * result + woceAutocheckIndex;
        result = prime * result + Arrays.hashCode(standardized);
        result = prime * result + Arrays.hashCode(userMissVals);
        result = prime * result + Arrays.hashCode(userUnits);
        result = prime * result + Arrays.hashCode(userColNames);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( ! super.equals(obj) )
            return false;

        if ( ! ( obj instanceof StdUserDataArray ) )
            return false;
        StdUserDataArray other = (StdUserDataArray) obj;

        if ( ! stdMsgList.equals(other.stdMsgList) )
            return false;
        if ( woceAutocheckIndex != other.woceAutocheckIndex )
            return false;
        if ( ! Arrays.equals(standardized, other.standardized) )
            return false;
        if ( ! Arrays.equals(userColNames, other.userColNames) )
            return false;
        if ( ! Arrays.equals(userMissVals, other.userMissVals) )
            return false;
        if ( ! Arrays.equals(userUnits, other.userUnits) )
            return false;

        return true;
    }

    @Override
    public String toString() {
        String repr = "StdUserDataArray[numSamples=" + numSamples +
                ", numDataCols=" + numDataCols +
                "' woceAutocheckIndex=" + woceAutocheckIndex;
        repr += ",\n  stdMsgList=[";
        boolean first = true;
        for ( ADCMessage msg : stdMsgList ) {
            if ( first )
                first = false;
            else
                repr += ",";
            repr += "\n    " + msg.toString();
        }
        repr += "\n  ]";
        repr += ",\n  userColNames=" + Arrays.toString(userColNames);
        repr += ",\n  userUnits=" + Arrays.toString(userUnits);
        repr += ",\n  userMissVals=" + Arrays.toString(userMissVals);
        repr += ",\n  standardized=" + Arrays.toString(standardized);
        String superRepr = super.toString();
        int idx = superRepr.indexOf('\n');
        repr += "," + superRepr.substring(idx);
        return repr;
    }

}
