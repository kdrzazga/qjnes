package org.fortytwo.c64.model.io;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

public class D64File {
    public static final int NUM_TRACKS = 35;

    public Track[] tracks;

    public D64File(File d64File) throws IOException {
        var fileIn = new FileInputStream(d64File);
        parse(fileIn);
    }

    private void parse(InputStream in) throws IOException {
        tracks = new Track[NUM_TRACKS];

        Track track = null;
        for (int t = 0; t < NUM_TRACKS; t++) {
            tracks[t] = new Track(t + 1);
            track = tracks[t];
            int numSectors = track.getSectorCount();

            for (int s = 0; s < numSectors; s++) {
                var sector = new Sector(in);
                track.addSector(sector);
            }
        }
    }

    /*
     * Returns a particular Track, starting with index 1
     */
    public Track getTrack(int trackNum) {
        return tracks[trackNum - 1];
    }

    public class Track {
        public List<Sector> sectors;
        int trackNum;

        public Track(int trackNum) {
            this.trackNum = trackNum;
            sectors = new ArrayList<>();
        }

        public int getTrackNumber() {
            return trackNum;
        }

        public void addSector(Sector sector) {
            sectors.add(sector);
        }

        public Sector getSector(int sectorNum) {
            return sectors.get(sectorNum);
        }

        public int getSectorCount() {
            if (trackNum >= 1 && trackNum <= 17) {
                return 21;
            } else if (trackNum >= 18 && trackNum <= 24) {
                return 19;
            } else if (trackNum >= 15 && trackNum <= 30) {
                return 18;
            } else {
                return 17;
            }
        }
    }

    public class Sector {
        public static final int SECTOR_LENGTH = 256;
        public byte[] data;

        Sector(InputStream in) throws IOException {
            data = new byte[SECTOR_LENGTH];
            in.read(data);
        }

        Sector(byte[] src) {
            if (src.length > SECTOR_LENGTH) {
                throw new RuntimeException("Sectors can only be " + SECTOR_LENGTH + " bytes");
            }
            data = new byte[SECTOR_LENGTH];
            System.arraycopy(src, 0, data, 0, src.length);
        }
    }

}
