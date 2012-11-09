package com.jb;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

public class TagCleanup {

    private final static String CLARK_HOWARD_TRAVEL_TIP_PREFIX = "ch_travel_tip_";
    // private final static String CLARK_HOWARD_SHOW_PREFIX = "clark_howard_";
    // Clark Howard_ 07.06.12 - Hour 2.mp3
    // Clark Howard_ 10.29.12 - Hour 1
    private final static String CLARK_HOWARD_SHOW_PREFIX = "Clark Howard_ ";
    // private final static String GLINK_SHOW_PREFIX = "igs-";
    // 01 Ilyce Glink Show – Personal Finan.mp3
    private final static String GLINK_SHOW_PREFIX = "Ilyce Glink Show";
    private final static int DAYS_IN_YEAR = 366;

    public static void cleanupFolder(File startDir) {
        System.out.println("Processing folder " + startDir.getAbsolutePath());
        Collection<File> files = FileUtils.listFiles(startDir, new String[] { "mp3" }, true);
        System.out.println("Found " + files.size() + " files");
        for (File file : files) {
            cleanupFile(file);
        }
    }

    public static void cleanupFile(File file) {
        System.out.println("Processing file " + file.getAbsolutePath());
        try {
            String fileName = file.getName();
            fileName = StringUtils.removeEndIgnoreCase(fileName, ".mp3");
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            System.out.println("Before:  File=" + fileName + ", Artist=" + tag.getFirst(FieldKey.ARTIST) + ". Album=" + tag.getFirst(FieldKey.ALBUM) + ", Track=" + tag.getFirst(FieldKey.TRACK));
            if (fileName.startsWith(CLARK_HOWARD_TRAVEL_TIP_PREFIX)) {
                FileUtils.deleteQuietly(file);
                return;
            } else if (fileName.startsWith(CLARK_HOWARD_SHOW_PREFIX)) {
                // tag.setField(FieldKey.ARTIST, "Clark Howard");
                // tag.setField(FieldKey.ALBUM, "Clark Howard Show");
                // old: of the form clark_howard_061912_101691831.mp3 or ch_travel_tip_101689361.mp3
                // of the form Clark Howard: 07.06.12 - Hour 2
                String title = tag.getFirst(FieldKey.TITLE);
                String[] titleParts = title.split(" ");

                SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yy");
                Date date = sdf.parse(titleParts[2]);
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                int newTrackNumber = (DAYS_IN_YEAR - cal.get(Calendar.DAY_OF_YEAR)) * 2;
                // if ("Hr2".equalsIgnoreCase(titleParts[3])) {
                // newTrackNumber++;
                // }
                if (title.endsWith("Hour 2")) {
                    newTrackNumber++;
                }
                tag.setField(FieldKey.TRACK, StringUtils.leftPad(newTrackNumber + "", 9, "0"));
                // tag.setField(FieldKey.YEAR, String.valueOf(cal.get(Calendar.YEAR)));
                // tag.setField(FieldKey.GENRE, "Podcast");
                audioFile.commit();
                tag = audioFile.getTag();
            } else if (fileName.contains(GLINK_SHOW_PREFIX)) {
                // tag.setField(FieldKey.ARTIST, "Ilyce Glink");
                // tag.setField(FieldKey.ALBUM, "Ilyce Glink Show");
                // of the form igs-6-10-12
                // Ilyce Glink Show – Personal Finance Advice – June 24, 2012
                String title = tag.getFirst(FieldKey.TITLE);
                String showDate = title.substring("Ilyce Glink Show – Personal Finance Advice – ".length());

                SimpleDateFormat sdf = new SimpleDateFormat("MMMMMMMMMMMMM dd, yyyy");
                Date date = sdf.parse(showDate);
                Calendar cal = new GregorianCalendar();
                cal.setTime(date);
                int newTrackNumber = DAYS_IN_YEAR - cal.get(Calendar.DAY_OF_YEAR);
                tag.setField(FieldKey.TRACK, StringUtils.leftPad(newTrackNumber + "", 9, "0"));
                audioFile.commit();
                tag = audioFile.getTag();
            }
            System.out.println("After:  File=" + fileName + ", Artist=" + tag.getFirst(FieldKey.ARTIST) + ". Album=" + tag.getFirst(FieldKey.ALBUM) + ", Track=" + tag.getFirst(FieldKey.TRACK));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(new Date() + " begin");
        if (args.length == 1) {
            File file = new File(args[0]);
            if (file.exists()) {
                if (file.isDirectory()) {
                    cleanupFolder(file);
                } else if (file.isFile()) {
                    cleanupFile(file);
                } else {
                    System.out.println("The file, " + file.getAbsolutePath() + ", exists but it isn't a file or folder!!!");
                }
            } else {
                System.out.println("The file, " + file.getAbsolutePath() + ", doesn't exist!!!");
            }
            System.out.println(new Date() + " complete");
        } else {
            // System.out.println("Usage: TagCleanup file/folder");
            cleanupFolder(new File("/Users/jonathan/Music/iTunes/iTunes Media/Podcasts/The Clark Howard Show"));
            cleanupFolder(new File("/Users/jonathan/Music/iTunes/iTunes Media/Podcasts/ThinkGlink » Audio"));
        }
    }
}
