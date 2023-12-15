package ru.shop.backend.search.util;

public class StringUtils {
    public static Boolean isContainErrorChar(String text){
        return text.contains("[") || text.contains("]")
                || text.contains("\"") || text.contains("/")
                || text.contains(";");
    }

    public static String convert(String message) {
        boolean result = message.matches(".*\\p{InCyrillic}.*");
        char[] ru = {'й','ц','у','к','е','н','г','ш','щ','з','х','ъ','ф','ы','в','а','п','р','о','л','д','ж','э', 'я','ч', 'с','м','и','т','ь','б', 'ю','.',
                ' ','0','1','2','3','4','5','6','7','8','9','-'};
        char[] en = {'q','w','e','r','t','y','u','i','o','p','[',']','a','s','d','f','g','h','j','k','l',';','"','z','x','c','v','b','n','m',',','.','/',
                ' ','0','1','2','3','4','5','6','7','8','9','-'};
        StringBuilder builder = new StringBuilder();

        if (result) {
            for (int i = 0; i < message.length(); i++) {
                for (int j = 0; j < ru.length; j++ ) {
                    if (message.charAt(i) == ru[j]) {
                        builder.append(en[j]);
                    }
                }
            }
        } else {
            for (int i = 0; i < message.length(); i++) {
                for (int j = 0; j < en.length; j++ ) {
                    if (message.charAt(i) == en[j]) {
                        builder.append(ru[j]);
                    }
                }
            }
        }
        return builder.toString();
    }
}