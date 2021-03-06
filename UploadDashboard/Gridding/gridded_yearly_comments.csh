#!/bin/csh 
#


ncatted -O -h -a title,global,o,c,'SOCAT gridded v2020 Yearly 1x1 degree gridded dataset' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,global,o,c,'Surface Ocean Carbon Atlas (SOCAT) Gridded (binned) SOCAT observations, with a spatial grid of \n1x1 degree and yearly in time. The gridded fields are computed from the monthly 1-degree gridded data, \nwhich uses only SOCAT datasets with QC flags of A through D and SOCAT data points flagged with WOCE \nflag values of 2. This yearly data is computed using data from the start to the end of each year as \ndescribed in the summary attribute of each variable.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a references,global,o,c,'http://www.socat.info/' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a SOCAT_Notes,global,o,c,'SOCAT gridded v2020 05-June-2020' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a caution,global,o,c,'NO INTERPOLATION WAS PERFORMED. SIGNIFICANT BIASES ARE PRESENT IN THESE GRIDDED RESULTS DUE TO THE \nARBITRARY AND SPARSE LOCATIONS OF DATA VALUES IN BOTH SPACE AND TIME.' SOCAT_tracks_gridded_yearly.nc

ncatted -O -h -a summary,count_ncruise_year,o,c,'Counts datasets which returned any data in the month and grid cell. The monthly cruise counts within the year are summed.'  SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,fco2_count_nobs_year,o,c,'Counts all observations in the month and grid cell. The monthly observation counts within the year are summed.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,fco2_ave_unwtd_year,o,c,'Mean of all observations from all datasets. The means for the months within the year are averaged. They are not re-weighted for the varying lengths of the months.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,fco2_ave_weighted_year,o,c,'The weighted cruise means for the months within the year is averaged.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,fco2_max_unwtd_year,o,c,'The maximum monthly value for the year' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,fco2_min_unwtd_year,o,c,'The minimum monthly value for the year' SOCAT_tracks_gridded_yearly.nc

ncatted -O -h -a summary,sst_count_nobs_year,o,c,'Counts all observations in the month and grid cell. The monthly observation counts within the year are summed.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,sst_ave_unwtd_year,o,c,'Mean of all observations from all datasets. The means for the months within the year are averaged. They are not re-weighted for the varying lengths of the months.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,sst_ave_weighted_year,o,c,'The weighted cruise means for the months within the year is averaged.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,sst_max_unwtd_year,o,c,'The maximum monthly value for the year' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,sst_min_unwtd_year,o,c,'The minimum monthly value for the year' SOCAT_tracks_gridded_yearly.nc

ncatted -O -h -a summary,salinity_count_nobs_year,o,c,'Counts all observations in the month and grid cell. The monthly observation counts within the year are summed.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,salinity_ave_unwtd_year,o,c,'Mean of all observations from all datasets. The means for the months within the year are averaged. They are not re-weighted for the varying lengths of the months.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,salinity_ave_weighted_year,o,c,'The weighted cruise means for the months within the year is averaged.' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,salinity_max_unwtd_year,o,c,'The maximum monthly value for the year' SOCAT_tracks_gridded_yearly.nc
ncatted -O -h -a summary,salinity_min_unwtd_year,o,c,'The minimum monthly value for the year' SOCAT_tracks_gridded_yearly.nc

ncdump -h SOCAT_tracks_gridded_yearly.nc
