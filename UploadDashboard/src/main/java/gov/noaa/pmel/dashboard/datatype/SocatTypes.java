/**
 *
 */
package gov.noaa.pmel.dashboard.datatype;

import gov.noaa.pmel.dashboard.server.DashboardServerUtils;
import gov.noaa.pmel.dashboard.shared.DashboardUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * SOCAT standard types required in various classes.
 *
 * @author Karl Smith
 */
public class SocatTypes {

    public static final String CO2_CATEGORY = "CO2";
    public static final String PRESSURE_CATEGORY = "Pressure";
    public static final String SALINITY_CATEGORY = "Salinity";
    public static final String TEMPERATURE_CATEGORY = "Temperature";

    // Unit arrays for static types in this class
    public static final ArrayList<String> SALINITY_UNITS =
            new ArrayList<String>(Arrays.asList("PSU"));

    public static final ArrayList<String> TEMPERATURE_UNITS =
            new ArrayList<String>(Arrays.asList("degrees C"));

    public static final ArrayList<String> PRESSURE_UNITS =
            new ArrayList<String>(Arrays.asList("hPa", "kPa", "mmHg"));

    public static final ArrayList<String> XCO2_UNITS =
            new ArrayList<String>(Arrays.asList("umol/mol"));

    public static final ArrayList<String> PCO2_UNITS =
            new ArrayList<String>(Arrays.asList("uatm"));

    public static final ArrayList<String> FCO2_UNITS =
            new ArrayList<String>(Arrays.asList("uatm"));

    public static final ArrayList<String> DISTANCE_UNITS =
            new ArrayList<String>(Arrays.asList("km"));


    // Additional metadata
    public static final StringDashDataType SOCAT_VERSION = new StringDashDataType("socat_version",
            200.0, "SOCAT version",
            "SOCAT Version number with status", null,
            null, DashboardUtils.NO_UNITS);

    public static final StringDashDataType ALL_REGION_IDS = new StringDashDataType("all_region_ids",
            201.0, "all Region IDs",
            "Sorted unique region IDs", null,
            null, DashboardUtils.NO_UNITS);

    public static final StringDashDataType SOCAT_DOI = new StringDashDataType("socat_doi",
            202.0, "SOCAT DOI",
            "DOI of SOCAT-enhanced data", null,
            null, DashboardUtils.NO_UNITS);


    // Additional data provided by the user
    public static final DoubleDashDataType SALINITY = new DoubleDashDataType("sal",
            600.0, "salinity",
            "salinity", "sea_surface_salinity",
            SALINITY_CATEGORY, SALINITY_UNITS);


    public static final DoubleDashDataType TEQU = new DoubleDashDataType("Temperature_equi",
            610.0, "T_equ",
            "equilibrator chamber temperature", null,
            TEMPERATURE_CATEGORY, TEMPERATURE_UNITS);

    public static final DoubleDashDataType SST = new DoubleDashDataType("temp",
            611.0, "SST",
            "sea surface temperature", "sea_surface_temperature",
            TEMPERATURE_CATEGORY, TEMPERATURE_UNITS);


    public static final DoubleDashDataType PEQU = new DoubleDashDataType("Pressure_equi",
            620.0, "P_equ",
            "equilibrator chamber pressure", null,
            PRESSURE_CATEGORY, PRESSURE_UNITS);

    public static final DoubleDashDataType PATM = new DoubleDashDataType("Pressure_atm",
            621.0, "P_atm",
            "sea-level air pressure", "air_pressure_at_sea_level",
            PRESSURE_CATEGORY, PRESSURE_UNITS);


