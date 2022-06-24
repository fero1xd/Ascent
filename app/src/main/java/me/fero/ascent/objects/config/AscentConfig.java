package me.fero.ascent.objects.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AscentConfig {

    private static final Dotenv dotenv = Dotenv.configure().directory("./").filename(".env").load();

    public static String get(String key) {
        return dotenv.get(key.toUpperCase());
    }

    public static class Lavalink {
        public final boolean isEnabled;
        public final LavalinkNode[] nodes;

        public Lavalink(boolean enable, LavalinkNode[] nodes) {
            this.isEnabled = enable;
            this.nodes = nodes;
        }

        public static class LavalinkNode {
            public final String wsurl;
            public final String pass;
            public final String region;

            public LavalinkNode(String wsurl, String pass, String region) {
                this.wsurl = wsurl;
                this.pass = pass;
                this.region = region;
            }
        }
    }


    public static Lavalink getLavalinkNodes() {
        String[] nodes = get("lavalink_nodes").split(",");
        final boolean lavalinkEnable = Boolean.parseBoolean(get("lavalink_enable"));

        final Lavalink.LavalinkNode[] res = new Lavalink.LavalinkNode[nodes.length];

        for(int i = 0; i < nodes.length; i++) {
            String[] s = nodes[i].split("_");

            res[i] = new Lavalink.LavalinkNode(
                    s[0],
                    s[1],
                    s[2]
            );
        }

        return new Lavalink(lavalinkEnable, res);
    }
}
