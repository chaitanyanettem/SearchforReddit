package chaitanya.im.searchforreddit;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Chaitanya Nettem on 12/10/16.
 */
public class UtilMethodsTest {
    @Test
    public void extractYoutubeID() throws Exception {
        assertEquals(UtilMethods.extractYoutubeID("youtube.com/watch?v=oGHWghMfbEU"), "oGHWghMfbEU");
        assertEquals(UtilMethods.extractYoutubeID("https://youtube.com/watch?v=oGHWghMfbEU"), "oGHWghMfbEU");
        assertEquals(UtilMethods.extractYoutubeID("https://www.youtube.com/watch?v=oGHWghMfbEU"), "oGHWghMfbEU");
        assertEquals(UtilMethods.extractYoutubeID("youtu.be/cfrp6qfbc2g"), "cfrp6qfbc2g");
        assertEquals(UtilMethods.extractYoutubeID("youtube.com/watch?v=cfrp6qfbc2g"), "cfrp6qfbc2g");
    }

}