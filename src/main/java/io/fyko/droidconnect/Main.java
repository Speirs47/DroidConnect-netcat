package io.fyko.droidconnect;

public class Main {
    /**
     * Nazwa programu
     */
    public static String appName = "DroidConnect netcat";
    /**
     * Wersja MAJOR. Zmieniana przy bardzo dużych zmianach w logice aplikacji.
     */
    public static int major = 2;
    /**
     * Wersja MINOR. Zmieniana przy poprawkach błędów lub dodawaniu nowych funkcjonalności, które
     * zrywają kompatybilność z wcześniejszymi wersjami.
     */
    public static int minor = 0;
    /**
     * Wersja BUILD. Zmieniana przy dodawaniu nowych funkcjonalności, które są kompatybilne z wcześniejszymi wersjami API.
     */
    public static int build = 0;
    /**
     * Wersja PATCH. Zmieniana przy poprawkach błędów, które nie zrywają kompatybilności z wcześnieszymi wersjami.
     */
    public static int patch = 0;

    public static void main(String[] args) {
        // ten prosty main służy temu, aby nie trzeba było podawać lokalizacji javafx przy uruchamianiu programu
        FrmNetCat.main(args);
    }


}