    public static final DoubleDashDataType XCO2_WATER_TEQU_DRY = new DoubleDashDataType("xCO2_water_equi_temp_dry_ppm",
            630.0, "xCO2_water_Tequ_dry",
            "water xCO2 dry using equi temp",
            "mole_fraction_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, XCO2_UNITS);

    public static final DoubleDashDataType XCO2_WATER_SST_DRY = new DoubleDashDataType("xCO2_water_sst_dry_ppm",
            631.0, "xCO2_water_SST_dry",
            "water xCO2 dry using sst",
            "mole_fraction_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, XCO2_UNITS);

    public static final DoubleDashDataType PCO2_WATER_TEQU_WET = new DoubleDashDataType("pCO2_water_equi_temp",
            632.0, "pCO2_water_Tequ_wet",
            "water pCO2 wet using equi temp",
            "surface_partial_pressure_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, PCO2_UNITS);

    public static final DoubleDashDataType PCO2_WATER_SST_WET = new DoubleDashDataType("pCO2_water_sst_100humidity_uatm",
            633.0, "pCO2_water_SST_wet",
            "water pCO2 wet using sst",
            "surface_partial_pressure_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, PCO2_UNITS);

    public static final DoubleDashDataType FCO2_WATER_TEQU_WET = new DoubleDashDataType("fCO2_water_equi_uatm",
            634.0, "fCO2_water_Tequ_wet",
            "water fCO2 wet using equi temp",
            "surface_partial_pressure_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, FCO2_UNITS);

    public static final DoubleDashDataType FCO2_WATER_SST_WET = new DoubleDashDataType("fCO2_water_sst_100humidity_uatm",
            635.0, "fCO2_water_SST_wet",
            "water fCO2 wet using sst",
            "surface_partial_pressure_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, FCO2_UNITS);


    public static final StringDashDataType WOCE_CO2_WATER = new StringDashDataType("WOCE_CO2_water",
            650.0, "WOCE CO2_water",
            "WOCE flag for aqueous CO2", null,
            DashboardServerUtils.QUALITY_CATEGORY,
            DashboardUtils.NO_UNITS);

    /**
     * User-provided comment for WOCE_CO2_WATER; user type only, used for generating WOCE events from user-provided
     * data.
     */
    public static final StringDashDataType COMMENT_WOCE_CO2_WATER = new StringDashDataType("comment_WOCE_CO2_water",
            651.0, "comment WOCE CO2_water",
            "comment about WOCE_CO2_water flag",
            null,
            null, DashboardUtils.NO_UNITS);

    public static final StringDashDataType WOCE_CO2_ATM = new StringDashDataType("WOCE_CO2_atm",
            652.0, "WOCE_CO2_atm",
            "WOCE flag for atmospheric CO2", null,
            DashboardServerUtils.QUALITY_CATEGORY,
            DashboardUtils.NO_UNITS);

    /**
     * User-provided comment for WOCE_CO2_ATM; user type only, used for generating WOCE events from user-provided data.
     */
    public static final StringDashDataType COMMENT_WOCE_CO2_ATM = new StringDashDataType("comment_WOCE_CO2_atm",
            653.0, "comment WOCE CO2_atm",
            "comment about WOCE_CO2_atm flag", null,
            null, DashboardUtils.NO_UNITS);


    // Computed or looked-up values
    public static final DoubleDashDataType WOA_SALINITY = new DoubleDashDataType("woa_sss",
            700.0, "WOA SSS",
            "salinity from World Ocean Atlas",
            "sea_surface_salinity",
            SALINITY_CATEGORY, SALINITY_UNITS);

    public static final DoubleDashDataType NCEP_SLP = new DoubleDashDataType("pressure_ncep_slp",
            701.0, "NCEP SLP",
            "sea level air pressure from NCEP/NCAR reanalysis",
            "air_pressure_at_sea_level",
            PRESSURE_CATEGORY, PRESSURE_UNITS);

    public static final StringDashDataType REGION_ID = new StringDashDataType("region_id",
            702.0, "Region ID",
            "SOCAT region ID",
            null, DashboardServerUtils.LOCATION_CATEGORY,
            DashboardUtils.NO_UNITS);

    public static final DoubleDashDataType ETOPO2_DEPTH = new DoubleDashDataType("etopo2",
            705.0, "ETOPO2 depth",
            "bathymetry from ETOPO2", "sea_floor_depth",
            DashboardServerUtils.BATHYMETRY_CATEGORY,
            DashboardUtils.DEPTH_UNITS);

    public static final DoubleDashDataType GVCO2 = new DoubleDashDataType("gvCO2",
            706.0, "GlobalView CO2",
            "GlobalView xCO2",
            "mole_fraction_of_carbon_dioxide_in_air",
            CO2_CATEGORY, XCO2_UNITS);

    public static final DoubleDashDataType DIST_TO_LAND = new DoubleDashDataType("dist_to_land",
            707.0, "dist to land",
            "distance to major land mass", null,
            DashboardServerUtils.LOCATION_CATEGORY, DISTANCE_UNITS);

    public static final DoubleDashDataType FCO2_REC = new DoubleDashDataType("fCO2_recommended",
            710.0, "fCO2_rec",
            "fCO2 recommended",
            "surface_partial_pressure_of_carbon_dioxide_in_sea_water",
            CO2_CATEGORY, FCO2_UNITS);

    public static final IntDashDataType FCO2_SOURCE = new IntDashDataType("fCO2_source",
            711.0, "fCO2 src",
            "Algorithm number for recommended fCO2", null,
            DashboardServerUtils.IDENTIFIER_CATEGORY,
            DashboardUtils.NO_UNITS);

}