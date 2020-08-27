package com.hkxx.drone.worker;


import com.hkxx.drone.Config;
import com.hkxx.drone.worker.template.LinkhubProfile;
import com.hkxx.drone.worker.template.TemplateHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manage Linkhub Profiles.
 * Just create / modify / delete profile files in file system,
 * Linkhub should scan the change of file and update its service.
 */
public class LinkhubProfileManager {

    public static final String SUFFIX_PROFILE = ".lrp";
    private static Map<String, LinkhubProfile> profiles = new HashMap<>();

    /*
     * Create or update the profile file.
     */
    public static boolean updateProfile(LinkhubProfile profile) {
        if (profile == null) {
            return false;
        }
        // load the profile
        String templateStr = TemplateHelper.loadTemplate(profile.getTemplateName(), profile.addToMap(null));
        if (templateStr == null) {
            return false;
        }
        synchronized (profiles) {
            profiles.put(profile.name, profile);
        }
        File file = new File(Config.linkhubProfilePath, profile.name + SUFFIX_PROFILE);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(templateStr);
            fileWriter.flush();
            fileWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Delete the profile file.
     */
    public static boolean deleteProfile(String profileName) {
        if (profileName == null) {
            return false;
        }
        synchronized (profiles) {
            if (profiles.containsKey(profileName)) {
                profiles.remove(profileName);
            }
        }
        File file = new File(Config.linkhubProfilePath, profileName + SUFFIX_PROFILE);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return true;
    }

    /*
     * Delete all profile files in the dir.
     */
    public static boolean removeAll() {
        synchronized (profiles) {
            profiles.clear();
        }
        File dir = new File(Config.linkhubProfilePath);
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(SUFFIX_PROFILE)) {
                    file.delete();
                }
            }
        }
        return true;
    }
}
