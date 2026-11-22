package com.jojostands.client;

public final class ClientState {
    public static int mode = 0;
    public static String mobName = "Zombie";
    public static String blockName = "Block of Gold";

    public static final String[] MODES = {
            "Criar vida (plantas, animais, curar)",
            "Bloco → Mob aliado",
            "Invocar mob protetor",
            "Bloco → outro bloco",
            "Comandar aliados (ir / seguir)"
    };
    /** Como usar cada modo (aparece no HUD). */
    public static final String[] USAGE = {
            "Clique num bloco: vida | Clique no ar: cura",
            "Clique num bloco para vira-lo no mob",
            "Clique no ar/bloco: mob aparece e te protege",
            "Clique num bloco para troca-lo",
            "Clique num bloco: aliados vao | Shift+ar: seguir"
    };
    private ClientState() {}
}
