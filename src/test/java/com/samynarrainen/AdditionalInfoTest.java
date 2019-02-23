package com.samynarrainen;

import com.samynarrainen.domain.Entry;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by Samy on 19/08/2017.
 * Tests using additional info for matching.
 */
public class AdditionalInfoTest extends TestCase {

    /**
     * On AP, DVD Specials are made apparent, whereas they aren't in the same way on MAL.
     */
    public void testDvdSpecial() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Clannad: Another World, Kyou Chapter";
        entry.AnimePlanetURL = "clannad-another-world-kyou-chapter";
        assertEquals(6351, Main.compareAdditionalInfo(entry));
    }

    /**
     * AP doesn't specify an end year if it's the same as the start year.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testEndOnSameYear() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Black Rock Shooter TV";
        entry.AnimePlanetURL = "black-rock-shooter-tv";
        assertEquals(11285, Main.compareAdditionalInfo(entry));
    }

    /**
     * Shows which haven't aired yet have different information.
     * @throws IOException
     * @throws InterruptedException
     */
    /*
    public void testAPNotAired() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Escha Chron";
        entry.AnimePlanetURL = "escha-chron";
        assertEquals(34208, Main.compareAdditionalInfo(entry));
    }
    */

    /**
     * General parser for anime that have shown issues in the past.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testGeneral() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Fate/stay night: Unlimited Blade Works 2";
        entry.AnimePlanetURL = "fate-stay-night-unlimited-blade-works-2";
        assertEquals(28701, Main.compareAdditionalInfo(entry));
    }

    /**
     * General parser for anime that have shown issues in the past.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testGeneral2() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Yuru Yuri ♪♪";
        entry.AnimePlanetURL = "yuru-yuri-2";
        assertEquals(12403, Main.compareAdditionalInfo(entry));
    }

    /**
     * General parser for anime that have shown issues in the past.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testGeneral3() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Kore wa Zombie desu ka? (2012)";
        entry.AnimePlanetURL = "kore-wa-zombie-desu-ka-2012";
        assertEquals(15437, Main.compareAdditionalInfo(entry));
    }

    /**
     * General parser for anime that have shown issues in the past.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testGeneral4() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Code Geass Gaiden: Boukoku no Akito";
        entry.AnimePlanetURL = "code-geass-gaiden-boukoku-no-akito";
        assertEquals(-1, Main.compareAdditionalInfo(entry));
    }

    /**
     * Sometimes manga results are shown before anime ones.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testMangaResultsAppearFirst() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Fate/stay night: Heaven's Feel";
        entry.AnimePlanetURL = "fate-stay-night-heavens-feel";
        assertEquals(25537, Main.compareAdditionalInfo(entry));
    }

    /**
     * Sometimes season information is on AP, but not MAL.
     * Should we let this through, or be strict and expect perfect matches?
     * @throws IOException
     * @throws InterruptedException
     */
    public void testMissingSeasonalInfoOnMAL() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Ebiten: Ebisugawa Public High School's Tenmonbu";
        entry.AnimePlanetURL = "ebiten-ebisugawa-public-high-schools-tenmonbu";
        assertEquals(-1, Main.compareAdditionalInfo(entry)); //Want it to be 14073 ideally though...
    }

    /**
     * Studio information missing from AP
     * @throws IOException
     * @throws InterruptedException
     */
    public void testMissingStudioAP() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Haiyoru! Nyaru-ani: Remember My Love (craft-sensei) Special";
        entry.AnimePlanetURL = "haiyoru-nyaruani-remember-my-love-craft-sensei-special";
        assertEquals(-1, Main.compareAdditionalInfo(entry));
    }

    public void testOtherType() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Steins;Gate 0";
        entry.AnimePlanetURL = "steins-gate-0";
        assertEquals(-1, Main.compareAdditionalInfo(entry));
    }

    public void testIgnoreCapsStudio() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Mirai Nikki TV";
        entry.AnimePlanetURL = "mirai-nikki-tv";
        assertEquals(10620, Main.compareAdditionalInfo(entry));
    }

    public void testIgnoreCapsStudio2() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Steins;Gate The Movie";
        entry.AnimePlanetURL = "steins-gate-the-movie";
        assertEquals(11577, Main.compareAdditionalInfo(entry));
    }

    public void testMusicVideo() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Tengen Toppa Gurren Lagann: Kirameki Yoko Box ~Pieces of Sweet Stars~";
        entry.AnimePlanetURL = "tengen-toppa-gurren-lagann-kirameki-yoko-box-pieces-of-sweet-stars";
        assertEquals(-1, Main.compareAdditionalInfo(entry)); //Ideally 6548, but studios don't match.
    }

    /**
     * The same studio sometimes has different names.
     * @throws IOException
     * @throws InterruptedException
     */
    public void testStudioMismatch() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Little Witch Academia TV";
        entry.AnimePlanetURL = "little-witch-academia-tv";
        assertEquals(33489, Main.compareAdditionalInfo(entry));
    }

    public void testMisreadAPSeason() throws IOException, InterruptedException {
        Entry entry = new Entry();
        entry.name = "Kami nomi zo Shiru Sekai: Ayukawa Tenri-hen";
        entry.AnimePlanetURL = "kami-nomi-zo-shiru-sekai-ayukawa-tenri-hen";
        assertEquals(15117, Main.compareAdditionalInfo(entry));
    }
}
