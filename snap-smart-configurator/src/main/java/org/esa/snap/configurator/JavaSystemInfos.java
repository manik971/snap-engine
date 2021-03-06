/*
 * Copyright (C) 2015 CS SI
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.configurator;

import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Class for system information retreival, such as RAM, CPUs, disks, etc.
 *
 * @author Nicolas Ducoin
 * @version $Revisions$ $Dates$
 */
public class JavaSystemInfos implements SystemInfos {


    private String[] diskNames = null;
    private Map<String, DiskBenchmarker> disksBenchMarkers = null;
    private int benchmarkFileSize = 0;
    private int benchmarkNbSamples = 0;

    private static JavaSystemInfos systemInfos = null;

    /**
     * Constructor is private, this is class is a singleton. This way it not to recompute all information each time we
     * need an instance of JavaSystemInfos.
     */
    private JavaSystemInfos(int benchmarkFileSize, int benchmarkNbSamples) {
        this.benchmarkNbSamples = benchmarkNbSamples;
        this.benchmarkFileSize = benchmarkFileSize;

        updateSystemInfos();
    }

    /**
     * Returns an instance of SystemInfos
     * @return an instance of JavaSystemInfos
     */
    public static JavaSystemInfos getInstance() {
        if(systemInfos == null) {
            systemInfos = new JavaSystemInfos(DiskBenchmarker.DEFAULT_FILE_SIZE, DiskBenchmarker.DEFAULT_NB_SAMPLES);
        }
        return systemInfos;
    }

    /**
     * Returns an instance of SystemInfos
     * @param benchmarkFileSize the size of the file used to perform bechmarks, in MegaByte
     * @param benchmarkNbSamples the number of samples used to perform benchmarks
     * @return an instance of JavaSystemInfos
     */
    public static JavaSystemInfos getInstance(int benchmarkFileSize, int benchmarkNbSamples) {
        if(systemInfos == null) {
            systemInfos = new JavaSystemInfos(benchmarkFileSize, benchmarkNbSamples);
        }
        return systemInfos;
    }

    /**
     * initialisation
     */
    private void updateSystemInfos() {
        diskNames = getDisksNames();

        disksBenchMarkers = new HashMap<String, DiskBenchmarker>();

        for(String diskName : diskNames) {
            DiskBenchmarker benchmarker = new DiskBenchmarker(diskName, benchmarkFileSize, benchmarkNbSamples);
            disksBenchMarkers.put(diskName, benchmarker);
        }
    }

        @Override
    public int getNbCPUs() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public long getRAM() {
        OperatingSystemMXBean mxbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalRAMInByte = mxbean.getTotalPhysicalMemorySize();
        long totalRAMInMegaBytes = Math.round(totalRAMInByte/(1E6));
        return totalRAMInMegaBytes;
    }

    @Override
    public long getFreeRAM() {
        OperatingSystemMXBean mxbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long freeRAMInByte = mxbean.getFreePhysicalMemorySize();
        long freeRAMInMegaBytes = Math.round(freeRAMInByte/(1024*1024));
        return freeRAMInMegaBytes;
    }

    @Override
    public long getThisAppRam() {
        long thisAppRAMInBytes = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        long ThisAppRAMInMegaBytes = Math.round(thisAppRAMInBytes/(1024*1024));
        return ThisAppRAMInMegaBytes;

    }

    @Override
    public String[] getDisksNames() {
        if(diskNames==null) {
            Iterable<Path> fileSystemsIterable = FileSystems.getDefault().getRootDirectories();
            Vector<String> driveNames = new Vector<>();
            for (Path path : fileSystemsIterable) {
                driveNames.add(path.toString());
            }

            String[] driveNamesArray = new String[driveNames.size()];
            diskNames = driveNames.toArray(driveNamesArray);
        }

        return diskNames;
    }

    @Override
    public long getDiskFreeSize(String diskId) {
        File thisDisk = new File(diskId);
        long diskFreeSpaceInBytes = thisDisk.getFreeSpace();
        long diskFreeSpaceInMegaBytes = Math.round(diskFreeSpaceInBytes / (1024 * 1024));
        return diskFreeSpaceInMegaBytes;
    }

    @Override
    public double getDiskWriteSpeed(String diskId) throws IOException {
        return disksBenchMarkers.get(diskId).getWriteSpeed();
    }

    @Override
    public double getDiskReadSpeed(String diskId) throws IOException {
        return disksBenchMarkers.get(diskId).getReadSpeed();
    }
}
