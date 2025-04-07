package business;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChanceCard {

    private static final Random rand = new Random();

    // ukázkový seznam karet
    private static final List<String> cardMessages = new ArrayList<>(List.of(
            "Zaplať 100Kč za opravy domu",
            "Získej 200Kč jako dárek",
            "Jdi do vězení!",
            "Posuň se na START",
            "Získej 50Kč za nález",
            "Zaplať 150Kč pokutu",
            "Jdi na pole 5 (rychlejší nákup)",
            "Získej 300Kč od všech"
    ));

    public static void applyRandomEffect(Player p, GameState state) {
        String card = cardMessages.get(rand.nextInt(cardMessages.size()));

        switch (card) {
            case "Zaplať 100Kč za opravy domu" -> {
                p.subtractMoney(100);
            }
            case "Získej 200Kč jako dárek" -> {
                p.addMoney(200);
            }
            case "Jdi do vězení!" -> {
                p.goToJail();
            }
            case "Posuň se na START" -> {
                p.moveTo(0);
            }
            case "Získej 50Kč za nález" -> {
                p.addMoney(50);
            }
            case "Zaplať 150Kč pokutu" -> {
                p.subtractMoney(150);
            }
            case "Jdi na pole 5 (rychlejší nákup)" -> {
                p.moveTo(5);
            }
            case "Získej 300Kč od všech" -> {
                for (Player other : state.getPlayers()) {
                    if (other != p && !other.isBankrupt()) {
                        other.subtractMoney(300);
                        p.addMoney(300);
                    }
                }
            }
        }
        System.out.println("[ŠANCE] " + p.getName() + " vytáhl kartu: " + card);
    }
}
