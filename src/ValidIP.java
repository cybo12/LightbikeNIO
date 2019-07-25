import java.util.regex.Pattern;

//source : http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
//copy/paste and understanding of the method used there
//start by 0/1 = followed by anything, if start by 2 then 0-4 and 0-9, if start by 25 then 0-5 ending by . then repeat
public class ValidIP {
    /**
     * source : http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
     * copy/paste and understanding of the method used there
     * start by 0/1 = followed by anything, if start by 2 then 0-4 and 0-9, if start by 25 then 0-5 ending by . then repeat
     */
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Check if user enter right ip format
     *
     * @param ip to validate
     * @return boolean if pattern respected
     */
    public static boolean validate(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}


// Une conversation GLOBALE partagée entre TOUS les users.
//privée = de toute façon besoin d'être connecté.
//privée = double click sur l'user va update la jlist messages avec les messages uniquement partagés entre les deux users
//double click = créer une conversation dans une table conversation privée, avec dedans deux chatusers, et
