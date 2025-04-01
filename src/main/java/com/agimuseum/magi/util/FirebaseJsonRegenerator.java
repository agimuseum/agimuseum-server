package com.agimuseum.magi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to regenerate the Firebase service account JSON file
 * if the current one is corrupted.
 */
@Component
public class FirebaseJsonRegenerator {

    @Value("${firebase.config.path}")
    private String firebaseConfigPath;

    /**
     * Regenerates the Firebase service account JSON file from the values
     * in your application properties.
     *
     * Note: You should only use this if your current JSON file is corrupted.
     */
    public void regenerateFirebaseJson() throws IOException {
        // Replace these values with your actual Firebase service account details
        Map<String, Object> serviceAccount = new HashMap<>();
        serviceAccount.put("type", "service_account");
        serviceAccount.put("project_id", "agimuseum");
        serviceAccount.put("private_key_id", "9a639db8d559005c1a0c6d1a26d0ed5344411911");
        serviceAccount.put("private_key", "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCUstEL0XbpLSnw\niCZSnhC4NBzJu7Z8AHvVQ4azsSXz3CQ4UGCEoa5aOfCuEVr7mXujzmfDk/4oTsJ0\nlBP298qp4XOv55iAOBTA06SIYHu3Z0hkLP0Cvh78qkx8O3YGaphVY54oAneGcGfn\nZm+KgRwwWBhA6Iz7aT2XVrpsOGTcCKkEmO6ZlYyljogpoWYYKde0K2Aatf1pI3el\ncIyNd5utNgyBDU2OwmLm1MMzRiaAnUITKA749a1aGyFn9Igwgy1RrbKXIG7NW3ox\n7ZQWvPpE/3YXEew5bHwOJcR0PewBed6jP0rfhyK3U8k6F74XMuq6liODGpAVcvLe\nbkyVlq7BAgMBAAECggEAA7zxC2KDIj576OuLRxVpJzBMLvgUkM50TnqplrfT0+sL\nUA0ZOAxWdTcyqTWPe3ayyH3mVuDQCVRdiA+25sT458deoKPqTgLa5rQLVVBTm9sx\nUy9cVubPoZtoW9Ek2OlyFedwsOHK/mqx+fu1vx4axs5YIb2W2bUeEsFf41uRkYRu\nZ9ykFzypacA7ikqXOqkuwE5T99+ewne2M2IE43wA1+SbcPyXYZaHsTPKQ//qfWSW\nu6G2ms4dd1cAoNamZBiLBG1nRijlkagV368bgxfjqxb2v8/sxvedjtTaEN/hNq1f\n2Divz1dPwvfX3vd3rvRSc0cUav9As6welvTk7n8z1wKBgQDFgINJfAh6WxRUA6bZ\nWn9SN0Wl2DOVBREY/n0C96RFte9qPfnKUf209bRY1WsJdAWNiPU4Fm/hJVS+u5Yc\nCfrnK6dV3/aOpwDYMUH3GK/pTDHL8vCXYLhkHV+cIA+Z6VVCbE+Xkhq0OKbgJrxR\nusI8Sh8Z2Z4f0yNQsNeeEwNVAwKBgQDAvc21S22813fnxZ5TDaBbdR2f0A2dIZIS\nV6t5MP8GvBTHxfwR2s+DVGvwQ+DJZyvE+gIC20NT3a94h6d/Ecxkw61fMR2kZS8e\nqKWFzvvu3iJMPpi7fw/YeSPfLyEtQj08U3Ho6Bem7zyrGxLpdbCPbJNdjx8ZBVl1\nucAIL2w36wKBgB47fd1RzzprlsEFjhTXdYXwUAkC/2JgVC2vRxOdNNg28V8h/Yqq\n4ltBAUpX4Pqhd/gibucXCCk+vao0rNW98VpRpFvQ3L0Zc1eksqRQcx0TPMwrdAi4\nFYG9vVT402qL/TEdw1FAXDYaNfE5/LnGDNWNwxUB6uKUP2OqrlbUMj5hAoGAMN3u\nkdCV+hMHK5RHF/eUeJuw7xvo17XzOwNPTmLFxgdZnWztA/neMwXsd/Y9v9KQnlhk\nuiStZU4Hdx+tr+HrWIDkLAZf5ZZh4/wOO2bHKUDYoUwdnSwR8GiGV8ezOa5ZqDnf\n35U4qfJuB1PN77MPGM4GbuoikE1y6Q43Sdv8LBECgYAvUx+d0xfFAjAOPvZNjMII\nQwQOqGuFuXjjRzhllC6JB+iqN9mejykyF59Ob0E03XFTp1M6ufSJxZ9pL0/htzs4\nuzYBk7zsUb7GyRwuTVa9hPQjoweeWD2U0nHjNqHLNoePhMFwNMsqkl6N5GehPEHC\nGUEGkXgXDd5g6BjKGOcMhQ==\n-----END PRIVATE KEY-----\n");
        serviceAccount.put("client_email", "firebase-adminsdk-fbsvc@agimuseum.iam.gserviceaccount.com");
        serviceAccount.put("client_id", "103376091520372250513");
        serviceAccount.put("auth_uri", "https://accounts.google.com/o/oauth2/auth");
        serviceAccount.put("token_uri", "https://oauth2.googleapis.com/token");
        serviceAccount.put("auth_provider_x509_cert_url", "https://www.googleapis.com/oauth2/v1/certs");
        serviceAccount.put("client_x509_cert_url", "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40agimuseum.iam.gserviceaccount.com");
        serviceAccount.put("universe_domain", "googleapis.com");

        // Create backup of existing file if it exists
        Path existingFile = Paths.get(firebaseConfigPath);
        if (Files.exists(existingFile)) {
            Files.copy(existingFile, Paths.get(firebaseConfigPath + ".backup"));
        }

        // Write the new file with pretty-printing
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(firebaseConfigPath), serviceAccount);
    }

    /**
     * Alternative method to generate the JSON file using a direct string,
     * which can help preserve exact formatting of the private key.
     */
    public void regenerateFirebaseJsonDirect() throws IOException {
        String jsonContent = "{\n" +
                "  \"type\": \"service_account\",\n" +
                "  \"project_id\": \"agimuseum\",\n" +
                "  \"private_key_id\": \"9a639db8d559005c1a0c6d1a26d0ed5344411911\",\n" +
                "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCUstEL0XbpLSnw\\niCZSnhC4NBzJu7Z8AHvVQ4azsSXz3CQ4UGCEoa5aOfCuEVr7mXujzmfDk/4oTsJ0\\nlBP298qp4XOv55iAOBTA06SIYHu3Z0hkLP0Cvh78qkx8O3YGaphVY54oAneGcGfn\\nZm+KgRwwWBhA6Iz7aT2XVrpsOGTcCKkEmO6ZlYyljogpoWYYKde0K2Aatf1pI3el\\ncIyNd5utNgyBDU2OwmLm1MMzRiaAnUITKA749a1aGyFn9Igwgy1RrbKXIG7NW3ox\\n7ZQWvPpE/3YXEew5bHwOJcR0PewBed6jP0rfhyK3U8k6F74XMuq6liODGpAVcvLe\\nbkyVlq7BAgMBAAECggEAA7zxC2KDIj576OuLRxVpJzBMLvgUkM50TnqplrfT0+sL\\nUA0ZOAxWdTcyqTWPe3ayyH3mVuDQCVRdiA+25sT458deoKPqTgLa5rQLVVBTm9sx\\nUy9cVubPoZtoW9Ek2OlyFedwsOHK/mqx+fu1vx4axs5YIb2W2bUeEsFf41uRkYRu\\nZ9ykFzypacA7ikqXOqkuwE5T99+ewne2M2IE43wA1+SbcPyXYZaHsTPKQ//qfWSW\\nu6G2ms4dd1cAoNamZBiLBG1nRijlkagV368bgxfjqxb2v8/sxvedjtTaEN/hNq1f\\n2Divz1dPwvfX3vd3rvRSc0cUav9As6welvTk7n8z1wKBgQDFgINJfAh6WxRUA6bZ\\nWn9SN0Wl2DOVBREY/n0C96RFte9qPfnKUf209bRY1WsJdAWNiPU4Fm/hJVS+u5Yc\\nCfrnK6dV3/aOpwDYMUH3GK/pTDHL8vCXYLhkHV+cIA+Z6VVCbE+Xkhq0OKbgJrxR\\nusI8Sh8Z2Z4f0yNQsNeeEwNVAwKBgQDAvc21S22813fnxZ5TDaBbdR2f0A2dIZIS\\nV6t5MP8GvBTHxfwR2s+DVGvwQ+DJZyvE+gIC20NT3a94h6d/Ecxkw61fMR2kZS8e\\nqKWFzvvu3iJMPpi7fw/YeSPfLyEtQj08U3Ho6Bem7zyrGxLpdbCPbJNdjx8ZBVl1\\nucAIL2w36wKBgB47fd1RzzprlsEFjhTXdYXwUAkC/2JgVC2vRxOdNNg28V8h/Yqq\\n4ltBAUpX4Pqhd/gibucXCCk+vao0rNW98VpRpFvQ3L0Zc1eksqRQcx0TPMwrdAi4\\nFYG9vVT402qL/TEdw1FAXDYaNfE5/LnGDNWNwxUB6uKUP2OqrlbUMj5hAoGAMN3u\\nkdCV+hMHK5RHF/eUeJuw7xvo17XzOwNPTmLFxgdZnWztA/neMwXsd/Y9v9KQnlhk\\nuiStZU4Hdx+tr+HrWIDkLAZf5ZZh4/wOO2bHKUDYoUwdnSwR8GiGV8ezOa5ZqDnf\\n35U4qfJuB1PN77MPGM4GbuoikE1y6Q43Sdv8LBECgYAvUx+d0xfFAjAOPvZNjMII\\nQwQOqGuFuXjjRzhllC6JB+iqN9mejykyF59Ob0E03XFTp1M6ufSJxZ9pL0/htzs4\\nuzYBk7zsUb7GyRwuTVa9hPQjoweeWD2U0nHjNqHLNoePhMFwNMsqkl6N5GehPEHC\\nGUEGkXgXDd5g6BjKGOcMhQ==\\n-----END PRIVATE KEY-----\\n\",\n" +
                "  \"client_email\": \"firebase-adminsdk-fbsvc@agimuseum.iam.gserviceaccount.com\",\n" +
                "  \"client_id\": \"103376091520372250513\",\n" +
                "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40agimuseum.iam.gserviceaccount.com\",\n" +
                "  \"universe_domain\": \"googleapis.com\"\n" +
                "}";

        // Create backup of existing file if it exists
        Path existingFile = Paths.get(firebaseConfigPath);
        if (Files.exists(existingFile)) {
            Files.copy(existingFile, Paths.get(firebaseConfigPath + ".backup"));
        }

        // Write the JSON content directly
        Files.write(Paths.get(firebaseConfigPath), jsonContent.getBytes());
    }
}