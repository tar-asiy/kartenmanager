import sum.ereignis.*;
import sum.komponenten.*;
import sum.werkzeuge.*;
import sum.strukturen.*;

/**
 * Kartenmanager_SuS:
 * Verwendung einer Liste<Karte> (aus sum.strukturen.Liste) 
 * anstelle eines Arrays. 
 * Die GUI wurde beibehalten (Stapelgr., Wert, Farbe, Position usw.).
 * - Einfügen/Entfernen mit Liste
 * - Sortieren/Erneuern (Platzhalter) 
 * - Zeichnen ab y=150
 */
public class Kartenmanager_SuS extends Ereignisanwendung {

    // -----------------------------
    // 1) GUI-Objekte
    // -----------------------------
    private Knopf startKnopf;   // "Sortieren"
    private Knopf updateKnopf;  // "Erneuern"
    private Knopf einfKnopf;    // "Einfügen"
    private Knopf entfKnopf;    // "Entfernen"

    private Etikett infoEtikett;    
    private Etikett labelStapelsize; 
    private Etikett labelWert;  
    private Etikett labelFarbe; 
    private Etikett labelPos;   

    private Auswahl auswahlWert;   
    private Auswahl auswahlFarbe;  

    private Textfeld tfUmfang;  // neue Stapelgröße
    private Textfeld tfPos;     // Position für Einfügen/Entfernen

    private Buntstift stift;

    // -----------------------------
    // 2) Listen-Datenstruktur
    // -----------------------------
    // Karte => Hilfsklasse für (wert, farbe)
    private class Karte {
        int wert;   // 1..13 => "2..Ass"
        int farbe;  // 0..3  => "Kreuz..Pik"

        public Karte(int w, int f) {
            this.wert = w;
            this.farbe= f;
        }
    }

    // Unsere Liste<Karte> anstelle von int[][]
    private Liste<Karte> karten;
    private int kartenAnzahl = 52;  // "logische" Stapelgröße

    // Namen für Werte und Farben (Index 1..13 => "2..Ass")
    private String[] werteNamen = {
       "", "2", "3", "4", "5", "6", 
       "7", "8", "9", "10", "Bube", "Dame", "König", "Ass"
    };
    private String[] farbenNamen = {
       "Kreuz", "Karo", "Herz", "Pik"
    };

    // Zeitmessung
    private long startZeit;
    private long endZeit;

    public Kartenmanager_SuS() {
        super();

        stift = new Buntstift();

        // 3) GUI: 
        //    - InfoEtikett + Stapelgröße/Erneuern/Sortieren (links)
        //    - Wert/Farbe/Position + Einfügen/Entfernen (rechts)
        //    - Karten ab y=150

        infoEtikett    = new Etikett( 50,  20, 400, 30, 
            "Karten mit Liste (Einfügen/Entfernen).");

        labelStapelsize= new Etikett( 50,  60, 80, 25, "Stapelgr.:");
        tfUmfang       = new Textfeld(130, 60, 60, 25, "52"); // Standard=52
        updateKnopf    = new Knopf(200, 60, 100, 30, "Erneuern");
        startKnopf     = new Knopf(310, 60, 100, 30, "Sortieren");

        // Wert/Farbe/Position 
        labelWert   = new Etikett(450,  20, 40, 25, "Wert:");
        auswahlWert = new Auswahl(500, 20, 80, 25);

        labelFarbe  = new Etikett(600, 20, 50, 25, "Farbe:");
        auswahlFarbe= new Auswahl(660,20, 80, 25);

        labelPos    = new Etikett(450, 60, 60, 25, "Position:");
        tfPos       = new Textfeld(520, 60, 60, 25, "0");

        einfKnopf   = new Knopf(600, 60, 100, 30, "Einfügen");
        entfKnopf   = new Knopf(710, 60, 100, 30, "Entfernen");

        // Auswahllisten befüllen: Index 0..12 => "2..Ass"
        for(int w=1; w<=13; w++){
            auswahlWert.haengeAn(werteNamen[w]);
        }
        // Index 0..3 => "Kreuz..Pik"
        for(int f=0; f<4; f++){
            auswahlFarbe.haengeAn(farbenNamen[f]);
        }

        // 4) Ereignismethoden
        startKnopf.setzeBearbeiterGeklickt("Sort_Klick");
        updateKnopf.setzeBearbeiterGeklickt("Update_Klick");
        einfKnopf.setzeBearbeiterGeklickt("Einfuegen_Klick");
        entfKnopf.setzeBearbeiterGeklickt("Entfernen_Klick");

        // 5) Listen-Datenstruktur anlegen
        karten = new Liste<Karte>(); 
        // => "kartenAnzahl" als "logische" Größe, 
        //    wir initialisieren so viele Einträge

        initialisiereKarten(0); // füllt die Liste mit 52 Karten

        // 6) Zeichnen ab y=150
        zeichneKarten(0, 50, 150);

        this.fuehreAus();
    }

