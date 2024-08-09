module edu.promo.map {
    exports edu.promo.map;
    provides java.util.Map with edu.promo.map.PromoHashMap;
}
