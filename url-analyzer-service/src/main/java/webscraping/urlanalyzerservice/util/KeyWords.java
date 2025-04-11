package webscraping.urlanalyzerservice.util;

import java.util.ArrayList;
import java.util.List;

public class KeyWords {

    public static List<String> getECommerceKeyWords() {
        return new ArrayList<>() {{
            add("корзина");
            add("доставка");
            add("магазин");
            add("магазины");
            add("товар");
            add("товары");
            add("маркетплейс");
        }};
    }

    public static List<String> getBlogKeyWords() {
        return new ArrayList<>() {{
            add("статьи");
            add("статья");
            add("новости");
            add("видео");
            add("пост");
            add("блог");
            add("контент");
        }};
    }
}