    // ----------------------------------------------------------
    // Initialisieren & Zeichnen
    // ----------------------------------------------------------

    /**
     * initialisiereKarten(i):
     *  Legt kartenAnzahl Karten an.
     *  Jede Karte zufällig => wert in [1..13], farbe in [0..3]
     */
    void initialisiereKarten(int i) {
        if(i>=kartenAnzahl) return;

        // Erzeuge eine neue Karte
        int w = (int)(Math.random()*13)+1;
        int f = (int)(Math.random()*4);

        // Zur Sicherheit: Erst zumEnde(), dann fuegeDahinterEin
        karten.zumEnde();
        karten.fuegeDahinterEin(new Karte(w, f));

        initialisiereKarten(i+1);
    }

    /**
     * zeichneKarten(index, x, y):
     *  Durchläuft die Liste per gehZuPosition(index),
     *  holt das aktuelleElement() => (wert, farbe),
     *  zeichnet die Karte.
     */
    void zeichneKarten(int index, int x, int y) {
        if (index >= karten.laenge()) return; // Ende der Liste

        // Zur Position index gehen
        karten.geheZuPosition(index+1);
        Karte c = karten.aktuellesElement(); // => (wert, farbe)

        String name = farbenNamen[c.farbe] + " " + werteNamen[c.wert];
        zeichneEineKarte(x, y, name);

        x += 90;
        if ((index+1)%13 == 0) {
            x = 50;
            y += 130;
        }

        // rekursiver Aufruf
        zeichneKarten(index+1, x, y);
    }

    void zeichneEineKarte(int x, int y, String name) {
        stift.bewegeBis(x,y);
        stift.zeichneRechteck(80,120);
        stift.bewegeBis(x+5, y+50);
        stift.schreibeText(name);
    }

    /**
     * Vorgabe:
     * stift.radiere();
     * zeichneKarten(0,50,150);
     * stift.normal();
     */
    void loescheAnzeige() {
        stift.radiere();
        zeichneKarten(0, 50, 150);
        stift.normal();
    }

    // ----------------------------------------------------------
    // Ereignismethoden
    // ----------------------------------------------------------

    public void Sort_Klick() {
        //loescheAnzeige();
        //startZeit = System.currentTimeMillis();

        // z.B. 1000 Durchläufe
        for(int i=0; i<1000; i++){
            // Sortierplatzhalter
        }

        //endZeit=System.currentTimeMillis();
        //double d = endZeit - startZeit;

        //zeichneKarten(0, 50, 150);
        infoEtikett.setzeInhalt("Wird später umgesetzt");
    }

    public void Update_Klick() {
        String s = tfUmfang.inhaltAlsText().trim();
        int neuAnz;
        try {
            neuAnz = Integer.parseInt(s);
            if(neuAnz<=0) neuAnz=52;
        } catch(NumberFormatException ex) {
            neuAnz=52;
        }
        loescheAnzeige();

        // Alte Liste verwerfen => Neue Liste
        karten = new Liste<Karte>();
        kartenAnzahl= neuAnz;

        initialisiereKarten(0);
        zeichneKarten(0, 50, 150);

        infoEtikett.setzeInhalt("Neuer Stapel: "+kartenAnzahl+" Karten (Liste).");
    }

    public void Einfuegen_Klick() {
        //Platzhalter
    }

    public void Entfernen_Klick() {
        //Platzhalter
    }
}
